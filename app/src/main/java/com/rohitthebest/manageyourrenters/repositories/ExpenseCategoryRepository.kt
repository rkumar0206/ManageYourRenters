package com.rohitthebest.manageyourrenters.repositories

import com.rohitthebest.manageyourrenters.database.dao.ExpenseCategoryDAO
import com.rohitthebest.manageyourrenters.database.model.ExpenseCategory
import javax.inject.Inject

class ExpenseCategoryRepository @Inject constructor(
    val dao: ExpenseCategoryDAO
) {

    suspend fun insertExpenseCategory(expenseCategory: ExpenseCategory) =
        dao.insertExpenseCategory(expenseCategory)

    suspend fun insertAllExpenseCategory(expenseCategories: List<ExpenseCategory>) =
        dao.insertAllExpenseCategory(expenseCategories)

    suspend fun updateExpenseCategory(expenseCategory: ExpenseCategory) =
        dao.updateExpenseCategory(expenseCategory)

    suspend fun deleteExpenseCategory(expenseCategory: ExpenseCategory) =
        dao.deleteExpenseCategory(expenseCategory)

    suspend fun deleteAllExpenseCategories() = dao.deleteAllExpenseCategories()

    suspend fun deleteAllExpenseCategoriesByIsSynced(isSynced: Boolean) =
        dao.deleteAllExpenseCategoriesByIsSynced(isSynced)

    fun getAllExpenseCategories() = dao.getAllExpenseCategories()
    fun getExpenseCategoriesByKey(expenseCategoryKeys: List<String>) =
        dao.getExpenseCategoriesByKey(expenseCategoryKeys)

    fun getExpenseCategoryByKey(key: String) = dao.getExpenseCategoryByKey(key)

    fun getAllExpenseCategoriesByLimit(limit: Int) = dao.getAllExpenseCategoriesByLimit(limit)
}