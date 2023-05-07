package com.rohitthebest.manageyourrenters.database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rohitthebest.manageyourrenters.database.dao.BudgetDao
import com.rohitthebest.manageyourrenters.database.dao.IncomeDao
import com.rohitthebest.manageyourrenters.database.model.Budget
import com.rohitthebest.manageyourrenters.database.model.Income

@Database(
    entities = [Budget::class, Income::class],
    version = 1,
    exportSchema = false
)
abstract class BudgetAndIncomeDatabase : RoomDatabase() {

    abstract fun getBudgetDao(): BudgetDao
    abstract fun getIncomeDao(): IncomeDao
}