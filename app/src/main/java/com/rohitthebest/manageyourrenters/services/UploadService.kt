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
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.BORROWERS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.BORROWER_PAYMENTS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EMI_PAYMENTS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EMIs
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EXPENSES
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EXPENSE_CATEGORIES
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.MONTHLY_PAYMENTS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.MONTHLY_PAYMENT_CATEGORIES
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.RENTERS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.RENTER_PAYMENTS
import com.rohitthebest.manageyourrenters.repositories.*
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.insertToFireStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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

        CoroutineScope(Dispatchers.IO).launch {

            var model: Any? = null

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

                else -> {
                    stopSelf()
                }
            }

            if (model != null) {

                updateIsSyncedValueOfDocument(
                    collection,
                    model,
                    insertToFireStore(docRef, model)
                )
                stopSelf()
            } else {

                stopSelf()
            }
        }

        return START_NOT_STICKY
    }


    private suspend fun updateIsSyncedValueOfDocument(
        collection: String,
        document: Any,
        isSyncedValue: Boolean
    ) {

        if (!isSyncedValue) {
            when (collection) {

                getString(R.string.renters) -> {

                    val renter = document as Renter
                    renter.isSynced = getString(R.string.f)
                    renterRepository.updateRenter(renter)
                }

                getString(R.string.renter_payments) -> {

                    val payment = document as RenterPayment
                    payment.isSynced = isSyncedValue
                    renterPaymentRepository.updateRenterPayment(payment)
                }

                getString(R.string.borrowers) -> {

                    val borrower = document as Borrower
                    borrower.isSynced = isSyncedValue
                    borrowerRepository.update(borrower)
                }

                getString(R.string.borrowerPayments) -> {
                    val borrowerPayment = document as BorrowerPayment
                    borrowerPayment.isSynced = isSyncedValue
                    borrowerPaymentRepository.updateBorrowerPayment(borrowerPayment)
                }

                getString(R.string.emis) -> {
                    val emi = document as EMI
                    emi.isSynced = isSyncedValue
                    emiRepository.updateEMI(emi)
                }

                getString(R.string.emiPayments) -> {
                    val emiPayment = document as EMIPayment
                    emiPayment.isSynced = isSyncedValue
                    emiPaymentRepository.updateEMIPayment(emiPayment)
                }

                EXPENSE_CATEGORIES -> {
                    val expenseCategory = document as ExpenseCategory
                    expenseCategory.isSynced = isSyncedValue
                    expenseCategoryRepository.updateExpenseCategory(expenseCategory)
                }

                EXPENSES -> {

                    val expense = document as Expense
                    expense.isSynced = isSyncedValue
                    expenseRepository.updateExpense(expense)
                }

                MONTHLY_PAYMENT_CATEGORIES -> {

                    val category = document as MonthlyPaymentCategory
                    category.isSynced = isSyncedValue
                    monthlyPaymentCategoryRepository.updateMonthlyPaymentCategory(category)
                }
                MONTHLY_PAYMENTS -> {

                    val monthlyPayment = document as MonthlyPayment
                    monthlyPayment.isSynced = isSyncedValue
                    monthlyPaymentRepository.updateMonthlyPayment(monthlyPayment)
                }

                else -> stopSelf()
            }

        }

        if (isSyncedValue) {

            Log.d(
                TAG,
                "updateIsSyncedValueOfDocument: upload in collection $collection, was successful"
            )
        } else {

            Log.d(
                TAG,
                "updateIsSyncedValueOfDocument: upload in collection $collection, was UNSUCCESSFUL"
            )
        }


    }

    override fun onBind(p0: Intent?): IBinder? {

        return null
    }
}
