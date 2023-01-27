package com.rohitthebest.manageyourrenters.database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rohitthebest.manageyourrenters.database.dao.PaymentMethodDao
import com.rohitthebest.manageyourrenters.database.model.PaymentMethod

@Database(
    entities = [PaymentMethod::class],
    exportSchema = false,
    version = 1
)
abstract class PaymentMethodDatabase : RoomDatabase() {

    abstract fun getPaymentMethodDao(): PaymentMethodDao
}