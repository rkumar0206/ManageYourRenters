package com.rohitthebest.manageyourrenters.services

import android.annotation.SuppressLint
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
import com.rohitthebest.manageyourrenters.others.Constants.RANDOM_ID_KEY
import com.rohitthebest.manageyourrenters.others.Constants.UPLOAD_DATA_KEY
import com.rohitthebest.manageyourrenters.ui.activities.HomeActivity
import com.rohitthebest.manageyourrenters.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "UploadService"

class UploadService : Service() {

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val collection = intent?.getStringExtra(COLLECTION_KEY)
        val key = intent?.getStringExtra(DOCUMENT_KEY)
        val uploadData = intent?.getStringExtra(UPLOAD_DATA_KEY)
        val randomId = intent?.getIntExtra(RANDOM_ID_KEY, 1003)

        val image = R.drawable.ic_house_renters

        val pendingIntent: PendingIntent =
            Intent(this, HomeActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val notification = NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(image)
            .setContentIntent(pendingIntent)
            .setContentTitle("Uploading to $collection.")
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
                            "onStartCommand: Uploaded payment to collection $collection with key $key"
                        )
                        stopSelf()
                    }
                }

                getString(R.string.borrowers) -> {

                    if (insertToFireStore(docRef, fromStringToBorrower(uploadData!!))) {

                        Log.d(
                            TAG,
                            "onStartCommand: Uploaded borrower to collection $collection with key $key"
                        )
                        stopSelf()
                    }
                }

                getString(R.string.borrowerPayments) -> {

                    if (insertToFireStore(docRef, fromStringToBorrowerPayment(uploadData!!))) {

                        Log.d(
                            TAG,
                            "onStartCommand: Uploaded borrower payment to collection $collection with key $key"
                        )
                        stopSelf()
                    }
                }

                getString(R.string.emis) -> {

                    if (insertToFireStore(docRef, fromStringToEMI(uploadData!!))) {

                        Log.d(
                            TAG,
                            "onStartCommand: Uploaded emi to collection $collection with key $key"
                        )
                        stopSelf()
                    }
                }

                getString(R.string.emiPayments) -> {

                    if (insertToFireStore(docRef, fromStringToEMIPayment(uploadData!!))) {

                        Log.d(
                            TAG,
                            "onStartCommand: Uploaded emi_payment to collection $collection with key $key"
                        )
                        stopSelf()
                    }
                }

                else -> {

                    stopSelf()
                }
            }

        }


        return START_NOT_STICKY
    }


    override fun onBind(p0: Intent?): IBinder? {

        return null
    }
}
