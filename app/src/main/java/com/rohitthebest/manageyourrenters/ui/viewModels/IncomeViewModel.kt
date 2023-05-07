package com.rohitthebest.manageyourrenters.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.database.model.Income
import com.rohitthebest.manageyourrenters.repositories.IncomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncomeViewModel @Inject constructor(
    private val repository: IncomeRepository
) : ViewModel() {

    fun insertIncome(income: Income) = viewModelScope.launch {
        repository.insertIncome(income)
    }

    fun insertAllIncome(incomes: List<Income>) = viewModelScope.launch {
        repository.insertAllIncome(incomes)
    }

    fun updateIncome(income: Income) = viewModelScope.launch {
        repository.updateIncome(income)
    }

    fun deleteIncome(income: Income) = viewModelScope.launch {
        repository.deleteIncome(income)
    }

    fun deleteAllIncomes() = viewModelScope.launch {
        repository.deleteAllIncomes()
    }

    fun getAllIncomes() = repository.getAllIncomes().asLiveData()

    fun getIncomeByKey(incomeKey: String) =
        repository.getIncomeByKey(incomeKey = incomeKey).asLiveData()
}