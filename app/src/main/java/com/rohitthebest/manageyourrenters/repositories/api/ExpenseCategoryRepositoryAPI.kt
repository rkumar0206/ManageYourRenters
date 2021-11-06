package com.rohitthebest.manageyourrenters.repositories.api

import com.rohitthebest.manageyourrenters.api.services.ExpenseCategoryAPI
import com.rohitthebest.manageyourrenters.data.apiModels.ExpenseCategory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseCategoryRepositoryAPI @Inject constructor(
    private val expenseCategoryAPI: ExpenseCategoryAPI
) {

    suspend fun addExpenseCategory(uid: String, expenseCategory: ExpenseCategory) =
        expenseCategoryAPI.postExpenseCategory(
            uid, expenseCategory
        )

    suspend fun getExpenseCategories(uid: String) = expenseCategoryAPI.getExpenseCategories(uid)

    suspend fun getExpenseCategoryById(uid: String, id: Long) = expenseCategoryAPI.getCategoryById(
        uid, id
    )

    suspend fun updateExpenseCategoryById(
        uid: String,
        id: Long,
        expenseCategory: ExpenseCategory
    ) = expenseCategoryAPI.updateCategoryById(uid, id, expenseCategory)

    suspend fun deleteExpenseCategoryById(
        uid: String,
        id: Long
    ) = expenseCategoryAPI.deleteCategoryById(uid, id)
}