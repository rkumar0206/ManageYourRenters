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
import com.rohitthebest.manageyourrenters.others.Constants.KEY_LIST_KEY
import com.rohitthebest.manageyourrenters.utils.ConversionWithGson.Companion.convertJSONToStringList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DeleteAllDocumentsService : Service() {

    private val TAG = "DeleteAllDocuments"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val keys = intent?.getStringExtra(KEY_LIST_KEY)
        val collection = intent?.getStringExtra(Constants.COLLECTION_KEY)
        val randomId = intent?.getIntExtra(Constants.RANDOM_ID_KEY, 3000)

        val notification = NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(R.drawable.ic_baseline_delete_forever_24)
            .setContentTitle("Deleting from cloud.")
            .build()

        startForeground(randomId!!, notification)

        val keyList = convertJSONToStringList(keys)

        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        if (keyList.isEmpty()) {

            stopSelf()
        } else {

            keyList.forEach {

                collection?.let { it1 -> db.collection(it1).document(it!!) }?.let { it2 ->
                    batch.delete(
                        it2
                    )
                }
            }

            CoroutineScope(Dispatchers.IO).launch {

                delay(250)

                if (deleteFiles(batch)) {

                    Log.d(TAG, "onStartCommand: Deleted All Files")
                    stopSelf()
                }
            }
        }

        return START_REDELIVER_INTENT
    }

    private suspend fun deleteFiles(batch: WriteBatch): Boolean {

        return try {
            batch.commit()
                .await()

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