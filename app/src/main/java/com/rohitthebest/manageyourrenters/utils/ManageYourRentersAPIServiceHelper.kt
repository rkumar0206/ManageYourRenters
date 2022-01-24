package com.rohitthebest.manageyourrenters.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.rohitthebest.manageyourrenters.others.Constants.EXPENSE_CATEGORY_KEY
import com.rohitthebest.manageyourrenters.others.Constants.EXPENSE_KEY
import com.rohitthebest.manageyourrenters.others.Constants.REQUEST_METHOD_KEY
import com.rohitthebest.manageyourrenters.services.ExpenseCategoryService
import com.rohitthebest.manageyourrenters.services.ExpenseService

fun expenseCategoryServiceHelper(
    context: Context,
    expenseCategoryKey: String,
    requestMethod: String
) {

    val foregroundService = Intent(context, ExpenseCategoryService::class.java)

    foregroundService.putExtra(
        REQUEST_METHOD_KEY,
        requestMethod
    )

    foregroundService.putExtra(
        EXPENSE_CATEGORY_KEY,
        expenseCategoryKey
    )

    ContextCompat.startForegroundService(context, foregroundService)
}

fun expenseServiceHelper(
    context: Context,
    expenseKey: String,
    requestMethod: String
) {

    val foregroundService = Intent(context, ExpenseService::class.java)

    foregroundService.putExtra(
        REQUEST_METHOD_KEY,
        requestMethod
    )

    foregroundService.putExtra(
        EXPENSE_KEY,
        expenseKey
    )

    ContextCompat.startForegroundService(context, foregroundService)
}