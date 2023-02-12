package com.rohitthebest.manageyourrenters.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.BillPeriodType
import com.rohitthebest.manageyourrenters.database.model.*
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants
import com.rohitthebest.manageyourrenters.repositories.*
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.getDataFromFireStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val TAG = "SyncDocumentsFromFirestore"

@AndroidEntryPoint
class SyncDocumentsFromFirestoreService : Service() {

    @Inject
    lateinit var renterRepository: RenterRepository

    @Inject
    lateinit var renterPaymentRepository: RenterPaymentRepository

    @Inject
    lateinit var borrowerRepository: BorrowerRepository

    @Inject
    lateinit var borrowerPaymentRepository: BorrowerPaymentRepository

    @Inject
    lateinit var partialPaymentRepository: PartialPaymentRepository

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

    private var uid = ""


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        uid = Functions.getUid()!!

        val job1 = CoroutineScope(Dispatchers.IO).launch {
            syncRenterAndRenterPayment()
        }

        val job2 = CoroutineScope(Dispatchers.IO).launch {
            syncBorrowerAndBorrowerPayments()
        }

        val job3 = CoroutineScope(Dispatchers.IO).launch {
            syncEMIAndEMIPayments()
        }

        val job4 = CoroutineScope(Dispatchers.IO).launch {
            syncExpenseCategoryAndExpense()
        }

        val job5 = CoroutineScope(Dispatchers.IO).launch {
            syncMonthlyPayments()
        }

        val job6 = CoroutineScope(Dispatchers.IO).launch {
            syncPaymentMethods()
        }

        CoroutineScope(Dispatchers.IO).launch {

            delay(50)

            while (!job1.isCompleted || !job2.isCompleted || !job3.isCompleted || !job4.isCompleted || !job5.isCompleted || !job6.isCompleted) {

                delay(100)
                Log.d(TAG, "onStartCommand: sync time extended")
            }

            Log.d(TAG, "onStartCommand: Sync completed")

            Functions.saveBooleanToSharedPreference(
                applicationContext,
                Constants.IS_SYNCED_SHARED_PREF_NAME,
                Constants.IS_SYNCED_SHARED_PREF_KEY,
                true
            )

            stopSelf()
        }

        val notification = NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(R.drawable.ic_house_renters)
            .setContentTitle("Syncing data from cloud")
            .setProgress(100, 0, true)
            .build()

        startForeground(6826, notification)

        // staring stop timer
        CoroutineScope(Dispatchers.IO).launch {

            Log.d(TAG, "onStartCommand: timer started for 140 seconds")
            delay(TimeUnit.SECONDS.toMillis(140))
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private suspend fun syncMonthlyPayments() {

        // monthly payment category
        getDataFromFireStore(
            FirestoreCollectionsConstants.MONTHLY_PAYMENT_CATEGORIES,
            uid,
        ) {}?.let { monthlyPaymentCategories ->


            if (monthlyPaymentCategories.size() != 0) {

                withContext(Dispatchers.Main) {

                    monthlyPaymentCategoryRepository.deleteAllMonthlyPaymentCategoriesByIsSynced(
                        true
                    )
                    delay(50)
                    monthlyPaymentCategoryRepository.insertAllMonthlyPaymentCategory(
                        monthlyPaymentCategories.toObjects(
                            MonthlyPaymentCategory::class.java
                        )
                    )

                    // monthly payments
                    getDataFromFireStore(
                        FirestoreCollectionsConstants.MONTHLY_PAYMENTS,
                        uid,
                    ) {}?.let { monthlyPayments ->

                        if (monthlyPayments.size() != 0) {

                            withContext(Dispatchers.Main) {

                                monthlyPaymentRepository.deleteAllMonthlyPaymentByIsSynced(true)
                                delay(50)
                                monthlyPaymentRepository.insertAllMonthlyPayment(
                                    monthlyPayments.toObjects(
                                        MonthlyPayment::class.java
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun syncExpenseCategoryAndExpense() {

        // expense categories
        getDataFromFireStore(
            FirestoreCollectionsConstants.EXPENSE_CATEGORIES,
            uid,
        ) {}?.let { expenseCategory ->

            if (expenseCategory.size() != 0) {

                expenseCategoryRepository.deleteAllExpenseCategoriesByIsSynced(true)
                delay(50)
                expenseCategoryRepository.insertAllExpenseCategory(
                    expenseCategory.toObjects(
                        ExpenseCategory::class.java
                    )
                )

                //expenses
                getDataFromFireStore(
                    FirestoreCollectionsConstants.EXPENSES,
                    uid,
                ) {}?.let { expenses ->

                    if (expenses.size() != 0) {

                        expenseRepository.deleteExpenseByIsSynced(true)
                        delay(50)
                        expenseRepository.insertAllExpense(
                            expenses.toObjects(
                                Expense::class.java
                            )
                        )

                    }
                }
            }
        }
    }

    private suspend fun syncEMIAndEMIPayments() {

        // emis
        getDataFromFireStore(
            FirestoreCollectionsConstants.EMIs,
            uid,
        ) {}?.let { emis ->

            if (emis.size() != 0) {

                emiRepository.deleteEMIsByIsSynced(true)
                delay(50)
                emiRepository.insertAllEMI(emis.toObjects(EMI::class.java))

                //emi payments
                getDataFromFireStore(
                    FirestoreCollectionsConstants.EMI_PAYMENTS,
                    uid
                ) {}?.let { emiPayments ->

                    if (emiPayments.size() != 0) {

                        emiPaymentRepository.deleteEMIPaymentsByIsSynced(true)
                        delay(50)
                        emiPaymentRepository.insertAllEMIPayment(emiPayments.toObjects(EMIPayment::class.java))
                    }
                }
            }
        }
    }

    private suspend fun syncBorrowerAndBorrowerPayments() {

        //borrower
        getDataFromFireStore(
            FirestoreCollectionsConstants.BORROWERS,
            uid,
        ) {}?.let { borrowerSnapshot ->

            if (borrowerSnapshot.size() != 0) {


                borrowerRepository.deleteBorrowerByIsSynced(true)
                delay(50)
                borrowerRepository.insertBorrowers(borrowerSnapshot.toObjects(Borrower::class.java))

                //borrower payments
                getDataFromFireStore(
                    FirestoreCollectionsConstants.BORROWER_PAYMENTS,
                    uid,
                ) {}?.let { paymemtsSnapshot ->

                    if (paymemtsSnapshot.size() != 0) {

                        borrowerPaymentRepository.deleteBorrowerPaymentsByIsSynced(true)
                        delay(50)
                        borrowerPaymentRepository.insertAllBorrowerPayment(
                            paymemtsSnapshot.toObjects(
                                BorrowerPayment::class.java
                            )
                        )

                        // partial payments
                        getDataFromFireStore(
                            FirestoreCollectionsConstants.PARTIAL_PAYMENTS,
                            uid
                        ) {}?.let { partialPaymentSnapshot ->

                            if (partialPaymentSnapshot.size() != 0) {
                                partialPaymentRepository.deletePartialPaymentsByIsSynced(true)
                                delay(50)
                                partialPaymentRepository.insertAllPartialPayment(
                                    partialPaymentSnapshot.toObjects(
                                        PartialPayment::class.java
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun syncRenterAndRenterPayment() {

        CoroutineScope(Dispatchers.IO).launch {

            val renters = getDataFromFireStore(
                collection = FirestoreCollectionsConstants.RENTERS,
                uid = uid
            ) {}

            // renter
            renters?.let { renterSnapshot ->

                if (renterSnapshot.size() != 0) {

                    renterRepository.deleteRenterByIsSynced(getString(R.string.t))
                    delay(50)
                    renterRepository.insertRenters(renterSnapshot.toObjects(Renter::class.java))

                    //renter payments
                    val renterPayments = getDataFromFireStore(
                        collection = FirestoreCollectionsConstants.RENTER_PAYMENTS,
                        uid = uid
                    ) {}

                    renterPayments?.let { paymentSnapshot ->

                        if (paymentSnapshot.size() != 0) {

                            renterPaymentRepository.deleteAllPaymentsByIsSynced(true)
                            delay(50)

                            val paymentList = paymentSnapshot.toObjects(
                                RenterPayment::class.java
                            )

                            paymentList.forEach { payment ->

                                if (payment.billPeriodInfo.billPeriodType == BillPeriodType.BY_MONTH) {

                                    val selectedYear = payment.billPeriodInfo.billYear

                                    if (payment.billPeriodInfo.renterBillMonthType?.forBillYear == null ||
                                        payment.billPeriodInfo.renterBillMonthType?.forBillYear == 0
                                    ) {
                                        payment.billPeriodInfo.renterBillMonthType?.forBillYear =
                                            selectedYear
                                    }

                                    if (payment.billPeriodInfo.renterBillMonthType?.toBillYear == null ||
                                        payment.billPeriodInfo.renterBillMonthType?.toBillYear == 0
                                    ) {
                                        payment.billPeriodInfo.renterBillMonthType?.toBillYear =
                                            selectedYear
                                    }
                                }

                                renterPaymentRepository.insertRenterPayment(payment)
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun syncPaymentMethods() {

        getDataFromFireStore(
            FirestoreCollectionsConstants.PAYMENT_METHODS,
            uid
        ) {}?.let { paymentMethodSnapshot ->

            if (paymentMethodSnapshot.size() != 0) {

                paymentMethodRepository.deleteByIsSyncedValue(true)
                delay(50)
                paymentMethodRepository.insertAllPaymentMethod(
                    paymentMethodSnapshot.toObjects(
                        PaymentMethod::class.java
                    )
                )
            }
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}