package com.rohitthebest.manageyourrenters.database.dao

import androidx.room.*
import com.rohitthebest.manageyourrenters.database.model.PartialPayment
import kotlinx.coroutines.flow.Flow

@Dao
interface PartialPaymentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPartialPayment(partialPayment: PartialPayment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPartialPayment(partialPayments: List<PartialPayment>)

    @Delete
    suspend fun deletePartialPayment(partialPayment: PartialPayment)

    @Query("DELETE FROM partial_payment_table")
    suspend fun deleteAllPartialPayments()

    @Query("DELETE FROM partial_payment_table  WHERE isSynced = :isSynced")
    suspend fun deletePartialPaymentsByIsSynced(isSynced: Boolean)

    @Query("DELETE FROM partial_payment_table WHERE `key` IN (:partialPaymentKeys)")
    suspend fun deleteAllByProvideList(partialPaymentKeys: List<String>)

    @Query("DELETE FROM partial_payment_table WHERE borrowerPaymentKey= :borrowerPaymentKey")
    suspend fun deleteAllPartialPaymentByBorrowerPaymentKey(borrowerPaymentKey: String)

    @Query("DELETE FROM partial_payment_table WHERE borrowerId= :borrowerId")
    suspend fun deleteAllPartialPaymentByBorrowerId(borrowerId: String)

    @Query("SELECT * FROM partial_payment_table")
    fun getAllPartialPayments(): Flow<List<PartialPayment>>

    @Query("SELECT * FROM partial_payment_table WHERE borrowerPaymentKey = :borrowerPaymentKey ORDER BY created DESC")
    fun getPartialPaymentByBorrowerPaymentKey(borrowerPaymentKey: String): Flow<List<PartialPayment>>

    // it will give the list of keys of a particular borrower payment
    // making it suspend because it will only be used in viewModel and not in fragments or activities
    @Query("SELECT `key` FROM partial_payment_table WHERE borrowerPaymentKey= :borrowerPaymentKey and isSynced = 1")
    suspend fun getKeysByBorrowerPaymentKey(borrowerPaymentKey: String): List<String>

    // will give the list of keys of a particular borrower
    @Query("SELECT `key` FROM partial_payment_table WHERE borrowerId =:borrowerId")
    suspend fun getKeysByBorrowerId(borrowerId: String): List<String>

    @Query("UPDATE partial_payment_table SET isSynced = 0 WHERE `key` = :key")
    suspend fun updateIsSyncedValueToFalse(key: String)
}

