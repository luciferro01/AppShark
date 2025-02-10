package com.mohil_bansal.appshark.data.repository

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.mohil_bansal.appshark.data.detectTechnology
import com.mohil_bansal.appshark.domain.models.AppDetails
import com.mohil_bansal.appshark.domain.repository.AppRepository
import com.mohil_bansal.appshark.utils.Helper
import java.util.zip.ZipFile

class AppRepositoryImpl(private val context: Context) : AppRepository {
    private val pm: PackageManager = context.packageManager

    override suspend fun getAppDetails(packageName: String): AppDetails {
        // Include extra flags to fetch receivers and services.
        val pkg: PackageInfo = pm.getPackageInfo(
            packageName,
            PackageManager.GET_ACTIVITIES or PackageManager.GET_META_DATA or
                    PackageManager.GET_PROVIDERS or PackageManager.GET_PERMISSIONS or
                    PackageManager.GET_RECEIVERS or PackageManager.GET_SERVICES
        )

        // Use the helper to load the app icon efficiently.
        val icon: ImageBitmap = pkg.applicationInfo?.let { Helper().getAppIconAsImageBitmap(pm, it) } ?: ImageBitmap(1, 1)

        // Get architecture from primaryCpuAbi (TODO: further refine if needed)
        val architecture = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //TODO: Fix this as primaryCpuAbi is deprecated
            "N/A"
//            pkg.applicationInfo.primaryCpuAbi ?: "N/A"
        } else {
            "N/A"
        }

        // For language, without additional info we return "N/A"
        val language = "N/A"

        // Get activities, meta-data, and content providers as before.
        val activities = pkg.activities?.map { it.name } ?: emptyList()
        val metaDataMap = mutableMapOf<String, String>()
        pkg.applicationInfo?.metaData?.keySet()?.forEach { key ->
            metaDataMap[key] = pkg.applicationInfo?.metaData?.get(key)?.toString() ?: ""
        }
        val contentProviders = pkg.providers?.map { it.authority } ?: emptyList()

        // Get permissions using requestedPermissionsFlags.
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

        // Determine the framework/technology using a helper.
        val technology = detectTechnology(pkg)

        // Get additional details:
        //TODO: Minimum SDK version is not available in the PackageInfo object.
//        val minSdkVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) pkg.minSdkVersion else 1
        val minSdkVersion = 1
        val targetSdkVersion = pkg.applicationInfo?.targetSdkVersion
        val compiledVersion = pkg.versionName ?: "N/A"
        val broadcastReceivers = pkg.receivers?.map { it.name } ?: emptyList()
        val services = pkg.services?.map { it.name } ?: emptyList()
        // For native libraries, we return the nativeLibraryDir as a list (or empty if null).
        val nativeLibraries = pkg.applicationInfo?.nativeLibraryDir?.let { listOf(it) } ?: emptyList()

//        return pkg.applicationInfo?.let { pm.getApplicationLabel(it).toString() }?.let { label ->
//            if (targetSdkVersion != null) {
//                AppDetails(
//                    packageName = packageName,
//                    appName = label,
//                    icon = icon,
//                    architecture = architecture,
//                    language = language,
//                    minSdkVersion = minSdkVersion,
//                    targetSdkVersion = targetSdkVersion,
//                    compiledVersion = compiledVersion,
//                    activities = activities,
//                    metaData = metaDataMap,
//                    contentProviders = contentProviders,
//                    permissionsAllowed = permissionsAllowed,
//                    permissionsDenied = permissionsDenied,
//                    technology = technology,
//                    broadcastReceivers = broadcastReceivers,
//                    services = services,
//                    nativeLibraries = nativeLibraries
//                )
//            }
//        }
        val label = pkg.applicationInfo?.let { pm.getApplicationLabel(it).toString() } ?: "Unknown"
        return if (targetSdkVersion != null) {
            AppDetails(
                packageName = packageName,
                appName = label,
                icon = icon,
                architecture = architecture,
                language = language,
                minSdkVersion = minSdkVersion,
                targetSdkVersion = targetSdkVersion,
                compiledVersion = compiledVersion,
                activities = activities,
                metaData = metaDataMap,
                contentProviders = contentProviders,
                permissionsAllowed = permissionsAllowed,
                permissionsDenied = permissionsDenied,
                technology = technology,
                broadcastReceivers = broadcastReceivers,
                services = services,
                nativeLibraries = nativeLibraries
            )
        } else {
            // Handle the case where targetSdkVersion is null
            AppDetails(
                packageName = packageName,
                appName = label,
                icon = icon,
                architecture = architecture,
                language = language,
                minSdkVersion = minSdkVersion,
                targetSdkVersion = 0, // or any default value
                compiledVersion = compiledVersion,
                activities = activities,
                metaData = metaDataMap,
                contentProviders = contentProviders,
                permissionsAllowed = permissionsAllowed,
                permissionsDenied = permissionsDenied,
                technology = technology,
                broadcastReceivers = broadcastReceivers,
                services = services,
                nativeLibraries = nativeLibraries
            )
        }
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
