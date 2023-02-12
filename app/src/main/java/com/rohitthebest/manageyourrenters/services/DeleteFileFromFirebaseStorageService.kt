package com.rohitthebest.manageyourrenters.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.storage.FirebaseStorage
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.utils.deleteFileFromFirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class DeleteFileFromFirebaseStorageService : Service() {

    private val TAG = "DeleteImageService"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val docUrl = intent?.getStringExtra(Constants.DELETE_FILE_FROM_FIREBASE_KEY)

        val randomId = intent?.getIntExtra(Constants.RANDOM_ID_KEY, 93)

        val notification = NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(R.drawable.ic_document)
            .setContentTitle("Deleting file from cloud storage.")
            .build()

        startForeground(randomId!!, notification)

        val mStorageRef = docUrl?.let {
            FirebaseStorage.getInstance()
                .getReferenceFromUrl(it)
        }

        CoroutineScope(Dispatchers.IO).launch {

            if (deleteFileFromFirebaseStorage(mStorageRef)) {

                stopSelf()
                Log.d(TAG, "onStartCommand: Image Deleted from firebase storage.")
            }
        }

        // starting stop timer
        CoroutineScope(Dispatchers.IO).launch {

            Log.d(
                TAG,
                "onStartCommand: timer started for ${Constants.SERVICE_STOP_TIME_IN_SECONDS} seconds"
            )
            delay(TimeUnit.SECONDS.toMillis(Constants.SERVICE_STOP_TIME_IN_SECONDS))
            stopSelf()
        }


        return START_NOT_STICKY
    }


    override fun onBind(p0: Intent?): IBinder? {

        return null
    }
}