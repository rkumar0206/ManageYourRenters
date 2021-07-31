package com.rohitthebest.manageyourrenters.services

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
import com.rohitthebest.manageyourrenters.others.Constants.UPDATE_DOCUMENT_MAP_KEY
import com.rohitthebest.manageyourrenters.utils.updateDocumentOnFireStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpdateService : Service() {

    private val TAG = "UpdateService"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val collection = intent?.getStringExtra(COLLECTION_KEY)
        val key = intent?.getStringExtra(DOCUMENT_KEY)
        val map =
            intent?.getSerializableExtra(UPDATE_DOCUMENT_MAP_KEY) as HashMap<String, Any?>

        val randomId = intent.getIntExtra(RANDOM_ID_KEY, 1000)

        val notification = NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(R.drawable.ic_baseline_payment_24)
            .setContentTitle("Updating on $collection.")
            .build()

        startForeground(randomId, notification)


        val docRef = FirebaseFirestore.getInstance()
            .collection(collection!!)
            .document(key!!)

        CoroutineScope(Dispatchers.IO).launch {

            if (updateDocumentOnFireStore(docRef, map)) {

                Log.d(
                    TAG,
                    "onStartCommand: Updated document in collection $collection with key $key"
                )
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {

        return null
    }
}