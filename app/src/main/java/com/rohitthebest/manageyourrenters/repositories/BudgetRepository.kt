package com.rohitthebest.manageyourrenters.repositories

import com.rohitthebest.manageyourrenters.database.dao.BudgetDao
import com.rohitthebest.manageyourrenters.database.model.Budget
import javax.inject.Inject

class BudgetRepository @Inject constructor(
    val dao: BudgetDao
) {

    suspend fun insertBudget(budget: Budget) = dao.insertBudget(budget)

    suspend fun insertAllBudget(budgets: List<Budget>) =
        dao.insertAllBudget(budgets)

    suspend fun updateBudget(budget: Budget) =
        dao.updateBudget(budget)

    suspend fun deleteBudget(budget: Budget) =
        dao.deleteBudget(budget)

    suspend fun deleteAllBudgets() = dao.deleteAllBudgets()

    suspend fun deleteBudgetsByExpenseCategoryKey(expenseCategoryKey: String) =
        dao.deleteBudgetsByExpenseCategoryKey(expenseCategoryKey)

    suspend fun deleteBudgetsByMonthAndYear(month: Int, year: Int) =
        dao.deleteBudgetsByMonthAndYear(month, year)

    fun getAllBudgets() = dao.getAllBudgets()

    fun getAllBudgetsByKey(keyList: List<String?>) = dao.getAllBudgetsByKey(keyList)

    fun getAllBudgetsByMonthAndYear(month: Int, year: Int) =
        dao.getAllBudgetsByMonthAndYear(month, year)

    fun getAllBudgetsByMonthAndYearString(monthYearString: String) =
        dao.getAllBudgetsByMonthAndYearString(monthYearString)

    fun getTheOldestSavedBudgetYear() = dao.getTheOldestSavedBudgetYear()

    fun getBudgetByKey(budgetKey: String) = dao.getBudgetBuyBudgetKey(budgetKey)

    fun getTotalBudgetByMonthAndYear(month: Int, year: Int) =
        dao.getTotalBudgetByMonthAndYear(month, year)

    fun getAllTotalBudgetByYear(year: Int) = dao.getAllTotalBudgetByYear(year)

    fun getExpenseCategoryKeysOfAllBudgetsByMonthAndYear(month: Int, year: Int) =
        dao.getExpenseCategoryKeysOfAllBudgetsByMonthAndYear(
            month, year
        )

    suspend fun getKeysByExpenseCategoryKey(expenseCategoryKey: String) =
        dao.getKeysByExpenseCategoryKey(
            expenseCategoryKey
        )

    suspend fun getKeysByMonthAndYear(month: Int, year: Int) = dao.getKeysByMonthAndYear(
        month, year
    )

    suspend fun deleteByIsSyncedValue(isSynced: Boolean) = dao.deleteByIsSyncedValue(isSynced)

    fun getAllBudgetMonthAndYearForWhichBudgetIsAdded() =
        dao.getAllBudgetMonthAndYearForWhichBudgetIsAdded()

    fun isAnyBudgetAddedForThisMonthAndYear(monthYearString: String) =
        dao.isAnyBudgetAddedForThisMonthAndYear(
            monthYearString
        )
}