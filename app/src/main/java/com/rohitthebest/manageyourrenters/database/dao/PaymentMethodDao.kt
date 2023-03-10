package com.rohitthebest.manageyourrenters.database.dao

import androidx.room.*
import com.rohitthebest.manageyourrenters.database.model.PaymentMethod
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentMethodDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethod(paymentMethod: PaymentMethod)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPaymentMethod(paymentMethods: List<PaymentMethod>)

    @Update
    suspend fun updatePaymentMethod(paymentMethod: PaymentMethod)

    @Delete
    suspend fun deletePaymentMethod(paymentMethod: PaymentMethod)

    @Query("DELETE FROM payment_method_table WHERE isSynced = :isSynced")
    suspend fun deleteByIsSyncedValue(isSynced: Boolean)

    @Query("DELETE FROM payment_method_table")
    suspend fun deleteAllPaymentMethods()

    @Query("SELECT * FROM payment_method_table")
    fun getAllPaymentMethods(): Flow<List<PaymentMethod>>

    @Query("SELECT * FROM payment_method_table WHERE `key` = :paymentMethodKey")
    fun getPaymentMethodByKey(paymentMethodKey: String): Flow<PaymentMethod>

    @Query("UPDATE payment_method_table SET isSynced = 0 WHERE `key` = :key")
    suspend fun updateIsSyncedValueToFalse(key: String)
}
