package com.rohitthebest.manageyourrenters.repositories.api

import com.rohitthebest.manageyourrenters.api.services.MonthlyPaymentCategoryAPI
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPaymentCategory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonthlyPaymentCategoryRepositoryAPI @Inject constructor(
    private val monthlyPaymentCategoryAPI: MonthlyPaymentCategoryAPI
) {

    suspend fun insertMonthlyPaymentCategory(
        uid: String,
        monthlyPaymentCategory: MonthlyPaymentCategory
    ) = monthlyPaymentCategoryAPI.postMonthLyPaymentCategory(uid, monthlyPaymentCategory)

    suspend fun getMonthlyPaymentCategoryByKey(uid: String, key: String) =
        monthlyPaymentCategoryAPI.getMonthlyPaymentsByKey(uid, key)

    suspend fun getMonthlyPaymentCategories(uid: String) =
        monthlyPaymentCategoryAPI.getMonthlyPaymentCategories(uid)

    suspend fun updateMonthlyPaymentCategory(uid: String, key: String) =
        monthlyPaymentCategoryAPI.updateCategoryByKey(uid, key)

    suspend fun deleteMonthlyPaymentCategory(uid: String, key: String) =
        monthlyPaymentCategoryAPI.deleteCategoryByKey(uid, key)

}