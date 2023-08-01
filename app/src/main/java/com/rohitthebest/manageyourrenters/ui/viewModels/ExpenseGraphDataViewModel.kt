package com.rohitthebest.manageyourrenters.ui.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.ExpenseCategoryAndTheirTotalExpenseAmounts
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.repositories.ExpenseRepository
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ExpenseGraphDataViewModel @Inject constructor(
    app: Application,
    private val expenseRepository: ExpenseRepository
) : AndroidViewModel(app) {

    /*
        This variable will be used for storing the data in Pair as
         first: ExpenseCategoryAndTotalAmount
         second:  Total of all the totalAmounts
     */
    private val _expenseGraphData =
        MutableLiveData<Pair<List<ExpenseCategoryAndTheirTotalExpenseAmounts>, Double>>()
    val expenseGraphData: LiveData<Pair<List<ExpenseCategoryAndTheirTotalExpenseAmounts>, Double>> get() = _expenseGraphData

    fun getTotalExpenseAmountsWithTheirExpenseCategoryNames(paymentMethodKeys: List<String> = emptyList()) {

        viewModelScope.launch {

            val expenseCategoriesWithTheirAmounts =
                try {
                    if (paymentMethodKeys.isEmpty()) {
                        expenseRepository.getTotalExpenseAmountsWithTheirExpenseCategoryKeys()
                            .first()
                    } else {
                        getExpenseCategoriesAndTheirAmountsAfterFilteringByPaymentMethods(
                            paymentMethodKeys
                        )
                    }
                } catch (e: Exception) {
                    emptyList()
                }

            val totalAmount = if (expenseCategoriesWithTheirAmounts.isNotEmpty()) {
                try {
                    if (paymentMethodKeys.isEmpty()) {
                        expenseRepository.getTotalExpenseAmount().first()
                    } else {
                        expenseCategoriesWithTheirAmounts.sumOf { it.totalAmount }
                    }
                } catch (e: NullPointerException) {
                    0.0
                }
            } else {
                0.0
            }

            setValueForExpenseGraphDataPair(expenseCategoriesWithTheirAmounts, totalAmount)
        }
    }

    private suspend fun getExpenseCategoriesAndTheirAmountsAfterFilteringByPaymentMethods(
        paymentMethodKeys: List<String>
    ): List<ExpenseCategoryAndTheirTotalExpenseAmounts> {

        val expenses = expenseRepository.getAllExpenses().first()

        val filteredList =
            expenseRepository.applyExpenseFilterByPaymentMethods(paymentMethodKeys, expenses)

        return expenseRepository.getTotalExpenseAmountsWithTheirExpenseCategoryKeysByListOfExpenseKeys(
            filteredList.map { expense -> expense.key }
        ).first()
    }

    fun getTotalExpenseAmountsWithTheirExpenseCategoryNamesByDateRange(
        date1: Long,
        date2: Long,
        paymentMethodKeys: List<String> = emptyList()
    ) {

        viewModelScope.launch {
            val expenseCategoriesWithTheirAmounts =
                try {
                    if (paymentMethodKeys.isEmpty()) {
                        expenseRepository.getTotalExpenseAmountsWithTheirExpenseCategoryKeysByDateRange(
                            date1,
                            date2
                        ).first()
                    } else {
                        getTotalExpenseAmountsWithTheirExpenseCategoryKeysByDateRangeAndFilterByPaymentMethods(
                            date1,
                            date2,
                            paymentMethodKeys
                        )
                    }
                } catch (e: Exception) {
                    emptyList()
                }

            val totalAmount = if (expenseCategoriesWithTheirAmounts.isNotEmpty()) {
                try {
                    if (paymentMethodKeys.isEmpty()) {
                        expenseRepository.getTotalExpenseAmountByDateRange(
                            date1,
                            date2
                        ).first()
                    } else {
                        expenseCategoriesWithTheirAmounts.sumOf { it.totalAmount }
                    }
                } catch (e: java.lang.NullPointerException) {
                    0.0
                }
            } else {
                0.0
            }

            setValueForExpenseGraphDataPair(expenseCategoriesWithTheirAmounts, totalAmount)
        }
    }

    private suspend fun getTotalExpenseAmountsWithTheirExpenseCategoryKeysByDateRangeAndFilterByPaymentMethods(
        date1: Long,
        date2: Long,
        paymentMethodKeys: List<String>
    ): List<ExpenseCategoryAndTheirTotalExpenseAmounts> {

        val expenses = expenseRepository.getExpensesByDateRange(date1, date2).first()

        val filteredList =
            expenseRepository.applyExpenseFilterByPaymentMethods(paymentMethodKeys, expenses)

        return expenseRepository.getTotalExpenseAmountsWithTheirExpenseCategoryKeysByDateRangeAndByListOfExpenseKeys(
            date1, date2, filteredList.map { expense -> expense.key }
        ).first()
    }

    fun getTotalExpenseAmountWithTheirExpenseCategoryNamesForSelectedExpenseCategories(
        selectedExpenseCategoryKeys: List<String>
    ) {

        viewModelScope.launch {

            val expenseCategoriesWithTheirAmounts =
                try {
                    expenseRepository.getTotalExpenseAmountsWithTheirExpenseCategoryKeysForSelectedExpenseCategories(
                        selectedExpenseCategoryKeys
                    ).first()
                } catch (e: Exception) {
                    emptyList()
                }

            val totalAmount =
                try {
                    expenseRepository.getTotalExpenseByCategoryKeys(selectedExpenseCategoryKeys)
                        .first()
                } catch (e: java.lang.NullPointerException) {
                    0.0
                }

            setValueForExpenseGraphDataPair(expenseCategoriesWithTheirAmounts, totalAmount)
        }
    }

    fun getTotalExpenseAmountWithTheirExpenseCategoryNamesForSelectedExpenseCategoriesByDateRange(
        selectedExpenseCategoryKeys: List<String>,
        date1: Long,
        date2: Long
    ) {

        viewModelScope.launch {

            val expenseCategoriesWithTheirAmounts =
                try {
                    expenseRepository.getTotalExpenseAmountsWithTheirExpenseCategoryKeysForSelectedExpenseCategoriesByDateRange(
                        selectedExpenseCategoryKeys, date1, date2
                    ).first()
                } catch (e: Exception) {
                    emptyList()
                }

            val totalAmount =
                try {
                    expenseRepository.getTotalExpenseByCategoryKeysAndDateRange(
                        selectedExpenseCategoryKeys,
                        date1,
                        date2
                    ).first()
                } catch (e: NullPointerException) {
                    0.0
                }

            setValueForExpenseGraphDataPair(expenseCategoriesWithTheirAmounts, totalAmount)
        }
    }


    private fun setValueForExpenseGraphDataPair(
        expenseCategoriesWithTheirAmounts: List<ExpenseCategoryAndTheirTotalExpenseAmounts>,
        totalAmount: Double
    ) {

        val expenseGraphDataPair: Pair<List<ExpenseCategoryAndTheirTotalExpenseAmounts>, Double> =
            Pair(expenseCategoriesWithTheirAmounts, totalAmount)

        _expenseGraphData.value = expenseGraphDataPair
    }

    private val _expenseOfEachMonth = MutableLiveData<List<Pair<String, Double>>>()
    val expenseOfEachMonth: LiveData<List<Pair<String, Double>>> get() = _expenseOfEachMonth

    fun getExpensesOfAllMonthsOfYear(year: Int) {

        val context = getApplication<Application>().applicationContext

        viewModelScope.launch {

            val listOfExpensesInEachMonth = ArrayList<Pair<String, Double>>()
            val monthList = context.resources.getStringArray(R.array.months_short).asList()

            for (i in 1..12) {

                val calendar = Calendar.getInstance()
                calendar.set(year, i - 1, 2)

                val startAndEndDateInMillis =
                    WorkingWithDateAndTime.getMillisecondsOfStartAndEndDayOfMonth(
                        calendar.timeInMillis
                    )

                val amount = try {
                    expenseRepository.getTotalExpenseAmountByDateRange(
                        startAndEndDateInMillis.first,
                        startAndEndDateInMillis.second + Constants.ONE_DAY_MILLISECONDS
                    ).first()

                } catch (e: NullPointerException) {
                    e.printStackTrace()
                    0.0
                }

                listOfExpensesInEachMonth.add(Pair(monthList[i - 1], amount))
            }

            _expenseOfEachMonth.value = listOfExpensesInEachMonth
        }

    }

    fun getExpensesOfAllMonthsOfYearForSelectedCategory(
        year: Int,
        selectedExpenseCategoryKey: String
    ) {

        val context = getApplication<Application>().applicationContext

        viewModelScope.launch {

            val listOfExpensesInEachMonth = ArrayList<Pair<String, Double>>()
            val monthList = context.resources.getStringArray(R.array.months_short).asList()

            for (i in 1..12) {

                val calendar = Calendar.getInstance()
                calendar.set(year, i - 1, 2)

                val startAndEndDateInMillis =
                    WorkingWithDateAndTime.getMillisecondsOfStartAndEndDayOfMonth(
                        calendar.timeInMillis
                    )

                val amount = try {
                    expenseRepository.getTotalExpenseAmountByCategoryKeyAndDateRange(
                        selectedExpenseCategoryKey,
                        startAndEndDateInMillis.first,
                        startAndEndDateInMillis.second + Constants.ONE_DAY_MILLISECONDS
                    ).first()

                } catch (e: NullPointerException) {
                    e.printStackTrace()
                    0.0
                }

                listOfExpensesInEachMonth.add(Pair(monthList[i - 1], amount))
            }

            _expenseOfEachMonth.value = listOfExpensesInEachMonth
        }

    }

}