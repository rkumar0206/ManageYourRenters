package com.rohitthebest.manageyourrenters.database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rohitthebest.manageyourrenters.database.dao.ExpenseCategoryDAO
import com.rohitthebest.manageyourrenters.database.dao.ExpenseDAO
import com.rohitthebest.manageyourrenters.database.model.apiModels.Expense
import com.rohitthebest.manageyourrenters.database.model.apiModels.ExpenseCategory

@Database(
    entities = [ExpenseCategory::class, Expense::class],
    version = 1,
    exportSchema = false
)
abstract class ExpenseDatabase : RoomDatabase() {

    abstract fun getExpenseCategoryDAO(): ExpenseCategoryDAO

    abstract fun getExpenseDAO(): ExpenseDAO
}
