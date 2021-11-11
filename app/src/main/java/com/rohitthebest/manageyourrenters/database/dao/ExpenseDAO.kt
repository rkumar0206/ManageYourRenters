package com.rohitthebest.manageyourrenters.database.dao

import androidx.room.*
import com.rohitthebest.manageyourrenters.database.model.apiModels.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllExpense(expenses: List<Expense>)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expense_table")
    suspend fun deleteAllExpenses()

    @Query("DELETE FROM expense_table WHERE categoryKey = :expenseCategoryKey")
    suspend fun deleteExpenseByExpenseCategoryKey(expenseCategoryKey: String)

    @Query("SELECT * FROM expense_table")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expense_table WHERE categoryKey = :expenseCategoryKey")
    fun getExpensesByExpenseCategoryKey(expenseCategoryKey: String): Flow<List<Expense>>

    @Query("SELECT * FROM expense_table WHERE `key` = :expenseKey")
    fun getExpenseByKey(expenseKey: String): Flow<Expense>
}
