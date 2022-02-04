package com.rohitthebest.manageyourrenters.ui.viewModels

import android.content.Context
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.*
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.repositories.RenterPaymentRepository
import com.rohitthebest.manageyourrenters.repositories.RenterRepository
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.convertStringListToJSON
import com.rohitthebest.manageyourrenters.utils.deleteAllDocumentsUsingKeyFromFirestore
import com.rohitthebest.manageyourrenters.utils.deleteDocumentFromFireStore
import com.rohitthebest.manageyourrenters.utils.uploadDocumentToFireStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "RenterViewModel"

@HiltViewModel
class RenterViewModel @Inject constructor(
    private val repo: RenterRepository,
    private val paymentRepository: RenterPaymentRepository,
    private val state: SavedStateHandle
) : ViewModel() {

    // ------------------------- UI related ----------------------------

    companion object {

        private const val RENTER_RV_KEY = "cabcjnsnksbkajavjsnjdn"
    }

    fun saveRenterRvState(rvState: Parcelable?) {

        state.set(RENTER_RV_KEY, rvState)
    }

    private val _renterRvState: MutableLiveData<Parcelable> = state.getLiveData(
        RENTER_RV_KEY
    )

    val renterRvState: LiveData<Parcelable> get() = _renterRvState

    // ---------------------------------------------------------------

    fun insertRenter(context: Context, renter: Renter) = viewModelScope.launch {

        if (isInternetAvailable(context)) {

            renter.isSynced = context.getString(R.string.t)

            uploadDocumentToFireStore(
                context,
                context.getString(R.string.renters),
                renter.key!!
            )
        } else {

            renter.isSynced = context.getString(R.string.f)
        }

        repo.insertRenter(renter)
    }


    fun updateRenter(context: Context, renter: Renter) = viewModelScope.launch {

        if (isInternetAvailable(context)) {

            renter.isSynced = context.getString(R.string.t)

            uploadDocumentToFireStore(
                context,
                context.getString(R.string.renters),
                renter.key!!
            )
        } else {

            renter.isSynced = context.getString(R.string.f)
        }

        repo.updateRenter(renter)

    }

    fun insertRenters(renters: List<Renter>) = viewModelScope.launch {

        repo.insertRenters(renters)
    }

    fun deleteRenter(context: Context, renter: Renter) = viewModelScope.launch {

        val paymentKeys = paymentRepository.getPaymentKeysByRenterKey(renter.key!!)

        Log.d(TAG, "deleteRenter: PaymentsKeys : $paymentKeys")

        if (isInternetAvailable(context)) {

            deleteDocumentFromFireStore(
                context,
                context.getString(R.string.renters),
                renter.key!!
            )

            if (paymentKeys.isNotEmpty()) {

                deleteAllDocumentsUsingKeyFromFirestore(
                    context,
                    context.getString(R.string.payments),
                    convertStringListToJSON(paymentKeys)
                )
            }
        }

        repo.deleteRenter(renter)
        paymentRepository.deleteAllPaymentsOfRenter(renterKey = renter.key!!)
    }

    fun deleteAllRenter() = viewModelScope.launch {

        repo.deleteAllRenter()
        paymentRepository.deleteAllRenterPayments()
    }

    fun deleteRenterByIsSynced(isSynced: String) = viewModelScope.launch {

        repo.deleteRenterByIsSynced(isSynced)
    }

    fun getAllRentersList() = repo.getAllRentersList().asLiveData()

    fun getRenterCount() = repo.getRenterCount().asLiveData()

    fun getRenterByIsSynced(isSynced: String) = repo.getRenterByIsSynced(isSynced).asLiveData()

    fun getRenterByKey(renterKey: String) = repo.getRenterByKey(renterKey).asLiveData()
}