package com.rohitthebest.manageyourrenters.ui.viewModels

import android.app.Application
import android.os.Parcelable
import androidx.lifecycle.*
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.*
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.database.model.RenterPayment
import com.rohitthebest.manageyourrenters.others.FirestoreCollectionsConstants.RENTER_PAYMENTS
import com.rohitthebest.manageyourrenters.repositories.RenterPaymentRepository
import com.rohitthebest.manageyourrenters.repositories.RenterRepository
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "RenterPaymentViewModel"

@HiltViewModel
class RenterPaymentViewModel @Inject constructor(
    app: Application,
    private val paymentRepository: RenterPaymentRepository,
    private val renterRepository: RenterRepository,
    private val state: SavedStateHandle
) : AndroidViewModel(app) {

    // ------------------------- UI related ----------------------------

    companion object {

        private const val RENTER_PAYMENT_RV_KEY = "jbkjbajacjhbgaaagyqvgvdqv"
    }

    fun saveRenterPaymentRvState(rvState: Parcelable?) {

        state.set(RENTER_PAYMENT_RV_KEY, rvState)
    }

    private val _renterPaymentRvState: MutableLiveData<Parcelable> = state.getLiveData(
        RENTER_PAYMENT_RV_KEY
    )

    val renterPaymentRvState: LiveData<Parcelable> get() = _renterPaymentRvState

    // ---------------------------------------------------------------

    fun insertPayment(
        renterPayment: RenterPayment,
        supportingDocumentHelperModel: SupportingDocumentHelperModel? = null
    ) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        updateRenterDuesOrAdvance(renterPayment, renterPayment.renterKey)

        if (isInternetAvailable(context)) {

            renterPayment.isSynced = true
            uploadDocumentToFireStore(
                context,
                RENTER_PAYMENTS,
                renterPayment.key
            )

            if (supportingDocumentHelperModel != null && supportingDocumentHelperModel.documentType != DocumentType.URL) {

                supportingDocumentHelperModel.modelName =
                    RENTER_PAYMENTS

                uploadFileToFirebaseCloudStorage(
                    context,
                    supportingDocumentHelperModel,
                    renterPayment.key
                )
            }

        } else {

            renterPayment.isSynced = false
        }

        paymentRepository.insertRenterPayment(renterPayment)
    }

    private fun updateRenterDuesOrAdvance(
        renterPayment: RenterPayment?,
        renterKey: String
    ) =

        viewModelScope.launch {

            val context = getApplication<Application>().applicationContext

            val renter: Renter = renterRepository.getRenterByKey(renterKey).first()

            var amountPaid = 0.0
            var netDemand = 0.0

            if (renterPayment != null) {

                amountPaid = renterPayment.amountPaid
                netDemand = renterPayment.netDemand
            }

            renter.modified = System.currentTimeMillis()
            renter.dueOrAdvanceAmount = amountPaid - netDemand

            if (isInternetAvailable(context)) {

                if (renter.isSynced == context.getString(R.string.f)) {

                    // upload to firestore
                    renter.isSynced = context.getString(R.string.t)

                    uploadDocumentToFireStore(
                        context,
                        context.getString(R.string.renters),
                        renter.key!!
                    )

                } else {

                    // update on firestore

                    val map = HashMap<String, Any?>()

                    map["modified"] = renter.modified
                    map["dueOrAdvanceAmount"] = renter.dueOrAdvanceAmount

                    updateDocumentOnFireStore(
                        context,
                        map,
                        context.getString(R.string.renters),
                        renter.key!!
                    )

                }
            } else {

                renter.isSynced = context.getString(R.string.f)
            }

            renterRepository.updateRenter(renter)
        }


    fun updatePayment(
        oldRenterPayment: RenterPayment,
        renterPayment: RenterPayment
    ) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        if (isInternetAvailable(context)) {

            renterPayment.isSynced = true

            if (!oldRenterPayment.isSynced) {

                uploadDocumentToFireStore(
                    context,
                    RENTER_PAYMENTS,
                    renterPayment.key
                )
            } else {

                val map = compareRenterPaymentModel(oldRenterPayment, renterPayment)
                if (map.isNotEmpty()) {
                    updateDocumentOnFireStore(
                        context,
                        map,
                        RENTER_PAYMENTS,
                        renterPayment.key
                    )
                }
            }
        } else {

            renterPayment.isSynced = false
        }

        paymentRepository.updateRenterPayment(renterPayment)
    }

    fun deletePayment(renterPayment: RenterPayment) = viewModelScope.launch {

        val context = getApplication<Application>().applicationContext

        val renterKey = renterPayment.renterKey

        if (isInternetAvailable(context)) {

            deleteDocumentFromFireStore(
                context,
                RENTER_PAYMENTS,
                renterPayment.key
            )

            if (renterPayment.isSupportingDocAdded
                && renterPayment.supportingDocument != null
                && renterPayment.supportingDocument?.documentType != DocumentType.URL
            ) {

                deleteFileFromFirebaseStorage(
                    context,
                    renterPayment.supportingDocument?.documentUrl!!
                )
            }

        }

        paymentRepository.deleteRenterPayment(renterPayment)

        // update renter's due or advance from last payment after deleting this payment
        val lastPayment = paymentRepository.getLastRenterPayment(renterKey).first()

        updateRenterDuesOrAdvance(lastPayment, renterKey)
    }

    fun addOrReplaceBorrowerSupportingDocument(
        renterPayment: RenterPayment,
        supportDocumentHelper: SupportingDocumentHelperModel
    ) {
        val context = getApplication<Application>().applicationContext

        val oldRenterPayment = renterPayment.copy()

        if (oldRenterPayment.supportingDocument != null && oldRenterPayment.supportingDocument?.documentType != DocumentType.URL) {

            // if borrower payment contains supporting document previously, then call delete service also

            deleteFileFromFirebaseStorage(
                context,
                renterPayment.supportingDocument?.documentUrl!!
            )
        }

        renterPayment.modified = System.currentTimeMillis()
        if (supportDocumentHelper.documentType == DocumentType.URL) {

            val supportingDoc = SupportingDocument(
                supportDocumentHelper.documentName,
                supportDocumentHelper.documentUrl,
                supportDocumentHelper.documentType
            )

            renterPayment.isSupportingDocAdded = true
            renterPayment.supportingDocument = supportingDoc

            updatePayment(oldRenterPayment, renterPayment)
        } else {

            supportDocumentHelper.modelName = RENTER_PAYMENTS

            if (!oldRenterPayment.isSynced) {
                insertPayment(renterPayment, supportDocumentHelper)
                return
            }
            uploadFileToFirebaseCloudStorage(
                context, supportDocumentHelper, renterPayment.key
            )
        }
    }

    fun getAllPaymentsListOfRenter(renterKey: String) =
        paymentRepository.getAllPaymentsListOfRenter(renterKey).asLiveData()

    fun getPaymentByPaymentKey(paymentKey: String) =
        paymentRepository.getPaymentByPaymentKey(paymentKey).asLiveData()

    fun getLastRenterPayment(renterKey: String) =
        paymentRepository.getLastRenterPayment(renterKey).asLiveData()

    fun getTotalRevenueOfAllTime() = paymentRepository.getTotalRevenueOfAllTime().asLiveData()

    // for date type, not checking the if the date is in between two dates
    // as user may add payment for any date
    // so here checking if record with exact date is present in db or not
    suspend fun validateByDateField(
        renterKey: String,
        renterBillDateType: RenterBillDateType
    ): Boolean {

        return withContext(Dispatchers.IO) {

            val renterDateTypeList = paymentRepository.getAllPaymentsListOfRenter(renterKey)
                .first()
                .filter { it.billPeriodInfo.billPeriodType == BillPeriodType.BY_DATE }
                .map { it.billPeriodInfo.renterBillDateType }

            val recordExists = renterDateTypeList
                .any {

                    val fromBillDateDb =
                        WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                            it?.fromBillDate, "dd-MM-yyyy"
                        )

                    val toBillDateDb =
                        WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                            it?.toBillDate, "dd-MM-yyyy"
                        )

                    val fromBillDateArg =
                        WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                            renterBillDateType.fromBillDate, "dd-MM-yyyy"
                        )

                    val toBillDateArg =
                        WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                            renterBillDateType.toBillDate, "dd-MM-yyyy"
                        )

                    fromBillDateDb == fromBillDateArg
                            && toBillDateDb == toBillDateArg
                }

            // if recordExists = true (record already present with selected date)
            !recordExists
        }
    }

    // for month type, checking if any month is coming between two previously added month range
    suspend fun validateByMonthField(
        renterKey: String,
        renterBillMonthType: RenterBillMonthType
    ): Boolean {

        return withContext(Dispatchers.IO) {

            val renterMonthTypeList = paymentRepository.getAllPaymentsListOfRenter(renterKey)
                .first()
                .filter { it.billPeriodInfo.billPeriodType == BillPeriodType.BY_MONTH }
                .map { it.billPeriodInfo.renterBillMonthType }

            val recordExists = renterMonthTypeList.any {

                val dbFrom = WorkingWithDateAndTime.getTimeInMillisForYearMonthAndDate(
                    Triple(it!!.forBillYear, it.forBillMonth, 1)
                )

                val dbTo = WorkingWithDateAndTime.getTimeInMillisForYearMonthAndDate(
                    Triple(it.toBillYear, it.toBillMonth, 1)
                )

                val argFrom = WorkingWithDateAndTime.getTimeInMillisForYearMonthAndDate(
                    Triple(renterBillMonthType.forBillYear, renterBillMonthType.forBillMonth, 1)
                )

                val argTo = WorkingWithDateAndTime.getTimeInMillisForYearMonthAndDate(
                    Triple(renterBillMonthType.toBillYear, renterBillMonthType.toBillMonth, 1)
                )

                (argFrom in dbFrom..dbTo) || (argTo in dbFrom..dbTo)
            }

            // if record exists then then passed range is not valid
            !recordExists
        }
    }
}