package com.rohitthebest.manageyourrenters.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.BORROWER_PAYMENTS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.BUDGETS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.PARTIAL_PAYMENTS
import com.rohitthebest.manageyourrenters.repositories.BudgetRepository
import com.rohitthebest.manageyourrenters.repositories.UpdateIsSyncedValueForAnyTableRepository
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.convertJSONToStringList
import com.rohitthebest.manageyourrenters.utils.fromStringToPartialPaymentList
import com.rohitthebest.manageyourrenters.utils.uploadFilesOnFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val TAG = "UploadDocumentListToFir"

@AndroidEntryPoint
class UploadDocumentListToFireStoreService : Service() {

    @Inject
    lateinit var budgetRepository: BudgetRepository

    @Inject
    lateinit var updateIsSyncedValueForAnyTableRepository: UpdateIsSyncedValueForAnyTableRepository

    private lateinit var db: FirebaseFirestore
    private lateinit var batch: WriteBatch

    private var isUploadSuccessful = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val collection = intent?.getStringExtra(Constants.COLLECTION_KEY)
        // this is used when list of objects need to be uploaded directly without db interaction (ex: partialPayments)
        val uploadData = intent?.getStringExtra(Constants.UPLOAD_DATA_KEY)
        // this is used when only list of keys are passed and objects are retrieved through db here (ex: budgets)
        val keys = intent?.getStringExtra(Constants.KEY_LIST_KEY)
        val randomId = intent?.getIntExtra(Constants.RANDOM_ID_KEY, 2003)

        db = FirebaseFirestore.getInstance()
        batch = db.batch()

        val pendingIntent: PendingIntent =
            Functions.getPendingIntentForForegroundServiceNotification(
                this,
                if (collection == PARTIAL_PAYMENTS) BORROWER_PAYMENTS else collection
                    ?: ""
            )

        val image = R.drawable.ic_baseline_payment_24

        val notification = NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(image)
            .setContentIntent(pendingIntent)
            .setContentTitle("Uploading list to the $collection...")
            .setProgress(100, 0, true)
            .build()

        startForeground(randomId!!, notification)

        val keyList = if (collection != PARTIAL_PAYMENTS) {
            convertJSONToStringList(keys)
        } else {
            emptyList()
        }

        // starting stop timer
        val stopTimerJob = CoroutineScope(Dispatchers.IO).launch {

            Log.d(
                TAG,
                "onStartCommand: timer started for ${Constants.SERVICE_STOP_TIME_IN_SECONDS} seconds"
            )
            delay(TimeUnit.SECONDS.toMillis(Constants.SERVICE_STOP_TIME_IN_SECONDS))

            if (!isUploadSuccessful && collection != PARTIAL_PAYMENTS) {

                updateIsSyncedValueToFalse(collection!!, keyList)
            }

            stopSelf()
        }

        CoroutineScope(Dispatchers.IO).launch {

            when (collection) {

                PARTIAL_PAYMENTS -> {

                    Log.d(TAG, "onStartCommand: $uploadData")

                    val partialPaymentList = fromStringToPartialPaymentList(uploadData!!)

                    if (partialPaymentList.isNotEmpty()) {

                        partialPaymentList.forEach { partialPayment ->
                            batch.set(
                                db.collection(collection).document(partialPayment.key),
                                partialPayment
                            )
                        }
                        if (uploadFilesOnFirestore(batch)) {
                            Log.d(TAG, "onStartCommand: List Upload SUCCESSFUL")
                            isUploadSuccessful = true
                            stopTimerJob.cancel()
                            stopSelf()
                        } else {
                            Log.d(TAG, "onStartCommand: List Upload UNSUCCESSFUL")
                            stopTimerJob.cancel()
                            stopSelf()
                        }
                    } else {
                        stopTimerJob.cancel()
                        stopSelf()
                    }
                }

                BUDGETS -> {

                    if (keyList.isNotEmpty()) {
                        handleBudgetList(keyList, collection)
                    } else {
                        stopTimerJob.cancel()
                        stopSelf()
                    }
                }

                else -> {
                    stopTimerJob.cancel()
                    stopSelf()
                }
            }
        }

        return START_NOT_STICKY
    }

    private suspend fun handleBudgetList(keyList: List<String?>, collection: String) {

        val budgetModelList = budgetRepository.getAllBudgetsByKey(keyList).first()

        if (budgetModelList.isNotEmpty()) {

            budgetModelList.forEach { budget ->
                batch.set(db.collection(collection).document(budget.key), budget)
            }

            if (uploadFilesOnFirestore(batch)) {

                Log.d(TAG, "onStartCommand: Budget List Upload SUCCESSFUL")
                isUploadSuccessful = true
                stopSelf()
            } else {
                Log.d(TAG, "onStartCommand: Budget List Upload UNSUCCESSFUL")

                updateIsSyncedValueToFalse(collection, keyList)
                stopSelf()
            }
        }
    }

    private suspend fun updateIsSyncedValueToFalse(collection: String, keyList: List<String?>) {

        keyList.forEach { key ->

            key?.let {
                updateIsSyncedValueForAnyTableRepository.updateIsSyncValueToFalse(
                    collection, key
                )
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {

        return null
    }
}