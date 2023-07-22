package com.rohitthebest.manageyourrenters.repositories

import com.rohitthebest.manageyourrenters.database.dao.IncomeDao
import com.rohitthebest.manageyourrenters.database.model.Income
import com.rohitthebest.manageyourrenters.others.Constants
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

    fun getAllTotalIncomeByYear(year: Int) = dao.getAllTotalIncomeByYear(year)

    fun getAllIncomeSources() = dao.getAllIncomeSources()
    suspend fun deleteByIsSyncedValue(isSynced: Boolean) = dao.deleteByIsSyncedValue(isSynced)

    fun applyFilterByPaymentMethods(
        paymentMethodKeys: List<String>,
        incomes: List<Income>
    ): List<Income> {

        val isOtherPaymentMethodKeyPresent =
            paymentMethodKeys.contains(Constants.PAYMENT_METHOD_OTHER_KEY)

        val resultIncomes = incomes.filter { income ->

            if (isOtherPaymentMethodKeyPresent) {
                // for other payment method, get all the expenses where payment methods is null as well as payment method is other
                income.linkedPaymentMethods == null || income.linkedPaymentMethods!!.any { it in paymentMethodKeys }
            } else {
                income.linkedPaymentMethods != null && income.linkedPaymentMethods!!.any { it in paymentMethodKeys }
            }
        }

        return resultIncomes
    }

}