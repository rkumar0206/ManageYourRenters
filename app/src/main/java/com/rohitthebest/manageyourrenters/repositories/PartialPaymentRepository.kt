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

    suspend fun updatePartialPayment(partialPayment: PartialPayment) =
        dao.updatePartialPayment(partialPayment)

    suspend fun deletePartialPayment(partialPayment: PartialPayment) =
        dao.deletePartialPayment(partialPayment)

    suspend fun deleteAllPartialPayments() = dao.deleteAllPartialPayments()

    suspend fun deleteAllByProvideList(partialPaymentKeys: List<String>) =
        dao.deleteAllByProvideList(partialPaymentKeys)

    suspend fun deleteAllPartialPaymentByBorrowerPaymentKey(borrowerPaymentKey: String) =
        dao.deleteAllPartialPaymentByBorrowerPaymentKey(borrowerPaymentKey)

    fun getAllPartialPayments() = dao.getAllPartialPayments()

    fun getPartialPaymentByBorrowerPaymentKey(borrowerPaymentKey: String) =
        dao.getPartialPaymentByBorrowerPaymentKey(borrowerPaymentKey)

    fun getTheSumOfPartialPaymentsOfBorrowerPayment(borrowerPaymentKey: String) =
        dao.getTheSumOfPartialPaymentsOfBorrowerPayment(borrowerPaymentKey)

    fun getThePartialPaymentsByIsSyncedAndBorrowerPayment(
        borrowerPaymentKey: String,
        isSynced: Boolean
    ) = dao.getThePartialPaymentsByIsSyncedAndBorrowerPayment(borrowerPaymentKey, isSynced)

    suspend fun getKeysByBorrowerPaymentKey(borrowerPaymentKey: String) =
        dao.getKeysByBorrowerPaymentKey(borrowerPaymentKey)
}