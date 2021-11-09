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
import com.rohitthebest.manageyourrenters.repositories.api.ExpenseRepositoryAPI
import com.rohitthebest.manageyourrenters.ui.activities.HomeActivity
import com.rohitthebest.manageyourrenters.utils.fromStringToExpense
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

private const val TAG = "ExpenseService"

@AndroidEntryPoint
class ExpenseService : Service() {

    @Inject
    private lateinit var expenseRepositoryAPI: ExpenseRepositoryAPI

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
            .setContentTitle("Expense category")
            .build()

        startForeground(Random.nextInt(1001, 8999), notification)

        val expense = fromStringToExpense(expenseString!!)

        when (requestMethod) {


            getString(R.string.post) -> {

                CoroutineScope(Dispatchers.IO).launch {

                    val response = expenseRepositoryAPI.addExpenseByCategoryKey(
                        expense.uid, expense.categoryKey, expense
                    )

                    if (response.isSuccessful && response.code() == 201) {

                        Log.i(
                            TAG,
                            "onStartCommand: expense category successfully added with key ${expense.key}"
                        )

                        Log.i(TAG, "onStartCommand: ${response.body()}")

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