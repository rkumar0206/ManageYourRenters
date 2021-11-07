package com.rohitthebest.manageyourrenters.repositories.api

import com.rohitthebest.manageyourrenters.api.services.ExpenseAPI
import com.rohitthebest.manageyourrenters.data.apiModels.Expense
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepositoryAPI @Inject constructor(
    private val expenseAPI: ExpenseAPI
) {

    suspend fun addExpenseByCategoryId(
        uid: String, categoryId: Long, expense: Expense
    ) = expenseAPI.postExpenseByCategoryId(uid, categoryId, expense)


    suspend fun addExpenseByCategoryKey(
        uid: String, categoryKey: String, expense: Expense
    ) = expenseAPI.postExpenseByCategoryKey(uid, categoryKey, expense)

    suspend fun getExpensesByCategoryId(
        uid: String, categoryId: Long
    ) = expenseAPI.getExpensesByCategoryId(uid, categoryId)

    suspend fun getExpensesByCategoryKey(
        uid: String, categoryKey: String
    ) = expenseAPI.getExpensesByCategoryKey(uid, categoryKey)

    suspend fun getExpensesByUID(
        uid: String
    ) = expenseAPI.getExpensesByUID(uid)


    suspend fun getExpenseById(
        uid: String, id: Long
    ) = expenseAPI.getExpenseById(uid, id)

    suspend fun getExpenseByKey(
        uid: String, key: String
    ) = expenseAPI.getExpenseByKey(uid, key)

    suspend fun updateExpenseById(
        uid: String, id: Long, categoryId: Long, expense: Expense
    ) = expenseAPI.updateExpenseById(uid, id, categoryId, expense)

    suspend fun updateExpenseByKey(
        uid: String, key: String, categoryKey: String, expense: Expense
    ) = expenseAPI.updateExpenseByKey(uid, key, categoryKey, expense)

    suspend fun deleteExpenseById(
        uid: String, id: Long
    ) = expenseAPI.deleteExpenseById(uid, id)

    suspend fun deleteExpenseByKey(
        uid: String, key: String
    ) = expenseAPI.deleteExpenseByKey(uid, key)


}