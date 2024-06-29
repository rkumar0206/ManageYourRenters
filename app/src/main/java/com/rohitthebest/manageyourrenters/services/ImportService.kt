package com.rohitthebest.manageyourrenters.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.ImportServiceHelperModel
import com.rohitthebest.manageyourrenters.database.model.Expense
import com.rohitthebest.manageyourrenters.database.model.ExpenseCategory
import com.rohitthebest.manageyourrenters.database.model.PaymentMethod
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants
import com.rohitthebest.manageyourrenters.repositories.ExpenseCategoryRepository
import com.rohitthebest.manageyourrenters.repositories.ExpenseRepository
import com.rohitthebest.manageyourrenters.repositories.PaymentMethodRepository
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.uploadBatchDocumentsToFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ImportService"

@AndroidEntryPoint
class ImportService : Service() {

    @Inject
    lateinit var expenseCategoryRepository: ExpenseCategoryRepository

    @Inject
    lateinit var expenseRepository: ExpenseRepository

    @Inject
    lateinit var paymentMethodRepository: PaymentMethodRepository

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val importServiceHelperModelJsonString = intent?.getStringExtra(Constants.UPLOAD_DATA_KEY)

        val pendingIntent: PendingIntent =
            Functions.getPendingIntentForForegroundServiceNotification(
                this, FirestoreCollectionsConstants.EXPENSE_CATEGORIES
            )

        val notification = NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(R.drawable.ic_house_renters)
            .setContentIntent(pendingIntent)
            .setContentTitle("Syncing import expenses with cloud.")
            .build()

        startForeground(9018, notification)

        if (importServiceHelperModelJsonString != null) {

            CoroutineScope(Dispatchers.IO).launch {

                val importServiceModels = Gson().fromJson(
                    importServiceHelperModelJsonString,
                    Array<ImportServiceHelperModel>::class.java
                )

                Log.d(
                    TAG,
                    "onStartCommand: ImportServiceHelperModels size: ${importServiceModels.size}"
                )

                val db = FirebaseFirestore.getInstance()
                val batch = db.batch()

                importServiceModels?.forEach { importServiceHelperModel ->

                    importServiceHelperModel.documents.forEach { document ->

                        batch.set(
                            db.collection(importServiceHelperModel.collectionKey)
                                .document(document.key), document.value
                        )
                    }

                    insertDocumentsToDB(importServiceHelperModel)

                }

                delay(250)

                if (uploadBatchDocumentsToFirestore(batch)) {

                    Log.d(TAG, "onStartCommand: Uploaded All Files to Firestore")
                    stopSelf()
                }
            }

        } else {
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private suspend fun insertDocumentsToDB(importServiceHelperModel: ImportServiceHelperModel) {

        val gson = Gson()

        when (importServiceHelperModel.collectionKey) {

            FirestoreCollectionsConstants.EXPENSE_CATEGORIES -> {

                val expenseCategories =
                    importServiceHelperModel.documents.map {
                        gson.fromJson(
                            gson.toJson(it.value),
                            ExpenseCategory::class.java
                        )
                    }

                Log.d(TAG, "insertDocumentsToDB: expenseCategories: $expenseCategories")

                expenseCategoryRepository.insertAllExpenseCategory(expenseCategories)
            }

            FirestoreCollectionsConstants.EXPENSES -> {

                val expenses = importServiceHelperModel.documents.map {
                    gson.fromJson(
                        gson.toJson(it.value),
                        Expense::class.java
                    )
                }

                Log.d(TAG, "insertDocumentsToDB: expenses: $expenses")
                expenseRepository.insertAllExpense(expenses)
            }

            FirestoreCollectionsConstants.PAYMENT_METHODS -> {

                val paymentMethods =
                    importServiceHelperModel.documents.map {

                        gson.fromJson(
                            gson.toJson(it.value),
                            PaymentMethod::class.java
                        )
                    }

                Log.d(TAG, "insertDocumentsToDB: paymentMethods: $paymentMethods")
                paymentMethodRepository.insertAllPaymentMethod(paymentMethods)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {

        return null
    }
}