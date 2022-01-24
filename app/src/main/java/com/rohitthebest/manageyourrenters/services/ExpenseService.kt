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
import com.rohitthebest.manageyourrenters.database.model.apiModels.Expense
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.repositories.ExpenseRepository
import com.rohitthebest.manageyourrenters.repositories.api.ExpenseRepositoryAPI
import com.rohitthebest.manageyourrenters.ui.activities.HomeActivity
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.fromStringToExpense
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

private const val TAG = "ExpenseService"

@AndroidEntryPoint
class ExpenseService : Service() {

    @Inject
    lateinit var expenseRepositoryAPI: ExpenseRepositoryAPI

    @Inject
    lateinit var expenseRepository: ExpenseRepository

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val requestMethod = intent?.getStringExtra(Constants.REQUEST_METHOD_KEY)
        val expenseString = intent?.getStringExtra(Constants.EXPENSE_KEY)

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
            .setContentTitle("Expense")
            .setProgress(100, 0, true)
            .build()

        startForeground(Random.nextInt(1001, 8999), notification)

        val expense = fromStringToExpense(expenseString!!)


        when (requestMethod) {

            getString(R.string.post) -> {

                CoroutineScope(Dispatchers.IO).launch {

                    val response = expenseRepositoryAPI.addExpenseByCategoryKey(
                        expense.uid, expense.categoryKey, expense
                    )

                    if (response.isSuccessful && (response.code() == 201 || response.code() == 200)) {

                        Log.i(
                            TAG,
                            "onStartCommand: expense category successfully added with key ${expense.key}"
                        )

                        Log.i(TAG, "onStartCommand: ${response.body()}")

                        updateExpenseSyncValueInLocalDatabase(expense, true)
                        stopSelf()
                    } else {

                        Log.i(
                            TAG,
                            "Error occurred"
                        )

                        Log.i(TAG, "onStartCommand: ${response.body()}")

                        val expenseOriginal = expenseRepository.getExpenseByKey(expense.key).first()

                        expenseOriginal.isSynced = false
                        expenseRepository.updateExpense(expenseOriginal)

                        delay(200)

                        //updateExpenseSyncValueInLocalDatabase(expense, false)
                        stopSelf()
                    }

                }

            }

            getString(R.string.put) -> {

                CoroutineScope(Dispatchers.IO).launch {

                    val response = expenseRepositoryAPI.updateExpenseByKey(
                        expense.uid, expense.key, expense.categoryKey, expense
                    )

                    if (response.isSuccessful && response.code() == 200) {

                        Log.i(
                            TAG,
                            "onStartCommand: expense category successfully update with key ${expense.key}"
                        )

                        Log.i(TAG, "onStartCommand: ${response.body()}")

                        updateExpenseSyncValueInLocalDatabase(expense, true)

                        stopSelf()

                    } else {

                        Log.i(
                            TAG,
                            "Error occurred"
                        )

                        Log.i(TAG, "onStartCommand: ${response.body()}")

                        updateExpenseSyncValueInLocalDatabase(expense, false)
                        stopSelf()
                    }
                }

            }

            getString(R.string.delete_one) -> {

                CoroutineScope(Dispatchers.IO).launch {

                    val response = expenseRepositoryAPI.deleteExpenseByKey(
                        expense.uid, expense.key
                    )

                    if (response.isSuccessful && response.code() == 204) {

                        Log.i(
                            TAG,
                            "onStartCommand: expense category successfully deleted with key ${expense.key}"
                        )

                        stopSelf()
                    } else {

                        Log.i(
                            TAG,
                            "Error occurred"
                        )

                        Log.i(TAG, "onStartCommand: ${response.body()}")

                        showToast("Something went wrong!! Try again!", Toast.LENGTH_LONG)

                        // when the expense is not deleted from cloud then insert the expense to the local database again
                        expenseRepository.insertExpense(expense)

                        stopSelf()
                    }
                }

            }
        }


        return START_NOT_STICKY
    }

    private suspend fun updateExpenseSyncValueInLocalDatabase(
        expense: Expense,
        isSyncedValue: Boolean
    ) {

        Log.d(TAG, "updateExpenseSyncValueInLocalDatabase: ")

        expense.isSynced = isSyncedValue
        expenseRepository.updateExpense(expense)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}