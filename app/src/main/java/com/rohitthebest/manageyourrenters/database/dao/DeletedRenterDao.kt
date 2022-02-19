package com.rohitthebest.manageyourrenters.database.dao

import androidx.room.*
import com.rohitthebest.manageyourrenters.database.model.DeletedRenter
import kotlinx.coroutines.flow.Flow

@Dao
interface DeletedRenterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeletedRenter(deletedRenter: DeletedRenter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDeletedRenter(deletedRenters: List<DeletedRenter>)

    @Update
    suspend fun updateDeletedRenter(deletedRenter: DeletedRenter)

    @Delete
    suspend fun deleteDeletedRenter(deletedRenter: DeletedRenter)

    @Query("DELETE FROM deleted_renter_table")
    suspend fun deleteAllDeletedRenters()

    @Query("SELECT * FROM deleted_renter_table")
    fun getAllDeletedRenters(): Flow<List<DeletedRenter>>

    @Query("SELECT * FROM deleted_renter_table WHERE `key` = :deletedRenterKey")
    fun getDeletedRenterByKey(deletedRenterKey: String): Flow<DeletedRenter>

}
