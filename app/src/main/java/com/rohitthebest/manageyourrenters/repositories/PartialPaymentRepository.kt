package com.rohitthebest.manageyourrenters.repositories

import com.rohitthebest.manageyourrenters.database.dao.PartialPaymentDao
import com.rohitthebest.manageyourrenters.database.model.PartialPayment
import javax.inject.Inject

class PartialPaymentRepository @Inject constructor(
    val dao: PartialPaymentDao
) {

    suspend fun insertPartialPayment(partialPayment: PartialPayment) =
        dao.insertPartialPayment(partialPayment)

    suspend fun insertAllPartialPayment(partialPayments: List<PartialPayment>) =
        dao.insertAllPartialPayment(partialPayments)

    suspend fun deletePartialPayment(partialPayment: PartialPayment) =
        dao.deletePartialPayment(partialPayment)

    suspend fun deleteAllPartialPayments() = dao.deleteAllPartialPayments()

    suspend fun deletePartialPaymentsByIsSynced(isSynced: Boolean) =
        dao.deletePartialPaymentsByIsSynced(isSynced)

    suspend fun deleteAllByProvideList(partialPaymentKeys: List<String>) =
        dao.deleteAllByProvideList(partialPaymentKeys)

    suspend fun deleteAllPartialPaymentByBorrowerPaymentKey(borrowerPaymentKey: String) =
        dao.deleteAllPartialPaymentByBorrowerPaymentKey(borrowerPaymentKey)

    suspend fun deleteAllPartialPaymentByBorrowerId(borrowerId: String) =
        dao.deleteAllPartialPaymentByBorrowerId(borrowerId)

    fun getAllPartialPayments() = dao.getAllPartialPayments()

    fun getPartialPaymentByBorrowerPaymentKey(borrowerPaymentKey: String) =
        dao.getPartialPaymentByBorrowerPaymentKey(borrowerPaymentKey)

    suspend fun getKeysByBorrowerPaymentKey(borrowerPaymentKey: String) =
        dao.getKeysByBorrowerPaymentKey(borrowerPaymentKey)

    suspend fun getKeysByBorrowerId(borrowerId: String): List<String> =
        dao.getKeysByBorrowerId(borrowerId)
}