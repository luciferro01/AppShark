package com.mohil_bansal.appshark.domain.models

import androidx.compose.ui.graphics.ImageBitmap

data class AppDetails(
    val packageName: String,
    val appName: String,
    val icon: ImageBitmap,
    val architecture: String,
    val language: String,
    val minSdkVersion: Int,
    val targetSdkVersion: Int,
    val compiledVersion: String,
    val activities: List<String>,
    val metaData: Map<String, String>,
    val contentProviders: List<String>,
    val permissionsAllowed: List<String>,
    val permissionsDenied: List<String>,
    val technology: String,
    val broadcastReceivers: List<String>,
    val services: List<String>,
    val nativeLibraries: List<String>
)
