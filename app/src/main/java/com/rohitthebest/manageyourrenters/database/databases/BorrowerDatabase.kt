package com.rohitthebest.manageyourrenters.database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rohitthebest.manageyourrenters.database.dao.BorrowerDao
import com.rohitthebest.manageyourrenters.database.model.Borrower

@Database(
    entities = [Borrower::class],
    version = 1,
    exportSchema = false
)
abstract class BorrowerDatabase : RoomDatabase() {

    abstract fun getBorrowerDao(): BorrowerDao

}
