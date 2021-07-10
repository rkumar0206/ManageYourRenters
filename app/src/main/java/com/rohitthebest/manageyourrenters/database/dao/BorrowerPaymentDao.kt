package com.rohitthebest.manageyourrenters.database.dao

import androidx.room.*
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

    @Query("SELECT * FROM borrower_payment_table ORDER BY modified desc")
    fun getAllBorrowerPayments(): Flow<List<BorrowerPayment>>

    @Query("SELECT * FROM borrower_payment_table WHERE borrowerKey= :borrowerKey ORDER BY modified desc")
    fun getPaymentsByBorrowerKey(borrowerKey: String): Flow<List<BorrowerPayment>>

    // getting the due amount of the borrower where isDueCleared is false
    @Query("SELECT SUM(amountTakenOnRent) as total FROM borrower_payment_table WHERE isDueCleared = 0")
    fun getTotalDueOfTheBorrower(borrowerKey: String): Flow<Double>

    @Query("SELECT * FROM borrower_payment_table WHERE `key` = :paymentKey")
    fun getBorrowerPaymentByKey(paymentKey: String): Flow<BorrowerPayment>
}





