package com.rohitthebest.manageyourrenters.database.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rohitthebest.manageyourrenters.database.dao.BorrowerDao
import com.rohitthebest.manageyourrenters.database.model.Borrower
import com.rohitthebest.manageyourrenters.database.typeConverters.TypeConvertersForDatabase

@Database(
    entities = [Borrower::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(TypeConvertersForDatabase::class)
abstract class BorrowerDatabase : RoomDatabase() {

    abstract fun getBorrowerDao(): BorrowerDao

}
