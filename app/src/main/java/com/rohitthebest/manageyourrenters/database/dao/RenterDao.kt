package com.rohitthebest.manageyourrenters.database.dao

import androidx.room.*
import com.rohitthebest.manageyourrenters.database.model.Renter
import kotlinx.coroutines.flow.Flow

@Dao
interface RenterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRenter(renter: Renter)

    @Insert
    suspend fun insertRenters(renters: List<Renter>)

    @Update
    suspend fun updateRenter(renter: Renter)

    @Delete
    suspend fun deleteRenter(renter: Renter)

    @Query("DELETE FROM renter_table")
    suspend fun deleteAll()

    @Query("DELETE FROM renter_table WHERE isSynced = :isSynced")
    suspend fun deleteRenterByIsSynced(isSynced: String)

    @Query("SELECT * FROM renter_table ORDER BY modified DESC, timeStamp DESC")
    fun getAllRentersList(): Flow<List<Renter>>

    @Query("SELECT COUNT(id) FROM renter_table")
    fun getRentersCount(): Flow<Int>

    @Query("SELECT * FROM renter_table WHERE isSynced = :isSynced ORDER BY modified DESC, timeStamp DESC")
    fun getRenterByIsSynced(isSynced: String): Flow<List<Renter>>

    @Query("SELECT * FROM renter_table WHERE `key` =:renterKey")
    fun getRenterByKey(renterKey: String): Flow<Renter>

}