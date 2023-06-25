package com.rohitthebest.manageyourrenters.ui.viewModels

import android.app.Application
import android.content.Context
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rohitthebest.manageyourrenters.database.model.ExpenseCategory
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.BUDGETS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EXPENSES
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EXPENSE_CATEGORIES
import com.rohitthebest.manageyourrenters.repositories.BudgetRepository
import com.rohitthebest.manageyourrenters.repositories.ExpenseCategoryRepository
import com.rohitthebest.manageyourrenters.repositories.ExpenseRepository
import com.rohitthebest.manageyourrenters.repositories.MonthlyPaymentRepository
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.compareExpenseCategoryModel
import com.rohitthebest.manageyourrenters.utils.convertStringListToJSON
import com.rohitthebest.manageyourrenters.utils.deleteAllDocumentsUsingKeyFromFirestore
import com.rohitthebest.manageyourrenters.utils.deleteDocumentFromFireStore
import com.rohitthebest.manageyourrenters.utils.deleteFileFromFirebaseStorage
import com.rohitthebest.manageyourrenters.utils.isValid
import com.rohitthebest.manageyourrenters.utils.updateDocumentOnFireStore
import com.rohitthebest.manageyourrenters.utils.uploadDocumentToFireStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.set

private const val TAG = "ExpenseCategoryViewMode"

@HiltViewModel
class ExpenseCategoryViewModel @Inject constructor(
    app: Application,
    private val expenseCategoryRepository: ExpenseCategoryRepository,
    private val expenseRepository: ExpenseRepository,
    private val budgetRepository: BudgetRepository,
    private val monthlyPaymentRepository: MonthlyPaymentRepository,
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

    fun updateExpenseCategory(
        oldValue: ExpenseCategory,
        newValue: ExpenseCategory
    ) =

        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext

            if (isInternetAvailable(context)) {

                newValue.isSynced = true

                if (!oldValue.isSynced) {

                    uploadDocumentToFireStore(
                        context,
                        EXPENSE_CATEGORIES,
                        newValue.key
                    )
                } else {
                    val map = compareExpenseCategoryModel(oldValue, newValue)
                    if (map.isNotEmpty()) {
                        updateDocumentOnFireStore(
                            context,
                            map,
                            EXPENSE_CATEGORIES,
                            newValue.key
                        )
                    }
                }

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
                val budgetKeys = budgetRepository.getKeysByExpenseCategoryKey(expenseCategory.key)

                unlinkMonthlyPaymentsIfAny(context, expenseKeys)

                if (expenseKeys.isNotEmpty()) {

                    deleteAllDocumentsUsingKeyFromFirestore(
                        context,
                        EXPENSES,
                        convertStringListToJSON(expenseKeys)
                    )
                }

                if (budgetKeys.isNotEmpty()) {

                    deleteAllDocumentsUsingKeyFromFirestore(
                        context,
                        BUDGETS,
                        convertStringListToJSON(budgetKeys)
                    )
                }

            }

            expenseRepository.deleteExpenseByExpenseCategoryKey(expenseCategory.key)
            budgetRepository.deleteBudgetsByExpenseCategoryKey(expenseCategory.key)
            expenseCategoryRepository.deleteExpenseCategory(expenseCategory)
        }

    // issue #12
    private suspend fun unlinkMonthlyPaymentsIfAny(context: Context, expenseKeys: List<String>) {

        expenseKeys.forEach { key ->

            try {

                val monthlyPayment =
                    monthlyPaymentRepository.getMonthlyPaymentByKey(key).first()

                monthlyPayment.expenseCategoryKey = ""

                if (isInternetAvailable(context)) {

                    val map = HashMap<String, Any?>()
                    map["expenseCategoryKey"] = ""

                    updateDocumentOnFireStore(
                        context,
                        map,
                        FirestoreCollectionsConstants.MONTHLY_PAYMENTS,
                        monthlyPayment.key
                    )

                }

                monthlyPaymentRepository.updateMonthlyPayment(monthlyPayment)
            } catch (e: Exception) {

                Log.d(
                    TAG,
                    "unlinkMonthlyPaymentIfAny: No monthly payment found with expense key : $key"
                )
                e.printStackTrace()
            }

        }

    }

    fun deleteAllExpenseCategories() = viewModelScope.launch {

        expenseCategoryRepository.deleteAllExpenseCategories()
        expenseRepository.deleteAllExpenses()
    }

    fun getExpenseCategoryByKey(key: String) =
        expenseCategoryRepository.getExpenseCategoryByKey(key).asLiveData()

    fun getAllExpenseCategories() = expenseCategoryRepository.getAllExpenseCategories().asLiveData()

}