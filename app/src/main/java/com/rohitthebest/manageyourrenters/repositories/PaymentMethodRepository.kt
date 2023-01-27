package com.rohitthebest.manageyourrenters.repositories

import com.rohitthebest.manageyourrenters.database.dao.PaymentMethodDao
import com.rohitthebest.manageyourrenters.database.model.PaymentMethod
import javax.inject.Inject

class PaymentMethodRepository @Inject constructor(
    val dao: PaymentMethodDao
) {

    suspend fun insertPaymentMethod(paymentMethod: PaymentMethod) =
        dao.insertPaymentMethod(paymentMethod)

    suspend fun insertAllPaymentMethod(paymentMethods: List<PaymentMethod>) =
        dao.insertAllPaymentMethod(paymentMethods)

    suspend fun updatePaymentMethod(paymentMethod: PaymentMethod) =
        dao.updatePaymentMethod(paymentMethod)

    suspend fun deletePaymentMethod(paymentMethod: PaymentMethod) =
        dao.deletePaymentMethod(paymentMethod)

    suspend fun deleteAllPaymentMethods() = dao.deleteAllPaymentMethods()

    fun getAllPaymentMethods() = dao.getAllPaymentMethods()

}