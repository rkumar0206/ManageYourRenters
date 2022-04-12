package com.rohitthebest.manageyourrenters.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPaymentCategory
import com.rohitthebest.manageyourrenters.repositories.MonthlyPaymentCategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MonthlyPaymentCategoryViewModel @Inject constructor(
    private val repository: MonthlyPaymentCategoryRepository
) : ViewModel() {

    fun insertMonthlyPaymentCategory(monthlyPaymentCategory: MonthlyPaymentCategory) =
        viewModelScope.launch {
            repository.insertMonthlyPaymentCategory(monthlyPaymentCategory)
        }

    fun insertAllMonthlyPaymentCategory(monthlyPaymentCategories: List<MonthlyPaymentCategory>) =
        viewModelScope.launch {
            repository.insertAllMonthlyPaymentCategory(monthlyPaymentCategories)
        }

    fun updateMonthlyPaymentCategory(monthlyPaymentCategory: MonthlyPaymentCategory) =
        viewModelScope.launch {
            repository.updateMonthlyPaymentCategory(monthlyPaymentCategory)
        }

    fun deleteMonthlyPaymentCategory(monthlyPaymentCategory: MonthlyPaymentCategory) =
        viewModelScope.launch {
            repository.deleteMonthlyPaymentCategory(monthlyPaymentCategory)
        }

    fun deleteAllMonthlyPaymentCategories() = viewModelScope.launch {
        repository.deleteAllMonthlyPaymentCategories()
    }

    fun deleteAllMonthlyPaymentCategoriesByIsSynced(isSynced: Boolean) = viewModelScope.launch {
        repository.deleteAllMonthlyPaymentCategoriesByIsSynced(isSynced)
    }

    fun getAllMonthlyPaymentCategories() = repository.getAllMonthlyPaymentCategories().asLiveData()

    fun getMonthlyPaymentCategoryUsingKey(key: String) =
        repository.getMonthlyPaymentCategoryUsingKey(key).asLiveData()
}