package com.rohitthebest.manageyourrenters.ui.fragments.houseRenters

import android.annotation.SuppressLint
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
@SuppressLint("SetTextI18n")
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
    private lateinit var workingWithDateAndTime: WorkingWithDateAndTime

    private var monthList: List<String> = emptyList()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRenterBillBinding.bind(view)

        includeBinding = binding.include
        workingWithDateAndTime = WorkingWithDateAndTime()
        monthList = resources.getStringArray(R.array.months).toList()

        getMessage()

        intiListeners()

    }

    private fun intiListeners() {

        binding.toolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
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
                        workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                            receivedPayment.billPeriodInfo.renterBillDateType?.fromBillDate
                        )
                    }" +
                            "_to_${
                                workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
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
                        requireActivity().onBackPressed()
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

                includeBinding.showBillRenterMobile.setOnClickListener {

                    showMobileNumberOptionMenu(
                        requireActivity(),
                        includeBinding.showBillRenterMobile,
                        receivedRenter.mobileNumber
                    )
                }
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

                "${workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(receivedPayment.billPeriodInfo.renterBillDateType?.fromBillDate)}" +
                        " to ${
                            workingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
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
                    "${receivedPayment.currencySymbol} ${abs(dueOfLastPayment).format(2)}"

                includeBinding.showBillPaidInAdvanceInlastPayAmount.text =
                    "${receivedPayment.currencySymbol} 0.00"

            }

            dueOfLastPayment < 0.0 -> {

                //advance
                includeBinding.showBillDueOfLastPayAmount.text =
                    "${receivedPayment.currencySymbol} 0.00"

                includeBinding.showBillPaidInAdvanceInlastPayAmount.text =
                    "${receivedPayment.currencySymbol} ${abs(dueOfLastPayment).format(2)}"
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

        includeBinding.showBillDueAmount.text =

            when {

                dueOrAdvance < 0.0 -> {

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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
