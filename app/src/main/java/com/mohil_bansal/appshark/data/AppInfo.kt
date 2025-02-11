package com.mohil_bansal.appshark.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import com.mohil_bansal.appshark.utils.Helper
import java.util.zip.ZipFile

data class AppInfo(
    val packageName: String,
    val appName: String,
    val technology: String,
    val icon: ImageBitmap
)
fun getCategorizedApps(context: Context): Triple<List<AppInfo>, List<AppInfo>, List<AppInfo>> {
    val pm = context.packageManager
    val packages = pm.getInstalledPackages(PackageManager.GET_ACTIVITIES)

    val sideloadedApps = mutableListOf<AppInfo>()
    val systemApps = mutableListOf<AppInfo>()
    val allApps = mutableListOf<AppInfo>()

    for (pkg in packages) {
        val appName = pkg.applicationInfo?.let { pm.getApplicationLabel(it).toString() }
        val packageName = pkg.packageName
        val technology = detectTechnology(pkg)
        val drawable = pkg.applicationInfo?.let { pm.getApplicationIcon(it) }
        val bitmap = (drawable as? BitmapDrawable)?.bitmap
//        val icon = bitmap?.asImageBitmap() ?: androidx.compose.ui.graphics.ImageBitmap(1, 1)
        val icon = pkg.applicationInfo?.let { Helper().getAppIconAsImageBitmap(pm, it) } ?: ImageBitmap(1, 1)

        val appInfo = appName?.let { AppInfo(packageName, it, technology, icon) }
        appInfo?.let {
            allApps.add(it)
            val isSystemApp = (pkg.applicationInfo?.flags ?: 0) and ApplicationInfo.FLAG_SYSTEM != 0
            if (isSystemApp) {
                systemApps.add(it)
            } else {
                sideloadedApps.add(it)
            }
        }
    }
    return Triple(sideloadedApps, systemApps, allApps)
}

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
