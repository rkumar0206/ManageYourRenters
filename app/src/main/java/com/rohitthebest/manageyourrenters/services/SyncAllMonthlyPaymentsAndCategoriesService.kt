package com.rohitthebest.manageyourrenters.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.rohitthebest.manageyourrenters.repositories.MonthlyPaymentCategoryRepository
import com.rohitthebest.manageyourrenters.repositories.MonthlyPaymentRepository
import com.rohitthebest.manageyourrenters.repositories.api.MonthlyPaymentCategoryRepositoryAPI
import com.rohitthebest.manageyourrenters.repositories.api.MonthlyPaymentRepositoryAPI
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.isValid
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "SyncAllMonthlyPaymentsA"

@AndroidEntryPoint
class SyncAllMonthlyPaymentsAndCategoriesService : Service() {

    @Inject
    lateinit var monthlyPaymentRepositoryAPI: MonthlyPaymentRepositoryAPI

    @Inject
    lateinit var monthlyPaymentCategoryRepositoryAPI: MonthlyPaymentCategoryRepositoryAPI

    @Inject
    lateinit var monthlyPaymentCategoryRepository: MonthlyPaymentCategoryRepository

    @Inject
    lateinit var monthlyPaymentRepository: MonthlyPaymentRepository

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val uid = Functions.getUid()

        if (uid.isValid()) {

            CoroutineScope(Dispatchers.IO).launch {

                withContext(Dispatchers.Default) {
                    job1(uid!!)
                }

                withContext(Dispatchers.Default) {
                    job2(uid!!)
                }

                onDone()
            }

        } else {

            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun onDone() {

        Log.d(TAG, "onStartCommand: all jobs completed")
        stopSelf()
    }

    private suspend fun job1(uid: String) {

        val monthlyPaymentCategoryResponse =
            monthlyPaymentCategoryRepositoryAPI.getMonthlyPaymentCategories(uid)

        if (monthlyPaymentCategoryResponse.isSuccessful && monthlyPaymentCategoryResponse.code() == 200) {

            monthlyPaymentCategoryRepository.deleteAllMonthlyPaymentCategoriesByIsSynced(
                true
            )

            monthlyPaymentCategoryResponse.body()?.monthlyPaymentCategories?.let { categories ->
                monthlyPaymentCategoryRepository.insertAllMonthlyPaymentCategory(
                    categories
                )

                Log.d(TAG, "onStartCommand monthly payments -> categories : $categories")
            }
        }

    }

    private suspend fun job2(uid: String) {

        val monthlyPaymentResponse =
            monthlyPaymentRepositoryAPI.getMonthlyPaymentsByUid(uid)

        if (monthlyPaymentResponse.isSuccessful && monthlyPaymentResponse.code() == 200) {

            monthlyPaymentRepository.deleteAllMonthlyPaymentByIsSynced(
                true
            )

            monthlyPaymentResponse.body()?.monthlyPayments?.let { payments ->
                monthlyPaymentRepository.insertAllMonthlyPayment(
                    payments
                )

                Log.d(TAG, "onStartCommand: monthly payments -> payments : $payments")
            }
        }

    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}