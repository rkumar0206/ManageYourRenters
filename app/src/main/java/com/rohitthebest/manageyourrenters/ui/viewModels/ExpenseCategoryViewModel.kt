package com.rohitthebest.manageyourrenters.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.database.model.apiModels.ExpenseCategory
import com.rohitthebest.manageyourrenters.repositories.ExpenseCategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseCategoryViewModel @Inject constructor(
    private val expenseCategoryRepository: ExpenseCategoryRepository
) : ViewModel() {

    fun insertExpenseCategory(expenseCategory: ExpenseCategory) = viewModelScope.launch {
        expenseCategoryRepository.insertExpenseCategory(expenseCategory)
    }

    fun insertAllExpenseCategory(expenseCategories: List<ExpenseCategory>) = viewModelScope.launch {
        expenseCategoryRepository.insertAllExpenseCategory(expenseCategories)
    }

    fun updateExpenseCategory(expenseCategory: ExpenseCategory) = viewModelScope.launch {
        expenseCategoryRepository.updateExpenseCategory(expenseCategory)
    }

    fun deleteExpenseCategory(expenseCategory: ExpenseCategory) = viewModelScope.launch {
        expenseCategoryRepository.deleteExpenseCategory(expenseCategory)
    }

    fun deleteAllExpenseCategories() = viewModelScope.launch {
        expenseCategoryRepository.deleteAllExpenseCategories()
    }

    fun getAllExpenseCategories() = expenseCategoryRepository.getAllExpenseCategories().asLiveData()

}