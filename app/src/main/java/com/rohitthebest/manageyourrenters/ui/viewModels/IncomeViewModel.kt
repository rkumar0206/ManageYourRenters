package com.rohitthebest.manageyourrenters.ui.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.database.model.Income
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants
import com.rohitthebest.manageyourrenters.repositories.IncomeRepository
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.compareObjects
import com.rohitthebest.manageyourrenters.utils.deleteDocumentFromFireStore
import com.rohitthebest.manageyourrenters.utils.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.updateDocumentOnFireStore
import com.rohitthebest.manageyourrenters.utils.uploadDocumentToFireStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncomeViewModel @Inject constructor(
    app: Application,
    private val repository: IncomeRepository
) : AndroidViewModel(app) {

    fun insertIncome(income: Income) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        if (context.isInternetAvailable()) {

            income.isSynced = true

            uploadDocumentToFireStore(
                context, FirestoreCollectionsConstants.INCOMES, income.key
            )
        } else {

            income.isSynced = false
        }

        repository.insertIncome(income)
    }

    fun insertAllIncome(incomes: List<Income>) = viewModelScope.launch {
        repository.insertAllIncome(incomes)
    }

    fun updateIncome(oldValue: Income, newValue: Income) = viewModelScope.launch {
        val context = getApplication<Application>().applicationContext

        if (Functions.isInternetAvailable(context)) {

            newValue.isSynced = true

            if (!oldValue.isSynced) {
                uploadDocumentToFireStore(
                    context,
                    FirestoreCollectionsConstants.INCOMES,
                    newValue.key
                )
            } else {

                val map = compareObjects(
                    oldData = oldValue,
                    newData = newValue,
                    notToCompareFields = listOf("modified", "isSynced")
                )

                if (map.isNotEmpty()) {

                    map["modified"] = newValue.modified

                    updateDocumentOnFireStore(
                        context,
                        map,
                        FirestoreCollectionsConstants.INCOMES,
                        oldValue.key
                    )
                }
            }
        } else {
            newValue.isSynced = false
        }

        repository.updateIncome(newValue)
    }

    fun deleteIncome(income: Income) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        deleteDocumentFromFireStore(
            context,
            FirestoreCollectionsConstants.INCOMES,
            income.key
        )

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