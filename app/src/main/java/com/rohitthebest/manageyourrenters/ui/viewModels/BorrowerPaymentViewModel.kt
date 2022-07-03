package com.rohitthebest.manageyourrenters.ui.viewModels

import android.app.Application
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.*
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.DocumentType
import com.rohitthebest.manageyourrenters.data.InterestCalculatorFields
import com.rohitthebest.manageyourrenters.data.SupportingDocument
import com.rohitthebest.manageyourrenters.data.SupportingDocumentHelperModel
import com.rohitthebest.manageyourrenters.database.model.BorrowerPayment
import com.rohitthebest.manageyourrenters.repositories.BorrowerPaymentRepository
import com.rohitthebest.manageyourrenters.repositories.PartialPaymentRepository
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "BorrowerPaymentViewMode"

@HiltViewModel
class BorrowerPaymentViewModel @Inject constructor(
    app: Application,
    private val borrowerPaymentRepository: BorrowerPaymentRepository,
    private val partialPaymentRepository: PartialPaymentRepository,
    private val state: SavedStateHandle
) : AndroidViewModel(app) {

    // ------------------------- UI related ----------------------------

    companion object {

        private const val BORROWER_PAYMENT_RV_KEY = "wwrvnjssabbbiopjdn"
    }

    fun saveBorrowerPaymentRvState(rvState: Parcelable?) {

        state.set(BORROWER_PAYMENT_RV_KEY, rvState)
    }

    private val _borrowerPaymentRvState: MutableLiveData<Parcelable> = state.getLiveData(
        BORROWER_PAYMENT_RV_KEY
    )

    val borrowerPaymentRvState: LiveData<Parcelable> get() = _borrowerPaymentRvState

    // ---------------------------------------------------------------


    fun insertBorrowerPayment(
        borrowerPayment: BorrowerPayment,
        supportingDocumentHelperModel: SupportingDocumentHelperModel? = null
    ) =
        viewModelScope.launch {

            val context = getApplication<Application>().applicationContext

            if (isInternetAvailable(context)) {

                borrowerPayment.isSynced = true

                uploadDocumentToFireStore(
                    context,
                    context.getString(R.string.borrowerPayments),
                    borrowerPayment.key
                )

                if (supportingDocumentHelperModel != null && supportingDocumentHelperModel.documentType != DocumentType.URL
                ) {
                    supportingDocumentHelperModel.modelName =
                        context.getString(R.string.borrowerPayments)
                    uploadFileToFirebaseCloudStorage(
                        context, supportingDocumentHelperModel, borrowerPayment.key
                    )
                }

            } else {

                borrowerPayment.isSynced = false
            }

            borrowerPaymentRepository.insertBorrowerPayment(borrowerPayment)
        }

    fun insertBorrowerPayments(borrowerPayments: List<BorrowerPayment>) = viewModelScope.launch {
        borrowerPaymentRepository.insertAllBorrowerPayment(borrowerPayments)
    }

    fun updateBorrowerPayment(
        oldBorrowerPayment: BorrowerPayment,
        borrowerPayment: BorrowerPayment
    ) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        if (isInternetAvailable(context)) {

            borrowerPayment.isSynced = true

            if (!oldBorrowerPayment.isSynced) {

                uploadDocumentToFireStore(
                    context,
                    context.getString(R.string.borrowerPayments),
                    borrowerPayment.key
                )
            } else {

                val map =
                    compareBorrowerPaymentModel(oldBorrowerPayment, borrowerPayment)

                if (map.isNotEmpty()) {

                    updateDocumentOnFireStore(
                        context,
                        map,
                        context.getString(R.string.borrowerPayments),
                        borrowerPayment.key
                    )
                }
            }
        } else {
            borrowerPayment.isSynced = false
        }

        borrowerPaymentRepository.updateBorrowerPayment(borrowerPayment)
        getPaymentsByBorrowerKey(borrowerPayment.borrowerKey)
    }

    fun deleteBorrowerPayment(borrowerPayment: BorrowerPayment) =
        viewModelScope.launch {

            val context = getApplication<Application>().applicationContext

            val partialPaymentKeys =
                partialPaymentRepository.getKeysByBorrowerPaymentKey(borrowerPayment.key)

            Log.d(TAG, "deleteBorrowerPayment: Partial payment keys : $partialPaymentKeys")

            if (isInternetAvailable(context)) {

                deleteDocumentFromFireStore(
                    context,
                    context.getString(R.string.borrowerPayments),
                    borrowerPayment.key
                )

                if (borrowerPayment.isSupportingDocAdded) {

                    if (borrowerPayment.supportingDocument?.documentType != DocumentType.URL) {

                        deleteFileFromFirebaseStorage(
                            context,
                            borrowerPayment.supportingDocument?.documentUrl!!
                        )
                    }
                }

                if (partialPaymentKeys.isNotEmpty()) {

                    deleteAllDocumentsUsingKeyFromFirestore(
                        context,
                        context.getString(R.string.partialPayments),
                        convertStringListToJSON(partialPaymentKeys)
                    )
                }
            }

            val borrowerKey = borrowerPayment.borrowerKey
            partialPaymentRepository.deleteAllPartialPaymentByBorrowerPaymentKey(borrowerPayment.key)
            borrowerPaymentRepository.deleteBorrowerPayment(borrowerPayment)

            getPaymentsByBorrowerKey(borrowerKey)
        }


    fun addOrReplaceBorrowerSupportingDocument(
        borrowerPayment: BorrowerPayment,
        supportDocumentHelper: SupportingDocumentHelperModel
    ) {
        val context = getApplication<Application>().applicationContext

        val oldBorrowerPayment = borrowerPayment.copy()

        if (oldBorrowerPayment.supportingDocument != null && oldBorrowerPayment.supportingDocument?.documentType != DocumentType.URL) {

            // if borrower payment contains supporting document previously, then call delete service also

            deleteFileFromFirebaseStorage(
                context,
                borrowerPayment.supportingDocument?.documentUrl!!
            )
        }

        borrowerPayment.modified = System.currentTimeMillis()
        if (supportDocumentHelper.documentType == DocumentType.URL) {

            val supportingDoc = SupportingDocument(
                supportDocumentHelper.documentName,
                supportDocumentHelper.documentUrl,
                supportDocumentHelper.documentType
            )

            borrowerPayment.isSupportingDocAdded = true
            borrowerPayment.supportingDocument = supportingDoc

            updateBorrowerPayment(oldBorrowerPayment, borrowerPayment)
        } else {

            supportDocumentHelper.modelName = context.getString(R.string.borrowerPayments)

            if (!oldBorrowerPayment.isSynced) {
                insertBorrowerPayment(borrowerPayment, supportDocumentHelper)
                return
            }
            uploadFileToFirebaseCloudStorage(
                context, supportDocumentHelper, borrowerPayment.key
            )
        }
    }

    fun deleteAllBorrowerPayments() = viewModelScope.launch {
        borrowerPaymentRepository.deleteAllBorrowerPayments()
    }

    fun deleteAllBorrowerPaymentsByBorrowerKey(borrowerKey: String) = viewModelScope.launch {

        borrowerPaymentRepository.deleteAllBorrowerPaymentsByBorrowerKey(borrowerKey)
    }

    fun deleteBorrowerPaymentsByIsSynced(isSynced: Boolean) = viewModelScope.launch {

        borrowerPaymentRepository.deleteBorrowerPaymentsByIsSynced(isSynced)
    }

    private val _allPaymentsListOfBorrower = MutableStateFlow<List<BorrowerPayment>>(emptyList())

    val allPaymentsListOfBorrower = _allPaymentsListOfBorrower.asStateFlow()

    fun getPaymentsByBorrowerKey(borrowerKey: String) {

        viewModelScope.launch {

            val allPayments =
                borrowerPaymentRepository.getPaymentsByBorrowerKey(borrowerKey).first()

            allPayments.forEach { payment ->

                val partialPayments =
                    partialPaymentRepository.getPartialPaymentByBorrowerPaymentKey(payment.key)
                        .first()

                payment.totalAmountPaid =
                    partialPayments.fold(0.0) { acc, partialPayment -> acc + partialPayment.amount }

                if (payment.isDueCleared) {

                    payment.dueLeftAmount = 0.0
                } else {

                    payment.dueLeftAmount = payment.amountTakenOnRent - payment.totalAmountPaid
                }

                if (payment.isInterestAdded && payment.interest != null && !payment.isDueCleared) {

                    val interestAndAmount = Functions.calculateInterestAndAmount(
                        InterestCalculatorFields(
                            0L, payment.amountTakenOnRent, payment.interest!!,
                            Functions.calculateNumberOfDays(
                                payment.created,
                                System.currentTimeMillis()
                            )
                        )
                    )

                    payment.totalInterestTillNow = interestAndAmount.first
                    payment.dueLeftAmount += interestAndAmount.first
                }
            }

            _allPaymentsListOfBorrower.value = allPayments
        }
    }

    //fun getPaymentsByBorrowerKey(borrowerKey: String)  = borrowerPaymentRepository.getPaymentsByBorrowerKey(borrowerKey).asLiveData()

    fun getTotalDueOfTheBorrower(borrowerKey: String) =
        borrowerPaymentRepository.getTotalDueOfTheBorrower(borrowerKey).asLiveData()

    fun getBorrowerPaymentByKey(paymentKey: String) =
        borrowerPaymentRepository.getBorrowerPaymentByKey(paymentKey).asLiveData()

}