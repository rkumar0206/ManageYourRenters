package com.rohitthebest.manageyourrenters.database.dao


import androidx.room.*
import com.rohitthebest.manageyourrenters.database.model.RenterPayment
import kotlinx.coroutines.flow.Flow

@Dao
interface RenterPaymentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRenterPayment(renterPayment: RenterPayment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRenterPayment(renterPayments: List<RenterPayment>)

    @Update
    suspend fun updateRenterPayment(renterPayment: RenterPayment)

    @Delete
    suspend fun deleteRenterPayment(renterPayment: RenterPayment)

    @Query("DELETE FROM renter_payment_table")
    suspend fun deleteAllRenterPayments()

    @Query("SELECT * FROM renter_payment_table")
    fun getAllRenterPayments(): Flow<List<RenterPayment>>

    @Query("DELETE FROM renter_payment_table WHERE renterKey =:renterKey")
    suspend fun deleteAllPaymentsOfRenter(renterKey: String)

    @Query("DELETE FROM renter_payment_table WHERE isSynced =:isSynced")
    suspend fun deleteAllPaymentsByIsSynced(isSynced: Boolean)

    @Query("SELECT * FROM renter_payment_table WHERE renterKey =:renterKey ORDER BY created DESC")
    fun getAllPaymentsListOfRenter(renterKey: String): Flow<List<RenterPayment>>

    @Query("SELECT * FROM renter_payment_table WHERE `key` =:paymentKey")
    fun getPaymentByPaymentKey(paymentKey: String): Flow<RenterPayment>

    @Query("SELECT `key` FROM renter_payment_table WHERE renterKey = :renterKey")
    suspend fun getPaymentKeysByRenterKey(renterKey: String): List<String>

    @Query("SELECT * FROM renter_payment_table WHERE renterKey = :renterKey ORDER BY created DESC LIMIT 1")
    fun getLastRenterPayment(renterKey: String): Flow<RenterPayment>

    @Query("SELECT SUM(amountPaid) FROM renter_payment_table")
    fun getTotalRevenueOfAllTime(): Flow<Double>
}
