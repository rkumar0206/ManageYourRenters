package com.rohitthebest.manageyourrenters.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.COLLECTION_KEY
import com.rohitthebest.manageyourrenters.others.Constants.DOCUMENT_KEY
import com.rohitthebest.manageyourrenters.others.Constants.RANDOM_ID_KEY
import com.rohitthebest.manageyourrenters.others.Constants.UPLOAD_DATA_KEY
import com.rohitthebest.manageyourrenters.utils.ConversionWithGson.Companion.convertJSONtoPayment
import com.rohitthebest.manageyourrenters.utils.ConversionWithGson.Companion.convertJSONtoRenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UploadService : Service() {

    private val TAG = "UploadService"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val collection = intent?.getStringExtra(COLLECTION_KEY)
        val key = intent?.getStringExtra(DOCUMENT_KEY)
        val uploadData = intent?.getStringExtra(UPLOAD_DATA_KEY)
        val randomId = intent?.getIntExtra(RANDOM_ID_KEY, 1003)

        val notification = NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(R.drawable.ic_baseline_person_add_24)
            .setContentTitle("Uploading renter details on cloud.")
            .build()

        startForeground(randomId!!, notification)

        val docRef = FirebaseFirestore.getInstance()
            .collection(collection!!)
            .document(key!!)

        CoroutineScope(Dispatchers.IO).launch {

            when (collection) {

                getString(R.string.renters) -> {

                    if (insertToFireStore(docRef, convertJSONtoRenter(uploadData))) {

                        Log.d(
                            TAG,
                            "onStartCommand: Uploaded renter details to collection $collection with key : $key"
                        )
                        stopSelf()
                    }
                }

                getString(R.string.payments) -> {

                    if (insertToFireStore(docRef, convertJSONtoPayment(uploadData))) {

                        Log.d(
                            TAG,
                            "onStartCommand: Uploaded This category to collection $collection and key $key"
                        )
                        stopSelf()
                    }
                }

/*
                getString(R.string.subCategories) -> {

                    if (insertToFireStore(docRef, convertJSONtoSubCategory(uploadData)!!)) {

                        Log.d(
                            TAG,
                            "onStartCommand: Uploaded This sub-category to collection $collection and key $key"
                        )
                        stopSelf()
                    }
                }
*/

/*
                getString(R.string.texts) -> {

                    if (insertToFireStore(docRef, convertJSONtoText(uploadData)!!)) {

                        Log.d(
                            TAG,
                            "onStartCommand: Uploaded This note to collection $collection and key $key"
                        )
                        stopSelf()
                    }
                }
*/

                else -> {

                    stopSelf()
                }
            }

        }


        return START_NOT_STICKY
    }

    private suspend fun insertToFireStore(docRef: DocumentReference, data: Any): Boolean {

        return try {

            Log.i(TAG, "insertToFireStore")

            docRef.set(data)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

    }


    override fun onBind(p0: Intent?): IBinder? {

        return null
    }
}