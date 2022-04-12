package com.rohitthebest.manageyourrenters.repositories.api

import com.rohitthebest.manageyourrenters.api.services.MonthlyPaymentAPI
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPayment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonthlyPaymentRepositoryAPI @Inject constructor(
    private val monthlyPaymentAPI: MonthlyPaymentAPI
) {

    suspend fun insertMonthlyPayment(
        uid: String,
        categoryKey: String,
        monthlyPayment: MonthlyPayment
    ) = monthlyPaymentAPI.postMonthlyPaymentUsingCategoryKey(uid, categoryKey, monthlyPayment)

    suspend fun getMonthlyPaymentByKey(uid: String, key: String) =
        monthlyPaymentAPI.getMonthlyPaymentByKey(uid, key)

    suspend fun getMonthlyPaymentsByCategoryKey(uid: String, categoryKey: String) =
        monthlyPaymentAPI.getMonthlyPaymentsByCategoryKey(uid, categoryKey)

    suspend fun getMonthlyPaymentsByUid(uid: String) =
        monthlyPaymentAPI.getMonthlyPaymentsByUid(uid)

    suspend fun updateMonthlyPaymentsKey(
        uid: String,
        key: String,
        categoryKey: String,
        monthlyPayment: MonthlyPayment
    ) =
        monthlyPaymentAPI.updateMonthPaymentByKey(uid, key, categoryKey, monthlyPayment)

    suspend fun deleteMonthlyPaymentByKey(uid: String, key: String) =
        monthlyPaymentAPI.deleteMonthlyPaymentByKey(uid, key)

}