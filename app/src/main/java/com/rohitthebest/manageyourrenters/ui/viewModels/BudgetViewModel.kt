package com.rohitthebest.manageyourrenters.ui.viewModels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.database.model.Budget
import com.rohitthebest.manageyourrenters.database.model.Expense
import com.rohitthebest.manageyourrenters.database.model.ExpenseCategory
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.BUDGETS
import com.rohitthebest.manageyourrenters.repositories.BudgetRepository
import com.rohitthebest.manageyourrenters.repositories.ExpenseCategoryRepository
import com.rohitthebest.manageyourrenters.repositories.ExpenseRepository
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.compareObjects
import com.rohitthebest.manageyourrenters.utils.deleteDocumentFromFireStore
import com.rohitthebest.manageyourrenters.utils.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.updateDocumentOnFireStore
import com.rohitthebest.manageyourrenters.utils.uploadDocumentToFireStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "BudgetViewModel"

@HiltViewModel
class BudgetViewModel @Inject constructor(
    app: Application,
    private val budgetRepository: BudgetRepository,
    private val expenseCategoryRepository: ExpenseCategoryRepository,
    private val expenseRepository: ExpenseRepository
) : AndroidViewModel(app) {

    fun insertBudget(budget: Budget) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        if (context.isInternetAvailable()) {

            budget.isSynced = true

            uploadDocumentToFireStore(
                context, BUDGETS, budget.key
            )
        } else {

            budget.isSynced = false
        }

        budgetRepository.insertBudget(budget)
    }

    fun insertAllBudget(budgets: List<Budget>) = viewModelScope.launch {
        budgetRepository.insertAllBudget(budgets)
    }

    fun updateBudget(oldValue: Budget, newValue: Budget) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        if (Functions.isInternetAvailable(context)) {

            newValue.isSynced = true

            if (!oldValue.isSynced) {
                uploadDocumentToFireStore(
                    context,
                    BUDGETS,
                    newValue.key
                )
            } else {

                val map = compareObjects(
                    oldData = oldValue,
                    newData = newValue,
                    notToCompareFields = listOf("modified")
                )

                Log.d(TAG, "updateBudget: difference: $map")

                if (map.isNotEmpty()) {

                    map["modified"] = newValue.modified

                    updateDocumentOnFireStore(
                        context,
                        map,
                        BUDGETS,
                        oldValue.key
                    )
                }
            }
        } else {
            newValue.isSynced = false
        }

        budgetRepository.updateBudget(newValue)
    }

    fun deleteBudget(budget: Budget) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        deleteDocumentFromFireStore(
            context,
            BUDGETS,
            budget.key
        )

        budgetRepository.deleteBudget(budget)
    }

    fun deleteAllBudgets() = viewModelScope.launch {
        budgetRepository.deleteAllBudgets()
    }

    private val _allBudgetListByMonthAndYear = MutableLiveData<List<Budget>>()
    val allBudgetListByMonthAndYear: LiveData<List<Budget>> get() = _allBudgetListByMonthAndYear

    fun getAllBudgetsByMonthAndYear(
        month: Int = WorkingWithDateAndTime.getCurrentMonth(),
        year: Int = WorkingWithDateAndTime.getCurrentYear(),
        selectedPaymentMethods: List<String> = emptyList()
    ) {

        viewModelScope.launch {

            var allBudgets =
                budgetRepository.getAllBudgetsByMonthAndYear(month, year).first()

            val allBudgetExpenseCategoryKeys = allBudgets.map { it.expenseCategoryKey }
            var expenseCategories =
                expenseCategoryRepository.getExpenseCategoriesByKey(allBudgetExpenseCategoryKeys)
                    .first()

            val datePairForExpense =
                WorkingWithDateAndTime.getMillisecondsOfStartAndEndDayOfMonthForGivenMonthAndYear(
                    month, year
                )

            allBudgets = allBudgets.sortedBy { it.expenseCategoryKey }
            expenseCategories = expenseCategories.sortedBy { it.key }

            addOtherDetailsToBudget(
                allBudgets,
                expenseCategories,
                datePairForExpense,
                selectedPaymentMethods
            )

            _allBudgetListByMonthAndYear.value = allBudgets
        }
    }

    fun getAllExpenseCategoryOfBudgetsByMonthAndYear(
        month: Int = WorkingWithDateAndTime.getCurrentMonth(),
        year: Int = WorkingWithDateAndTime.getCurrentYear()
    ) = budgetRepository.getExpenseCategoryKeysOfAllBudgetsByMonthAndYear(month, year).asLiveData()


    private val _allExpenseCategoryAsBudgets = MutableLiveData<List<Budget>>()
    val allExpenseCategoryAsBudgets: LiveData<List<Budget>> get() = _allExpenseCategoryAsBudgets

    fun getAllExpenseCategoryAsBudget(month: Int, year: Int) {

        viewModelScope.launch {

            // get all expense categories
            val allExpenseCategories =
                ArrayList(expenseCategoryRepository.getAllExpenseCategories().first())

            // get all budgets for the selected month and year
            val allBudgetsAlreadyAddedByMonthAndYear =
                budgetRepository.getAllBudgetsByMonthAndYear(month, year).first()


            // get all the expense category key for which budget is added
            val budgetCategoryKeys =
                allBudgetsAlreadyAddedByMonthAndYear.map { it.expenseCategoryKey }

            val categoriesWithBudgetAlreadyAdded = allExpenseCategories.filter {
                budgetCategoryKeys.contains(it.key)
            }

            // remove all the expense categories for which budget is already added
            allExpenseCategories.removeAll(categoriesWithBudgetAlreadyAdded.toSet())

            val datePairForExpense =
                WorkingWithDateAndTime.getMillisecondsOfStartAndEndDayOfMonthForGivenMonthAndYear(
                    month, year
                )

            addOtherDetailsToBudget(
                allBudgetsAlreadyAddedByMonthAndYear.sortedBy { it.expenseCategoryKey },
                categoriesWithBudgetAlreadyAdded.sortedBy { it.key },
                datePairForExpense
            )

            val expenseCategoryAsBudget: ArrayList<Budget> =
                ArrayList(allBudgetsAlreadyAddedByMonthAndYear)

            allExpenseCategories.forEach { category ->

                val budget = Budget()
                budget.apply {
                    this.currentExpenseAmount = 0.0
                    this.budgetLimit = 0.0
                    this.month = month
                    this.year = year
                    this.expenseCategoryKey = category.key
                    this.monthYearString = this.generateMonthYearString()
                    this.categoryName = category.categoryName
                    this.categoryImageUrl = category.imageUrl ?: ""
                    this.modified = System.currentTimeMillis()
                }
                expenseCategoryAsBudget.add(budget)
            }

            _allExpenseCategoryAsBudgets.value =
                expenseCategoryAsBudget.sortedBy { it.modified }
        }
    }

    private suspend fun addOtherDetailsToBudget(
        allBudgets: List<Budget>,
        expenseCategories: List<ExpenseCategory>,
        datePairForExpense: Pair<Long, Long>,
        selectedPaymentMethods: List<String> = emptyList()
    ) {

        allBudgets.forEachIndexed { index, budget ->

            budget.categoryName = expenseCategories[index].categoryName
            budget.categoryImageUrl = expenseCategories[index].imageUrl ?: ""

            val currentExpense = try {
                if (selectedPaymentMethods.isEmpty()) {
                    expenseRepository.getTotalExpenseAmountByCategoryKeyAndDateRange(
                        expenseCategories[index].key,
                        datePairForExpense.first,
                        datePairForExpense.second + Constants.ONE_DAY_MILLISECONDS
                    ).first()
                } else {
                    val tempExpense = expenseRepository.getExpenseByDateRangeAndExpenseCategoryKey(
                        expenseCategories[index].key,
                        datePairForExpense.first,
                        datePairForExpense.second + Constants.ONE_DAY_MILLISECONDS
                    ).first()

                    val expenseAfterPaymentMethodFilter = applyExpenseFilterByPaymentMethods(
                        selectedPaymentMethods,
                        tempExpense
                    )

                    expenseAfterPaymentMethodFilter.sumOf { it.amount }
                }
            } catch (e: Exception) {
                0.0
            }

            Log.d(
                TAG,
                "addOtherDetailsToBudget: currentExpense (${budget.categoryName}): $currentExpense"
            )

            budget.currentExpenseAmount = currentExpense
        }

    }

    private fun applyExpenseFilterByPaymentMethods(
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


    fun getTheOldestSavedBudgetYear() = budgetRepository.getTheOldestSavedBudgetYear().asLiveData()

    fun getBudgetByKey(budgetKey: String) = budgetRepository.getBudgetByKey(budgetKey).asLiveData()

    fun getTotalBudgetByMonthAndYear(month: Int, year: Int) =
        budgetRepository.getTotalBudgetByMonthAndYear(month, year).asLiveData()
}