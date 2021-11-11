package com.rohitthebest.manageyourrenters.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.rohitthebest.manageyourrenters.repositories.ExpenseCategoryRepository
import com.rohitthebest.manageyourrenters.repositories.ExpenseRepository
import com.rohitthebest.manageyourrenters.repositories.api.ExpenseCategoryRepositoryAPI
import com.rohitthebest.manageyourrenters.repositories.api.ExpenseRepositoryAPI
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.isValid
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "GetAllExpenseCategorySe"

@AndroidEntryPoint
class GetAllExpenseAndExpenseCategoryService : Service() {

    @Inject
    lateinit var expenseCategoryRepositoryAPI: ExpenseCategoryRepositoryAPI

    @Inject
    lateinit var expenseRepositoryAPI: ExpenseRepositoryAPI

    @Inject
    lateinit var expenseCategoryRepository: ExpenseCategoryRepository

    @Inject
    lateinit var expenseRepository: ExpenseRepository

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val uid = Functions.getUid()

        CoroutineScope(Dispatchers.IO).launch {

            if (uid.isValid()) {

                val expenseCategoryResponse =
                    expenseCategoryRepositoryAPI.getExpenseCategories(uid.toString())

                if (expenseCategoryResponse.isSuccessful && expenseCategoryResponse.code() == 200) {

                    expenseCategoryResponse.body()?.expenseCategories?.let { expenseCategories ->

                        expenseCategoryRepository.insertAllExpenseCategory(
                            expenseCategories
                        )

                        Log.d(TAG, "onStartCommand: $expenseCategories")

                    }
                }

                val expenseResponse = expenseRepositoryAPI.getExpensesByUID(uid.toString())

                if (expenseResponse.isSuccessful && expenseResponse.code() == 200) {

                    expenseResponse.body()?.expenses?.let { expenses ->

                        expenseRepository.insertAllExpense(expenses)

                        Log.d(TAG, "onStartCommand: $expenses")
                    }
                }

                stopSelf()

            }
        }


        return START_NOT_STICKY
    }


    override fun onBind(intent: Intent?): IBinder? {

        return null
    }
}