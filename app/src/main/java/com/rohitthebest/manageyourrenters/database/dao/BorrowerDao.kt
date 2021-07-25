package com.rohitthebest.manageyourrenters.database.dao

import androidx.room.*
import com.rohitthebest.manageyourrenters.database.model.Borrower
import kotlinx.coroutines.flow.Flow

@Dao
interface BorrowerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBorrower(borrower: Borrower)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBorrowers(borrowers: List<Borrower>)

    @Update
    suspend fun update(borrower: Borrower)

    @Delete
    suspend fun delete(borrower: Borrower)

    @Query("DELETE FROM borrower_table")
    suspend fun deleteAllBorrower()

    @Query("DELETE FROM borrower_table WHERE isSynced = :isSynced")
    suspend fun deleteBorrowerByIsSynced(isSynced: Boolean)

    @Query("SELECT * FROM borrower_table ORDER BY totalDueAmount DESC, modified DESC")
    fun getAllBorrower(): Flow<List<Borrower>>

    @Query("SELECT * FROM borrower_table WHERE `key` = :borrowerKey")
    fun getBorrowerByKey(borrowerKey: String): Flow<Borrower>
}
