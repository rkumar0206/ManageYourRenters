package com.rohitthebest.manageyourrenters.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPayment
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.repositories.MonthlyPaymentCategoryRepository
import com.rohitthebest.manageyourrenters.repositories.MonthlyPaymentRepository
import com.rohitthebest.manageyourrenters.repositories.api.MonthlyPaymentCategoryRepositoryAPI
import com.rohitthebest.manageyourrenters.repositories.api.MonthlyPaymentRepositoryAPI
import com.rohitthebest.manageyourrenters.ui.activities.HomeActivity
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

private const val TAG = "MonthlyPaymentService"

@AndroidEntryPoint
class MonthlyPaymentService : Service() {

    @Inject
    lateinit var monthlyPaymentCategoryRepositoryAPI: MonthlyPaymentCategoryRepositoryAPI

    @Inject
    lateinit var monthlyPaymentCategoryRepository: MonthlyPaymentCategoryRepository

    @Inject
    lateinit var monthlyPaymentRepositoryAPI: MonthlyPaymentRepositoryAPI

    @Inject
    lateinit var monthlyPaymentRepository: MonthlyPaymentRepository

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val requestMethod = intent?.getStringExtra(Constants.REQUEST_METHOD_KEY)
        val monthlyPaymentKey =
            intent?.getStringExtra(Constants.MONTHLY_PAYMENT_KEY)

        val pendingIntent: PendingIntent =
            Intent(this, HomeActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val image = R.drawable.ic_track_money

        val notification = NotificationCompat.Builder(
            this,
            Constants.NOTIFICATION_CHANNEL_ID
        ).setSmallIcon(image)
            .setContentIntent(pendingIntent)
            .setContentTitle(
                when (requestMethod) {
                    getString(R.string.post) -> "Saving to cloud"
                    getString(R.string.put) -> "Updating on cloud"
                    getString(R.string.delete_one) -> "Deleting from cloud"
                    else -> "Monthly payment"
                }
            )
            .setProgress(100, 0, true)
            .build()

        startForeground(Random.nextInt(1001, 8999), notification)

        CoroutineScope(Dispatchers.IO).launch {

            val monthlyPayment = monthlyPaymentRepository
                .getMonthlyPaymentByKey(monthlyPaymentKey!!).first()

            when (requestMethod) {

                getString(R.string.post) -> {

                    try {

                        val monthlyPaymentCategory =
                            monthlyPaymentCategoryRepository.getMonthlyPaymentCategoryUsingKey(
                                monthlyPayment.categoryKey
                            ).first()

                        // check if the monthly payment category is synced with cloud database, if not, sync it first

                        if (!monthlyPaymentCategory.isSynced) {

                            val catResponse =
                                monthlyPaymentCategoryRepositoryAPI.insertMonthlyPaymentCategory(
                                    monthlyPaymentCategory.uid,
                                    monthlyPaymentCategory
                                )

                            if (catResponse.isSuccessful && (catResponse.code() == 201 || catResponse.code() == 200)) {

                                monthlyPaymentCategory.isSynced = true
                                monthlyPaymentCategoryRepository.updateMonthlyPaymentCategory(
                                    monthlyPaymentCategory
                                )

                                postMonthlyPayment(monthlyPayment)
                            } else {

                                updateMonthlyPaymentSyncValueInLocalDatabase(monthlyPayment, false)
                                stopSelf()
                            }
                        } else {

                            postMonthlyPayment(monthlyPayment)
                        }

                    } catch (e: Exception) {

                        e.printStackTrace()
                    }

                }

                getString(R.string.put) -> {

                    try {

                        val monthlyPaymentCategory =
                            monthlyPaymentCategoryRepository.getMonthlyPaymentCategoryUsingKey(
                                monthlyPayment.categoryKey
                            ).first()

                        if (!monthlyPaymentCategory.isSynced) {

                            val catResponse =
                                monthlyPaymentCategoryRepositoryAPI.insertMonthlyPaymentCategory(
                                    monthlyPayment.uid,
                                    monthlyPaymentCategory
                                )

                            if (catResponse.isSuccessful && (catResponse.code() == 201 || catResponse.code() == 200)) {

                                monthlyPaymentCategory.isSynced = true
                                monthlyPaymentCategoryRepository.updateMonthlyPaymentCategory(
                                    monthlyPaymentCategory
                                )

                                putMonthlyPayment(monthlyPayment)
                            } else {

                                updateMonthlyPaymentSyncValueInLocalDatabase(monthlyPayment, false)
                                stopSelf()
                            }
                        } else {

                            putMonthlyPayment(monthlyPayment)
                        }

                    } catch (e: Exception) {

                        e.printStackTrace()
                    }
                }

                getString(R.string.delete_one) -> {

                    try {
                        // [Note] : in delete method, the monthly payment will not be initialized as it
                        // will not be present in the local database anymore because it has been deleted
                        // from the viewModel, and hence cannot be used inside this condition

                        val response = monthlyPaymentRepositoryAPI.deleteMonthlyPaymentByKey(
                            Functions.getUid()!!, monthlyPaymentKey
                        )

                        if (response.isSuccessful && response.code() == 204) {

                            stopSelf()
                        } else {

                            Log.i(TAG, "Error occurred")

                            showToast("Something went wrong!! Try again!", Toast.LENGTH_LONG)

                            val mp = monthlyPaymentRepositoryAPI.getMonthlyPaymentByKey(
                                Functions.getUid()!!,
                                monthlyPaymentKey
                            ).body()

                            mp?.let { myMonthlyPayment ->

                                monthlyPaymentRepository.insertMonthlyPayment(myMonthlyPayment)
                            }

                            delay(200)

                            stopSelf()
                        }
                    } catch (e: Exception) {

                        e.printStackTrace()
                        stopSelf()
                    }

                }
            }
        }
        return START_NOT_STICKY
    }

    private suspend fun putMonthlyPayment(monthlyPayment: MonthlyPayment) {

        val response = monthlyPaymentRepositoryAPI.updateMonthlyPaymentsKey(
            monthlyPayment.uid, monthlyPayment.key, monthlyPayment.categoryKey, monthlyPayment
        )

        if (response.isSuccessful && response.code() == 200) {

            Log.i(TAG, "onStartCommand: ${response.body()}")

            updateMonthlyPaymentSyncValueInLocalDatabase(monthlyPayment, true)

        } else {

            Log.i(TAG, "Error occurred")

            updateMonthlyPaymentSyncValueInLocalDatabase(monthlyPayment, false)
        }

        stopSelf()
    }


    private suspend fun postMonthlyPayment(monthlyPayment: MonthlyPayment) {

        val response = monthlyPaymentRepositoryAPI.insertMonthlyPayment(
            monthlyPayment.uid, monthlyPayment.categoryKey, monthlyPayment
        )

        if (response.isSuccessful && (response.code() == 201 || response.code() == 200)) {


            Log.i(TAG, "onStartCommand: ${response.body()}")

            updateMonthlyPaymentSyncValueInLocalDatabase(monthlyPayment, true)

        } else {

            Log.i(TAG, "Error occurred")

            updateMonthlyPaymentSyncValueInLocalDatabase(monthlyPayment, false)
        }

        stopSelf()
    }

    private suspend fun updateMonthlyPaymentSyncValueInLocalDatabase(
        monthlyPayment: MonthlyPayment,
        isSyncedValue: Boolean
    ) {

        monthlyPayment.isSynced = isSyncedValue
        monthlyPaymentRepository.updateMonthlyPayment(monthlyPayment)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}