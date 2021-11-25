package com.rohitthebest.manageyourrenters.repositories

import com.rohitthebest.manageyourrenters.database.dao.ExpenseDAO
import com.rohitthebest.manageyourrenters.database.model.apiModels.Expense
import javax.inject.Inject

class ExpenseRepository @Inject constructor(
    val dao: ExpenseDAO
) {

    suspend fun insertExpense(expense: Expense) = dao.insertExpense(expense)

    suspend fun insertAllExpense(expenses: List<Expense>) =
        dao.insertAllExpense(expenses)

    suspend fun updateExpense(expense: Expense) =
        dao.updateExpense(expense)

    suspend fun deleteExpense(expense: Expense) =
        dao.deleteExpense(expense)

    suspend fun deleteAllExpenses() = dao.deleteAllExpenses()

    suspend fun deleteExpenseByExpenseCategoryKey(expenseCategoryKey: String) =
        dao.deleteExpenseByExpenseCategoryKey(expenseCategoryKey)

    suspend fun deleteExpenseByIsSynced(isSynced: Boolean) = dao.deleteExpenseByIsSynced(isSynced)

    fun getAllExpenses() = dao.getAllExpenses()

    fun getExpenseAmountSumByExpenseCategoryKey(expenseCategoryKey: String) =
        dao.getExpenseAmountSumByExpenseCategoryKey(expenseCategoryKey)

    fun getExpenseAmountSumByExpenseCategoryByDateRange(
        expenseCategoryKey: String, date1: Long, date2: Long
    ) = dao.getExpenseAmountSumByExpenseCategoryByDateRange(
        expenseCategoryKey, date1, date2
    )

    fun getTotalExpenseAmountByExpenseCategory(expenseCategoryKey: String) =
        dao.getTotalExpenseAmountByExpenseCategory(expenseCategoryKey)

    fun getExpenseByDateRangeAndExpenseCategoryKey(
        expenseCategoryKey: String, date1: Long, date2: Long
    ) = dao.getExpenseByDateRangeAndExpenseCategoryKey(expenseCategoryKey, date1, date2)

    fun getExpensesByExpenseCategoryKey(expenseCategoryKey: String) =
        dao.getExpensesByExpenseCategoryKey(expenseCategoryKey)

    fun getExpenseByKey(expenseKey: String) = dao.getExpenseByKey(expenseKey)
}