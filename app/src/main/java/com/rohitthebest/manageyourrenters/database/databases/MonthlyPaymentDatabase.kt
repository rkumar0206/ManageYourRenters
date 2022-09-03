package com.rohitthebest.manageyourrenters.database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rohitthebest.manageyourrenters.database.dao.MonthlyPaymentCategoryDao
import com.rohitthebest.manageyourrenters.database.dao.MonthlyPaymentDao
import com.rohitthebest.manageyourrenters.database.model.MonthlyPayment
import com.rohitthebest.manageyourrenters.database.model.MonthlyPaymentCategory
import com.rohitthebest.manageyourrenters.database.typeConverters.TypeConvertersForDatabase

@Database(
    entities = [MonthlyPaymentCategory::class, MonthlyPayment::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(TypeConvertersForDatabase::class)
abstract class MonthlyPaymentDatabase : RoomDatabase() {

    abstract fun getMonthlyPaymentCategoryDao(): MonthlyPaymentCategoryDao

    abstract fun getMonthlyPaymentDao(): MonthlyPaymentDao
}