package com.rohitthebest.manageyourrenters.database.dao

import androidx.room.*
import com.rohitthebest.manageyourrenters.database.model.PartialPayment
import kotlinx.coroutines.flow.Flow

@Dao
interface PartialPaymentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartialPayment(partialPayment: PartialPayment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPartialPayment(partialPayments: List<PartialPayment>)

    @Update
    suspend fun updatePartialPayment(partialPayment: PartialPayment)

    @Delete
    suspend fun deletePartialPayment(partialPayment: PartialPayment)

    @Query("DELETE FROM partial_payment_table")
    suspend fun deleteAllPartialPayments()

    @Query("SELECT * FROM partial_payment_table")
    fun getAllPartialPayments(): Flow<List<PartialPayment>>

    @Query("SELECT * FROM partial_payment_table WHERE borrowerPaymentKey = :borrowerPaymentKey ORDER BY created DESC")
    fun getPartialPaymentByBorrowerPaymentKey(borrowerPaymentKey: String): Flow<List<PartialPayment>>
}

