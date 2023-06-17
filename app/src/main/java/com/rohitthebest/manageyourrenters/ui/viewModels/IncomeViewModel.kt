package com.rohitthebest.manageyourrenters.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.database.model.Income
import com.rohitthebest.manageyourrenters.others.Constants
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

    fun updateIncome(oldIncome: Income, updatedIncome: Income) = viewModelScope.launch {
        repository.updateIncome(updatedIncome)
    }

    fun deleteIncome(income: Income) = viewModelScope.launch {
        repository.deleteIncome(income)
    }

    fun deleteAllIncomes() = viewModelScope.launch {
        repository.deleteAllIncomes()
    }

    fun getAllIncomes() = repository.getAllIncomes().asLiveData()

    fun getAllIncomesByMonthAndYear(month: Int, year: Int) = repository.getAllIncomesByMonthAndYear(
        month, year
    ).asLiveData()

    fun getIncomeByKey(incomeKey: String) =
        repository.getIncomeByKey(incomeKey = incomeKey).asLiveData()

    fun getTotalIncomeAddedByMonthAndYear(month: Int, year: Int) =
        repository.getTotalIncomeAddedByMonthAndYear(month, year).asLiveData()

    fun getAllIncomeSources() = repository.getAllIncomeSources().asLiveData()

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