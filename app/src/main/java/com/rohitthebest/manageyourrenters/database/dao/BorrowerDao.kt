package com.rohitthebest.manageyourrenters.database.dao

import androidx.room.*
import com.rohitthebest.manageyourrenters.database.model.Borrower
import kotlinx.coroutines.flow.Flow

@Dao
interface BorrowerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBorrower(borrower: Borrower)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBorrower(vararg borrowers: Borrower)

    @Update
    suspend fun update(borrower: Borrower)

    @Delete
    suspend fun delete(borrower: Borrower)

    @Query("DELETE FROM borrower_table")
    suspend fun deleteAllBorrower()

    @Query("SELECT * FROM borrower_table ORDER BY timeStamp DESC")
    fun getAllBorrower(): Flow<List<Borrower>>

    @Query("SELECT * FROM borrower_table WHERE `key` = :borrowerKey")
    fun getBorrowerKey(borrowerKey: String): Flow<Borrower>

    @Query("SELECT * FROM borrower_table WHERE isSynced = :isSynced ORDER BY timeStamp DESC")
    fun getBorrowerByIsSynced(isSynced: Boolean): Flow<List<Borrower>>
}
