package com.rohitthebest.manageyourrenters.ui.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.rohitthebest.manageyourrenters.database.model.Expense
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EXPENSES
import com.rohitthebest.manageyourrenters.repositories.ExpenseRepository
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

private const val TAG = "ExpenseViewModel"

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    app: Application,
    private val expenseRepository: ExpenseRepository
) : AndroidViewModel(app) {

    fun insertExpense(expense: Expense) =
        viewModelScope.launch {

            val context = getApplication<Application>().applicationContext

            if (isInternetAvailable(context)) {

                uploadDocumentToFireStore(
                    context,
                    EXPENSES,
                    expense.key
                )

            } else {

                expense.isSynced = false
            }

            expenseRepository.insertExpense(expense)

            Functions.showToast(context, "Expense saved")

        }

    fun insertAllExpense(expenses: List<Expense>) = viewModelScope.launch {
        expenseRepository.insertAllExpense(expenses)
    }

    fun updateExpense(oldValue: Expense, newValue: Expense) =
        viewModelScope.launch {

            val context = getApplication<Application>().applicationContext

            if (isInternetAvailable(context)) {

                updateDocumentOnFireStore(
                    context,
                    compareExpenseModel(oldValue, newValue),
                    EXPENSES,
                    oldValue.key
                )

            } else {

                newValue.isSynced = false
            }

            expenseRepository.updateExpense(newValue)

            Functions.showToast(context, "Expense updated")
        }

    fun deleteExpense(expense: Expense) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        if (isInternetAvailable(context)) {

            deleteDocumentFromFireStore(
                context,
                EXPENSES,
                expense.key
            )
        }

        expenseRepository.deleteExpense(expense)
        Functions.showToast(context, "Expense deleted")
    }

    fun deleteAllExpenses() = viewModelScope.launch {
        expenseRepository.deleteAllExpenses()
    }

    fun deleteAllExpensesByIsSynced(isSynced: Boolean) = viewModelScope.launch {
        expenseRepository.deleteExpenseByIsSynced(isSynced)
    }

    fun getAllExpenses() = expenseRepository.getAllExpenses().asLiveData()

    fun getAllSpentOn() = expenseRepository.getAllSpentOn().asLiveData()

    fun getExpenseAmountSumByExpenseCategoryKey(expenseCategoryKey: String) =
        expenseRepository.getExpenseAmountSumByExpenseCategoryKey(expenseCategoryKey)

    fun getExpenseAmountSumByExpenseCategoryByDateRange(
        expenseCategoryKey: String, date1: Long, date2: Long
    ) = expenseRepository.getExpenseAmountSumByExpenseCategoryByDateRange(
        expenseCategoryKey, date1, date2
    )

    fun getTotalExpenseAmountByExpenseCategory(expenseCategoryKey: String) =
        expenseRepository.getTotalExpenseAmountByExpenseCategory(expenseCategoryKey).asLiveData()

    fun getTotalExpenseAmount() = expenseRepository.getTotalExpenseAmount().asLiveData()

    fun getTotalExpenseAmountByDateRange(date1: Long, date2: Long) =
        expenseRepository.getTotalExpenseAmountByDateRange(date1, date2).asLiveData()

    fun getTotalExpenseAmountByCategoryKeyAndDateRange(
        expenseCategoryKey: String,
        date1: Long,
        date2: Long
    ) =
        expenseRepository.getTotalExpenseAmountByCategoryKeyAndDateRange(
            expenseCategoryKey,
            date1,
            date2
        ).asLiveData()

    fun getExpensesByDateRange(date1: Long, date2: Long) =
        expenseRepository.getExpensesByDateRange(date1, date2).asLiveData()

    fun getExpenseByDateRangeAndExpenseCategoryKey(
        expenseCategoryKey: String, date1: Long, date2: Long
    ) = expenseRepository.getExpenseByDateRangeAndExpenseCategoryKey(
        expenseCategoryKey,
        date1,
        date2
    ).asLiveData()

    fun getExpenseByKey(expenseKey: String) =
        expenseRepository.getExpenseByKey(expenseKey).asLiveData()

    fun getExpensesByExpenseCategoryKey(expenseCategoryKey: String) =
        expenseRepository.getExpensesByExpenseCategoryKey(expenseCategoryKey).asLiveData()

    private val _expenseOfEachMonth = MutableLiveData<List<Double>>(emptyList())
    val expenseOfEachMonth: LiveData<List<Double>> get() = _expenseOfEachMonth

    fun getExpensesOfAllMonthsOfYear(year: Int) {

        viewModelScope.launch {

            val calendars = ArrayList<Calendar>()

            val listOfExpensesInEachMonth = ArrayList<Double>()

            for (i in 1..12) {

                calendars.add(Calendar.getInstance())
                calendars[i - 1].set(year, i - 1, 2)

                val startAndEndDateInMillis =
                    WorkingWithDateAndTime.getMillisecondsOfStartAndEndDayOfMonth(
                        calendars[i - 1].timeInMillis
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

                listOfExpensesInEachMonth.add(amount)

                Log.d(
                    TAG,
                    "getExpensesOfAllMonthsOfYear: Year : $year, Month : ${
                        calendars[i - 1].get(Calendar.MONTH)
                    }, Amount : $amount"
                )
            }

            _expenseOfEachMonth.value = listOfExpensesInEachMonth
        }

    }

}