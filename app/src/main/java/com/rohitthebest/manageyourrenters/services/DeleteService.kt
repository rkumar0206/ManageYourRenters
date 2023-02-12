package com.rohitthebest.manageyourrenters.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.COLLECTION_KEY
import com.rohitthebest.manageyourrenters.others.Constants.DOCUMENT_KEY
import com.rohitthebest.manageyourrenters.others.Constants.NOTIFICATION_CHANNEL_ID
import com.rohitthebest.manageyourrenters.others.Constants.RANDOM_ID_KEY
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.deleteFromFireStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class DeleteService : Service() {

    private val TAG = "DeleteService"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val collection = intent?.getStringExtra(COLLECTION_KEY)
        val key = intent?.getStringExtra(DOCUMENT_KEY)
        val randomId = intent?.getIntExtra(RANDOM_ID_KEY, 1006)

        val pendingIntent: PendingIntent =
            Functions.getPendingIntentForForegroundServiceNotification(
                this, collection ?: ""
            )

        val notification = NotificationCompat.Builder(
            this,
            NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(R.drawable.ic_baseline_delete_24)
            .setContentTitle("Deleting from $collection")
            .setContentIntent(pendingIntent)
            .setProgress(100, 0, true)
            .build()

        startForeground(randomId!!, notification)

        val docRef = FirebaseFirestore.getInstance()
            .collection(collection!!)
            .document(key!!)

        CoroutineScope(Dispatchers.IO).launch {

            if (deleteFromFireStore(docRef)) {

                stopSelf()
                Log.d(TAG, "onStartCommand: document deleted from collection $collection")
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