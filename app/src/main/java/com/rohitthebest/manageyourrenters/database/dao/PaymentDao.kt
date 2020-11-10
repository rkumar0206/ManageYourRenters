package com.rohitthebest.manageyourrenters.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.rohitthebest.manageyourrenters.database.entity.Payment

@Dao
interface PaymentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment : Payment) : Long

    @Delete
    suspend fun deletePayment(payment: Payment)

    @Query("DELETE FROM payment_table")
    suspend fun deleteAllPayment()

    @Query("DELETE FROM payment_table WHERE renterKey =:renterKey")
    suspend fun deleteAllPaymentsOfRenter(renterKey: String)

    @Query("SELECT * FROM payment_table ORDER BY timeStamp DESC")
    fun getAllPaymentsList(): LiveData<List<Payment>>

    @Query("SELECT * FROM payment_table WHERE renterKey =:renterKey ORDER BY timeStamp DESC")
    fun getAllPaymentsListOfRenter(renterKey: String): LiveData<List<Payment>>

    @Query("SELECT COUNT(id) FROM payment_table WHERE renterKey =:renterKey")
    fun getCountOfPaymentsOfRenter(renterKey: String): LiveData<Int>

    @Query("SELECT SUM(amountPaid - totalRent) FROM payment_table WHERE renterKey =:renterKey")
    fun getSumOfDueOrAdvance(renterKey: String): LiveData<Double>
}