package com.rohitthebest.manageyourrenters.database.dao

import androidx.room.*
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPaymentCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyPaymentCategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthlyPaymentCategory(monthlyPaymentCategory: MonthlyPaymentCategory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMonthlyPaymentCategory(monthlyPaymentCategories: List<MonthlyPaymentCategory>)

    @Update
    suspend fun updateMonthlyPaymentCategory(monthlyPaymentCategory: MonthlyPaymentCategory)

    @Delete
    suspend fun deleteMonthlyPaymentCategory(monthlyPaymentCategory: MonthlyPaymentCategory)

    @Query("DELETE FROM monthly_payment_category_table WHERE isSynced = :isSynced")
    suspend fun deleteAllMonthlyPaymentCategoriesByIsSynced(isSynced: Boolean)

    @Query("DELETE FROM monthly_payment_category_table")
    suspend fun deleteAllMonthlyPaymentCategories()

    @Query("SELECT * FROM monthly_payment_category_table ORDER BY modified DESC")
    fun getAllMonthlyPaymentCategories(): Flow<List<MonthlyPaymentCategory>>

    @Query("SELECT * FROM monthly_payment_category_table WHERE `key`= :key")
    fun getMonthlyPaymentCategoryUsingKey(key: String): Flow<MonthlyPaymentCategory>
}
