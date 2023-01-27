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

    @Query("DELETE FROM payment_method_table")
    suspend fun deleteAllPaymentMethods()

    @Query("SELECT * FROM payment_method_table")
    fun getAllPaymentMethods(): Flow<List<PaymentMethod>>

}
