package com.rohitthebest.manageyourrenters.database.dao

import androidx.room.*
import com.rohitthebest.manageyourrenters.data.KeyAndSupportingDoc
import com.rohitthebest.manageyourrenters.database.model.EMIPayment
import kotlinx.coroutines.flow.Flow

@Dao
interface EMIPaymentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEMIPayment(emiPayment: EMIPayment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEMIPayment(emiPayments: List<EMIPayment>)

    @Update
    suspend fun updateEMIPayment(emiPayment: EMIPayment)

    @Delete
    suspend fun deleteEMIPayment(emiPayment: EMIPayment)

    @Query("DELETE FROM emi_payment_table")
    suspend fun deleteAllEMIPayments()

    @Query("DELETE FROM emi_payment_table WHERE emiKey = :emiKey")
    suspend fun deletePaymentsByEMIKey(emiKey: String)

    @Query("DELETE FROM emi_payment_table WHERE isSynced = :isSynced")
    suspend fun deleteEMIPaymentsByIsSynced(isSynced: Boolean)

    @Query("SELECT * FROM emi_payment_table")
    fun getAllEMIPayments(): Flow<List<EMIPayment>>

    @Query("SELECT * FROM emi_payment_table WHERE emiKey =:emiKey ORDER BY modified DESC")
    fun getAllEMIPaymentsByEMIKey(emiKey: String): Flow<List<EMIPayment>>

    @Query("SELECT * FROM emi_payment_table WHERE `key` = :emiPaymentKey")
    fun getEMIPaymentByKey(emiPaymentKey: String): Flow<EMIPayment>

    // will be declared only in emi payment repository
    // this will return the keys of emi payments of a particular EMI
    @Query("SELECT (`key`), supportingDocument FROM emi_payment_table WHERE emiKey =:emiKey")
    suspend fun getEmiPaymentsKeysAndSupportingDocsByEMIKey(emiKey: String): List<KeyAndSupportingDoc>

    // get total emi amount paid
    @Query("SELECT SUM(amountPaid) as total FROM emi_payment_table WHERE emiKey =:emiKey")
    fun getTotalAmountPaidOfAnEMI(emiKey: String): Flow<Double>

    @Query("SELECT COUNT(id) FROM emi_payment_table WHERE emiKey = :emiKey")
    fun getTotalCountByKey(emiKey: String): Flow<Int>
}
