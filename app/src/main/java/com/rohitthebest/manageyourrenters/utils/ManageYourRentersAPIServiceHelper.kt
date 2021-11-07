package com.rohitthebest.manageyourrenters.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.rohitthebest.manageyourrenters.database.model.apiModels.ExpenseCategory
import com.rohitthebest.manageyourrenters.others.Constants.EXPENSE_CATEGORY_KEY
import com.rohitthebest.manageyourrenters.others.Constants.REQUEST_METHOD_KEY
import com.rohitthebest.manageyourrenters.services.ExpenseCategoryService

fun expenseCategoryService(
    context: Context,
    expenseCategory: ExpenseCategory,
    requestMethod: String
) {

    val foregroundService = Intent(context, ExpenseCategoryService::class.java)

    foregroundService.putExtra(
        REQUEST_METHOD_KEY,
        requestMethod
    )

    foregroundService.putExtra(
        EXPENSE_CATEGORY_KEY,
        fromExpenseCategoryToString(expenseCategory)
    )

    ContextCompat.startForegroundService(context, foregroundService)
}