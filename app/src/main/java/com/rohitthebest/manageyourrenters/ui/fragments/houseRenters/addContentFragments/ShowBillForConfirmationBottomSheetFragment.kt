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
        includeBinding.showBillRenterMobile.text =
            if (receivedRenter.mobileNumber.isValid()) receivedRenter.mobileNumber else "NA"
        includeBinding.showBillRenterAddress.text =
            if (receivedRenter.address.isValid()) receivedRenter.address else "NA"
    }

    private fun setBillingParameters() {

        //billing parameter
        includeBinding.showBillBillDate
            .setDateInTextView(receivedPayment.created)
        includeBinding.showBillBillTime
            .setDateInTextView(receivedPayment.created, "hh:mm a")

        includeBinding.showBillBillPeriod.text =
            if (receivedPayment.billPeriodInfo.billPeriodType == BillPeriodType.BY_MONTH) {

                receivedPayment.billPeriodInfo.renterBillMonthType?.let { byMonth ->

                    includeBinding.numberOfMonthsOrDaysTV.text =
                        getString(R.string.number_of_months)
                    includeBinding.numberOfMonthOrDaysValueTV.text =
                        byMonth.numberOfMonths.toString()

                    if ((byMonth.forBillMonth == byMonth.toBillMonth) &&
                        byMonth.forBillYear == byMonth.toBillYear
                    ) {

                        getString(
                            R.string.month_year,
                            monthList[byMonth.forBillMonth - 1],
                            byMonth.forBillYear.toString()
                        )
                    } else {

                        getString(
                            R.string.renter_bill_period_field_month,
                            monthList[receivedPayment.billPeriodInfo.renterBillMonthType?.forBillMonth!! - 1],
                            receivedPayment.billPeriodInfo.renterBillMonthType?.forBillYear.toString(),
                            monthList[receivedPayment.billPeriodInfo.renterBillMonthType?.toBillMonth!! - 1],
                            receivedPayment.billPeriodInfo.renterBillMonthType?.toBillYear.toString(),
                        )
                    }
                }

            } else {

                receivedPayment.billPeriodInfo.renterBillDateType?.let { byDate ->

                    includeBinding.numberOfMonthsOrDaysTV.text = getString(R.string.number_of_days)
                    includeBinding.numberOfMonthOrDaysValueTV.text = byDate.numberOfDays.toString()

                    getString(
                        R.string.renter_bill_period_field_date,
                        WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                            byDate.fromBillDate
                        ),
                        WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                            byDate.toBillDate
                        )
                    )
                }
            }
    }

    private fun setElectricFields() {

        if (receivedPayment.isElectricityBillIncluded) {

            //electricity
            includeBinding.showBillPreviousReading.text =
                getString(
                    R.string.renter_bill_electricity_units,
                    receivedPayment.electricityBillInfo?.previousReading?.format(2)
                )

            includeBinding.showBillCurrentReading.text =
                getString(
                    R.string.renter_bill_electricity_units,
                    receivedPayment.electricityBillInfo?.currentReading?.format(2)
                )

            includeBinding.showBillRate.text =
                getString(
                    R.string.renter_bill_electricity_rate,
                    receivedPayment.electricityBillInfo?.rate?.format(2)
                )

            includeBinding.showBillDifference.text =
                getString(
                    R.string.renter_bill_electricity_units,
                    receivedPayment.electricityBillInfo?.differenceInReading?.format(2)
                )

            includeBinding.showBillElectricityTotal.text =
                getString(
                    R.string.currency_amount,
                    receivedPayment.currencySymbol,
                    receivedPayment.electricityBillInfo?.totalElectricBill?.format(2)
                )


        }
    }

    private fun setExtraFields() {

        //Extra
        includeBinding.showBillExtraFieldName.text = getString(R.string.extra)

        if (receivedPayment.extras != null) {

            includeBinding.showBillExtraFieldName.text =
                if (!receivedPayment.extras?.fieldName.isValid()) {
                    getString(R.string.extra)
                } else {
                    receivedPayment.extras?.fieldName
                }

            includeBinding.showBillExtraFieldAmount.text =
                getString(
                    R.string.currency_amount,
                    receivedPayment.currencySymbol,
                    receivedPayment.extras?.fieldAmount?.format(2)
                )

        } else {

            includeBinding.showBillExtraFieldAmount.text =
                getString(
                    R.string.currency_amount,
                    receivedPayment.currencySymbol,
                    "0.0"
                )
        }
    }

    private fun setDuesOrAdvanceOfLastPayment() {

        val dueOfLastPayment = getDuesOfLastPayment()

        when {

            dueOfLastPayment > 0.0 -> {

                //due
                includeBinding.showBillDueOfLastPayAmount.text =
                    getString(
                        R.string.sign_currency_amount,
                        "+",
                        receivedPayment.currencySymbol,
                        abs(dueOfLastPayment).format(2)
                    )

                includeBinding.showBillPaidInAdvanceInlastPayAmount.text =
                    getString(
                        R.string.currency_amount,
                        receivedPayment.currencySymbol,
                        "0.0"
                    )
            }

            dueOfLastPayment < 0.0 -> {

                //advance
                includeBinding.showBillDueOfLastPayAmount.text =
                    getString(
                        R.string.currency_amount,
                        receivedPayment.currencySymbol,
                        "0.0"
                    )

                includeBinding.showBillPaidInAdvanceInlastPayAmount.text =
                    getString(
                        R.string.sign_currency_amount,
                        "-",
                        receivedPayment.currencySymbol,
                        abs(dueOfLastPayment).format(2)
                    )

            }

            else -> {
                includeBinding.showBillDueOfLastPayAmount.text =
                    getString(
                        R.string.currency_amount,
                        receivedPayment.currencySymbol,
                        "0.0"
                    )

                includeBinding.showBillPaidInAdvanceInlastPayAmount.text =
                    getString(
                        R.string.currency_amount,
                        receivedPayment.currencySymbol,
                        "0.0"
                    )
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
                    getString(
                        R.string.currency_amount,
                        receivedPayment.currencySymbol,
                        abs(dueOrAdvance).format(2)
                    )
                }

                dueOrAdvance > 0.0 -> {

                    includeBinding.showBillDueOrArrearTV.text =
                        getString(R.string.paid_in_advance).trim()

                    getString(
                        R.string.currency_amount,
                        receivedPayment.currencySymbol,
                        dueOrAdvance.format(2)
                    )
                }
                else -> {
                    getString(
                        R.string.currency_amount,
                        receivedPayment.currencySymbol,
                        "0.0"
                    )
                }
            }

    }

    private fun setTotalRent() {

        //total rent
        includeBinding.showBillHouseRent.text =
            getString(
                R.string.currency_amount,
                receivedPayment.currencySymbol,
                receivedPayment.houseRent.format(2)
            )

        includeBinding.showBillParking.text =
            getString(
                R.string.currency_amount,
                receivedPayment.currencySymbol,
                receivedPayment.parkingRent.format(2)
            )

        includeBinding.showBillElectricity.text =
            getString(
                R.string.currency_amount,
                receivedPayment.currencySymbol,
                if (receivedPayment.isElectricityBillIncluded) {
                    receivedPayment.electricityBillInfo?.totalElectricBill?.format(2)
                } else {
                    "0.0"
                }
            )

        includeBinding.showBillAmountPaid.text =
            getString(
                R.string.currency_amount,
                receivedPayment.currencySymbol,
                receivedPayment.amountPaid.format(2)
            )

        includeBinding.showBillNetDemand.text =
            getString(
                R.string.currency_amount,
                receivedPayment.currencySymbol,
                receivedPayment.netDemand.format(2)
            )

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