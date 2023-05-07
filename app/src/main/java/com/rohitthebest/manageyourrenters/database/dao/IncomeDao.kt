package com.rohitthebest.manageyourrenters.database.dao

import androidx.room.*
import com.rohitthebest.manageyourrenters.database.model.Income
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: Income)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllIncome(incomes: List<Income>)

    @Update
    suspend fun updateIncome(income: Income)

    @Delete
    suspend fun deleteIncome(income: Income)

    @Query("DELETE FROM income_table")
    suspend fun deleteAllIncomes()

    @Query("SELECT * FROM income_table")
    fun getAllIncomes(): Flow<List<Income>>

    @Query("SELECT * FROM income_table where `key` = :incomeKey")
    fun getIncomeByKey(incomeKey: String): Flow<Income>
}
