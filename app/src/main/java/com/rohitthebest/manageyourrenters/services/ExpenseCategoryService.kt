package com.rohitthebest.manageyourrenters.services

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.EXPENSE_CATEGORY_KEY
import com.rohitthebest.manageyourrenters.others.Constants.REQUEST_METHOD_KEY
import com.rohitthebest.manageyourrenters.repositories.api.ExpenseCategoryRepositoryAPI
import com.rohitthebest.manageyourrenters.ui.activities.HomeActivity
import com.rohitthebest.manageyourrenters.utils.fromStringToExpenseCategory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

private const val TAG = "ExpenseCategoryService"

@AndroidEntryPoint
class ExpenseCategoryService : Service() {

    @Inject
    lateinit var expenseCategoryRepositoryAPI: ExpenseCategoryRepositoryAPI

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val requestMethod = intent?.getStringExtra(REQUEST_METHOD_KEY)
        val expenseCategoryString = intent?.getStringExtra(EXPENSE_CATEGORY_KEY)

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
            .setContentTitle("Expense category")
            .build()

        startForeground(Random.nextInt(1001, 8999), notification)

        val expenseCategory = fromStringToExpenseCategory(expenseCategoryString!!)

        when (requestMethod) {

            getString(R.string.post) -> {

                CoroutineScope(Dispatchers.IO).launch {

                    val response = expenseCategoryRepositoryAPI.addExpenseCategory(
                        expenseCategory.uid, expenseCategory
                    )

                    if (response.isSuccessful && (response.code() == 201 || response.code() == 200)) {

                        Log.i(
                            TAG,
                            "onStartCommand: expense category successfully added with key ${expenseCategory.key}"
                        )

                        Log.i(TAG, "onStartCommand: ${response.body()}")

                        stopSelf()
                    } else {

                        Log.i(
                            TAG,
                            "Error occurred"
                        )

                        Log.i(TAG, "onStartCommand: ${response.body()}")

                        stopSelf()
                    }

                }

            }

            getString(R.string.put) -> {

                CoroutineScope(Dispatchers.IO).launch {

                    val response = expenseCategoryRepositoryAPI.updateExpenseCategoryByKey(
                        expenseCategory.uid, expenseCategory.key, expenseCategory
                    )

                    if (response.isSuccessful && response.code() == 200) {

                        Log.i(
                            TAG,
                            "onStartCommand: expense category successfully update with key ${expenseCategory.key}"
                        )

                        Log.i(TAG, "onStartCommand: ${response.body()}")

                        stopSelf()

                    } else {

                        Log.i(
                            TAG,
                            "Error occurred"
                        )

                        Log.i(TAG, "onStartCommand: ${response.body()}")

                        stopSelf()
                    }
                }

            }

            getString(R.string.delete_one) -> {

                CoroutineScope(Dispatchers.IO).launch {

                    val response = expenseCategoryRepositoryAPI.deleteExpenseCategoryByKey(
                        expenseCategory.uid, expenseCategory.key
                    )

                    if (response.isSuccessful && response.code() == 204) {

                        Log.i(
                            TAG,
                            "onStartCommand: expense category successfully deleted with key ${expenseCategory.key}"
                        )

                        stopSelf()
                    } else {

                        Log.i(
                            TAG,
                            "Error occurred"
                        )

                        Log.i(TAG, "onStartCommand: ${response.body()}")

                        stopSelf()
                    }
                }

            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}