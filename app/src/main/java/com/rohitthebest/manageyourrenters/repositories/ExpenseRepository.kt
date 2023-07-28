package com.rohitthebest.manageyourrenters.repositories

import com.rohitthebest.manageyourrenters.database.dao.ExpenseDAO
import com.rohitthebest.manageyourrenters.database.model.Expense
import com.rohitthebest.manageyourrenters.others.Constants
import javax.inject.Inject

class ExpenseRepository @Inject constructor(
    val dao: ExpenseDAO
) {

    suspend fun insertExpense(expense: Expense) = dao.insertExpense(expense)

    suspend fun insertAllExpense(expenses: List<Expense>) =
        dao.insertAllExpense(expenses)

    suspend fun updateExpense(expense: Expense) =
        dao.updateExpense(expense)

    suspend fun deleteExpense(expense: Expense) =
        dao.deleteExpense(expense)

    suspend fun deleteAllExpenses() = dao.deleteAllExpenses()

    suspend fun deleteExpenseByExpenseCategoryKey(expenseCategoryKey: String) =
        dao.deleteExpenseByExpenseCategoryKey(expenseCategoryKey)

    // issue #12
    suspend fun deleteExpenseByKey(expenseKey: String) = dao.deleteExpenseByKey(expenseKey)

    // issue #12
    suspend fun deleteExpenseByListOfKeys(expenseKeys: List<String>) =
        dao.deleteExpenseByListOfKeys(expenseKeys)

    suspend fun deleteExpenseByIsSynced(isSynced: Boolean) = dao.deleteExpenseByIsSynced(isSynced)

    fun getAllExpenses() = dao.getAllExpenses()

    fun getAllSpentOn() = dao.getAllSpentOn()

    fun getExpenseAmountSumByExpenseCategoryKey(expenseCategoryKey: String) =
        dao.getExpenseAmountSumByExpenseCategoryKey(expenseCategoryKey)

    fun getExpenseAmountSumByExpenseCategoryByDateRange(
        expenseCategoryKey: String, date1: Long, date2: Long
    ) = dao.getExpenseAmountSumByExpenseCategoryByDateRange(
        expenseCategoryKey, date1, date2
    )

    fun getTotalExpenseAmountByExpenseCategory(expenseCategoryKey: String) =
        dao.getTotalExpenseAmountByExpenseCategory(expenseCategoryKey)

    fun getTotalExpenseAmount() = dao.getTotalExpenseAmount()

    fun getTotalExpenseAmountByDateRange(date1: Long, date2: Long) =
        dao.getTotalExpenseAmountByDateRange(
            date1, date2
        )

    fun getTotalExpenseAmountByCategoryKeyAndDateRange(
        expenseCategoryKey: String,
        date1: Long,
        date2: Long
    ) =
        dao.getTotalExpenseAmountByCategoryKeyAndDateRange(expenseCategoryKey, date1, date2)

    fun getTotalExpenseByCategoryKeys(
        expenseCategoryKeys: List<String>
    ) = dao.getTotalExpenseByCategoryKeys(expenseCategoryKeys)

    fun getTotalExpenseByCategoryKeysAndDateRange(
        expenseCategoryKeys: List<String>,
        date1: Long,
        date2: Long
    ) = dao.getTotalExpenseByCategoryKeysAndDateRange(expenseCategoryKeys, date1, date2)

    fun getExpenseByCategoryKeysAndDateRange(
        expenseCategoryKeys: List<String>,
        date1: Long,
        date2: Long
    ) = dao.getExpenseByCategoryKeysAndDateRange(expenseCategoryKeys, date1, date2)

    fun getExpenseByDateRangeAndExpenseCategoryKey(
        expenseCategoryKey: String, date1: Long, date2: Long
    ) = dao.getExpenseByDateRangeAndExpenseCategoryKey(expenseCategoryKey, date1, date2)

    fun getExpensesByExpenseCategoryKey(expenseCategoryKey: String) =
        dao.getExpensesByExpenseCategoryKey(expenseCategoryKey)

    fun getExpenseByKey(expenseKey: String) = dao.getExpenseByKey(expenseKey)

    fun getExpensesByDateRange(date1: Long, date2: Long) = dao.getExpensesByDateRange(date1, date2)

    fun getExpensesByPaymentMethodKey(paymentMethodKey: String) = dao.getExpensesByPaymentMethodKey(
        paymentMethodKey
    )

    suspend fun getKeysByExpenseCategoryKey(expenseCategoryKey: String) =
        dao.getKeysByExpenseCategoryKey(expenseCategoryKey)

    fun getTotalExpenseAmountsWithTheirExpenseCategoryKeys() =
        dao.getTotalExpenseAmountsWithTheirExpenseCategoryKeys()

    fun getTotalExpenseAmountsWithTheirExpenseCategoryKeysByDateRange(date1: Long, date2: Long) =
        dao.getTotalExpenseAmountsWithTheirExpenseCategoryKeysByDateRange(date1, date2)

    fun getTotalExpenseAmountsWithTheirExpenseCategoryKeysForSelectedExpenseCategories(
        selectedExpenseCategories: List<String>
    ) = dao.getTotalExpenseAmountsWithTheirExpenseCategoryKeysForSelectedExpenseCategories(
        selectedExpenseCategories
    )

    fun getTotalExpenseAmountsWithTheirExpenseCategoryKeysForSelectedExpenseCategoriesByDateRange(
        selectedExpenseCategories: List<String>,
        date1: Long,
        date2: Long
    ) =
        dao.getTotalExpenseAmountsWithTheirExpenseCategoryKeysForSelectedExpenseCategoriesByDateRange(
            selectedExpenseCategories,
            date1,
            date2
        )

    fun isAnyExpenseAdded() = dao.isAnyExpenseAdded()

    fun applyExpenseFilterByPaymentMethods(
        paymentMethodKeys: List<String>,
        expenses: List<Expense>
    ): List<Expense> {

        val isOtherPaymentMethodKeyPresent =
            paymentMethodKeys.contains(Constants.PAYMENT_METHOD_OTHER_KEY)

        val resultExpenses = expenses.filter { expense ->

            if (isOtherPaymentMethodKeyPresent) {
                // for other payment method, get all the expenses where payment methods is null as well as payment method is other
                expense.paymentMethods == null || expense.paymentMethods!!.any { it in paymentMethodKeys }
            } else {
                expense.paymentMethods != null && expense.paymentMethods!!.any { it in paymentMethodKeys }
            }
        }

        return resultExpenses
    }
}