package com.rohitthebest.manageyourrenters.repositories

import com.rohitthebest.manageyourrenters.database.dao.PaymentDao
import com.rohitthebest.manageyourrenters.database.entity.Payment
import javax.inject.Inject

class PaymentRepository @Inject constructor(
    val dao : PaymentDao
) {

    suspend fun insertPayment(payment : Payment) = dao.insertPayment(payment)

    suspend fun deletePayment(payment: Payment) = dao.deletePayment(payment)

    suspend fun deleteAllPayments() = dao.deleteAllPayment()

    suspend fun deleteAllPaymentsOfRenter(renterKey : String) = dao.deleteAllPaymentsOfRenter(renterKey)

    fun getAllPaymentsList() = dao.getAllPaymentsList()

    fun getAllPaymentsListOfRenter(renterKey: String) = dao.getAllPaymentsListOfRenter(renterKey)

    fun getCountOfPaymentsOfRenter(renterKey : String) = dao.getCountOfPaymentsOfRenter(renterKey)
}