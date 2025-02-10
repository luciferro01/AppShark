package com.mohil_bansal.appshark.utils

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

class Helper {
    fun getAppIconAsImageBitmap(pm: PackageManager, appInfo: ApplicationInfo): ImageBitmap {
        return try {
            val drawable: Drawable = pm.getApplicationIcon(appInfo)
            // If it's already a BitmapDrawable, use its bitmap. Otherwise, convert:
            val bitmap: Bitmap = if (drawable is BitmapDrawable && drawable.bitmap != null) {
                drawable.bitmap
            } else {
                // Use an extension if available, or create a bitmap manually.
                // The toBitmap() extension from androidx.core.graphics.drawable is helpful.
                drawable.toBitmap()
            }
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            // Return a placeholder image of minimal size
            androidx.compose.ui.graphics.ImageBitmap(1, 1)
        }
    }

}