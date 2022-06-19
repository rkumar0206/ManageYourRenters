package com.rohitthebest.manageyourrenters.ui.viewModels

import android.app.Application
import android.os.Parcelable
import androidx.lifecycle.*
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.DocumentType
import com.rohitthebest.manageyourrenters.data.SupportingDocument
import com.rohitthebest.manageyourrenters.data.SupportingDocumentHelperModel
import com.rohitthebest.manageyourrenters.database.model.DeletedRenter
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.database.model.RenterPayment
import com.rohitthebest.manageyourrenters.repositories.DeletedRenterRepository
import com.rohitthebest.manageyourrenters.repositories.RenterPaymentRepository
import com.rohitthebest.manageyourrenters.repositories.RenterRepository
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "RenterViewModel"

@HiltViewModel
class RenterViewModel @Inject constructor(
    app: Application,
    private val repo: RenterRepository,
    private val paymentRepository: RenterPaymentRepository,
    private val deletedRenterRepository: DeletedRenterRepository,
    private val state: SavedStateHandle
) : AndroidViewModel(app) {

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

    fun insertRenter(
        renter: Renter,
        supportDocumentHelper: SupportingDocumentHelperModel? = null
    ) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        if (isInternetAvailable(context)) {

            renter.isSynced = context.getString(R.string.t)

            uploadDocumentToFireStore(
                context,
                context.getString(R.string.renters),
                renter.key!!
            )

            if (supportDocumentHelper != null
                && supportDocumentHelper.documentType != DocumentType.URL
            ) {

                supportDocumentHelper.modelName = context.getString(R.string.renters)
                uploadFileToFirebaseCloudStorage(
                    context, supportDocumentHelper, renter.key!!
                )
            }
        } else {

            renter.isSynced = context.getString(R.string.f)
        }

        repo.insertRenter(renter)
    }


    fun updateRenter(renter: Renter) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

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

    fun addOrReplaceBorrowerSupportingDocument(
        renter: Renter,
        supportDocumentHelper: SupportingDocumentHelperModel
    ) {

        val context = getApplication<Application>().applicationContext

        if (renter.supportingDocument != null && renter.supportingDocument?.documentType != DocumentType.URL) {

            // if borrower contains supporting document previously, then call delete service also

            deleteFileFromFirebaseStorage(
                context,
                renter.supportingDocument?.documentUrl!!
            )
        }

        if (supportDocumentHelper.documentType == DocumentType.URL) {

            val supportingDoc = SupportingDocument(
                supportDocumentHelper.documentName,
                supportDocumentHelper.documentUrl,
                supportDocumentHelper.documentType
            )

            renter.isSupportingDocAdded = true
            renter.supportingDocument = supportingDoc

            updateRenter(renter)
        } else {

            supportDocumentHelper.modelName = context.getString(R.string.renters)
            if (renter.isSynced != context.getString(R.string.t)) {
                insertRenter(renter, supportDocumentHelper)
                return
            }

            uploadFileToFirebaseCloudStorage(
                context, supportDocumentHelper, renter.key!!
            )
        }
    }


    fun deleteRenter(renter: Renter) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        if (isInternetAvailable(context)) {

            deleteDocumentFromFireStore(
                context,
                context.getString(R.string.renters),
                renter.key!!
            )

            if (renter.supportingDocument != null
                && renter.supportingDocument?.documentType != DocumentType.URL
            ) {

                deleteFileFromFirebaseStorage(
                    context,
                    renter.supportingDocument?.documentUrl!!
                )
            }

            val keysAndSupportingDocs =
                paymentRepository.getPaymentKeysAndSupportingDocumentByRenterKey(renter.key!!)

            val keys = keysAndSupportingDocs.map { it.key }
            val supportingDocument = keysAndSupportingDocs.map { it.supportingDocument }
                .filter { it != null && it.documentType != DocumentType.URL }

            supportingDocument.forEach { supportingDoc ->

                supportingDoc?.let { deleteFileFromFirebaseStorage(context, it.documentUrl) }
            }

            if (keysAndSupportingDocs.isNotEmpty()) {

                deleteAllDocumentsUsingKeyFromFirestore(
                    context,
                    context.getString(R.string.renter_payments),
                    convertStringListToJSON(keys)
                )
            }
        }

        // ---------- save the renter and last payment info to deleted renter database --------

        try {

            val lastPaymentInfo = paymentRepository.getLastRenterPayment(renter.key!!).first()

            val paymentHistory = HashMap<Long, Double>()

            paymentRepository.getAllPaymentsListOfRenter(renter.key!!).first()
                .forEach { renterPayment ->

                    paymentHistory[renterPayment.created] = renterPayment.amountPaid
                }

            saveToDeletedRenterTable(renter, lastPaymentInfo, paymentHistory)

        } catch (e: NullPointerException) {

            e.printStackTrace()
        }

        // --------------------------------------------------------------------------------------

        delay(100)

        repo.deleteRenter(renter)
        paymentRepository.deleteAllPaymentsOfRenter(renterKey = renter.key!!)
    }

    private suspend fun saveToDeletedRenterTable(
        renter: Renter,
        lastPaymentInfo: RenterPayment?,
        paymentHistory: Map<Long, Double>?
    ) {

        val deletedRenter = DeletedRenter(
            renter.key!!,
            System.currentTimeMillis(),
            renter,
            lastPaymentInfo ?: RenterPayment(),
            paymentHistory ?: emptyMap()
        )

        deletedRenterRepository.insertDeletedRenter(deletedRenter)
    }

    fun deleteAllRenter() = viewModelScope.launch {

        repo.deleteAllRenter()
        paymentRepository.deleteAllRenterPayments()
        deletedRenterRepository.deleteAllDeletedRenters()
    }

    fun deleteRenterByIsSynced(isSynced: String) = viewModelScope.launch {

        repo.deleteRenterByIsSynced(isSynced)
    }

    fun getAllRentersList() = repo.getAllRentersList().asLiveData()

    fun getRenterCount() = repo.getRenterCount().asLiveData()

    fun getRenterByIsSynced(isSynced: String) = repo.getRenterByIsSynced(isSynced).asLiveData()

    fun getRenterByKey(renterKey: String) = repo.getRenterByKey(renterKey).asLiveData()

    private val _renterNameWithTheirAmountPaid = MutableLiveData<Map<String, List<Double>>>()

    val renterNameWithTheirAmountPaid: LiveData<Map<String, List<Double>>> get() = _renterNameWithTheirAmountPaid

    fun getRentersWithTheirAmountPaid() {

        viewModelScope.launch {

            val nameWithTheirAmountPaid = repo.getRentersWithTheirAmountPaid().first()

            val map = HashMap<String, List<Double>>()

            map.putAll(nameWithTheirAmountPaid)

            val deletedRenters = deletedRenterRepository.getAllDeletedRenters().first()

            if (deletedRenters.isNotEmpty()) {

                deletedRenters.forEach { deletedRenter ->

                    val listOfAmountPaid = deletedRenter.paymentHistory.values.toList()

                    map[deletedRenter.renterInfo.name] = listOfAmountPaid
                }

            }

            _renterNameWithTheirAmountPaid.value = map
        }
    }

    fun getRentersWithTheirAmountPaidByDateCreated(
        startDate: Long,
        endDate: Long
    ) {

        viewModelScope.launch {

            val nameWithTheirAmountPaid =
                repo.getRentersWithTheirAmountPaidByDateCreated(startDate, endDate).first()

            val map = HashMap<String, List<Double>>()

            map.putAll(nameWithTheirAmountPaid)

            deletedRenterRepository.getAllDeletedRenters().first()
                .forEach { deletedRenter ->

                    val paymentsBetweenStartAndEndDate =
                        deletedRenter.paymentHistory.filterKeys { date ->

                            date in startDate..endDate
                        }

                    if (paymentsBetweenStartAndEndDate.isNotEmpty()) {

                        val listOfAmountPaid = paymentsBetweenStartAndEndDate.values.toList()

                        map[deletedRenter.renterInfo.name] = listOfAmountPaid
                    }

            }

            _renterNameWithTheirAmountPaid.value = map
        }
    }
}