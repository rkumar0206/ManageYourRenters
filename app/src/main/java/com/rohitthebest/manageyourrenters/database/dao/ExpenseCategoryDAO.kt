package com.rohitthebest.manageyourrenters.database.dao

import androidx.room.*
import com.rohitthebest.manageyourrenters.database.model.apiModels.ExpenseCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseCategoryDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseCategory(expenseCategory: ExpenseCategory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllExpenseCategory(expenseCategories: List<ExpenseCategory>)

    @Update
    suspend fun updateExpenseCategory(expenseCategory: ExpenseCategory)

    @Delete
    suspend fun deleteExpenseCategory(expenseCategory: ExpenseCategory)

    @Query("DELETE FROM expense_category_table")
    suspend fun deleteAllExpenseCategories()

    @Query("SELECT * FROM expense_category_table ORDER BY modified DESC")
    fun getAllExpenseCategories(): Flow<List<ExpenseCategory>>

}
