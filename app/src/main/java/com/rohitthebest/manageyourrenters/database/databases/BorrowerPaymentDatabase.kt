package com.rohitthebest.manageyourrenters.database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rohitthebest.manageyourrenters.database.dao.BorrowerPaymentDao
import com.rohitthebest.manageyourrenters.database.model.BorrowerPayment

@Database(
    entities = [BorrowerPayment::class],
    version = 1,
    exportSchema = false
)
abstract class BorrowerPaymentDatabase : RoomDatabase() {

    abstract fun getBorrowerPaymentDao(): BorrowerPaymentDao
}
