package cn.edu.sjtu.ega816.kotlinChatter

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

fun Context.toast(message: String, short: Boolean = true) {
    Toast.makeText(this, message, if (short) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
}

fun ImageView.display(uri: Uri) {
    setImageURI(uri)
    visibility = View.VISIBLE
}

fun Uri.toFile(context: Context): File? {
    // Try _data column first (works for local media on older API)
    try {
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                this, arrayOf("_data"), null, null, null
            )
            cursor?.run {
                if (moveToFirst()) {
                    val dataIdx = getColumnIndex("_data")
                    if (dataIdx >= 0) {
                        val path = getString(dataIdx)
                        if (path != null) {
                            val file = File(path)
                            if (file.exists()) return file
                        }
                    }
                }
            }
        } finally {
            cursor?.close()
        }
    } catch (e: Exception) {
        Log.d("Uri.toFile", "_data fallback failed: ${e.message}")
    }

    // Fallback: copy content URI stream to a temp file
    try {
        val mimeType = context.contentResolver.getType(this) ?: "image/jpeg"
        val ext = when {
            mimeType.contains("video") -> ".mp4"
            mimeType.contains("png") -> ".png"
            else -> ".jpg"
        }

        // Get display name
        var displayName = "upload_${System.currentTimeMillis()}$ext"
        context.contentResolver.query(this, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIdx = cursor.getColumnIndex("_display_name")
                if (nameIdx >= 0) {
                    val name = cursor.getString(nameIdx)
                    if (name != null) displayName = name
                }
            }
        }

        val tempDir = File(context.cacheDir, "upload_temp")
        tempDir.mkdirs()
        val tempFile = File(tempDir, displayName)

        context.contentResolver.openInputStream(this)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }

        if (tempFile.exists() && tempFile.length() > 0) {
            return tempFile
        }
    } catch (e: Exception) {
        Log.d("Uri.toFile", "Copy fallback failed: ${e.message}")
    }

    return null
}