package com.rohitthebest.manageyourrenters.ui.fragments.houseRenters.addContentFragments

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.BillPeriodType
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.database.model.RenterPayment
import com.rohitthebest.manageyourrenters.databinding.ShowBillLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.ShowRenterPaymentBillBottomsheetDialogBinding
import com.rohitthebest.manageyourrenters.others.Constants.RENTER_PAYMENT_CONFIRMATION_BILL_KEY
import com.rohitthebest.manageyourrenters.ui.viewModels.RenterViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

private const val TAG = "ShowBillForConfirmation"

@AndroidEntryPoint
class ShowBillForConfirmationBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: ShowRenterPaymentBillBottomsheetDialogBinding? = null
    private val binding get() = _binding!!
    private lateinit var includeBinding: ShowBillLayoutBinding

    private val renterViewModel by viewModels<RenterViewModel>()

    private var mListener: OnPaymentConfirmationBottomSheetDismissListener? = null
    private var isSaveBtnClicked = false

    private lateinit var receivedPayment: RenterPayment
    private lateinit var receivedRenter: Renter

    private var monthList: List<String> = emptyList()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(
            R.layout.show_renter_payment_bill_bottomsheet_dialog,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = ShowRenterPaymentBillBottomsheetDialogBinding.bind(view)
        includeBinding = binding.include
        monthList = resources.getStringArray(R.array.months).toList()

        initListeners()
        getMessage()
    }

    private fun getMessage() {

        try {

            arguments?.let { args ->

                if (!args.isEmpty) {

                    receivedPayment = args.getString(RENTER_PAYMENT_CONFIRMATION_BILL_KEY)
                        ?.convertJsonToObject(RenterPayment::class.java)!!

                    renterViewModel.getRenterByKey(receivedPayment.renterKey)
                        .observe(viewLifecycleOwner) {
                            receivedRenter = it
                            initializeUI()
                        }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            showToast(requireContext(), getString(R.string.something_went_wrong))
            dismiss()
        }
    }

    private fun initializeUI() {

        initializeValuesToBill()
    }

    private fun initializeValuesToBill() {

        setRenterInfo()

        setBillingParameters()

        setElectricFields()

        setDuesOrAdvanceOfLastPayment()

        setExtraFields()

        setDuesOrAdvance()

        setTotalRent()

        includeBinding.paymentMessageTV.text = if (receivedPayment.note.isValid()) {

            receivedPayment.note
        } else {
            "No message"
        }
    }

    //[Start of setting fields in bills textViews]

    private fun setRenterInfo() {

        //renter info
        includeBinding.showBillRenterName.text = receivedRenter.name
        includeBinding.showBillRenterMobile.text = receivedRenter.mobileNumber
        includeBinding.showBillRenterAddress.text = receivedRenter.address

    }

    private fun setBillingParameters() {

        //billing parameter
        includeBinding.showBillBillDate
            .setDateInTextView(receivedPayment.created)
        includeBinding.showBillBillTime
            .setDateInTextView(receivedPayment.created, "hh:mm a")
        includeBinding.showBillBillPeriod.text =
            if (receivedPayment.billPeriodInfo.billPeriodType == BillPeriodType.BY_MONTH) {

                "${monthList[receivedPayment.billPeriodInfo.renterBillMonthType?.forBillMonth!! - 1]}, ${receivedPayment.billPeriodInfo.billYear}"
            } else {

                "${WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(receivedPayment.billPeriodInfo.renterBillDateType?.fromBillDate)}" +
                        " to ${
                            WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                                receivedPayment.billPeriodInfo.renterBillDateType?.toBillDate
                            )
                        }"
            }
    }

    private fun setElectricFields() {

        if (receivedPayment.isElectricityBillIncluded) {

            //electricity
            includeBinding.showBillPreviousReading.text =
                "${
                    String.format(
                        "%.2f",
                        receivedPayment.electricityBillInfo?.previousReading
                    )
                } unit(s)"
            includeBinding.showBillCurrentReading.text =
                "${
                    String.format(
                        "%.2f",
                        receivedPayment.electricityBillInfo?.currentReading
                    )
                } unit(s)"
            includeBinding.showBillRate.text =
                "${String.format("%.2f", receivedPayment.electricityBillInfo?.rate)} per/unit"
            includeBinding.showBillDifference.text =
                "${
                    String.format(
                        "%.2f",
                        receivedPayment.electricityBillInfo?.differenceInReading
                    )
                } unit(s)"
            includeBinding.showBillElectricityTotal.text =
                "${receivedPayment.currencySymbol} ${receivedPayment.electricityBillInfo?.totalElectricBill}"

        }
    }

    private fun setExtraFields() {

        //Extra
        includeBinding.showBillExtraFieldName.text = "Extra"

        if (receivedPayment.extras != null) {

            includeBinding.showBillExtraFieldName.text =
                if (!receivedPayment.extras?.fieldName.isValid()) {

                    "Extra"
                } else {
                    receivedPayment.extras?.fieldName
                }

            includeBinding.showBillExtraFieldAmount.text =
                receivedPayment.currencySymbol + receivedPayment.extras?.fieldAmount?.format(2)
        } else {

            includeBinding.showBillExtraFieldAmount.text = receivedPayment.currencySymbol + " 0.0"
        }
    }

    private fun setDuesOrAdvanceOfLastPayment() {

        val dueOfLastPayment = getDuesOfLastPayment()

        when {

            dueOfLastPayment > 0.0 -> {

                //due
                includeBinding.showBillDueOfLastPayAmount.text =
                    "+ ${receivedPayment.currencySymbol} ${abs(dueOfLastPayment).format(2)}"

                includeBinding.showBillPaidInAdvanceInlastPayAmount.text =
                    "${receivedPayment.currencySymbol} 0.00"

            }

            dueOfLastPayment < 0.0 -> {

                //advance
                includeBinding.showBillDueOfLastPayAmount.text =
                    "${receivedPayment.currencySymbol} 0.00"

                includeBinding.showBillPaidInAdvanceInlastPayAmount.text =
                    "- ${receivedPayment.currencySymbol} ${abs(dueOfLastPayment).format(2)}"
            }

            else -> {
                includeBinding.showBillDueOfLastPayAmount.text =
                    "${receivedPayment.currencySymbol} 0.00"

                includeBinding.showBillPaidInAdvanceInlastPayAmount.text =
                    "${receivedPayment.currencySymbol} 0.00"
            }
        }
    }

    private fun setDuesOrAdvance() {

        val dueOrAdvance =
            receivedPayment.amountPaid - receivedPayment.netDemand

        includeBinding.showBillAmountPaid.changeTextColor(requireContext(), R.color.color_green)

        includeBinding.showBillDueAmount.text =

            when {

                dueOrAdvance < 0.0 -> {

                    includeBinding.showBillAmountPaid.changeTextColor(
                        requireContext(),
                        R.color.color_orange
                    )
                    //due
                    "${receivedPayment.currencySymbol} ${abs(dueOrAdvance).format(2)}"
                }

                dueOrAdvance > 0.0 -> {

                    includeBinding.showBillDueOrArrearTV.text =
                        "Paid in advance"
                    "${receivedPayment.currencySymbol} ${dueOrAdvance.format(2)}"
                }
                else -> {

                    "${receivedPayment.currencySymbol} 0.00"
                }
            }

    }

    private fun setTotalRent() {

        //total rent
        includeBinding.showBillHouseRent.text =
            "${receivedPayment.currencySymbol} ${
                receivedPayment.houseRent.format(2)
            }"

        includeBinding.showBillParking.text =
            "${receivedPayment.currencySymbol} ${
                receivedPayment.parkingRent.format(2)
            }"

        includeBinding.showBillElectricity.text =
            "${receivedPayment.currencySymbol} ${
                if (receivedPayment.isElectricityBillIncluded) {
                    receivedPayment.electricityBillInfo?.totalElectricBill?.format(2)
                } else {
                    0.0
                }
            }"

        includeBinding.showBillAmountPaid.text =
            "${receivedPayment.currencySymbol} ${
                receivedPayment.amountPaid.format(2)
            }"

        includeBinding.showBillNetDemand.text =
            "${receivedPayment.currencySymbol} ${
                receivedPayment.netDemand.format(2)
            }"
    }

    //[END of setting fields in bills textViews]

    private fun getDuesOfLastPayment(): Double {

        val houseRent = receivedPayment.houseRent
        val parking = receivedPayment.parkingRent

        val electricBill =
            if (receivedPayment.isElectricityBillIncluded)
                receivedPayment.electricityBillInfo?.totalElectricBill!!
            else
                0.0
        val extra = if (receivedPayment.extras != null) {

            receivedPayment.extras!!.fieldAmount
        } else {
            0.0
        }

        return receivedPayment.netDemand - (houseRent + parking + electricBill + extra)
    }

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {
            isSaveBtnClicked = false
            dismiss()
        }

        binding.toolbar.menu.findItem(R.id.menu_save_btn).setOnMenuItemClickListener {

            isSaveBtnClicked = true
            dismiss()
            true
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        Log.d(TAG, "onDismiss: Supporting document bottom sheet dismissed")

        if (mListener != null) {

            mListener!!.onPaymentConfirmationBottomSheetDismissed(isSaveBtnClicked)
        }
    }

    interface OnPaymentConfirmationBottomSheetDismissListener {

        fun onPaymentConfirmationBottomSheetDismissed(isSaveBtnClicked: Boolean)
    }

    fun setOnPaymentConfirmationBottomSheetDismissListener(listener: OnPaymentConfirmationBottomSheetDismissListener) {

        mListener = listener
    }

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): ShowBillForConfirmationBottomSheetFragment {
            val fragment = ShowBillForConfirmationBottomSheetFragment()
            fragment.arguments = bundle
            return fragment
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()

        dismiss()
    }
}