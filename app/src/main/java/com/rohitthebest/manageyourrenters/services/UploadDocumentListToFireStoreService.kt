package com.rohitthebest.manageyourrenters.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.utils.fromStringToPartialPaymentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "UploadDocumentListToFir"

class UploadDocumentListToFireStoreService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val collection = intent?.getStringExtra(Constants.COLLECTION_KEY)
        val key = intent?.getStringExtra(Constants.DOCUMENT_KEY)
        val uploadData = intent?.getStringExtra(Constants.UPLOAD_DATA_KEY)
        val randomId = intent?.getIntExtra(Constants.RANDOM_ID_KEY, 1003)

        val image =
            if (collection == getString(R.string.renters) || collection == getString(R.string.borrowers)) {

                R.drawable.ic_baseline_person_add_24
            } else {
                R.drawable.ic_baseline_payment_24
            }

        val notification = NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(image)
            .setContentTitle("Uploading list to the database...")
            .build()

        startForeground(randomId!!, notification)

        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        when (collection) {

            getString(R.string.partialPayments) -> {

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

                    CoroutineScope(Dispatchers.IO).launch {

                        delay(150)

                        if (uploadList(batch)) {

                            Log.d(TAG, "onStartCommand: Upload successfull")
                            stopSelf()
                        }
                    }


                } else {

                    stopSelf()
                }

            }
        }

        return START_NOT_STICKY
    }

    private suspend fun uploadList(batch: WriteBatch): Boolean {

        return try {

            batch.commit().await()

            true
        } catch (e: Exception) {

            e.printStackTrace()
            false
        }
    }

    override fun onBind(intent: Intent?): IBinder? {

        return null
    }
}