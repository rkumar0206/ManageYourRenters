package com.rohitthebest.manageyourrenters.ui.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.database.model.Budget
import com.rohitthebest.manageyourrenters.repositories.BudgetRepository
import com.rohitthebest.manageyourrenters.repositories.ExpenseCategoryRepository
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    app: Application,
    private val budgetRepository: BudgetRepository,
    private val expenseCategoryRepository: ExpenseCategoryRepository
) : AndroidViewModel(app) {

    fun insertBudget(budget: Budget) = viewModelScope.launch {
        budgetRepository.insertBudget(budget)
    }

    fun insertAllBudget(budgets: List<Budget>) = viewModelScope.launch {
        budgetRepository.insertAllBudget(budgets)
    }

    fun updateBudget(budget: Budget) = viewModelScope.launch {
        budgetRepository.updateBudget(budget)
    }

    fun deleteBudget(budget: Budget) = viewModelScope.launch {
        budgetRepository.deleteBudget(budget)
    }

    fun deleteAllBudgets() = viewModelScope.launch {
        budgetRepository.deleteAllBudgets()
    }

    private val _allBudgetList = MutableLiveData<List<Budget>>()
    val allBudgetList: LiveData<List<Budget>> get() = _allBudgetList

    fun getAllBudgetsByMonthAndYearString(monthAndYearString: String = WorkingWithDateAndTime.getCurrentMonthAndYearString()) {

        viewModelScope.launch {

            var allBudgets =
                budgetRepository.getAllBudgetsByMonthAndYear(monthAndYearString).first()
            //val allExpenseCategories = expenseCategoryRepository.getAllExpenseCategories().first()

            val allBudgetExpenseCategoryKeys = allBudgets.map { it.expenseCategoryKey }
            var expenseCategories =
                expenseCategoryRepository.getExpenseCategoriesByKey(allBudgetExpenseCategoryKeys)
                    .first()

            allBudgets = allBudgets.sortedBy { it.expenseCategoryKey }
            expenseCategories = expenseCategories.sortedBy { it.key }

            allBudgets.forEachIndexed { index, budget ->

                budget.categoryName = expenseCategories[index].categoryName
                budget.categoryImageUrl = expenseCategories[index].imageUrl ?: ""
            }
            _allBudgetList.value = allBudgets
        }
    }

    fun getTheOldestSavedBudgetYear() = budgetRepository.getTheOldestSavedBudgetYear().asLiveData()

    fun getBudgetByKey(budgetKey: String) = budgetRepository.getBudgetByKey(budgetKey).asLiveData()
}