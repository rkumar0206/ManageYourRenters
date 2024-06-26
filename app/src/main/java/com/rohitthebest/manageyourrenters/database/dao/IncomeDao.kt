package com.rohitthebest.manageyourrenters.database.dao

import androidx.room.*
import com.rohitthebest.manageyourrenters.data.MonthAndTotalSum
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

    @Query("DELETE FROM income_table WHERE isSynced = :isSynced")
    suspend fun deleteByIsSyncedValue(isSynced: Boolean)

    @Query("DELETE FROM income_table")
    suspend fun deleteAllIncomes()

    @Query("SELECT * FROM income_table")
    fun getAllIncomes(): Flow<List<Income>>

    @Query("SELECT * FROM income_table where month = :month AND year = :year")
    fun getAllIncomesByMonthAndYear(month: Int, year: Int): Flow<List<Income>>

    @Query("SELECT * FROM income_table where `key` = :incomeKey")
    fun getIncomeByKey(incomeKey: String): Flow<Income>

    @Query("SELECT SUM(income) FROM income_table where month = :month AND year = :year")
    fun getTotalIncomeAddedByMonthAndYear(month: Int, year: Int): Flow<Double>

    @Query("SELECT month, SUM(income) AS total FROM income_table where year = :year GROUP BY month, year")
    fun getAllTotalIncomeByYear(year: Int): Flow<List<MonthAndTotalSum>>


    @Query("SELECT DISTINCT source FROM income_table")
    fun getAllIncomeSources(): Flow<List<String>>

    @Query("UPDATE income_table SET isSynced = 0 WHERE `key` = :key")
    suspend fun updateIsSyncedValueToFalse(key: String)
}
