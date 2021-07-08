package com.rohitthebest.manageyourrenters.database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rohitthebest.manageyourrenters.database.dao.RenterDao
import com.rohitthebest.manageyourrenters.database.model.Renter

@Database(
    entities = [Renter::class],
    version = 123
)
abstract class RenterDatabase : RoomDatabase() {

    abstract fun getRenterDao() : RenterDao
}