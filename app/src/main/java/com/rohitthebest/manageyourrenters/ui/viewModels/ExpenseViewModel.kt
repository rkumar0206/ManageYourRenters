package com.rohitthebest.manageyourrenters.ui.viewModels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.rohitthebest.manageyourrenters.data.filter.*
import com.rohitthebest.manageyourrenters.database.model.Expense
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EXPENSES
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.MONTHLY_PAYMENTS
import com.rohitthebest.manageyourrenters.repositories.ExpenseRepository
import com.rohitthebest.manageyourrenters.repositories.MonthlyPaymentRepository
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject

private const val TAG = "ExpenseViewModel"

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    app: Application,
    private val expenseRepository: ExpenseRepository,
    private val monthlyPaymentRepository: MonthlyPaymentRepository
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

    // issue #12
    fun updateAmountUsingExpenseKey(expenseKey: String, amount: Double) = viewModelScope.launch {

        try {

            val oldValue = expenseRepository.getExpenseByKey(expenseKey).first()

            if (oldValue != null) {

                val newValue = oldValue.copy()
                newValue.amount = amount
                updateExpense(oldValue, newValue)
            }

        } catch (e: Exception) {

            e.printStackTrace()
        }

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

        unlinkMonthlyPaymentIfAny(expense, context)

        expenseRepository.deleteExpense(expense)
        Functions.showToast(context, "Expense deleted")
    }

    // issue #12
    private suspend fun unlinkMonthlyPaymentIfAny(expense: Expense, context: Context) {

        try {

            val monthlyPayment =
                monthlyPaymentRepository.getMonthlyPaymentByKey(expense.key).first()

            monthlyPayment.expenseCategoryKey = ""

            if (isInternetAvailable(context)) {

                val map = HashMap<String, Any?>()
                map["expenseCategoryKey"] = ""

                updateDocumentOnFireStore(
                    context,
                    map,
                    MONTHLY_PAYMENTS,
                    monthlyPayment.key
                )

            }

            monthlyPaymentRepository.updateMonthlyPayment(monthlyPayment)
        } catch (e: java.lang.NullPointerException) {

            Log.d(
                TAG,
                "unlinkMonthlyPaymentIfAny: No monthly payment found with expense key : ${expense.key}"
            )
            e.printStackTrace()
        }

    }


    // issue #12
    fun deleteExpenseByKey(expenseKey: String) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        if (isInternetAvailable(context)) {

            deleteDocumentFromFireStore(
                context,
                EXPENSES,
                expenseKey
            )
        }

        expenseRepository.deleteExpenseByKey(expenseKey)
        Functions.showToast(context, "Expense deleted")
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

    // issue #78
    private val _expensesByPaymentMethods = MutableLiveData<List<Expense>>(emptyList())
    val expensesByPaymentMethods: LiveData<List<Expense>> get() = _expensesByPaymentMethods

    fun getExpenseByPaymentMethodsKey(paymentMethodKeys: List<String>) {

        viewModelScope.launch {

            val expenses = expenseRepository.getAllExpenses().first()

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
            _expensesByPaymentMethods.value = resultExpenses
        }
    }

    fun getExpenseByPaymentMethodsKey(paymentMethodKey: String) =
        expenseRepository.getExpensesByPaymentMethodKey(
            paymentMethodKey
        ).asLiveData()

    fun applyFilter(expenses: List<Expense>, expenseFilterDto: ExpenseFilterDto): List<Expense> {

        var mExpenses = expenses

        if (expenseFilterDto.isPaymentMethodEnabled) {

            mExpenses = applyFilterByPaymentMethods(expenseFilterDto.paymentMethods, mExpenses)
        }

        if (expenseFilterDto.isAmountEnabled) {

            mExpenses = mExpenses.filter { expense: Expense ->

                when (expenseFilterDto.selectedAmountFilter) {

                    IntFilterOptions.isLessThan -> expense.amount < expenseFilterDto.amount
                    IntFilterOptions.isGreaterThan -> expense.amount > expenseFilterDto.amount
                    IntFilterOptions.isEqualsTo -> expense.amount == expenseFilterDto.amount
                    IntFilterOptions.isBetween -> (expense.amount >= expenseFilterDto.amount) && (expense.amount <= expenseFilterDto.amount2)
                }
            }
        }

        if (expenseFilterDto.isSpentOnEnabled) {

            mExpenses = mExpenses.filter { expense: Expense ->

                when (expenseFilterDto.selectedSpentOnFilter) {

                    StringFilterOptions.startsWith -> expense.spentOn.startsWith(expenseFilterDto.spentOnText)
                    StringFilterOptions.endsWith -> expense.spentOn.endsWith(expenseFilterDto.spentOnText)
                    StringFilterOptions.containsWith -> expense.spentOn.contains(expenseFilterDto.spentOnText)

                    StringFilterOptions.regex -> {
                        val pattern: Pattern = Pattern.compile(expenseFilterDto.spentOnText)
                        val matcher = pattern.matcher(expense.spentOn)
                        matcher.find()
                    }
                }
            }
        }

        if (expenseFilterDto.isSortByEnabled) {

            val isDescending = expenseFilterDto.sortOrder == SortOrder.descending

            mExpenses = when (expenseFilterDto.sortByFilter) {

                SortFilter.amount -> {
                    if (isDescending) {
                        mExpenses.sortedByDescending { it.amount }
                    } else {
                        mExpenses.sortedBy { it.amount }
                    }
                }

                SortFilter.dateCreated -> {

                    if (isDescending) {
                        mExpenses.sortedByDescending { it.created }
                    } else {
                        mExpenses.sortedBy { it.created }
                    }
                }

                SortFilter.dateModified -> {

                    if (isDescending) {
                        mExpenses.sortedByDescending { it.modified }
                    } else {
                        mExpenses.sortedBy { it.modified }
                    }
                }
            }
        }

        return mExpenses
    }

    private fun applyFilterByPaymentMethods(
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