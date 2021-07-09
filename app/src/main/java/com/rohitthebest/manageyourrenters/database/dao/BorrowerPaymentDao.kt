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

    @Query("DELETE FROM borrower_table")
    suspend fun deleteAllBorrowerPayments()

    @Query("SELECT * FROM borrower_table")
    fun getAllBorrowerPayment(): Flow<List<BorrowerPayment>>

}





