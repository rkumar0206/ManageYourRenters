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
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.PARTIAL_PAYMENTS
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.fromStringToPartialPaymentList
import com.rohitthebest.manageyourrenters.utils.uploadFilesOnFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private const val TAG = "UploadDocumentListToFir"

class UploadDocumentListToFireStoreService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val collection = intent?.getStringExtra(Constants.COLLECTION_KEY)
        val uploadData = intent?.getStringExtra(Constants.UPLOAD_DATA_KEY)
        val randomId = intent?.getIntExtra(Constants.RANDOM_ID_KEY, 1003)

        val pendingIntent: PendingIntent =
            Functions.getPendingIntentForForegroundServiceNotification(
                this,
                if (collection == getString(R.string.partialPayments)) getString(R.string.borrowerPayments) else collection
                    ?: ""
            )

        val image = R.drawable.ic_baseline_payment_24

        val notification = NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(image)
            .setContentIntent(pendingIntent)
            .setContentTitle("Uploading list to the $collection...")
            .build()

        startForeground(randomId!!, notification)

        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        CoroutineScope(Dispatchers.IO).launch {

            when (collection) {

                PARTIAL_PAYMENTS -> {

                    Log.d(TAG, "onStartCommand: $uploadData")

                    val partialPaymentList = fromStringToPartialPaymentList(uploadData!!)

                    if (partialPaymentList.isNotEmpty()) {

                        partialPaymentList.forEach { partialPayment ->

                            batch.set(
                                db.collection(collection)
                                    .document(partialPayment.key),
                                partialPayment
                            )
                        }

                        if (uploadFilesOnFirestore(batch)) {

                            Log.d(TAG, "onStartCommand: List Upload SUCCESSFUL")
                            stopSelf()
                        } else {
                            Log.d(TAG, "onStartCommand: List Upload UNSUCCESSFUL")
                            stopSelf()
                        }
                    } else {
                        stopSelf()
                    }
                }
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

    override fun onBind(intent: Intent?): IBinder? {

        return null
    }
}