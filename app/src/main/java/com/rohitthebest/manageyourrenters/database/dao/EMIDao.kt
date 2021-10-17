package com.rohitthebest.manageyourrenters.database.dao

import androidx.room.*
import com.rohitthebest.manageyourrenters.database.model.EMI
import kotlinx.coroutines.flow.Flow

@Dao
interface EMIDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEMI(emi: EMI)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEMI(emis: List<EMI>)

    @Update
    suspend fun updateEMI(emi: EMI)

    @Delete
    suspend fun deleteEMI(emi: EMI)

    @Query("DELETE FROM emi_table")
    suspend fun deleteAllEMIs()

    @Query("DELETE FROM emi_table WHERE isSynced = :isSynced")
    suspend fun deleteEMIsByIsSynced(isSynced: Boolean)

    @Query("SELECT * FROM emi_table ORDER BY modified DESC")
    fun getAllEMIs(): Flow<List<EMI>>

    @Query("SELECT * FROM emi_table WHERE `key` = :emiKey")
    fun getEMIByKey(emiKey: String): Flow<EMI>
}
