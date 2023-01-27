package com.rohitthebest.manageyourrenters.database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rohitthebest.manageyourrenters.database.dao.ExpenseCategoryDAO
import com.rohitthebest.manageyourrenters.database.dao.ExpenseDAO
import com.rohitthebest.manageyourrenters.database.model.Expense
import com.rohitthebest.manageyourrenters.database.model.ExpenseCategory
import com.rohitthebest.manageyourrenters.database.typeConverters.TypeConvertersForDatabase

@Database(
    entities = [ExpenseCategory::class, Expense::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(TypeConvertersForDatabase::class)
abstract class ExpenseDatabase : RoomDatabase() {

    abstract fun getExpenseCategoryDAO(): ExpenseCategoryDAO

    abstract fun getExpenseDAO(): ExpenseDAO
}
