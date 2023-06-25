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
import com.rohitthebest.manageyourrenters.database.model.*
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.COLLECTION_KEY
import com.rohitthebest.manageyourrenters.others.Constants.DOCUMENT_KEY
import com.rohitthebest.manageyourrenters.others.Constants.RANDOM_ID_KEY
import com.rohitthebest.manageyourrenters.others.Constants.SERVICE_STOP_TIME_IN_SECONDS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.BORROWERS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.BORROWER_PAYMENTS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.BUDGETS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EMI_PAYMENTS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EMIs
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EXPENSES
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EXPENSE_CATEGORIES
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.MONTHLY_PAYMENTS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.MONTHLY_PAYMENT_CATEGORIES
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.PAYMENT_METHODS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.RENTERS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.RENTER_PAYMENTS
import com.rohitthebest.manageyourrenters.repositories.*
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.insertToFireStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val TAG = "UploadService"

@AndroidEntryPoint
class UploadService : Service() {

    @Inject
    lateinit var borrowerRepository: BorrowerRepository

    @Inject
    lateinit var borrowerPaymentRepository: BorrowerPaymentRepository

    @Inject
    lateinit var renterRepository: RenterRepository

    @Inject
    lateinit var renterPaymentRepository: RenterPaymentRepository

    @Inject
    lateinit var emiRepository: EMIRepository

    @Inject
    lateinit var emiPaymentRepository: EMIPaymentRepository

    @Inject
    lateinit var expenseCategoryRepository: ExpenseCategoryRepository

    @Inject
    lateinit var expenseRepository: ExpenseRepository

    @Inject
    lateinit var monthlyPaymentCategoryRepository: MonthlyPaymentCategoryRepository

    @Inject
    lateinit var monthlyPaymentRepository: MonthlyPaymentRepository

    @Inject
    lateinit var paymentMethodRepository: PaymentMethodRepository

    @Inject
    lateinit var updateIsSyncedValueForAnyTableRepository: UpdateIsSyncedValueForAnyTableRepository

    @Inject
    lateinit var budgetRepository: BudgetRepository

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val collection = intent?.getStringExtra(COLLECTION_KEY)
        val key = intent?.getStringExtra(DOCUMENT_KEY)
        val randomId = intent?.getIntExtra(RANDOM_ID_KEY, 1003)

        val image = R.drawable.ic_house_renters

        val pendingIntent: PendingIntent =
            Functions.getPendingIntentForForegroundServiceNotification(
                this, collection ?: ""
            )

        val notification = NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(image)
            .setContentIntent(pendingIntent)
            .setContentTitle("Uploading to $collection.")
            .setProgress(100, 0, true)
            .build()

        startForeground(randomId!!, notification)

        val docRef = FirebaseFirestore.getInstance()
            .collection(collection!!)
            .document(key!!)

        var model: Any? = null

        // starting stop timer
        val stopTimerJob = CoroutineScope(Dispatchers.IO).launch {

            Log.d(TAG, "onStartCommand: timer started for $SERVICE_STOP_TIME_IN_SECONDS seconds")

            delay(TimeUnit.SECONDS.toMillis(SERVICE_STOP_TIME_IN_SECONDS))

            if (model != null) {

                updateIsSyncedValueForAnyTableRepository.updateIsSyncValueToFalse(collection, key)
                Log.d(
                    TAG,
                    "updateIsSyncedValueOfDocument: upload in collection $collection, was UNSUCCESSFUL"
                )
                stopSelf()
            } else {
                stopSelf()
            }
        }

        CoroutineScope(Dispatchers.IO).launch {

            when (collection) {

                RENTERS -> {
                    model = renterRepository.getRenterByKey(key).first()
                }

                RENTER_PAYMENTS -> {
                    model = renterPaymentRepository.getPaymentByPaymentKey(key).first()
                }

                BORROWERS -> {
                    model = borrowerRepository.getBorrowerByKey(key).first()
                }

                BORROWER_PAYMENTS -> {
                    model = borrowerPaymentRepository.getBorrowerPaymentByKey(key).first()
                }

                EMIs -> {
                    model = emiRepository.getEMIByKey(key).first()
                }

                EMI_PAYMENTS -> {
                    model = emiPaymentRepository.getEMIPaymentByKey(key).first()
                }

                EXPENSE_CATEGORIES -> {
                    model = expenseCategoryRepository.getExpenseCategoryByKey(key).first()
                }

                EXPENSES -> {
                    model = expenseRepository.getExpenseByKey(key).first()
                }

                MONTHLY_PAYMENT_CATEGORIES -> {
                    model = monthlyPaymentCategoryRepository.getMonthlyPaymentCategoryUsingKey(key)
                        .first()
                }

                MONTHLY_PAYMENTS -> {
                    model = monthlyPaymentRepository.getMonthlyPaymentByKey(key).first()
                }

                PAYMENT_METHODS -> {

                    model = paymentMethodRepository.getPaymentMethodByKey(key).first()
                }

                BUDGETS -> {

                    model = budgetRepository.getBudgetByKey(key).first()
                }

                else -> {
                    stopTimerJob.cancel()
                    stopSelf()
                }
            }

            if (model != null) {

                if (!insertToFireStore(docRef, model!!)) {

                    // upload was unsuccessful therefore updating the isSynced value to false
                    updateIsSyncedValueForAnyTableRepository.updateIsSyncValueToFalse(
                        collection,
                        key
                    )
                    Log.d(
                        TAG,
                        "updateIsSyncedValueOfDocument: upload in collection $collection, was UNSUCCESSFUL"
                    )
                } else {
                    Log.d(
                        TAG,
                        "updateIsSyncedValueOfDocument: upload in collection $collection, was successful"
                    )
                }
                stopTimerJob.cancel()
                stopSelf()
            } else {
                stopTimerJob.cancel()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {

        return null
    }
}
