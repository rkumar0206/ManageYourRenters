package com.rohitthebest.manageyourrenters.repositories

import com.rohitthebest.manageyourrenters.database.dao.MonthlyPaymentCategoryDao
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPaymentCategory
import javax.inject.Inject

class MonthlyPaymentCategoryRepository @Inject constructor(
    val dao: MonthlyPaymentCategoryDao
) {

    suspend fun insertMonthlyPaymentCategory(monthlyPaymentCategory: MonthlyPaymentCategory) =
        dao.insertMonthlyPaymentCategory(monthlyPaymentCategory)

    suspend fun insertAllMonthlyPaymentCategory(monthlyPaymentCategories: List<MonthlyPaymentCategory>) =
        dao.insertAllMonthlyPaymentCategory(monthlyPaymentCategories)

    suspend fun updateMonthlyPaymentCategory(monthlyPaymentCategory: MonthlyPaymentCategory) =
        dao.updateMonthlyPaymentCategory(monthlyPaymentCategory)

    suspend fun deleteMonthlyPaymentCategory(monthlyPaymentCategory: MonthlyPaymentCategory) =
        dao.deleteMonthlyPaymentCategory(monthlyPaymentCategory)

    suspend fun deleteAllMonthlyPaymentCategoriesByIsSynced(isSynced: Boolean) =
        dao.deleteAllMonthlyPaymentCategoriesByIsSynced(isSynced)

    suspend fun deleteAllMonthlyPaymentCategories() = dao.deleteAllMonthlyPaymentCategories()

    fun getAllMonthlyPaymentCategories() = dao.getAllMonthlyPaymentCategories()

    fun getMonthlyPaymentCategoryUsingKey(key: String) =
        dao.getMonthlyPaymentCategoryUsingKey(key)
}