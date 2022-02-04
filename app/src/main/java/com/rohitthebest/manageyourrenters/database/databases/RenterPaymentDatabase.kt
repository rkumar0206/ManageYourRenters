package com.rohitthebest.manageyourrenters.database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rohitthebest.manageyourrenters.database.dao.RenterPaymentDao
import com.rohitthebest.manageyourrenters.database.model.RenterPayment
import com.rohitthebest.manageyourrenters.database.typeConverters.TypeConvertersForDatabase

@Database(
    entities = [RenterPayment::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(TypeConvertersForDatabase::class)
abstract class RenterPaymentDatabase : RoomDatabase() {

    abstract fun getRenterPaymentDao(): RenterPaymentDao

}
