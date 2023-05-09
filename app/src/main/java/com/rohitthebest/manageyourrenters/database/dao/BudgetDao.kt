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

    @Query("SELECT * FROM budget_table")
    fun getAllBudgets(): Flow<List<Budget>>

    @Query("SELECT * FROM budget_table where month = :month and year = :year order by created DESC")
    fun getAllBudgetsByMonthAndYear(month: Int, year: Int): Flow<List<Budget>>

    @Query("SELECT year FROM budget_table order by year ASC LIMIT 1")
    fun getTheOldestSavedBudgetYear(): Flow<Int>

    @Query("SELECT * FROM budget_table where `key` = :budgetKey")
    fun getBudgetBuyBudgetKey(budgetKey: String): Flow<Budget>

    //todo: calculate total budget for specific month and year
}
