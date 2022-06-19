package com.rohitthebest.manageyourrenters.ui.viewModels

import android.app.Application
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.*
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPaymentCategory
import com.rohitthebest.manageyourrenters.repositories.MonthlyPaymentCategoryRepository
import com.rohitthebest.manageyourrenters.repositories.MonthlyPaymentRepository
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.deleteFileFromFirebaseStorage
import com.rohitthebest.manageyourrenters.utils.isValid
import com.rohitthebest.manageyourrenters.utils.monthlyPaymentCategoryServiceHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "MonthlyPaymentCategoryViewModel"

@HiltViewModel
class MonthlyPaymentCategoryViewModel @Inject constructor(
    app: Application,
    private val repository: MonthlyPaymentCategoryRepository,
    private val monthlyPaymentRepository: MonthlyPaymentRepository,
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

                Log.d(TAG, "insertMonthlyPaymentCategory: ")

                monthlyPaymentCategoryServiceHelper(
                    context,
                    monthlyPaymentCategory.key,
                    context.getString(R.string.post)
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
        monthlyPaymentCategory: MonthlyPaymentCategory,
        shouldUpload: Boolean = true
    ) =
        viewModelScope.launch {

            val context = getApplication<Application>().applicationContext

            if (isInternetAvailable(context) && shouldUpload) {

                monthlyPaymentCategory.isSynced = true

                monthlyPaymentCategoryServiceHelper(
                    context,
                    monthlyPaymentCategory.key,
                    context.getString(R.string.put)
                )
            } else {

                if (shouldUpload) {

                    monthlyPaymentCategory.isSynced = false
                }

            }

            repository.updateMonthlyPaymentCategory(monthlyPaymentCategory)
        }

    fun deleteMonthlyPaymentCategory(
        monthlyPaymentCategory: MonthlyPaymentCategory
    ) =
        viewModelScope.launch {

            val context = getApplication<Application>().applicationContext

            if (isInternetAvailable(context)) {

                monthlyPaymentCategoryServiceHelper(
                    context,
                    monthlyPaymentCategory.key,
                    context.getString(R.string.delete_one)
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
            }

            repository.deleteMonthlyPaymentCategory(monthlyPaymentCategory)
        }

    fun deleteAllMonthlyPaymentCategories() = viewModelScope.launch {
        monthlyPaymentRepository.deleteAllMonthlyPayments()
        repository.deleteAllMonthlyPaymentCategories()
    }

    fun deleteAllMonthlyPaymentCategoriesByIsSynced(isSynced: Boolean) = viewModelScope.launch {
        monthlyPaymentRepository.deleteAllMonthlyPaymentByIsSynced(isSynced)
        repository.deleteAllMonthlyPaymentCategoriesByIsSynced(isSynced)
    }

    fun getAllMonthlyPaymentCategories() = repository.getAllMonthlyPaymentCategories().asLiveData()

    fun getMonthlyPaymentCategoryUsingKey(key: String) =
        repository.getMonthlyPaymentCategoryUsingKey(key).asLiveData()
}