package com.rohitthebest.manageyourrenters.database.dao

import androidx.room.*
import com.rohitthebest.manageyourrenters.data.ExpenseCategoryAndTheirTotalExpenseAmounts
import com.rohitthebest.manageyourrenters.database.model.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllExpense(expenses: List<Expense>)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expense_table")
    suspend fun deleteAllExpenses()

    @Query("DELETE FROM expense_table WHERE categoryKey = :expenseCategoryKey")
    suspend fun deleteExpenseByExpenseCategoryKey(expenseCategoryKey: String)

    // issue #12
    @Query("DELETE FROM expense_table WHERE `key` = :expenseKey")
    suspend fun deleteExpenseByKey(expenseKey: String)

    // issue #12
    @Query("DELETE FROM expense_table WHERE `key` IN (:expenseKeys)")
    suspend fun deleteExpenseByListOfKeys(expenseKeys: List<String>)

    @Query("DELETE FROM expense_table WHERE isSynced = :isSynced")
    suspend fun deleteExpenseByIsSynced(isSynced: Boolean)

    @Query("SELECT SUM(amount) FROM expense_table WHERE categoryKey = :expenseCategoryKey")
    fun getExpenseAmountSumByExpenseCategoryKey(expenseCategoryKey: String): Flow<Double>

    @Query("SELECT SUM(amount) FROM expense_table WHERE categoryKey = :expenseCategoryKey AND created BETWEEN :date1 AND :date2")
    fun getExpenseAmountSumByExpenseCategoryByDateRange(
        expenseCategoryKey: String, date1: Long, date2: Long
    ): Flow<Double>

    @Query("SELECT SUM(amount) FROM expense_table WHERE categoryKey = :expenseCategoryKey")
    fun getTotalExpenseAmountByExpenseCategory(expenseCategoryKey: String): Flow<Double>

    @Query("SELECT SUM(amount) FROM expense_table")
    fun getTotalExpenseAmount(): Flow<Double>

    @Query("SELECT SUM(amount) FROM expense_table WHERE created BETWEEN :date1 AND :date2")
    fun getTotalExpenseAmountByDateRange(date1: Long, date2: Long): Flow<Double>

    @Query("SELECT SUM(amount) FROM expense_table WHERE categoryKey = :expenseCategoryKey AND created BETWEEN :date1 AND :date2")
    fun getTotalExpenseAmountByCategoryKeyAndDateRange(
        expenseCategoryKey: String,
        date1: Long,
        date2: Long
    ): Flow<Double>

    @Query("SELECT SUM(amount) FROM expense_table WHERE categoryKey IN (:expenseCategoryKeys)")
    fun getTotalExpenseByCategoryKeys(
        expenseCategoryKeys: List<String>
    ): Flow<Double>


    @Query("SELECT SUM(amount) FROM expense_table WHERE categoryKey IN (:expenseCategoryKeys) AND created BETWEEN :date1 AND :date2")
    fun getTotalExpenseByCategoryKeysAndDateRange(
        expenseCategoryKeys: List<String>,
        date1: Long,
        date2: Long
    ): Flow<Double>

    @Query("SELECT * FROM expense_table WHERE categoryKey IN (:expenseCategoryKeys) AND created BETWEEN :date1 AND :date2 order by created DESC")
    fun getExpenseByCategoryKeysAndDateRange(
        expenseCategoryKeys: List<String>,
        date1: Long,
        date2: Long
    ): Flow<List<Expense>>


    @Query("SELECT * FROM expense_table WHERE `key` = :expenseKey")
    fun getExpenseByKey(expenseKey: String): Flow<Expense?>

    @Query("SELECT * FROM expense_table ORDER BY created DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT DISTINCT spentOn FROM expense_table")
    fun getAllSpentOn(): Flow<List<String>>

    @Query("SELECT * FROM expense_table WHERE created BETWEEN :date1 AND :date2 ORDER BY created DESC")
    fun getExpensesByDateRange(date1: Long, date2: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expense_table WHERE categoryKey =:expenseCategoryKey AND created BETWEEN :date1 AND :date2 ORDER BY created DESC")
    fun getExpenseByDateRangeAndExpenseCategoryKey(
        expenseCategoryKey: String, date1: Long, date2: Long
    ): Flow<List<Expense>>

    @Query("SELECT * FROM expense_table WHERE categoryKey = :expenseCategoryKey ORDER BY created DESC")
    fun getExpensesByExpenseCategoryKey(expenseCategoryKey: String): Flow<List<Expense>>

    @Query("SELECT `key` FROM expense_table WHERE categoryKey = :expenseCategoryKey")
    suspend fun getKeysByExpenseCategoryKey(expenseCategoryKey: String): List<String>

    @Query("SELECT * FROM expense_table WHERE paymentMethods LIKE '%' || :paymentMethodKey || '%' ORDER BY created DESC")
    fun getExpensesByPaymentMethodKey(paymentMethodKey: String): Flow<List<Expense>>

    @Query("UPDATE expense_table SET isSynced = 0 WHERE `key` = :key")
    suspend fun updateIsSyncedValueToFalse(key: String)

    @Query(
        "SELECT expense_category_table.`key` as expenseCategoryKey, " +
                "expense_category_table.categoryName AS categoryName, " +
                "SUM(expense_table.amount) AS totalAmount " +
                "FROM expense_table " +
                "INNER JOIN expense_category_table " +
                "ON expense_table.categoryKey = expense_category_table.`key` " +
                "GROUP BY expense_category_table.`key`"
    )
    fun getTotalExpenseAmountsWithTheirExpenseCategoryKeys(): Flow<List<ExpenseCategoryAndTheirTotalExpenseAmounts>>

    @Query(
        "SELECT expense_category_table.`key` as expenseCategoryKey, " +
                "expense_category_table.categoryName AS categoryName, " +
                "SUM(expense_table.amount) AS totalAmount " +
                "FROM expense_table " +
                "INNER JOIN expense_category_table " +
                "ON expense_table.categoryKey = expense_category_table.`key` " +
                "WHERE expense_table.`key` in (:expenseKeys) " +
                "GROUP BY expense_category_table.`key`"
    )
    fun getTotalExpenseAmountsWithTheirExpenseCategoryKeysByListOfExpenseKeys(
        expenseKeys: List<String>
    ): Flow<List<ExpenseCategoryAndTheirTotalExpenseAmounts>>


    @Query(
        "SELECT expense_category_table.`key` AS expenseCategoryKey, " +
                "expense_category_table.categoryName AS categoryName, " +
                "SUM(expense_table.amount) AS totalAmount " +
                "FROM expense_table " +
                "INNER JOIN expense_category_table " +
                "ON expense_table.categoryKey = expense_category_table.`key` " +
                "WHERE expense_table.created BETWEEN :date1 AND :date2 " +
                "GROUP BY expense_category_table.`key`"
    )
    fun getTotalExpenseAmountsWithTheirExpenseCategoryKeysByDateRange(
        date1: Long,
        date2: Long
    ): Flow<List<ExpenseCategoryAndTheirTotalExpenseAmounts>>

    @Query(
        "SELECT expense_category_table.`key` AS expenseCategoryKey, " +
                "expense_category_table.categoryName AS categoryName, " +
                "SUM(expense_table.amount) AS totalAmount " +
                "FROM expense_table " +
                "INNER JOIN expense_category_table " +
                "ON expense_table.categoryKey = expense_category_table.`key` " +
                "WHERE expense_table.created BETWEEN :date1 AND :date2 " +
                "AND expense_table.`key` IN (:expenseKeys) " +
                "GROUP BY expense_category_table.`key`"
    )
    fun getTotalExpenseAmountsWithTheirExpenseCategoryKeysByDateRangeAndByListOfExpenseKeys(
        date1: Long,
        date2: Long,
        expenseKeys: List<String>
    ): Flow<List<ExpenseCategoryAndTheirTotalExpenseAmounts>>


    @Query(
        "SELECT expense_category_table.`key` AS expenseCategoryKey, " +
                "expense_category_table.categoryName AS categoryName, " +
                "SUM(expense_table.amount) AS totalAmount " +
                "FROM expense_table " +
                "INNER JOIN expense_category_table " +
                "ON expense_table.categoryKey = expense_category_table.`key` " +
                "WHERE expense_table.categoryKey IN (:selectedExpenseCategories) " +
                "GROUP BY expense_category_table.`key`"
    )
    fun getTotalExpenseAmountsWithTheirExpenseCategoryKeysForSelectedExpenseCategories(
        selectedExpenseCategories: List<String>
    ): Flow<List<ExpenseCategoryAndTheirTotalExpenseAmounts>>

    @Query(
        "SELECT expense_category_table.`key` AS expenseCategoryKey, " +
                "expense_category_table.categoryName AS categoryName, " +
                "SUM(expense_table.amount) AS totalAmount " +
                "FROM expense_table " +
                "INNER JOIN expense_category_table " +
                "ON expense_table.categoryKey = expense_category_table.`key` " +
                "WHERE expense_table.categoryKey IN (:selectedExpenseCategories) " +
                "AND expense_table.created BETWEEN :date1 AND :date2 " +
                "GROUP BY expense_category_table.`key`"
    )
    fun getTotalExpenseAmountsWithTheirExpenseCategoryKeysForSelectedExpenseCategoriesByDateRange(
        selectedExpenseCategories: List<String>,
        date1: Long,
        date2: Long
    ): Flow<List<ExpenseCategoryAndTheirTotalExpenseAmounts>>


    @Query("SELECT EXISTS(select `key` from expense_table limit 1)")
    fun isAnyExpenseAdded(): Flow<Boolean>
}
