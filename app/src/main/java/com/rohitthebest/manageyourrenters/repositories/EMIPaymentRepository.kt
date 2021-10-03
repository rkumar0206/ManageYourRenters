package com.rohitthebest.manageyourrenters.repositories

import com.rohitthebest.manageyourrenters.database.dao.EMIPaymentDao
import com.rohitthebest.manageyourrenters.database.model.EMIPayment
import javax.inject.Inject

class EMIPaymentRepository @Inject constructor(
    val dao: EMIPaymentDao
) {

    suspend fun insertEMIPayment(emiPayment: EMIPayment) = dao.insertEMIPayment(emiPayment)

    suspend fun insertAllEMIPayment(emiPayments: List<EMIPayment>) =
        dao.insertAllEMIPayment(emiPayments)

    suspend fun updateEMIPayment(emiPayment: EMIPayment) =
        dao.updateEMIPayment(emiPayment)

    suspend fun deleteEMIPayment(emiPayment: EMIPayment) =
        dao.deleteEMIPayment(emiPayment)

    suspend fun deleteAllEMIPayments() = dao.deleteAllEMIPayments()

    suspend fun deletePaymentsByEMIKey(emiKey: String) = dao.deletePaymentsByEMIKey(emiKey)

    suspend fun deleteEMIPaymentsByIsSynced(isSynced: Boolean) =
        dao.deleteEMIPaymentsByIsSynced(isSynced)

    fun getAllEMIPayments() = dao.getAllEMIPayments()

    fun getAllEMIPaymentsByEMIKey(emiKey: String) = dao.getAllEMIPaymentsByEMIKey(emiKey)

    fun getEMIPaymentByKey(emiPaymentKey: String) = dao.getEMIPaymentByKey(emiPaymentKey)

    fun getEmiPaymentsKeysByEMIKey(emiKey: String) = dao.getEmiPaymentsKeysByEMIKey(emiKey)

    fun getTotalAmountPaidOfAnEMI(emiKey: String) = dao.getTotalAmountPaidOfAnEMI(emiKey)
}