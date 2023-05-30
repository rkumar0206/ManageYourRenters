package com.rohitthebest.manageyourrenters.repositories

import com.rohitthebest.manageyourrenters.database.dao.IncomeDao
import com.rohitthebest.manageyourrenters.database.model.Income
import javax.inject.Inject

class IncomeRepository @Inject constructor(
    val dao: IncomeDao
) {

    suspend fun insertIncome(income: Income) = dao.insertIncome(income)

    suspend fun insertAllIncome(incomes: List<Income>) =
        dao.insertAllIncome(incomes)

    suspend fun updateIncome(income: Income) =
        dao.updateIncome(income)

    suspend fun deleteIncome(income: Income) =
        dao.deleteIncome(income)

    suspend fun deleteAllIncomes() = dao.deleteAllIncomes()

    fun getAllIncomes() = dao.getAllIncomes()

    fun getAllIncomesByMonthAndYear(month: Int, year: Int) =
        dao.getAllIncomesByMonthAndYear(month, year)

    fun getIncomeByKey(incomeKey: String) = dao.getIncomeByKey(incomeKey)

    fun getTotalIncomeAddedByMonthAndYear(month: Int, year: Int) =
        dao.getTotalIncomeAddedByMonthAndYear(month, year)

    fun getAllIncomeSources() = dao.getAllIncomeSources()
}