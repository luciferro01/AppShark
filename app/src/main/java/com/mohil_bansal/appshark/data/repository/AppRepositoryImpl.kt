package com.mohil_bansal.appshark.data.repository

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.asImageBitmap
import com.mohil_bansal.appshark.domain.models.AppDetails
import com.mohil_bansal.appshark.domain.repository.AppRepository
import java.util.zip.ZipFile
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import com.mohil_bansal.appshark.utils.Helper

class AppRepositoryImpl(private val context: Context) : AppRepository {
    private val pm: PackageManager = context.packageManager

    override suspend fun getAppDetails(packageName: String): AppDetails {
        val pkg: PackageInfo = pm.getPackageInfo(
            packageName,
            PackageManager.GET_ACTIVITIES or PackageManager.GET_META_DATA or PackageManager.GET_PROVIDERS or PackageManager.GET_PERMISSIONS
        )
        val icon = pkg.applicationInfo?.let { Helper().getAppIconAsImageBitmap(pm, it) } ?: ImageBitmap(1, 1)


        // Get the app icon and convert it (assumes BitmapDrawable; you might need to handle other types)
        val drawable = pkg.applicationInfo?.let { pm.getApplicationIcon(it) }
        val bitmap = (drawable as? BitmapDrawable)?.bitmap
        val imageBitmap = bitmap?.asImageBitmap() ?: androidx.compose.ui.graphics.ImageBitmap(1, 1)

        // Get architecture from primaryCpuAbi

        val architecture = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            pkg.applicationInfo.primaryCpuAbi ?: "N/A"
            //TODO: Fix this architecure issue

            "N/A"
        } else {
            "N/A"
        }

        // For language, without better info, we return "N/A"
        val language = "N/A"

        // Get the list of activity class names
        val activities = pkg.activities?.map { it.name } ?: emptyList()

        // Convert meta-data Bundle to Map<String, String>
        val metaDataMap = mutableMapOf<String, String>()
        pkg.applicationInfo?.metaData?.keySet()?.forEach { key ->
            metaDataMap[key] = pkg.applicationInfo?.metaData?.get(key)?.toString() ?: ""
        }

        // Get content providers by listing the authorities
        val contentProviders = pkg.providers?.map { it.authority } ?: emptyList()

        // Get permissions: the PackageInfo.requestedPermissionsFlags array aligns with requestedPermissions.
        // Get permissions: the PackageInfo.requestedPermissionsFlags array aligns with requestedPermissions.
        val permissionsAllowed = mutableListOf<String>()
        val permissionsDenied = mutableListOf<String>()
        val requestedPermissions = pkg.requestedPermissions ?: emptyArray()
        val requestedFlags = pkg.requestedPermissionsFlags
        for (i in requestedPermissions.indices) {
            if (((requestedFlags?.get(i) ?: 0) and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                permissionsAllowed.add(requestedPermissions[i])
            } else {
                permissionsDenied.add(requestedPermissions[i])
            }
        }

        // Determine the framework/technology using a helper
        val technology = detectTechnology(pkg)

        return pkg.applicationInfo?.let { pm.getApplicationLabel(it).toString() }?.let {
            AppDetails(
                packageName = packageName,
                appName = it,
                icon = icon,
                architecture = architecture,
                language = language,
                activities = activities,
                metaData = metaDataMap,
                contentProviders = contentProviders,
                permissionsAllowed = permissionsAllowed,
                permissionsDenied = permissionsDenied,
                technology = technology
            )
        }!!
    }

    // Heuristic to detect the framework.
    private fun detectTechnology(pkg: PackageInfo): String {
        pkg.activities?.forEach { activity ->
            val actName = activity.name
            when {
                actName.contains("io.flutter.embedding.android.FlutterActivity") ->
                    return "Flutter"
                actName.contains("com.facebook.react") ->
                    return "React Native"
                actName.contains("com.unity3d.player.UnityPlayerActivity") ||
                        actName.contains("com.unity3d.player") ->
                    return "Unity"
                actName.contains("mono.android") ||
                        actName.contains("Xamarin.Forms.Platform.Android") ->
                    return "Xamarin"
                actName.contains("org.apache.cordova.CordovaActivity") ->
                    return "Cordova"
                actName.contains("androidx.compose") ->
                    return "Jetpack Compose"
            }
        }
        // As a heuristic for native code: many Kotlin apps include a .kotlin_module file in the APK.
        try {
            val sourceDir = pkg.applicationInfo?.sourceDir ?: return "Native (Java)"
            ZipFile(sourceDir).use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    if (entry.name.endsWith(".kotlin_module")) {
                        return "Native (Kotlin)"
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback on error.
        }
        return "Native (Java)"
    }
}
