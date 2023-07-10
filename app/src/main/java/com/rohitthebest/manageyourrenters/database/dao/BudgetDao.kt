package com.rohitthebest.manageyourrenters.database.dao

import androidx.room.*
import com.rohitthebest.manageyourrenters.database.model.Budget
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBudget(budgets: List<Budget>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateBudget(budget: Budget)

    @Delete
    suspend fun deleteBudget(budget: Budget)

    @Query("DELETE FROM budget_table")
    suspend fun deleteAllBudgets()

    @Query("DELETE FROM budget_table WHERE expenseCategoryKey = :expenseCategoryKey")
    suspend fun deleteBudgetsByExpenseCategoryKey(expenseCategoryKey: String)

    @Query("DELETE FROM budget_table WHERE isSynced = :isSynced")
    suspend fun deleteByIsSyncedValue(isSynced: Boolean)

    @Query("SELECT * FROM budget_table")
    fun getAllBudgets(): Flow<List<Budget>>

    @Query("SELECT * FROM budget_table where month = :month and year = :year order by created DESC")
    fun getAllBudgetsByMonthAndYear(month: Int, year: Int): Flow<List<Budget>>

    @Query("SELECT year FROM budget_table order by year ASC LIMIT 1")
    fun getTheOldestSavedBudgetYear(): Flow<Int>

    @Query("SELECT * FROM budget_table where `key` = :budgetKey")
    fun getBudgetBuyBudgetKey(budgetKey: String): Flow<Budget>

    @Query("SELECT expenseCategoryKey FROM budget_table where month = :month and year = :year")
    fun getExpenseCategoryKeysOfAllBudgetsByMonthAndYear(month: Int, year: Int): Flow<List<String>>

    @Query("SELECT SUM(budgetLimit) FROM budget_table where month = :month and year = :year")
    fun getTotalBudgetByMonthAndYear(month: Int, year: Int): Flow<Double>

    @Query("UPDATE budget_table SET isSynced = 0 WHERE `key` = :key")
    suspend fun updateIsSyncedValueToFalse(key: String)

    @Query("SELECT `key` FROM budget_table WHERE expenseCategoryKey = :expenseCategoryKey")
    suspend fun getKeysByExpenseCategoryKey(expenseCategoryKey: String): List<String>

    @Query("SELECT DISTINCT monthYearString FROM budget_table ORDER BY year DESC, month DESC")
    fun getAllBudgetMonthAndYearForWhichBudgetIsAdded(): Flow<List<String>>

    @Query("SELECT `key` FROM budget_table WHERE monthYearString = :monthYearString LIMIT 2")
    fun isAnyBudgetAddedForThisMonthAndYear(monthYearString: String): Flow<List<String>>
}
