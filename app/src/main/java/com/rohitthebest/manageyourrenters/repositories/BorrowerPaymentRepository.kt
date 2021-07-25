package com.rohitthebest.manageyourrenters.repositories

import com.rohitthebest.manageyourrenters.database.dao.BorrowerPaymentDao
import com.rohitthebest.manageyourrenters.database.model.BorrowerPayment
import javax.inject.Inject

class BorrowerPaymentRepository @Inject constructor(
    val dao: BorrowerPaymentDao
) {

    suspend fun insertBorrowerPayment(borrowerPayment: BorrowerPayment) =
        dao.insertBorrowerPayment(borrowerPayment)

    suspend fun insertAllBorrowerPayment(borrowerPayments: List<BorrowerPayment>) =
        dao.insertAllBorrowerPayment(borrowerPayments)

    suspend fun updateBorrowerPayment(borrowerPayment: BorrowerPayment) =
        dao.updateBorrowerPayment(borrowerPayment)

    suspend fun deleteBorrowerPayment(borrowerPayment: BorrowerPayment) =
        dao.deleteBorrowerPayment(borrowerPayment)

    suspend fun deleteAllBorrowerPayments() = dao.deleteAllBorrowerPayments()

    suspend fun deleteAllBorrowerPaymentsByBorrowerKey(borrowerKey: String) =
        dao.deleteAllBorrowerPaymentsByBorrowerKey(borrowerKey)

    suspend fun deleteBorrowerPaymentsByIsSynced(isSynced: Boolean) =
        dao.deleteBorrowerPaymentsByIsSynced(isSynced)

    fun getPaymentsByBorrowerKey(borrowerKey: String) = dao.getPaymentsByBorrowerKey(borrowerKey)

    fun getTotalDueOfTheBorrower(borrowerKey: String) = dao.getTotalDueOfTheBorrower(borrowerKey)

    fun getBorrowerPaymentByKey(paymentKey: String) = dao.getBorrowerPaymentByKey(paymentKey)

    suspend fun getPaymentKeysByBorrowerKey(borrowerKey: String) =
        dao.getPaymentKeysByBorrowerKey(borrowerKey)

}