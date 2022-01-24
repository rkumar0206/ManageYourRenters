package com.rohitthebest.manageyourrenters.services

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.apiModels.ExpenseCategory
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.EXPENSE_CATEGORY_KEY
import com.rohitthebest.manageyourrenters.others.Constants.REQUEST_METHOD_KEY
import com.rohitthebest.manageyourrenters.repositories.ExpenseCategoryRepository
import com.rohitthebest.manageyourrenters.repositories.ExpenseRepository
import com.rohitthebest.manageyourrenters.repositories.api.ExpenseCategoryRepositoryAPI
import com.rohitthebest.manageyourrenters.ui.activities.HomeActivity
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getUid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

private const val TAG = "ExpenseCategoryService"

@AndroidEntryPoint
class ExpenseCategoryService : Service() {

    @Inject
    lateinit var expenseCategoryRepositoryAPI: ExpenseCategoryRepositoryAPI

    @Inject
    lateinit var expenseCategoryRepository: ExpenseCategoryRepository

    @Inject
    lateinit var expenseRepository: ExpenseRepository

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val requestMethod = intent?.getStringExtra(REQUEST_METHOD_KEY)
        val expenseCategoryKey = intent?.getStringExtra(EXPENSE_CATEGORY_KEY)

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
                    else -> "Expense category"
                }
            )
            .setProgress(100, 0, true)
            .build()

        startForeground(Random.nextInt(1001, 8999), notification)

        CoroutineScope(Dispatchers.IO).launch {

            val expenseCategory =
                expenseCategoryRepository.getExpenseCategoryByKey(expenseCategoryKey!!).first()

            when (requestMethod) {

                //[POST]
                getString(R.string.post) -> {

                    try {
                        val response = expenseCategoryRepositoryAPI.addExpenseCategory(
                            expenseCategory.uid, expenseCategory
                        )

                        if (response.isSuccessful && (response.code() == 201 || response.code() == 200)) {

                            Log.i(TAG, "onStartCommand: ${response.body()}")

                            updateExpenseCategoryIsSyncedValue(expenseCategory, true)

                        } else {

                            Log.i(TAG, "Error occurred")
                            updateExpenseCategoryIsSyncedValue(expenseCategory, false)

                        }
                    } catch (e: Exception) {

                        e.printStackTrace()
                    }

                    stopSelf()
                }

                //[PUT - update]
                getString(R.string.put) -> {

                    try {

                        val response = expenseCategoryRepositoryAPI.updateExpenseCategoryByKey(
                            expenseCategory.uid, expenseCategory.key, expenseCategory
                        )

                        if (response.isSuccessful && response.code() == 200) {

                            Log.i(TAG, "onStartCommand: ${response.body()}")
                            updateExpenseCategoryIsSyncedValue(expenseCategory, true)

                        } else {

                            Log.i(TAG, "Error occurred")
                            updateExpenseCategoryIsSyncedValue(expenseCategory, false)
                        }
                    } catch (e: Exception) {

                        e.printStackTrace()
                    }

                    stopSelf()
                }

                //[DELETE]
                getString(R.string.delete_one) -> {

                    try {// [Note] : in delete method, the expenseCategory will not be initialized as it
                        // will not be present in the local database anymore because it has been deleted
                        // from the viewModel, and hence cannot be used inside this condition

                        val response = expenseCategoryRepositoryAPI.deleteExpenseCategoryByKey(
                            getUid()!!, expenseCategoryKey
                        )

                        if (response.isSuccessful && response.code() == 204) {

                            Log.i(
                                TAG,
                                "onStartCommand: expense category successfully deleted with key $expenseCategoryKey"
                            )

                            // deleting all the expenses inside this category form local storage
                            expenseRepository.deleteExpenseByExpenseCategoryKey(expenseCategoryKey)
                            stopSelf()
                        } else {

                            Log.i(TAG, "Error occurred")

                            showToast(
                                "Something went wrong... Please try again...",
                                Toast.LENGTH_LONG
                            )
                            // when the category is not deleted from the cloud, then insert it again to
                            // the local database

                            val expenseCat = expenseCategoryRepositoryAPI.getExpenseCategoryByKey(
                                getUid()!!, expenseCategoryKey
                            ).body()

                            //[Note] : this is not the appropriate way to do this, as when the expenseCat
                            // is null, then the expense category and all the expenses inside it
                            // will be deleted from the local database, but it will never be deleted
                            // from the cloud database
                            if (expenseCat != null) {

                                expenseCategoryRepository.insertExpenseCategory(expenseCat)
                            } else {

                                expenseRepository.deleteExpenseByExpenseCategoryKey(
                                    expenseCategoryKey
                                )
                            }

                            delay(200)

                            stopSelf()
                        }
                    } catch (e: Exception) {

                        e.printStackTrace()
                    }
                }
            }
        }
        return START_NOT_STICKY
    }

    private suspend fun updateExpenseCategoryIsSyncedValue(
        expenseCategory: ExpenseCategory,
        isSyncedValue: Boolean
    ) {

        expenseCategory.isSynced = isSyncedValue
        expenseCategoryRepository.updateExpenseCategory(expenseCategory)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}