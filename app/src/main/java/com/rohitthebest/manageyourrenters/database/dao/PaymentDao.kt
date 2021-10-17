package com.rohitthebest.manageyourrenters.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.rohitthebest.manageyourrenters.database.model.Payment
import kotlinx.coroutines.flow.Flow


@Dao
interface PaymentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Insert
    suspend fun insertPayments(payments: List<Payment>)

    @Delete
    suspend fun deletePayment(payment: Payment)

    @Query("DELETE FROM payment_table")
    suspend fun deleteAllPayment()

    @Query("DELETE FROM payment_table WHERE renterKey =:renterKey")
    suspend fun deleteAllPaymentsOfRenter(renterKey: String)

    @Query("DELETE FROM payment_table WHERE isSynced =:isSynced")
    suspend fun deleteAllPaymentsByIsSynced(isSynced: String)

    @Query("SELECT * FROM payment_table ORDER BY timeStamp DESC")
    fun getAllPaymentsList(): LiveData<List<Payment>>

    @Query("SELECT * FROM payment_table WHERE renterKey =:renterKey ORDER BY timeStamp DESC")
    fun getAllPaymentsListOfRenter(renterKey: String): LiveData<List<Payment>>

    @Query("SELECT COUNT(id) FROM payment_table WHERE renterKey =:renterKey")
    fun getCountOfPaymentsOfRenter(renterKey: String): LiveData<Int>

    @Query("SELECT SUM(amountPaid - totalRent) FROM payment_table WHERE renterKey =:renterKey")
    fun getSumOfDueOrAdvance(renterKey: String): LiveData<Double>

    @Query("SELECT `key` FROM payment_table WHERE renterKey = :renterKey")
    suspend fun getPaymentKeysByRenterKey(renterKey: String): List<String>

    @Query("SELECT * FROM payment_table WHERE renterKey = :renterKey ORDER BY timeStamp DESC LIMIT 1")
    fun getLastRenterPayment(renterKey: String): Flow<List<Payment>>
}