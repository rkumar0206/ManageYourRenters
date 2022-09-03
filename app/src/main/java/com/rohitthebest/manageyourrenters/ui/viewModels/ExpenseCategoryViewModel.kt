package com.rohitthebest.manageyourrenters.ui.viewModels

import android.app.Application
import android.os.Parcelable
import androidx.lifecycle.*
import com.rohitthebest.manageyourrenters.database.model.apiModels.ExpenseCategory
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EXPENSE
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EXPENSE_CATEGORIES
import com.rohitthebest.manageyourrenters.repositories.ExpenseCategoryRepository
import com.rohitthebest.manageyourrenters.repositories.ExpenseRepository
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseCategoryViewModel @Inject constructor(
    app: Application,
    private val expenseCategoryRepository: ExpenseCategoryRepository,
    private val expenseRepository: ExpenseRepository,
    private val state: SavedStateHandle
) : AndroidViewModel(app) {

    // ------------------------- UI related ----------------------------

    companion object {

        private const val EXPENSE_CATEGORY_RV_KEY = "dcnjsncjkajakbaba_dcnjdn"
    }

    fun saveExpenseCategoryRvState(rvState: Parcelable?) {

        state[EXPENSE_CATEGORY_RV_KEY] = rvState
    }

    private val _expenseCategoryRvState: MutableLiveData<Parcelable> = state.getLiveData(
        EXPENSE_CATEGORY_RV_KEY
    )

    val expenseCategoryRvState: LiveData<Parcelable> get() = _expenseCategoryRvState

    // ---------------------------------------------------------------


    // ------------------------- Database related ---------------------------
    fun insertExpenseCategory(expenseCategory: ExpenseCategory) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        if (isInternetAvailable(context)) {

            expenseCategory.isSynced = true

            uploadDocumentToFireStore(
                context,
                EXPENSE_CATEGORIES,
                expenseCategory.key
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
        oldValue: ExpenseCategory,
        newValue: ExpenseCategory
    ) =

        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext

            if (isInternetAvailable(context)) {

                newValue.isSynced = true

                updateDocumentOnFireStore(
                    context,
                    compareExpenseCategoryModel(oldValue, newValue),
                    EXPENSE_CATEGORIES,
                    newValue.key
                )
            } else {

                newValue.isSynced = false
            }

            expenseCategoryRepository.updateExpenseCategory(newValue)
        }

    fun deleteExpenseCategory(expenseCategory: ExpenseCategory) =
        viewModelScope.launch {

            val context = getApplication<Application>().applicationContext

            if (isInternetAvailable(context)) {

                deleteDocumentFromFireStore(
                    context,
                    EXPENSE_CATEGORIES,
                    expenseCategory.key
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

                val expenseKeys = expenseRepository.getKeysByExpenseCategoryKey(expenseCategory.key)

                if (expenseKeys.isNotEmpty()) {

                    deleteAllDocumentsUsingKeyFromFirestore(
                        context,
                        EXPENSE,
                        convertStringListToJSON(expenseKeys)
                    )
                }

            }

            expenseRepository.deleteExpenseByExpenseCategoryKey(expenseCategory.key)
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