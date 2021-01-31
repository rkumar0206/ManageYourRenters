package com.rohitthebest.manageyourrenters.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.rohitthebest.manageyourrenters.database.entity.Renter

@Dao
interface RenterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRenter(renter: Renter)

    @Insert
    suspend fun insertRenters(renters: List<Renter>)

    @Delete
    suspend fun deleteRenter(renter: Renter)

    @Query("DELETE FROM renter_table")
    suspend fun deleteAll()

    @Query("SELECT * FROM renter_table ORDER BY timeStamp DESC")
    fun getAllRentersList(): LiveData<List<Renter>>

    @Query("SELECT COUNT(id) FROM renter_table")
    fun getRentersCount(): LiveData<Int>

    @Query("SELECT * FROM renter_table WHERE isSynced = :isSynced ORDER BY timeStamp DESC")
    fun getRenterByIsSynced(isSynced: String): LiveData<List<Renter>>

    @Query("SELECT * FROM renter_table WHERE `key` =:renterKey")
    fun getRenterByKey(renterKey: String): LiveData<Renter>

}