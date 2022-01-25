package com.rohitthebest.manageyourrenters.ui.viewModels

import android.content.Context
import android.os.Parcelable
import androidx.lifecycle.*
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.apiModels.ExpenseCategory
import com.rohitthebest.manageyourrenters.repositories.ExpenseCategoryRepository
import com.rohitthebest.manageyourrenters.repositories.ExpenseRepository
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.deleteFileFromFirebaseStorage
import com.rohitthebest.manageyourrenters.utils.expenseCategoryServiceHelper
import com.rohitthebest.manageyourrenters.utils.isValid
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseCategoryViewModel @Inject constructor(
    private val expenseCategoryRepository: ExpenseCategoryRepository,
    private val expenseRepository: ExpenseRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    // ------------------------- UI related ----------------------------

    companion object {

        private const val EXPENSE_CATEGORY_RV_KEY = "dcnjsncjkajakbaba_dcnjdn"
    }

    fun saveExpenseCategoryRvState(rvState: Parcelable?) {

        state.set(EXPENSE_CATEGORY_RV_KEY, rvState)
    }

    private val _expenseCategoryRvState: MutableLiveData<Parcelable> = state.getLiveData(
        EXPENSE_CATEGORY_RV_KEY
    )

    val expenseCategoryRvState: LiveData<Parcelable> get() = _expenseCategoryRvState

    // ---------------------------------------------------------------


    // ------------------------- Database related ---------------------------
    fun insertExpenseCategory(context: Context, expenseCategory: ExpenseCategory) =

        viewModelScope.launch {

            if (isInternetAvailable(context)) {

                expenseCategory.isSynced = true

                // this method invokes the expense category foreground service to upload the category
                expenseCategoryServiceHelper(
                    context,
                    expenseCategory.key,
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

    fun updateExpenseCategory(
        context: Context,
        expenseCategory: ExpenseCategory,
        shouldUpload: Boolean = true
    ) =

        viewModelScope.launch {

            if (isInternetAvailable(context) && shouldUpload) {

                expenseCategory.isSynced = true

                expenseCategoryServiceHelper(
                    context,
                    expenseCategory.key,
                    context.getString(R.string.put)
                )
            } else {

                if (shouldUpload) {
                    expenseCategory.isSynced = false
                }
            }

            expenseCategoryRepository.updateExpenseCategory(expenseCategory)
        }

    fun deleteExpenseCategory(context: Context, expenseCategory: ExpenseCategory) =
        viewModelScope.launch {

            if (isInternetAvailable(context)) {

                expenseCategoryServiceHelper(
                    context,
                    expenseCategory.key,
                    context.getString(R.string.delete_one)
                )

                // check if the image is saved to the firebase storage, if found, delete
                if (expenseCategory.imageUrl.isValid()) {

                    if (expenseCategory.imageUrl!!.contains("firebase")) {

                        deleteFileFromFirebaseStorage(
                            context,
                            expenseCategory.imageUrl!!
                        )
                    }
                }
            }

            expenseCategoryRepository.deleteExpenseCategory(expenseCategory)
        }

    fun deleteAllExpenseCategories() = viewModelScope.launch {

        expenseCategoryRepository.deleteAllExpenseCategories()
        expenseRepository.deleteAllExpenses()
    }

    fun getExpenseCategoryByKey(key: String) =
        expenseCategoryRepository.getExpenseCategoryByKey(key).asLiveData()

    fun getAllExpenseCategories() = expenseCategoryRepository.getAllExpenseCategories().asLiveData()

}