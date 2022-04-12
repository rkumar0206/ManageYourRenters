package com.rohitthebest.manageyourrenters.database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rohitthebest.manageyourrenters.database.dao.MonthlyPaymentCategoryDao
import com.rohitthebest.manageyourrenters.database.dao.MonthlyPaymentDao
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPayment
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPaymentCategory

@Database(
    entities = [MonthlyPaymentCategory::class, MonthlyPayment::class],
    version = 1,
    exportSchema = false
)
abstract class MonthlyPaymentDatabase : RoomDatabase() {

    abstract fun getMonthlyPaymentCategoryDao(): MonthlyPaymentCategoryDao

    abstract fun getMonthlyPaymentDao(): MonthlyPaymentDao
}