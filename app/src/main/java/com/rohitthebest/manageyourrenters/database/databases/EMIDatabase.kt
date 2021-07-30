package com.rohitthebest.manageyourrenters.database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rohitthebest.manageyourrenters.database.dao.EMIDao
import com.rohitthebest.manageyourrenters.database.model.EMI

@Database(
    entities = [EMI::class],
    version = 1,
    exportSchema = false
)
abstract class EMIDatabase : RoomDatabase() {

    abstract fun getEmiDao(): EMIDao
}
