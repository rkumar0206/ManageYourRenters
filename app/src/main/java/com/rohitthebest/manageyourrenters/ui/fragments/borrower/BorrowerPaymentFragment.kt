package com.rohitthebest.manageyourrenters.ui.fragments.borrower

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.borrowerAdapters.BorrowerPaymentAdapter
import com.rohitthebest.manageyourrenters.data.*
import com.rohitthebest.manageyourrenters.database.model.Borrower
import com.rohitthebest.manageyourrenters.database.model.BorrowerPayment
import com.rohitthebest.manageyourrenters.databinding.FragmentBorrowerPaymentBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.activities.ShowImageActivity
import com.rohitthebest.manageyourrenters.ui.fragments.CustomMenuItems
import com.rohitthebest.manageyourrenters.ui.fragments.SupportingDocumentDialogFragment
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerPaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.onViewOrDownloadSupportingDocument
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "BorrowerPaymentFragment"

@AndroidEntryPoint
class BorrowerPaymentFragment : Fragment(R.layout.fragment_borrower_payment),
    BorrowerPaymentAdapter.OnClickListener, CustomMenuItems.OnItemClickListener,
    SupportingDocumentDialogFragment.OnBottomSheetDismissListener,
    AddPartialPaymentFragment.OnPartialPaymentDismiss {

    private var _binding: FragmentBorrowerPaymentBinding? = null
    private val binding get() = _binding!!

    private var receivedBorrower: Borrower? = null
    private var receivedBorrowerKey: String = ""

    private val borrowerViewModel by viewModels<BorrowerViewModel>()
    private val borrowerPaymentViewModel by viewModels<BorrowerPaymentViewModel>()

    private lateinit var borrowerPaymentAdapter: BorrowerPaymentAdapter
    private var rvStateParcelable: Parcelable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBorrowerPaymentBinding.bind(view)

        borrowerPaymentAdapter = BorrowerPaymentAdapter()

        initListener()

        getRecyclerViewState()
        getMessage()

        setUpRecyclerView()

        binding.refreshLayout.isRefreshing = true
    }

    private fun initListener() {

        binding.borrowerPaymentToolbar.setNavigationOnClickListener {

            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.addPaymentFAB.setOnClickListener {

            if (receivedBorrowerKey != "") {

                val action =
                    BorrowerPaymentFragmentDirections.actionBorrowerPaymentFragmentToAddBorrowerPaymentFragment(
                        receivedBorrowerKey, ""
                    )
                findNavController().navigate(action)
            }
        }

        binding.refreshLayout.setOnRefreshListener {

            getBorrowerPayments()
        }
    }

    private fun getRecyclerViewState() {

        borrowerPaymentViewModel.borrowerPaymentRvState.observe(viewLifecycleOwner) { parcelable ->

            parcelable?.let {

                rvStateParcelable = it
            }
        }
    }

    private fun getMessage() {

        try {

            if (!arguments?.isEmpty!!) {

                val args = arguments?.let {

                    BorrowerPaymentFragmentArgs.fromBundle(it)
                }

                receivedBorrowerKey = args?.borrowerKeyMessage!!

                lifecycleScope.launch {

                    delay(300)

                    getBorrower()
                    getBorrowerPayments()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getBorrower() {

        borrowerViewModel.getBorrowerByKey(receivedBorrowerKey).observe(viewLifecycleOwner) {

            if (it != null) {
                receivedBorrower = it
                binding.borrowerPaymentToolbar.title = it.name
            }
        }
    }

    private fun getBorrowerPayments() {

        binding.refreshLayout.isRefreshing = true

        borrowerPaymentViewModel.getPaymentsByBorrowerKey(receivedBorrowerKey)

        lifecycleScope.launch {

            repeatOnLifecycle(Lifecycle.State.STARTED) {

                borrowerPaymentViewModel.allPaymentsListOfBorrower.collect { borrowerPayments ->

                    Log.d(TAG, "getBorrowerPayments: ")

                    borrowerPaymentAdapter.submitList(borrowerPayments)
                    binding.borrowerPaymentRV.layoutManager?.onRestoreInstanceState(
                        rvStateParcelable
                    )

                    if (borrowerPayments.isNotEmpty()) {

                        binding.borrowerPaymentRV.show()
                        binding.noBorrowerPaymentTV.hide()
                    } else {

                        binding.borrowerPaymentRV.hide()
                        binding.noBorrowerPaymentTV.show()
                    }

                    binding.refreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun setUpRecyclerView() {

        binding.borrowerPaymentRV.apply {

            adapter = borrowerPaymentAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            changeVisibilityOfFABOnScrolled(binding.addPaymentFAB)
        }

        borrowerPaymentAdapter.setOnClickListener(this)

    }

    //[START OF ADAPTER CLICK LISTENER]
    override fun onItemClick(borrowerPayment: BorrowerPayment, position: Int) {

        adapterItemPosition = position
        val bundle = Bundle()
        bundle.putString("borrower_payment_message", borrowerPayment.key)

        requireActivity().supportFragmentManager.let {

            AddPartialPaymentFragment.newInstance(bundle)
                .apply {
                    show(it, TAG)
                }.setOnPartialPaymentDialogDismissListener(this)
        }
    }

    override fun onPartialPaymentDismissed() {

        getBorrowerPayments()
    }

    override fun onShowMessageBtnClick(message: String) {

        var m = message

        if (!message.isValid()) {

            m = "No message added!!!"
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Message")
            .setMessage(m)
            .setPositiveButton("Ok") { dialog, _ ->

                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onInterestBtnClick(borrowerPayment: BorrowerPayment) {

        if (!borrowerPayment.isInterestAdded) {

            borrowerPayment.interest =
                Interest(InterestType.SIMPLE_INTEREST, 0.0, InterestTimeSchedule.ANNUALLY)
            showToast(requireContext(), "No interest added!!!")
        }

        val interestCalculatorFields = InterestCalculatorFields(
            borrowerPayment.created,
            borrowerPayment.amountTakenOnRent,
            borrowerPayment.interest!!
        )

        val action =
            BorrowerPaymentFragmentDirections.actionBorrowerPaymentFragmentToCalculateInterestBottomSheetFragment(
                interestCalcualatorFields = interestCalculatorFields.convertToJsonString()
            )

        findNavController().navigate(action)
    }

    private var borrowerPaymentForMenus: BorrowerPayment? = null
    private var adapterItemPosition = 0

    override fun onMenuBtnClick(borrowerPayment: BorrowerPayment, position: Int) {

        borrowerPaymentForMenus = borrowerPayment
        adapterItemPosition = position

        requireActivity().supportFragmentManager.let {

            val bundle = Bundle()
            bundle.putBoolean(Constants.SHOW_SYNC_MENU, !borrowerPayment.isSynced)
            bundle.putBoolean(Constants.SHOW_DOCUMENTS_MENU, true)

            CustomMenuItems.newInstance(
                bundle
            ).apply {
                show(it, TAG)
            }.setOnClickListener(this)
        }

    }

    override fun onEditMenuClick() {

        if (borrowerPaymentForMenus != null) {

            // checking if the borrower payment already contains some partial payment or not
            // if it contains at least one partial payment then user cannot edit the payment

            if (borrowerPaymentForMenus!!.dueLeftAmount < borrowerPaymentForMenus!!.amountTakenOnRent) {

                showToast(
                    requireContext(),
                    getString(R.string.this_payment_already_has_some_partial_payments),
                    Toast.LENGTH_LONG
                )
            } else {

                val action =
                    BorrowerPaymentFragmentDirections.actionBorrowerPaymentFragmentToAddBorrowerPaymentFragment(
                        receivedBorrowerKey, borrowerPaymentForMenus?.key
                    )
                findNavController().navigate(action)
            }
        }

    }

    override fun onCopyMenuClick() {}

    override fun onMoveMenuClick() {}

    override fun onDeleteMenuClick() {

        showAlertDialogForDeletion(
            requireContext(),
            {

                if (borrowerPaymentForMenus != null) {

                    if (isInternetAvailable(requireContext())) {

                        borrowerPaymentViewModel.deleteBorrowerPayment(borrowerPaymentForMenus!!)
                    } else {
                        showNoInternetMessage(requireContext())
                    }
                }
                it.dismiss()
            },
            {
                it.dismiss()
            }
        )

    }

    private fun checkSupportingDocumentValidation(): Boolean {

        if (!borrowerPaymentForMenus?.isSupportingDocAdded!!) {

            showToast(requireContext(), getString(R.string.no_supporting_doc_added))
            return false
        } else if (borrowerPaymentForMenus!!.isSupportingDocAdded && borrowerPaymentForMenus!!.supportingDocument == null) {

            showToast(requireContext(), getString(R.string.uploading_doc_progress_message))
            return false
        }

        return true
    }


    override fun onViewSupportingDocumentMenuClick() {

        if (borrowerPaymentForMenus != null && checkSupportingDocumentValidation()) {

            borrowerPaymentForMenus!!.supportingDocument?.let { supportingDoc ->

                if (supportingDoc.documentType == DocumentType.IMAGE) {

                    // open image activity
                    val intent = Intent(requireContext(), ShowImageActivity::class.java)
                    intent.putExtra(
                        Constants.GENERIC_KEY_FOR_ACTIVITY_OR_FRAGMENT_COMMUNICATION,
                        supportingDoc.convertToJsonString()
                    )
                    requireActivity().startActivity(intent)
                } else {
                    onViewOrDownloadSupportingDocument(
                        requireActivity(),
                        supportingDoc
                    )
                }
            }
        }
    }

    override fun onReplaceSupportingDocumentClick() {
        if (isInternetAvailable(requireContext())) {
            if (borrowerPaymentForMenus != null) {

                val supportingDocumentHelperModel = SupportingDocumentHelperModel()
                supportingDocumentHelperModel.modelName = getString(R.string.borrowerPayments)

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
                borrowerPaymentViewModel.addOrReplaceBorrowerSupportingDocument(
                    borrowerPaymentForMenus!!,
                    supportingDocumentHelperModel
                )
            }
        } else {

            showNoInternetMessage(requireContext())
        }
    }

    override fun onDeleteSupportingDocumentClick() {

        if (borrowerPaymentForMenus != null
            && checkSupportingDocumentValidation()
            && borrowerPaymentForMenus!!.supportingDocument != null
        ) {
            showAlertDialogForDeletion(
                requireContext(),
                {
                    if (borrowerPaymentForMenus!!.supportingDocument?.documentType != DocumentType.URL) {

                        if (!isInternetAvailable(requireContext())) {

                            showToast(
                                requireContext(),
                                getString(R.string.network_required_for_deleting_file_from_cloud)
                            )
                            return@showAlertDialogForDeletion
                        } else {

                            deleteFileFromFirebaseStorage(
                                requireContext(),
                                borrowerPaymentForMenus!!.supportingDocument?.documentUrl!!
                            )
                        }
                    }

                    val borrowerPayment = borrowerPaymentForMenus!!.copy()

                    borrowerPayment.modified = System.currentTimeMillis()
                    borrowerPayment.supportingDocument = null
                    borrowerPayment.isSupportingDocAdded = false

                    borrowerPaymentViewModel.updateBorrowerPayment(
                        borrowerPaymentForMenus!!,
                        borrowerPayment
                    )
                    showToast(requireContext(), getString(R.string.supporting_document_deleted))
                    borrowerPaymentViewModel.getPaymentsByBorrowerKey(borrowerPayment.borrowerKey)
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

        if (borrowerPaymentForMenus != null && borrowerPaymentForMenus!!.isSynced) {

            showToast(requireContext(), "Already synced")
        } else {

            if (isInternetAvailable(requireContext())) {

                borrowerPaymentForMenus?.isSynced = true
                borrowerPaymentViewModel.insertBorrowerPayment(borrowerPaymentForMenus!!)
                borrowerPaymentAdapter.notifyItemChanged(adapterItemPosition)

            } else {

                showNoInternetMessage(requireContext())
            }
        }

    }

    //[END OF ADAPTER CLICK LISTENER]

    override fun onDestroyView() {
        super.onDestroyView()

        borrowerPaymentViewModel.saveBorrowerPaymentRvState(
            binding.borrowerPaymentRV.layoutManager?.onSaveInstanceState()
        )

        _binding = null
    }
}
