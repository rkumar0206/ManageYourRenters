package com.rohitthebest.manageyourrenters.repositories

import com.rohitthebest.manageyourrenters.database.dao.RenterPaymentDao
import com.rohitthebest.manageyourrenters.database.model.RenterPayment
import javax.inject.Inject

class RenterPaymentRepository @Inject constructor(
    val dao: RenterPaymentDao
) {

    suspend fun insertRenterPayment(payment: RenterPayment) = dao.insertRenterPayment(payment)

    suspend fun insertAllRenterPayment(payments: List<RenterPayment>) =
        dao.insertAllRenterPayment(payments)

    suspend fun updateRenterPayment(payment: RenterPayment) = dao.updateRenterPayment(payment)

    suspend fun deleteRenterPayment(payment: RenterPayment) = dao.deleteRenterPayment(payment)

    suspend fun deleteAllRenterPayments() = dao.deleteAllRenterPayments()

    suspend fun deleteAllPaymentsOfRenter(renterKey: String) =
        dao.deleteAllPaymentsOfRenter(renterKey)

    suspend fun deleteAllPaymentsByIsSynced(isSynced: Boolean) =
        dao.deleteAllPaymentsByIsSynced(isSynced)

    fun getAllPaymentsListOfRenter(renterKey: String) = dao.getAllPaymentsListOfRenter(renterKey)

    fun getPaymentByPaymentKey(paymentKey: String) = dao.getPaymentByPaymentKey(paymentKey)

    suspend fun getPaymentKeysByRenterKey(renterKey: String) =
        dao.getPaymentKeysByRenterKey(renterKey)

    fun getLastRenterPayment(renterKey: String) = dao.getLastRenterPayment(renterKey)
}