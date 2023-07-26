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

    @Query("SELECT * FROM renter_table ORDER BY status, modified DESC, timeStamp DESC")
    fun getAllRentersList(): Flow<List<Renter>>

    @Query("SELECT COUNT(id) FROM renter_table")
    fun getRentersCount(): Flow<Int>

    @Query("SELECT * FROM renter_table WHERE isSynced = :isSynced ORDER BY modified DESC, timeStamp DESC")
    fun getRenterByIsSynced(isSynced: String): Flow<List<Renter>>

    @Query("SELECT * FROM renter_table WHERE `key` =:renterKey")
    fun getRenterByKey(renterKey: String): Flow<Renter>

//    @Query("SELECT * FROM renter_table INNER JOIN renter_payment_table ON renter_table.`key` = renter_payment_table.renterKey")
//    fun getRenterWithTheirPaymentList(): Flow<Map<Renter, List<RenterPayment>>>

    @MapInfo(keyColumn = "renterName", valueColumn = "amountPaid")
    @Query(
        "SELECT renter_table.name AS renterName, renter_payment_table.amountPaid AS amountPaid FROM renter_table INNER JOIN renter_payment_table ON renter_table.`key` = renter_payment_table.renterKey"
    )
    fun getRentersWithTheirAmountPaid(): Flow<Map<String, List<Double>>>

    @MapInfo(keyColumn = "renterName", valueColumn = "amountPaid")
    @Query(
        "SELECT renter_table.name AS renterName, renter_payment_table.amountPaid AS amountPaid FROM renter_table INNER JOIN renter_payment_table ON renter_table.`key` = renter_payment_table.renterKey WHERE renter_payment_table.created BETWEEN :startDate AND :endDate"
    )
    fun getRentersWithTheirAmountPaidByDateCreated(
        startDate: Long,
        endDate: Long
    ): Flow<Map<String, List<Double>>>

    @MapInfo(keyColumn = "renterName", valueColumn = "dues")
    @Query("SELECT renter_table.name AS renterName, renter_table.dueOrAdvanceAmount AS dues FROM renter_table WHERE dues < 0")
    fun getRentersWithTheirDues(): Flow<Map<String, Double>>

    @Query("SELECT DISTINCT address FROM renter_table")
    fun getAllDistinctAddress(): Flow<List<String>>

    @Query("UPDATE renter_table SET isSynced = 'false' WHERE `key` = :key")
    suspend fun updateIsSyncedValueToFalse(key: String)
}