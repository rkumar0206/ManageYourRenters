package com.rohitthebest.manageyourrenters.repositories

import com.rohitthebest.manageyourrenters.database.dao.MonthlyPaymentDao
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPayment
import javax.inject.Inject

class MonthlyPaymentRepository @Inject constructor(
    val dao: MonthlyPaymentDao
) {

    suspend fun insertMonthlyPayment(monthlyPayment: MonthlyPayment) =
        dao.insertMonthlyPayment(monthlyPayment)

    suspend fun insertAllMonthlyPayment(monthlyPayments: List<MonthlyPayment>) =
        dao.insertAllMonthlyPayment(monthlyPayments)

    suspend fun updateMonthlyPayment(monthlyPayment: MonthlyPayment) =
        dao.updateMonthlyPayment(monthlyPayment)

    suspend fun deleteMonthlyPayment(monthlyPayment: MonthlyPayment) =
        dao.deleteMonthlyPayment(monthlyPayment)

    suspend fun deleteAllMonthlyPaymentByIsSynced(isSynced: Boolean) =
        dao.deleteAllMonthlyPaymentByIsSynced(isSynced)

    suspend fun deleteAllMonthlyPaymentsByCategoryKey(categoryKey: String) =
        dao.deleteAllMonthlyPaymentsByCategoryKey(categoryKey)

    suspend fun deleteAllMonthlyPayments() = dao.deleteAllMonthlyPayments()

    fun getAllMonthlyPayments() = dao.getAllMonthlyPayments()

    fun getAllMonthlyPaymentsByCategoryKey(categoryKey: String) =
        dao.getAllMonthlyPaymentsByCategoryKey(categoryKey)

    fun getMonthlyPaymentByKey(key: String) = dao.getMonthlyPaymentByKey(key)

    fun getLastMonthlyPayment(monthlyPaymentCategoryKey: String) =
        dao.getLastMonthlyPayment(monthlyPaymentCategoryKey)
}