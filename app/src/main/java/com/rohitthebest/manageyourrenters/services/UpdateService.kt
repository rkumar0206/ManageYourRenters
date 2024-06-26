package com.rohitthebest.manageyourrenters.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.COLLECTION_KEY
import com.rohitthebest.manageyourrenters.others.Constants.DOCUMENT_KEY
import com.rohitthebest.manageyourrenters.others.Constants.RANDOM_ID_KEY
import com.rohitthebest.manageyourrenters.others.Constants.UPDATE_DOCUMENT_MAP_KEY
import com.rohitthebest.manageyourrenters.repositories.UpdateIsSyncedValueForAnyTableRepository
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.convertJsonToObject
import com.rohitthebest.manageyourrenters.utils.updateDocumentOnFireStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class UpdateService : Service() {

    private val TAG = "UpdateService"

    @Inject
    lateinit var updateIsSyncedValueForAnyTableRepository: UpdateIsSyncedValueForAnyTableRepository

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val collection = intent?.getStringExtra(COLLECTION_KEY)
        val key = intent?.getStringExtra(DOCUMENT_KEY)

        val map = intent?.getStringExtra(UPDATE_DOCUMENT_MAP_KEY)?.convertJsonToObject(HashMap::class.java) as HashMap<String, Any?>

        val randomId = intent?.getIntExtra(RANDOM_ID_KEY, 1000)

        val pendingIntent: PendingIntent =
            Functions.getPendingIntentForForegroundServiceNotification(
                this, collection ?: ""
            )

        val notification = NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(R.drawable.expense_shortcut_icon)
            .setContentIntent(pendingIntent)
            .setContentTitle("Updating on $collection.")
            .setProgress(100, 0, true)
            .build()

        startForeground(randomId!!, notification)

        val docRef = FirebaseFirestore.getInstance()
            .collection(collection!!)
            .document(key!!)

        // starting stop timer
        val stopTimerJob = CoroutineScope(Dispatchers.IO).launch {

            Log.d(
                TAG,
                "onStartCommand: timer started for ${Constants.SERVICE_STOP_TIME_IN_SECONDS} seconds"
            )
            delay(TimeUnit.SECONDS.toMillis(Constants.SERVICE_STOP_TIME_IN_SECONDS))

            updateIsSyncedValueForAnyTableRepository.updateIsSyncValueToFalse(
                collection, key
            )
            stopSelf()
        }

        CoroutineScope(Dispatchers.IO).launch {

            if (updateDocumentOnFireStore(docRef, map)) {

                Log.d(
                    TAG,
                    "onStartCommand: Updated document in collection $collection with key $key"
                )
                stopTimerJob.cancel()
                stopSelf()
            } else {
                Log.d(
                    TAG,
                    "onStartCommand: Updated document in collection $collection with key $key was UNSUCCESSFUL"
                )
                stopTimerJob.cancel()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {

        return null
    }
}