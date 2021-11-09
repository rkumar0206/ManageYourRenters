package com.rohitthebest.manageyourrenters.database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rohitthebest.manageyourrenters.database.dao.PaymentDao
import com.rohitthebest.manageyourrenters.database.model.Payment
import com.rohitthebest.manageyourrenters.database.typeConverters.TypeConvertersForDatabase

@Database(
    entities = [Payment::class],
    version = 21,
    exportSchema = false
)
@TypeConverters(TypeConvertersForDatabase::class)
abstract class PaymentDatabase : RoomDatabase() {

    abstract fun getPaymentDao() : PaymentDao
}