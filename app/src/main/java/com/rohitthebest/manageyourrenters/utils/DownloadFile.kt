package com.rohitthebest.manageyourrenters.utils

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.rohitthebest.manageyourrenters.R

fun downloadFileFromUrl(activity: Activity, url: String?, fileName: String?) {

    try {
        if (!url.isNullOrEmpty()) {
            val uri: Uri = Uri.parse(url)

            val request = DownloadManager.Request(uri)
            request.setMimeType(getMimeType(uri.toString()))
            request.setTitle(fileName)
            request.setDescription(activity.getString(R.string.downloading_file))

            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "${activity.getString(R.string.app_name)}/$fileName"
            )
            val dm = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            dm.enqueue(request)
        }
    } catch (e: IllegalStateException) {
        Toast.makeText(
            activity,
            "Please insert an SD card to download file",
            Toast.LENGTH_SHORT
        ).show()
    }
}

/**
 * Used to get MimeType from url.
 *
 * @param url Url.
 * @return Mime Type for the given url.
 */
fun getMimeType(url: String): String? {
    var type: String? = null
    val extension = MimeTypeMap.getFileExtensionFromUrl(url)
    if (extension != null) {
        val mime = MimeTypeMap.getSingleton()
        type = mime.getMimeTypeFromExtension(extension)
    }
    return type
}