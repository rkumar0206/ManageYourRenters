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

    fun getExpensesByExpenseCategoryKey(expenseCategoryKey: String) =
        dao.getExpensesByExpenseCategoryKey(expenseCategoryKey)

    fun getExpenseByKey(expenseKey: String) = dao.getExpenseByKey(expenseKey)
}