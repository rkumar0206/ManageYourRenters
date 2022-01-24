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
import com.rohitthebest.manageyourrenters.repositories.ExpenseCategoryRepository
import com.rohitthebest.manageyourrenters.repositories.ExpenseRepository
import com.rohitthebest.manageyourrenters.repositories.api.ExpenseCategoryRepositoryAPI
import com.rohitthebest.manageyourrenters.repositories.api.ExpenseRepositoryAPI
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

private const val TAG = "ExpenseService"

@AndroidEntryPoint
class ExpenseService : Service() {

    @Inject
    lateinit var expenseRepositoryAPI: ExpenseRepositoryAPI

    @Inject
    lateinit var expenseCategoryRepositoryAPI: ExpenseCategoryRepositoryAPI

    @Inject
    lateinit var expenseRepository: ExpenseRepository

    @Inject
    lateinit var expenseCategoryRepository: ExpenseCategoryRepository

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val requestMethod = intent?.getStringExtra(Constants.REQUEST_METHOD_KEY)
        val expenseKey = intent?.getStringExtra(Constants.EXPENSE_KEY)

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
                    else -> "Expense"
                }
            )
            .setProgress(100, 0, true)
            .build()

        startForeground(Random.nextInt(1001, 8999), notification)

        CoroutineScope(Dispatchers.IO).launch {

            val expense = expenseRepository.getExpenseByKey(expenseKey!!).first()

            when (requestMethod) {

                getString(R.string.post) -> {

                    try {

                        val expenseCategory =
                            expenseCategoryRepository.getExpenseCategoryByKey(expense.categoryKey)
                                .first()

                        // check if the expense category is synced with cloud database, if not, sync it first

                        if (!expenseCategory.isSynced) {

                            val catResponse = expenseCategoryRepositoryAPI.addExpenseCategory(
                                expenseCategory.uid,
                                expenseCategory
                            )

                            if (catResponse.isSuccessful && (catResponse.code() == 201 || catResponse.code() == 200)) {

                                expenseCategory.isSynced = true
                                expenseCategoryRepository.updateExpenseCategory(expenseCategory)

                                postExpense(expense)
                            } else {

                                updateExpenseSyncValueInLocalDatabase(expense, false)
                                stopSelf()
                            }
                        } else {

                            postExpense(expense)
                        }

                    } catch (e: Exception) {

                        e.printStackTrace()
                    }

                }

                getString(R.string.put) -> {

                    try {

                        val expenseCategory =
                            expenseCategoryRepository.getExpenseCategoryByKey(expense.categoryKey)
                                .first()

                        // check if the expense category is synced with cloud database, if not, sync it first
                        if (!expenseCategory.isSynced) {

                            val catResponse = expenseCategoryRepositoryAPI.addExpenseCategory(
                                expenseCategory.uid,
                                expenseCategory
                            )

                            if (catResponse.isSuccessful && (catResponse.code() == 201 || catResponse.code() == 200)) {

                                expenseCategory.isSynced = true
                                expenseCategoryRepository.updateExpenseCategory(expenseCategory)

                                putExpense(expense)
                            } else {

                                updateExpenseSyncValueInLocalDatabase(expense, false)
                                stopSelf()
                            }
                        } else {

                            putExpense(expense)
                        }

                    } catch (e: Exception) {

                        e.printStackTrace()
                    }
                }

                getString(R.string.delete_one) -> {

                    try {// [Note] : in delete method, the expense will not be initialized as it
                        // will not be present in the local database anymore because it has been deleted
                        // from the viewModel, and hence cannot be used inside this condition

                        val response = expenseRepositoryAPI.deleteExpenseByKey(
                            getUid()!!, expenseKey
                        )

                        if (response.isSuccessful && response.code() == 204) {

                            Log.i(
                                TAG,
                                "onStartCommand: expense category successfully deleted with key $expenseKey"
                            )

                            stopSelf()
                        } else {

                            Log.i(TAG, "Error occurred")

                            showToast("Something went wrong!! Try again!", Toast.LENGTH_LONG)

                            // when the expense is not deleted from cloud then insert the expense to the local database again
                            val exp =
                                expenseRepositoryAPI.getExpenseByKey(getUid()!!, expenseKey).body()

                            //[Note] : this is not the appropriate way to do this, as when the exp
                            // is null, then the expense will be deleted from the local database,
                            // but it will never be deleted from the cloud database
                            exp?.let { myExpense ->

                                expenseRepository.insertExpense(myExpense)

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

    private suspend fun putExpense(expense: Expense) {

        val response = expenseRepositoryAPI.updateExpenseByKey(
            expense.uid, expense.key, expense.categoryKey, expense
        )

        if (response.isSuccessful && response.code() == 200) {

            Log.i(TAG, "onStartCommand: ${response.body()}")

            updateExpenseSyncValueInLocalDatabase(expense, true)

        } else {

            Log.i(TAG, "Error occurred")

            updateExpenseSyncValueInLocalDatabase(expense, false)
        }

        stopSelf()
    }

    private suspend fun postExpense(expense: Expense) {

        val response = expenseRepositoryAPI.addExpenseByCategoryKey(
            expense.uid, expense.categoryKey, expense
        )

        if (response.isSuccessful && (response.code() == 201 || response.code() == 200)) {


            Log.i(TAG, "onStartCommand: ${response.body()}")

            updateExpenseSyncValueInLocalDatabase(expense, true)

        } else {

            Log.i(TAG, "Error occurred")

            updateExpenseSyncValueInLocalDatabase(expense, false)
        }

        stopSelf()
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