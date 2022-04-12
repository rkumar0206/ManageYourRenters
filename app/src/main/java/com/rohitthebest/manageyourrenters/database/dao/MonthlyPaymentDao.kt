package com.rohitthebest.manageyourrenters.database.dao

import androidx.room.*
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPayment
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyPaymentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthlyPayment(monthlyPayment: MonthlyPayment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMonthlyPayment(monthlyPayments: List<MonthlyPayment>)

    @Update
    suspend fun updateMonthlyPayment(monthlyPayment: MonthlyPayment)

    @Delete
    suspend fun deleteMonthlyPayment(monthlyPayment: MonthlyPayment)

    @Query("DELETE FROM monthly_payment_table WHERE isSynced = :isSynced")
    suspend fun deleteAllMonthlyPaymentByIsSynced(isSynced: Boolean)

    @Query("DELETE FROM monthly_payment_table")
    suspend fun deleteAllMonthlyPayments()

    @Query("SELECT * FROM monthly_payment_table")
    fun getAllMonthlyPayments(): Flow<List<MonthlyPayment>>

    @Query("SELECT * FROM monthly_payment_table WHERE categoryKey = :categoryKey ORDER BY modified DESC")
    fun getAllMonthlyPaymentsByCategoryKey(categoryKey: String): Flow<List<MonthlyPayment>>

    @Query("SELECT * FROM monthly_payment_table WHERE `key` = :key")
    fun getMonthlyPaymentByKey(key: String): Flow<MonthlyPayment>
}