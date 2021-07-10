package com.rohitthebest.manageyourrenters.database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rohitthebest.manageyourrenters.database.dao.BorrowerPaymentDao
import com.rohitthebest.manageyourrenters.database.model.BorrowerPayment
import com.rohitthebest.manageyourrenters.database.typeConverters.TypeConvertersForDatabase

@Database(
    entities = [BorrowerPayment::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(TypeConvertersForDatabase::class)
abstract class BorrowerPaymentDatabase : RoomDatabase() {

    abstract fun getBorrowerPaymentDao(): BorrowerPaymentDao
}
