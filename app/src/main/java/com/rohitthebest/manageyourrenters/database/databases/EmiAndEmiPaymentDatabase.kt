package com.rohitthebest.manageyourrenters.database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rohitthebest.manageyourrenters.database.dao.EMIDao
import com.rohitthebest.manageyourrenters.database.dao.EMIPaymentDao
import com.rohitthebest.manageyourrenters.database.model.EMI
import com.rohitthebest.manageyourrenters.database.model.EMIPayment
import com.rohitthebest.manageyourrenters.database.typeConverters.TypeConvertersForDatabase

@Database(
    entities = [EMI::class, EMIPayment::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(TypeConvertersForDatabase::class)
abstract class EmiAndEmiPaymentDatabase : RoomDatabase() {

    abstract fun getEmiDao(): EMIDao
    abstract fun getEMIPaymentDao(): EMIPaymentDao
}
