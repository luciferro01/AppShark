package com.mohil_bansal.appshark.data.repository

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import com.mohil_bansal.appshark.domain.models.AppDetails
import com.mohil_bansal.appshark.domain.repository.AppRepository
import com.mohil_bansal.appshark.utils.Helper
import java.io.File
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
        val icon: ImageBitmap =
            pkg.applicationInfo?.let { Helper().getAppIconAsImageBitmap(pm, it) }
                ?: ImageBitmap(1, 1)

        // Architecture: try to extract ABI info from nativeLibraryDir.
        val architecture = pkg.applicationInfo?.nativeLibraryDir?.let { libDir ->
            // Expecting a path like ".../lib/arm64-v8a"; extract substring after last "/"
            File(libDir).name
        } ?: "N/A"

        // Determine language based on technology heuristic.
        val technology = detectTechnology(pkg)
        val language = when (technology) {
            "Native (Kotlin)" -> "Kotlin"
            "Native (Java)" -> "Java"
            else -> technology
        }

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

        // Additional details:
        // Use pkg.minSdkVersion if available (API 24+); otherwise default to 1.
        val minSdkVersion: Double = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            pkg.minSdkVersion
//            pkg.applicationInfo?.targetSdkVersion ?: 1
            2.0
        } else {
            1.0
        }
        val targetSdkVersion = pkg.applicationInfo?.targetSdkVersion
        val compiledVersion = pkg.versionName ?: "N/A"
        val broadcastReceivers = pkg.receivers?.map { it.name } ?: emptyList()
        val services = pkg.services?.map { it.name } ?: emptyList()

        // For native libraries, list the file names in nativeLibraryDir (if available).
        val nativeLibraries: List<String> =
            pkg.applicationInfo?.nativeLibraryDir?.let { libDirPath ->
                val libDir = File(libDirPath)
                if (libDir.exists() && libDir.isDirectory) {
                    libDir.listFiles()?.map { it.name }?.toList() ?: emptyList()
                } else {
                    emptyList()
                }
            } ?: emptyList()

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
                targetSdkVersion = 0, // default value
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
        // First, try to detect by checking activity names.
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
        // If activity markers didn't work, inspect the APK's native libraries.
        try {
            val sourceDir = pkg.applicationInfo?.sourceDir ?: return "Native (Java)"
            ZipFile(sourceDir).use { zip ->
                // Cache entries to avoid multiple iterations.
                val entries = zip.entries().asSequence().toList()
                // Check for Flutter: all Flutter apps include libflutter.so.
                if (entries.any { it.name.contains("libflutter.so") }) {
                    return "Flutter"
                }
                // Check for React Native: many RN apps include libreactnativejni.so.
                if (entries.any { it.name.contains("libreactnativejni.so") }) {
                    return "React Native"
                }
                // Check for Kotlin: presence of .kotlin_module files.
                if (entries.any { it.name.endsWith(".kotlin_module") }) {
                    return "Native (Kotlin)"
                }
            }
        } catch (e: Exception) {
            // In case of error, fall back to native Java.
            return "Native (Java)"
        }
        return "Native (Java)"
    }

}