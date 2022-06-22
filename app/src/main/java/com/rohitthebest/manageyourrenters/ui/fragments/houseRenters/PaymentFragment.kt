package com.rohitthebest.manageyourrenters.ui.fragments.houseRenters

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.houseRenterAdapters.ShowPaymentAdapter
import com.rohitthebest.manageyourrenters.data.BillPeriodType
import com.rohitthebest.manageyourrenters.data.DocumentType
import com.rohitthebest.manageyourrenters.data.SupportingDocumentHelperModel
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.database.model.RenterPayment
import com.rohitthebest.manageyourrenters.databinding.FragmentPaymentBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.fragments.CustomMenuItems
import com.rohitthebest.manageyourrenters.ui.fragments.SupportingDocumentDialogFragment
import com.rohitthebest.manageyourrenters.ui.viewModels.RenterPaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.RenterViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs

private const val TAG = "PaymentFragment"

@SuppressLint("SetTextI18n")
@AndroidEntryPoint
class PaymentFragment : Fragment(), View.OnClickListener, ShowPaymentAdapter.OnClickListener,
    CustomMenuItems.OnItemClickListener,
    SupportingDocumentDialogFragment.OnBottomSheetDismissListener {

    private val renterViewModel: RenterViewModel by viewModels()
    private val renterPaymentViewModel: RenterPaymentViewModel by viewModels()

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    private var receivedRenter: Renter? = null

    private lateinit var paymentAdapter: ShowPaymentAdapter

    private var rvStateParcelable: Parcelable? = null

    private var monthList: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        monthList = resources.getStringArray(R.array.months).toList()
        paymentAdapter = ShowPaymentAdapter(monthList)

        showProgressBar()

        getMessage()
        initListener()
        setUpRecyclerView()

        getRvState()

    }

    private fun getRvState() {

        renterPaymentViewModel.renterPaymentRvState.observe(viewLifecycleOwner) { parcelable ->

            parcelable?.let {

                rvStateParcelable = it
            }
        }

    }


    private fun getMessage() {

        try {
            if (!arguments?.isEmpty!!) {

                val args = arguments?.let {

                    PaymentFragmentArgs.fromBundle(it)
                }

                getTheRenter(args?.renterInfoMessage)

                lifecycleScope.launch {

                    delay(300)
                    getPaymentListOfRenter()
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getTheRenter(renterKey: String?) {

        renterViewModel.getRenterByKey(renterKey!!).observe(viewLifecycleOwner) { renter ->

            Log.d(TAG, "getTheRenter: ")

            receivedRenter = renter

            updateCurrentDueOrAdvanceTV()

        }
    }

    private fun getPaymentListOfRenter() {

        try {

            renterPaymentViewModel.getAllPaymentsListOfRenter(receivedRenter?.key!!)
                .observe(viewLifecycleOwner) { paymentList ->

                    Log.d(TAG, "getPaymentListOfRenter: ")

                    if (paymentList.isNotEmpty()) {

                        hideNoPaymentsTV()
                        initializeSearchView(paymentList)

                    } else {

                        showNoPaymentsTV()
                    }

                    paymentAdapter.submitList(paymentList)
                    binding.paymentRV.layoutManager?.onRestoreInstanceState(rvStateParcelable)
                    rvStateParcelable = null
                    hideProgressBar()
                }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateCurrentDueOrAdvanceTV() {

        Log.d(TAG, "updateCurrentDueOrAdvanceTV: ")

        val currentDueOrAdvance =
            "${receivedRenter?.name} : ${abs(receivedRenter?.dueOrAdvanceAmount!!).format(2)}"

        when {

            receivedRenter?.dueOrAdvanceAmount!! < 0.0 -> {

                binding.dueOrAdvancedTV.changeTextColor(requireContext(), R.color.color_orange)
                binding.dueOrAdvancedTV.text =
                    "Current due of $currentDueOrAdvance"
            }
            receivedRenter?.dueOrAdvanceAmount!! > 0.0 -> {

                binding.dueOrAdvancedTV.changeTextColor(requireContext(), R.color.color_green)
                binding.dueOrAdvancedTV.text =
                    "Current advance of $currentDueOrAdvance"
            }
            else -> {

                binding.dueOrAdvancedTV.changeTextColor(requireContext(), R.color.color_green)
                binding.dueOrAdvancedTV.text =
                    "There is no due / advance of ${receivedRenter?.name}"
            }
        }
    }

    private fun initializeSearchView(paymentList: List<RenterPayment>?) {

        try {

            binding.paymentSV.onTextChangedListener { s ->

                if (s?.isEmpty()!!) {

                    binding.paymentRV.scrollToPosition(0)
                    paymentAdapter.submitList(paymentList)
                } else {

                    val filteredList = paymentList?.filter { payment ->

                        var from: String? = ""
                        var till: String? = ""
                        var month = ""

                        if (payment.billPeriodInfo.billPeriodType == BillPeriodType.BY_DATE) {

                            from =
                                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                                    payment.billPeriodInfo.renterBillDateType?.fromBillDate
                                )

                            till =
                                WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                                    payment.billPeriodInfo.renterBillDateType?.toBillDate
                                )
                        } else {

                            month =
                                monthList[payment.billPeriodInfo.renterBillMonthType?.forBillMonth?.minus(
                                    1
                                )!!]
                        }

                        month.lowercase(Locale.ROOT).contains(

                            s.toString().trim().lowercase(Locale.ROOT)
                        ) ||
                                from?.contains(s.toString().trim())!!
                                ||
                                till?.contains(s.toString().trim())!!

                    }

                    paymentAdapter.submitList(filteredList)
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setUpRecyclerView() {

        try {

            binding.paymentRV.apply {

                adapter = paymentAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
                changeVisibilityOfFABOnScrolled(binding.addPyamentFAB)
            }

            paymentAdapter.setOnClickListener(this)

        } catch (e: java.lang.Exception) {

            e.printStackTrace()
        }
    }

    override fun onPaymentClick(payment: RenterPayment) {

        val action = PaymentFragmentDirections.actionPaymentFragmentToRenterBillFragment(
            paymentKey = payment.key
        )

        findNavController().navigate(action)

    }

    private lateinit var renterPaymentForMenus: RenterPayment
    private var adapterItemPosition = 0

    override fun onMenuBtnClicked(payment: RenterPayment, position: Int) {

        renterPaymentForMenus = payment
        adapterItemPosition = position

        requireActivity().supportFragmentManager.let {

            val bundle = Bundle()

            bundle.putBoolean(Constants.SHOW_SYNC_MENU, !payment.isSynced)
            bundle.putBoolean(Constants.SHOW_DELETE_MENU, position == 0)
            bundle.putBoolean(Constants.SHOW_EDIT_MENU, false)
            bundle.putBoolean(Constants.SHOW_DOCUMENTS_MENU, true)

            CustomMenuItems.newInstance(
                bundle
            ).apply {
                show(it, TAG)
            }.setOnClickListener(this)
        }
    }

    override fun onEditMenuClick() {}
    override fun onCopyMenuClick() {}
    override fun onMoveMenuClick() {}

    override fun onDeleteMenuClick() {

        if (::renterPaymentForMenus.isInitialized) {

            showAlertDialogForDeletion(
                requireContext(),
                { dialog ->

                    if (isInternetAvailable(requireContext())) {

                        renterPaymentViewModel.deletePayment(renterPaymentForMenus)
                    } else {
                        showNoInternetMessage(requireContext())
                    }

                    dialog.dismiss()

                },
                { dialog ->

                    dialog.dismiss()
                }
            )
        }

    }

    private fun checkSupportingDocumentValidation(): Boolean {

        if (!renterPaymentForMenus.isSupportingDocAdded) {

            showToast(requireContext(), getString(R.string.no_supporting_doc_added))
            return false
        } else if (renterPaymentForMenus.isSupportingDocAdded && renterPaymentForMenus.supportingDocument == null) {

            showToast(requireContext(), getString(R.string.uploading_doc_progress_message))
            return false
        }

        return true
    }


    override fun onViewSupportingDocumentMenuClick() {

        if (::renterPaymentForMenus.isInitialized && checkSupportingDocumentValidation()) {

            renterPaymentForMenus.supportingDocument?.let { supportingDoc ->

                Functions.onViewOrDownloadSupportingDocument(
                    requireActivity(),
                    supportingDoc
                )
            }
        }
    }

    override fun onReplaceSupportingDocumentClick() {
        if (isInternetAvailable(requireContext())) {
            if (::renterPaymentForMenus.isInitialized) {

                val supportingDocumentHelperModel = SupportingDocumentHelperModel()
                supportingDocumentHelperModel.modelName = getString(R.string.renter_payments)

                showSupportDocumentBottomSheetDialog(supportingDocumentHelperModel)
            }
        } else {

            showNoInternetMessage(requireContext())
        }

    }

    private fun showSupportDocumentBottomSheetDialog(supportingDocmtHelperModel: SupportingDocumentHelperModel) {

        val bundle = Bundle()
        bundle.putString(
            Constants.SUPPORTING_DOCUMENT_HELPER_MODEL_KEY,
            supportingDocmtHelperModel.convertToJsonString()
        )

        requireActivity().supportFragmentManager.let {

            SupportingDocumentDialogFragment.newInstance(bundle)
                .apply {
                    show(it, TAG)
                }.setOnBottomSheetDismissListener(this)
        }
    }

    override fun onBottomSheetDismissed(
        isDocumentAdded: Boolean,
        supportingDocumentHelperModel: SupportingDocumentHelperModel
    ) {

        if (isInternetAvailable(requireContext())) {
            if (isDocumentAdded) {
                // call the viewmodel method for adding or replacing the document
                renterPaymentViewModel.addOrReplaceBorrowerSupportingDocument(
                    renterPaymentForMenus,
                    supportingDocumentHelperModel
                )
            }
        } else {

            showNoInternetMessage(requireContext())
        }
    }


    override fun onDeleteSupportingDocumentClick() {

        if (::renterPaymentForMenus.isInitialized
            && checkSupportingDocumentValidation()
            && renterPaymentForMenus.supportingDocument != null
        ) {
            showAlertDialogForDeletion(
                requireContext(),
                {
                    // if supporting doc is not a url then delete from cloud storage
                    if (renterPaymentForMenus.supportingDocument?.documentType != DocumentType.URL) {

                        if (!isInternetAvailable(requireContext())) {

                            showToast(
                                requireContext(),
                                getString(R.string.network_required_for_deleting_file_from_cloud)
                            )
                            return@showAlertDialogForDeletion
                        } else {

                            deleteFileFromFirebaseStorage(
                                requireContext(),
                                renterPaymentForMenus.supportingDocument?.documentUrl!!
                            )
                        }
                    }

                    val renterPayment = renterPaymentForMenus.copy()

                    renterPayment.modified = System.currentTimeMillis()
                    renterPayment.supportingDocument = null
                    renterPayment.isSupportingDocAdded = false

                    renterPaymentViewModel.updatePayment(
                        renterPaymentForMenus,
                        renterPayment
                    )
                    showToast(requireContext(), getString(R.string.supporting_document_deleted))
                    it.dismiss()
                },
                {
                    it.dismiss()
                }
            )
        } else {
            showToast(requireContext(), getString(R.string.no_supporting_doc_added))
        }


    }

    override fun onSyncMenuClick() {

        if (::renterPaymentForMenus.isInitialized) {
            if (isInternetAvailable(requireContext())) {

                if (renterPaymentForMenus.isSynced) {

                    showToast(requireContext(), getString(R.string.already_synced))
                } else {

                    renterPaymentViewModel.insertPayment(renterPaymentForMenus, null)
                    paymentAdapter.notifyItemChanged(adapterItemPosition)
                }

            } else {

                showNoInternetMessage(requireContext())
            }
        }

    }


    override fun onMessageBtnClicked(paymentMessage: String) {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Message")
            .setMessage(
                if (paymentMessage != "") {
                    paymentMessage
                } else {
                    "No message..."
                }
            )
            .setPositiveButton("Ok") { dialogInterface, _ ->

                dialogInterface.dismiss()
            }
            .create()
            .show()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener() {

        binding.addPyamentFAB.setOnClickListener(this)
        binding.deleteAllPaymentsBtn.setOnClickListener(this)
        binding.paymentBackBtn.setOnClickListener(this)

        binding.paymentRV.changeVisibilityOfFABOnScrolled(
            binding.addPyamentFAB
        )
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            binding.addPyamentFAB.id -> {

                try {

                    val action =
                        PaymentFragmentDirections.actionPaymentFragmentToAddPaymentFragment(
                            convertRenterToJSONString(receivedRenter!!)
                        )
                    findNavController().navigate(action)
                } catch (e: Exception) {

                    e.printStackTrace()
                }
            }

            binding.deleteAllPaymentsBtn.id -> {

                if (binding.noPaymentsTV.visibility == View.VISIBLE) {

                    showToast(requireContext(), "No Payments added!!!")
                } else {

                    deleteAllPaymentsAfterWarningMessage()
                }
            }

            binding.paymentBackBtn.id -> {

                requireActivity().onBackPressed()
            }
        }
    }

    private fun deleteAllPaymentsAfterWarningMessage() {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete all payments?")
            .setMessage(getString(R.string.delete_warning_message))
            .setPositiveButton("Delete All") { dialogInterface, _ ->

                if (isInternetAvailable(requireContext())) {

                    renterPaymentViewModel.deleteAllPaymentsOfRenter(
                        receivedRenter?.key!!
                    )

                    showToast(
                        requireContext(),
                        "Deleted all the payments of ${receivedRenter?.name}"
                    )
                } else {
                    showNoInternetMessage(requireContext())
                }
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->

                dialog.dismiss()
            }
            .create()
            .show()

    }

    private fun showNoPaymentsTV() {

        try {

            binding.noPaymentsTV.show()
            binding.paymentRV.hide()
            binding.paymentAppBarLL.hide()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideNoPaymentsTV() {

        try {

            binding.noPaymentsTV.hide()
            binding.paymentRV.show()
            binding.paymentAppBarLL.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showProgressBar() {

        try {

            binding.paymentFragProgressBar.show()
        } catch (e: java.lang.Exception) {

            e.printStackTrace()
        }
    }

    private fun hideProgressBar() {

        try {

            binding.paymentFragProgressBar.hide()
        } catch (e: java.lang.Exception) {

            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        renterPaymentViewModel.saveRenterPaymentRvState(binding.paymentRV.layoutManager?.onSaveInstanceState())

        hideKeyBoard(requireActivity())

        _binding = null
    }
}