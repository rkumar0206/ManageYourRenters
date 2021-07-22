package com.rohitthebest.manageyourrenters.database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rohitthebest.manageyourrenters.database.dao.PartialPaymentDao
import com.rohitthebest.manageyourrenters.database.model.PartialPayment

@Database(
    entities = [PartialPayment::class],
    version = 1,
    exportSchema = false
)
abstract class PartialPaymentDatabase : RoomDatabase() {

    abstract fun getPartialPaymentDao(): PartialPaymentDao

}
