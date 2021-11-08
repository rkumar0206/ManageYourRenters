package com.rohitthebest.manageyourrenters.ui.viewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.apiModels.ExpenseCategory
import com.rohitthebest.manageyourrenters.repositories.ExpenseCategoryRepository
import com.rohitthebest.manageyourrenters.repositories.ExpenseRepository
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.deleteFileFromFirebaseStorage
import com.rohitthebest.manageyourrenters.utils.expenseCategoryService
import com.rohitthebest.manageyourrenters.utils.isValid
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseCategoryViewModel @Inject constructor(
    private val expenseCategoryRepository: ExpenseCategoryRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    fun insertExpenseCategory(context: Context, expenseCategory: ExpenseCategory) =

        viewModelScope.launch {

            if (isInternetAvailable(context)) {

                expenseCategory.isSynced = true

                expenseCategoryService(
                    context,
                    expenseCategory,
                    context.getString(R.string.post)
                )
            } else {

                expenseCategory.isSynced = false
            }

            expenseCategoryRepository.insertExpenseCategory(expenseCategory)

            Functions.showToast(context, "Expense Category saved")
        }

    fun insertAllExpenseCategory(expenseCategories: List<ExpenseCategory>) = viewModelScope.launch {
        expenseCategoryRepository.insertAllExpenseCategory(expenseCategories)
    }

    fun updateExpenseCategory(context: Context, expenseCategory: ExpenseCategory) =
        viewModelScope.launch {

            if (isInternetAvailable(context)) {

                expenseCategory.isSynced = true

                expenseCategoryService(
                    context,
                    expenseCategory,
                    context.getString(R.string.put)
                )
            } else {

                expenseCategory.isSynced = false
            }

            expenseCategoryRepository.updateExpenseCategory(expenseCategory)
        }

    fun deleteExpenseCategory(context: Context, expenseCategory: ExpenseCategory) =
        viewModelScope.launch {

            if (isInternetAvailable(context)) {

                expenseCategoryService(
                    context,
                    expenseCategory,
                    context.getString(R.string.delete_one)
                )

                if (expenseCategory.imageUrl.isValid()) {

                    if (expenseCategory.imageUrl!!.contains("firebase")) {

                        deleteFileFromFirebaseStorage(
                            context,
                            expenseCategory.imageUrl!!
                        )
                    }
                }
            }

            expenseRepository.deleteExpenseByExpenseCategoryKey(expenseCategory.key)
            expenseCategoryRepository.deleteExpenseCategory(expenseCategory)
        }

    fun deleteAllExpenseCategories() = viewModelScope.launch {
        expenseCategoryRepository.deleteAllExpenseCategories()
    }

    fun getExpenseCategoryByKey(key: String) =
        expenseCategoryRepository.getExpenseCategoryByKey(key).asLiveData()

    fun getAllExpenseCategories() = expenseCategoryRepository.getAllExpenseCategories().asLiveData()

}