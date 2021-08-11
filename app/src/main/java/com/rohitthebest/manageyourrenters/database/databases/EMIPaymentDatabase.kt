package com.rohitthebest.manageyourrenters.database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rohitthebest.manageyourrenters.database.dao.EMIPaymentDao
import com.rohitthebest.manageyourrenters.database.model.EMIPayment
import com.rohitthebest.manageyourrenters.database.typeConverters.TypeConvertersForDatabase

@Database(
    entities = [EMIPayment::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(TypeConvertersForDatabase::class)
abstract class EMIPaymentDatabase : RoomDatabase() {

    abstract fun getEMIPaymentDao(): EMIPaymentDao
}
