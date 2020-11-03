package com.rohitthebest.manageyourrenters.database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rohitthebest.manageyourrenters.database.dao.PaymentDao
import com.rohitthebest.manageyourrenters.database.entity.Payment
import com.rohitthebest.manageyourrenters.database.typeConverters.TypeConvertersForDatabase

@Database(
    entities = [Payment::class],
    version = 21
)
@TypeConverters(TypeConvertersForDatabase::class)
abstract class PaymentDatabase : RoomDatabase() {

    abstract fun getPaymentDao() : PaymentDao
}