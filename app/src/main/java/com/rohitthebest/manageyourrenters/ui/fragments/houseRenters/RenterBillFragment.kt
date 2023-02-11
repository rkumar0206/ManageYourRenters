package com.rohitthebest.manageyourrenters.ui.fragments.houseRenters

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.data.BillPeriodType
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.database.model.RenterPayment
import com.rohitthebest.manageyourrenters.databinding.FragmentRenterBillBinding
import com.rohitthebest.manageyourrenters.databinding.ShowBillLayoutBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.DeletedRenterViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.RenterPaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.RenterViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.saveBitmapToCacheDirectoryAndShare
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showMobileNumberOptionMenu
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.abs

@AndroidEntryPoint
class RenterBillFragment : Fragment(R.layout.fragment_renter_bill) {

    private var _binding: FragmentRenterBillBinding? = null
    private val binding get() = _binding!!

    private val renterPaymentViewModel: RenterPaymentViewModel by viewModels()
    private val renterViewModel: RenterViewModel by viewModels()
    private val deletedRenterViewModel: DeletedRenterViewModel by viewModels()

    private lateinit var receivedRenter: Renter
    private lateinit var receivedPayment: RenterPayment

    private var receivedPaymentKey = ""

    private lateinit var includeBinding: ShowBillLayoutBinding

    private var monthList: List<String> = emptyList()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRenterBillBinding.bind(view)

        includeBinding = binding.include
        monthList = resources.getStringArray(R.array.months).toList()

        getMessage()

        intiListeners()

    }

    private fun intiListeners() {

        binding.toolbar.setNavigationOnClickListener {

            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        includeBinding.showBillRenterMobile.setOnClickListener {

            if (receivedRenter.mobileNumber.isValid()) {
                showMobileNumberOptionMenu(
                    requireActivity(),
                    includeBinding.showBillRenterMobile,
                    receivedRenter.mobileNumber
                )
            }
        }

        binding.toolbar.menu.findItem(R.id.menu_share_bill).setOnMenuItemClickListener {

            val bitmap = includeBinding.root.loadBitmap()

            lifecycleScope.launch {

                saveBitmapToCacheDirectoryAndShare(requireActivity(), bitmap)

            }

            true
        }
        binding.toolbar.menu.findItem(R.id.menu_save_bill).setOnMenuItemClickListener {

            val bitmap = includeBinding.root.loadBitmap()

            val fileName =
                if (receivedPayment.billPeriodInfo.billPeriodType == BillPeriodType.BY_MONTH) {

                    "${receivedRenter.name}_${monthList[receivedPayment.billPeriodInfo.renterBillMonthType?.forBillMonth!! - 1]}"
                } else {

                    "${receivedRenter.name}_" + "${
                        WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                            receivedPayment.billPeriodInfo.renterBillDateType?.fromBillDate
                        )
                    }" +
                            "_to_${
                                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                                    receivedPayment.billPeriodInfo.renterBillDateType?.toBillDate
                                )
                            }"

                }

            bitmap.saveToStorage(requireContext(), fileName)

            showToast(requireContext(), "Payment saved to phone storage.")

            true
        }
    }

    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            val args = arguments?.let { bundle ->

                RenterBillFragmentArgs.fromBundle(bundle)
            }

            args?.let { arg ->

                receivedPaymentKey = arg.paymentKey!!

                if (arg.isReceivedFromDeletedRenterFragment) {

                    // received key is the deletedRenter key
                    // therefore get the deletedRenter from the deletedRenter table

                    getDeletedRenterFromDatabase()


                } else {

                    if (receivedPaymentKey.isValid()) {

                        getPaymentFromDatabase()

                    } else {

                        showToast(requireContext(), "Something went wrong...")
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }

                }
            }

        }

    }

    private fun getDeletedRenterFromDatabase() {

        deletedRenterViewModel.getDeletedRenterByKey(receivedPaymentKey)
            .observe(viewLifecycleOwner) { deletedRenter ->

                receivedRenter = deletedRenter.renterInfo
                receivedPayment = deletedRenter.lastPaymentInfo

                initializeValuesToBill()
            }
    }

    private fun getPaymentFromDatabase() {

        renterPaymentViewModel.getPaymentByPaymentKey(receivedPaymentKey)
            .observe(viewLifecycleOwner) { payment ->

                receivedPayment = payment

                getRenterInfoFromDatabase()
            }
    }

    private fun getRenterInfoFromDatabase() {

        renterViewModel.getRenterByKey(receivedPayment.renterKey)
            .observe(viewLifecycleOwner) { renter ->

                receivedRenter = renter

                binding.toolbar.title = "${renter.name} - payment"

                initializeValuesToBill()
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
