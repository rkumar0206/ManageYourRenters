package com.rohitthebest.manageyourrenters.services

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPaymentCategory
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.repositories.MonthlyPaymentCategoryRepository
import com.rohitthebest.manageyourrenters.repositories.MonthlyPaymentRepository
import com.rohitthebest.manageyourrenters.repositories.api.MonthlyPaymentCategoryRepositoryAPI
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

private const val TAG = "MonthlyPaymentCategoryService"

@AndroidEntryPoint
class MonthlyPaymentCategoryService : Service() {

    @Inject
    lateinit var monthlyPaymentCategoryRepositoryAPI: MonthlyPaymentCategoryRepositoryAPI

    @Inject
    lateinit var monthlyPaymentCategoryRepository: MonthlyPaymentCategoryRepository

    @Inject
    lateinit var monthlyPaymentRepository: MonthlyPaymentRepository

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val requestMethod = intent?.getStringExtra(Constants.REQUEST_METHOD_KEY)
        val monthlyPaymentCategoryKey =
            intent?.getStringExtra(Constants.MONTHLY_PAYMENT_CATEGORY_KEY)

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
                    else -> "Monthly payment category"
                }
            )
            .setProgress(100, 0, true)
            .build()

        startForeground(Random.nextInt(1001, 8999), notification)

        CoroutineScope(Dispatchers.IO).launch {

            val monthlyPaymentCategory = monthlyPaymentCategoryRepository
                .getMonthlyPaymentCategoryUsingKey(monthlyPaymentCategoryKey!!).first()

            when (requestMethod) {

                getString(R.string.post) -> {

                    try {
                        val response =
                            monthlyPaymentCategoryRepositoryAPI.insertMonthlyPaymentCategory(
                                monthlyPaymentCategory.uid, monthlyPaymentCategory
                            )

                        if (response.isSuccessful && (response.code() == 201 || response.code() == 200)) {

                            Log.i(TAG, "onStartCommand: ${response.body()}")

                            updateMonthlyPaymentIsSyncedValue(monthlyPaymentCategory, true)

                            stopSelf()
                        } else {

                            Log.i(TAG, "Error occurred")
                            updateMonthlyPaymentIsSyncedValue(monthlyPaymentCategory, false)

                        }
                    } catch (e: Exception) {

                        e.printStackTrace()
                        stopSelf()
                    }
                }

                //[PUT - update]
                getString(R.string.put) -> {

                    try {

                        val response =
                            monthlyPaymentCategoryRepositoryAPI.updateMonthlyPaymentCategory(
                                monthlyPaymentCategory.uid,
                                monthlyPaymentCategoryKey,
                                monthlyPaymentCategory
                            )

                        if (response.isSuccessful && response.code() == 200) {

                            Log.i(TAG, "onStartCommand: ${response.body()}")
                            updateMonthlyPaymentIsSyncedValue(monthlyPaymentCategory, true)

                        } else {

                            Log.i(TAG, "Error occurred")
                            updateMonthlyPaymentIsSyncedValue(monthlyPaymentCategory, false)
                        }

                        stopSelf()
                    } catch (e: Exception) {

                        e.printStackTrace()
                        stopSelf()
                    }
                }

                //[DELETE]
                getString(R.string.delete_one) -> {

                    try {
                        // [Note] : in delete method, the expenseCategory will not be initialized as it
                        // will not be present in the local database anymore because it has been deleted
                        // from the viewModel, and hence cannot be used inside this condition

                        val response =
                            monthlyPaymentCategoryRepositoryAPI.deleteMonthlyPaymentCategory(
                                Functions.getUid()!!, monthlyPaymentCategoryKey
                            )

                        if (response.isSuccessful && response.code() == 204) {

                            Log.i(
                                TAG,
                                "onStartCommand: expense category successfully deleted with key $monthlyPaymentCategoryKey"
                            )

                            // deleting all the expenses inside this category form local storage
                            monthlyPaymentRepository.deleteAllMonthlyPaymentsByCategoryKey(
                                monthlyPaymentCategoryKey
                            )
                            stopSelf()
                        } else {

                            Log.i(TAG, "Error occurred")

                            showToast(
                                "Something went wrong... Please try again...",
                                Toast.LENGTH_LONG
                            )
                            // when the category is not deleted from the cloud, then insert it again to
                            // the local database

                            val monthlyPaymentCat =
                                monthlyPaymentCategoryRepositoryAPI.getMonthlyPaymentCategoryByKey(
                                    Functions.getUid()!!, monthlyPaymentCategoryKey
                                ).body()

                            //[Note] : this is not the appropriate way to do this, as when the expenseCat
                            // is null, then the expense category and all the expenses inside it
                            // will be deleted from the local database, but it will never be deleted
                            // from the cloud database
                            if (monthlyPaymentCat != null) {

                                monthlyPaymentCategoryRepository.insertMonthlyPaymentCategory(
                                    monthlyPaymentCat
                                )
                            } else {

                                monthlyPaymentRepository.deleteAllMonthlyPaymentsByCategoryKey(
                                    monthlyPaymentCategoryKey
                                )
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

    private suspend fun updateMonthlyPaymentIsSyncedValue(
        monthlyPaymentCategory: MonthlyPaymentCategory,
        isSyncedValue: Boolean
    ) {

        monthlyPaymentCategory.isSynced = isSyncedValue
        monthlyPaymentCategoryRepository.updateMonthlyPaymentCategory(monthlyPaymentCategory)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}