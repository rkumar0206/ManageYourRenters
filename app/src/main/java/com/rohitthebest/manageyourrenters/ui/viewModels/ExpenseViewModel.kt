package com.rohitthebest.manageyourrenters.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.apiModels.Expense
import com.rohitthebest.manageyourrenters.repositories.ExpenseRepository
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.expenseServiceHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    fun insertExpense(context: Context, expense: Expense) = viewModelScope.launch {

        if (isInternetAvailable(context)) {

            expense.isSynced = true

            expenseServiceHelper(
                context,
                expense,
                context.getString(R.string.post)
            )
        }

        expenseRepository.insertExpense(expense)

        Functions.showToast(context, "Expense saved")

    }

    fun insertAllExpense(expenses: List<Expense>) = viewModelScope.launch {
        expenseRepository.insertAllExpense(expenses)
    }

    fun updateExpense(context: Context, expense: Expense) = viewModelScope.launch {

        if (isInternetAvailable(context)) {

            expense.isSynced = true

            expenseServiceHelper(
                context,
                expense,
                context.getString(R.string.put)
            )
        } else {

            expense.isSynced = false
        }

        expenseRepository.updateExpense(expense)

        Functions.showToast(context, "Expense updated")
    }

    fun deleteExpense(context: Context, expense: Expense) = viewModelScope.launch {

        if (isInternetAvailable(context)) {

            expenseServiceHelper(
                context,
                expense,
                context.getString(R.string.delete_one)
            )
        }

        expenseRepository.deleteExpense(expense)
        Functions.showToast(context, "Expense deleted")
    }

    fun deleteAllExpenses() = viewModelScope.launch {
        expenseRepository.deleteAllExpenses()
    }

    fun getAllExpenses() = expenseRepository.getAllExpenses().asLiveData()

    fun getExpenseAmountSumByExpenseCategoryKey(expenseCategoryKey: String) =
        expenseRepository.getExpenseAmountSumByExpenseCategoryKey(expenseCategoryKey)

    fun getExpenseAmountSumByExpenseCategoryByDateRange(
        expenseCategoryKey: String, date1: Long, date2: Long
    ) = expenseRepository.getExpenseAmountSumByExpenseCategoryByDateRange(
        expenseCategoryKey, date1, date2
    )

    fun getTotalExpenseAmountByExpenseCategory(expenseCategoryKey: String) =
        expenseRepository.getTotalExpenseAmountByExpenseCategory(expenseCategoryKey).asLiveData()

    fun getTotalExpenseAmount() = expenseRepository.getTotalExpenseAmount().asLiveData()

    fun getTotalExpenseAmountByDateRange(date1: Long, date2: Long) =
        expenseRepository.getTotalExpenseAmountByDateRange(date1, date2).asLiveData()

    fun getTotalExpenseAmountByCategoryKeyAndDateRange(
        expenseCategoryKey: String,
        date1: Long,
        date2: Long
    ) =
        expenseRepository.getTotalExpenseAmountByCategoryKeyAndDateRange(
            expenseCategoryKey,
            date1,
            date2
        ).asLiveData()

    fun getExpensesByDateRange(date1: Long, date2: Long) =
        expenseRepository.getExpensesByDateRange(date1, date2).asLiveData()

    fun getExpenseByDateRangeAndExpenseCategoryKey(
        expenseCategoryKey: String, date1: Long, date2: Long
    ) = expenseRepository.getExpenseByDateRangeAndExpenseCategoryKey(
        expenseCategoryKey,
        date1,
        date2
    ).asLiveData()

    fun getExpenseByKey(expenseKey: String) =
        expenseRepository.getExpenseByKey(expenseKey).asLiveData()

    fun getExpensesByExpenseCategoryKey(expenseCategoryKey: String) =
        expenseRepository.getExpensesByExpenseCategoryKey(expenseCategoryKey).asLiveData()

}