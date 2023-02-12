package com.rohitthebest.manageyourrenters.database.dao

import androidx.room.*
import com.rohitthebest.manageyourrenters.data.KeyAndSupportingDoc
import com.rohitthebest.manageyourrenters.database.model.BorrowerPayment
import kotlinx.coroutines.flow.Flow


@Dao
interface BorrowerPaymentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBorrowerPayment(payment: BorrowerPayment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBorrowerPayment(payments: List<BorrowerPayment>)

    @Update
    suspend fun updateBorrowerPayment(payment: BorrowerPayment)

    @Delete
    suspend fun deleteBorrowerPayment(payment: BorrowerPayment)

    @Query("DELETE FROM borrower_payment_table")
    suspend fun deleteAllBorrowerPayments()

    @Query("DELETE FROM borrower_payment_table WHERE borrowerKey= :borrowerKey")
    suspend fun deleteAllBorrowerPaymentsByBorrowerKey(borrowerKey: String)

    @Query("DELETE FROM borrower_payment_table  WHERE isSynced = :isSynced")
    suspend fun deleteBorrowerPaymentsByIsSynced(isSynced: Boolean)

    @Query("SELECT * FROM borrower_payment_table WHERE borrowerKey= :borrowerKey ORDER BY isDueCleared = 0 desc, modified desc")
    fun getPaymentsByBorrowerKey(borrowerKey: String): Flow<List<BorrowerPayment>>

    // getting the due amount of the borrower where isDueCleared is false
    @Query("SELECT SUM(dueLeftAmount) as total FROM borrower_payment_table WHERE borrowerKey = :borrowerKey AND isDueCleared = 0")
    fun getTotalDueOfTheBorrower(borrowerKey: String): Flow<Double>

    @Query("SELECT * FROM borrower_payment_table WHERE `key` = :paymentKey")
    fun getBorrowerPaymentByKey(paymentKey: String): Flow<BorrowerPayment>

    @Query("SELECT `key`, supportingDocument FROM borrower_payment_table WHERE borrowerKey = :borrowerKey AND isSynced = 1")
    suspend fun getPaymentKeysAndSupportingDocumentByBorrowerKey(borrowerKey: String): List<KeyAndSupportingDoc>

    @Query("UPDATE borrower_payment_table SET isSynced = 0 WHERE `key` = :key")
    suspend fun updateIsSyncedValueToFalse(key: String)
}





