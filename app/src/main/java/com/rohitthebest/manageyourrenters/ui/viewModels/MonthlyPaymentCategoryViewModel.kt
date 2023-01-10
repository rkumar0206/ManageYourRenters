package com.rohitthebest.manageyourrenters.ui.viewModels

import android.app.Application
import android.os.Parcelable
import androidx.lifecycle.*
import com.rohitthebest.manageyourrenters.database.model.MonthlyPaymentCategory
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.EXPENSES
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.MONTHLY_PAYMENTS
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.MONTHLY_PAYMENT_CATEGORIES
import com.rohitthebest.manageyourrenters.repositories.ExpenseRepository
import com.rohitthebest.manageyourrenters.repositories.MonthlyPaymentCategoryRepository
import com.rohitthebest.manageyourrenters.repositories.MonthlyPaymentRepository
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MonthlyPaymentCategoryViewModel"

@HiltViewModel
class MonthlyPaymentCategoryViewModel @Inject constructor(
    app: Application,
    private val repository: MonthlyPaymentCategoryRepository,
    private val monthlyPaymentRepository: MonthlyPaymentRepository,
    private val expenseRepository: ExpenseRepository,
    private val state: SavedStateHandle
) : AndroidViewModel(app) {

    // ------------------------- UI related ----------------------------

    companion object {

        private const val MONTHLY_PAYMENT_CATEGORY_RV_KEY = "dccabjbdfknjsbhve_d14fn"
    }

    fun saveMonthlyPaymentCategoryRvState(rvState: Parcelable?) {

        state.set(MONTHLY_PAYMENT_CATEGORY_RV_KEY, rvState)
    }

    private val _monthlyPaymentCategoryRvState: MutableLiveData<Parcelable> = state.getLiveData(
        MONTHLY_PAYMENT_CATEGORY_RV_KEY
    )

    val monthlyPaymentCategoryRvState: LiveData<Parcelable> get() = _monthlyPaymentCategoryRvState

    // ---------------------------------------------------------------


    fun insertMonthlyPaymentCategory(monthlyPaymentCategory: MonthlyPaymentCategory) =
        viewModelScope.launch {

            val context = getApplication<Application>().applicationContext

            if (isInternetAvailable(context)) {

                monthlyPaymentCategory.isSynced = true

                uploadDocumentToFireStore(
                    context,
                    MONTHLY_PAYMENT_CATEGORIES,
                    monthlyPaymentCategory.key
                )

            } else {

            monthlyPaymentCategory.isSynced = false
        }

        repository.insertMonthlyPaymentCategory(monthlyPaymentCategory)

        Functions.showToast(context, "Monthly Payment Category saved")
    }

    fun insertAllMonthlyPaymentCategory(monthlyPaymentCategories: List<MonthlyPaymentCategory>) =
        viewModelScope.launch {
            repository.insertAllMonthlyPaymentCategory(monthlyPaymentCategories)
        }

    fun updateMonthlyPaymentCategory(
        oldValue: MonthlyPaymentCategory,
        newValue: MonthlyPaymentCategory
    ) =
        viewModelScope.launch {

            val context = getApplication<Application>().applicationContext

            if (isInternetAvailable(context)) {

                newValue.isSynced = true

                updateDocumentOnFireStore(
                    context,
                    compareMonthlyPaymentCategoryModel(oldValue, newValue),
                    MONTHLY_PAYMENT_CATEGORIES,
                    oldValue.key
                )
            } else {

                newValue.isSynced = false
            }

            repository.updateMonthlyPaymentCategory(newValue)
        }

    fun deleteMonthlyPaymentCategory(
        monthlyPaymentCategory: MonthlyPaymentCategory
    ) =
        viewModelScope.launch {

            val context = getApplication<Application>().applicationContext

            val monthlyPaymentKeys =
                monthlyPaymentRepository.getKeysByMonthlyPaymentCategoryKey(
                    monthlyPaymentCategory.key
                )

            if (isInternetAvailable(context)) {

                deleteDocumentFromFireStore(
                    context,
                    MONTHLY_PAYMENT_CATEGORIES,
                    monthlyPaymentCategory.key
                )

                // check if the image is saved to the firebase storage, if found, delete
                if (monthlyPaymentCategory.imageUrl.isValid()) {

                    if (monthlyPaymentCategory.imageUrl.contains("firebase")) {

                        deleteFileFromFirebaseStorage(
                            context,
                            monthlyPaymentCategory.imageUrl
                        )
                    }
                }


                if (monthlyPaymentKeys.isNotEmpty()) {

                    deleteAllDocumentsUsingKeyFromFirestore(
                        context,
                        MONTHLY_PAYMENTS,
                        convertStringListToJSON(monthlyPaymentKeys)
                    )

                    // issue #12
                    delay(50)
                    deleteAllDocumentsUsingKeyFromFirestore(
                        context,
                        EXPENSES,
                        convertStringListToJSON(monthlyPaymentKeys)
                    )
                }
            }

            if (monthlyPaymentKeys.isNotEmpty()) {
                // issue #12
                expenseRepository.deleteExpenseByListOfKeys(monthlyPaymentKeys)
            }
            monthlyPaymentRepository.deleteAllMonthlyPaymentsByCategoryKey(monthlyPaymentCategory.key)
            repository.deleteMonthlyPaymentCategory(monthlyPaymentCategory)
        }

    fun deleteAllMonthlyPaymentCategories() = viewModelScope.launch {
        monthlyPaymentRepository.deleteAllMonthlyPayments()
        repository.deleteAllMonthlyPaymentCategories()
    }

    fun deleteAllMonthlyPaymentCategoriesByIsSynced(isSynced: Boolean) = viewModelScope.launch {
        repository.deleteAllMonthlyPaymentCategoriesByIsSynced(isSynced)
    }

    fun getAllMonthlyPaymentCategories() = repository.getAllMonthlyPaymentCategories().asLiveData()

    fun getMonthlyPaymentCategoryUsingKey(key: String) =
        repository.getMonthlyPaymentCategoryUsingKey(key).asLiveData()
}