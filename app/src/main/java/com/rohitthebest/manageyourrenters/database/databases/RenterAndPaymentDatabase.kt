package com.rohitthebest.manageyourrenters.database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rohitthebest.manageyourrenters.database.dao.DeletedRenterDao
import com.rohitthebest.manageyourrenters.database.dao.RenterDao
import com.rohitthebest.manageyourrenters.database.dao.RenterPaymentDao
import com.rohitthebest.manageyourrenters.database.model.DeletedRenter
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.database.model.RenterPayment
import com.rohitthebest.manageyourrenters.database.typeConverters.TypeConvertersForDatabase

@Database(
    entities = [Renter::class, RenterPayment::class, DeletedRenter::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(TypeConvertersForDatabase::class)
abstract class RenterAndPaymentDatabase : RoomDatabase() {

    abstract fun getRenterDao(): RenterDao
    abstract fun getRenterPaymentDao(): RenterPaymentDao
    abstract fun getDeletedRenterDao(): DeletedRenterDao
}