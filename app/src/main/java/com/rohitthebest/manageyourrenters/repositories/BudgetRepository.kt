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

    fun getAllBudgets() = dao.getAllBudgets()

    fun getAllBudgetsByMonthAndYear(monthAndYearString: String) =
        dao.getAllBudgetsByMonthAndYear(monthAndYearString)

    fun getBudgetByKey(budgetKey: String) = dao.getBudgetBuyBudgetKey(budgetKey)
}