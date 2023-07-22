package com.rohitthebest.manageyourrenters.ui.viewModels


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.BarEntry
import com.rohitthebest.manageyourrenters.data.MonthAndTotalSum
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants
import com.rohitthebest.manageyourrenters.repositories.BudgetRepository
import com.rohitthebest.manageyourrenters.repositories.ExpenseRepository
import com.rohitthebest.manageyourrenters.repositories.IncomeRepository
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetAndIncomeGraphViewModel @Inject constructor(
    app: Application,
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository,
    private val incomeRepository: IncomeRepository
) : AndroidViewModel(app) {

    private var _incomeBudgetAndExpenseBarEntryData =
        MutableLiveData<Map<String, MutableList<BarEntry>>>(
            emptyMap()
        )

    val incomeBudgetAndExpenseBarEntryData get() = _incomeBudgetAndExpenseBarEntryData

    fun getBarEntryDataForIncomeBudgetAndExpenseByYear(
        year: Int,
        paymentMethodKeys: List<String> = emptyList()
    ) {

        viewModelScope.launch {

            // budget
            val budgetTotals = budgetRepository.getAllTotalBudgetByYear(year).first()
            val budgetBarEntry = getBudgetBarEntry(budgetTotals)

            // income
            val incomeTotals = if (paymentMethodKeys.isNotEmpty()) {
                incomeTotalAfterApplyingPaymentMethodFilter(
                    year, listOf(Constants.PAYMENT_METHOD_CASH_KEY)
                )
            } else {
                incomeRepository.getAllTotalIncomeByYear(year).first()
            }

            val incomeBarEntry = getIncomeBarEntry(incomeTotals)

            // expense
            val monthAndTotalExpenseList: ArrayList<MonthAndTotalSum> = ArrayList()
            for (i in 0..11) {

                val datePairForExpense =
                    WorkingWithDateAndTime.getMillisecondsOfStartAndEndDayOfMonthForGivenMonthAndYear(
                        i, year
                    )

                val categoryKeys =
                    budgetRepository.getExpenseCategoryKeysOfAllBudgetsByMonthAndYear(
                        i, year
                    ).first()


                val total: Double = try {

                    if (paymentMethodKeys.isNotEmpty()) {
                        getExpenseTotalAfterApplyingPaymentMethodFilter(
                            datePairForExpense, categoryKeys, paymentMethodKeys
                        )
                    } else {
                        expenseRepository.getTotalExpenseByCategoryKeysAndDateRange(
                            categoryKeys,
                            datePairForExpense.first,
                            datePairForExpense.second + Constants.ONE_DAY_MILLISECONDS
                        ).first()
                    }
                } catch (e: NullPointerException) {
                    0.0
                }

                monthAndTotalExpenseList.add(MonthAndTotalSum(i, total))
            }

            val expenseBarEntry = getExpenseBarEntry(monthAndTotalExpenseList)

            val consolidatedBarEntries: HashMap<String, MutableList<BarEntry>> = HashMap()

            consolidatedBarEntries[FirestoreCollectionsConstants.BUDGETS] = budgetBarEntry
            consolidatedBarEntries[FirestoreCollectionsConstants.INCOMES] = incomeBarEntry
            consolidatedBarEntries[FirestoreCollectionsConstants.EXPENSES] = expenseBarEntry

            _incomeBudgetAndExpenseBarEntryData.value = consolidatedBarEntries
        }
    }

    private fun getBudgetBarEntry(budgetTotals: List<MonthAndTotalSum>): ArrayList<BarEntry> {

        val budgetLimitList = ArrayList<BarEntry>()
        val monthAndBudgetSumMap = budgetTotals.associate { it.month to it.total }

        for (i in 1..12) {

            if (monthAndBudgetSumMap.contains(i - 1)) {
                budgetLimitList.add(BarEntry(i.toFloat(), monthAndBudgetSumMap[i - 1]!!.toFloat()))
            } else {
                budgetLimitList.add(BarEntry(i.toFloat(), 0f))
            }
        }

        return budgetLimitList
    }

    private fun getIncomeBarEntry(incomeTotals: List<MonthAndTotalSum>): ArrayList<BarEntry> {

        val incomeList = ArrayList<BarEntry>()
        val monthAndIncomeSumMap = incomeTotals.associate { it.month to it.total }

        for (i in 1..12) {

            if (monthAndIncomeSumMap.contains(i - 1)) {
                incomeList.add(BarEntry(i.toFloat(), monthAndIncomeSumMap[i - 1]!!.toFloat()))
            } else {
                incomeList.add(BarEntry(i.toFloat(), 0f))
            }
        }

        return incomeList
    }

    private fun getExpenseBarEntry(expenseTotals: List<MonthAndTotalSum>): ArrayList<BarEntry> {

        val expenseList = ArrayList<BarEntry>()
        val monthAndIncomeSumMap = expenseTotals.associate { it.month to it.total }

        for (i in 1..12) {

            if (monthAndIncomeSumMap.contains(i - 1)) {
                expenseList.add(BarEntry(i.toFloat(), monthAndIncomeSumMap[i - 1]!!.toFloat()))
            } else {
                expenseList.add(BarEntry(i.toFloat(), 0f))
            }
        }

        return expenseList
    }

    private suspend fun incomeTotalAfterApplyingPaymentMethodFilter(
        year: Int,
        paymentMethods: List<String>
    ): List<MonthAndTotalSum> {

        val incomeTotals: ArrayList<MonthAndTotalSum> = ArrayList()

        for (month in 0..11) {

            val incomes = incomeRepository.getAllIncomesByMonthAndYear(month, year).first()

            val total: Double = try {
                if (incomes.isEmpty()) {
                    0.0
                } else {
                    val tempIncome =
                        incomeRepository.applyFilterByPaymentMethods(paymentMethods, incomes)

                    tempIncome.sumOf { it.income }
                }
            } catch (e: NullPointerException) {
                e.printStackTrace()
                0.0
            }

            incomeTotals.add(MonthAndTotalSum(month, total))
        }

        return incomeTotals
    }

    private suspend fun getExpenseTotalAfterApplyingPaymentMethodFilter(
        datePairForExpense: Pair<Long, Long>,
        categoryKeys: List<String>,
        paymentMethodKeys: List<String>
    ): Double {

        val expenses = expenseRepository.getExpenseByCategoryKeysAndDateRange(
            categoryKeys,
            datePairForExpense.first,
            datePairForExpense.second + Constants.ONE_DAY_MILLISECONDS
        ).first()

        return try {
            if (expenses.isEmpty()) {
                0.0
            } else {
                val tempExpense = expenseRepository.applyExpenseFilterByPaymentMethods(
                    paymentMethodKeys,
                    expenses
                )

                tempExpense.sumOf { it.amount }
            }
        } catch (e: Exception) {
            0.0
        }

    }

}