package com.rohitthebest.manageyourrenters.ui.fragments.borrower

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.borrowerAdapters.BorrowerAdapter
import com.rohitthebest.manageyourrenters.data.DocumentType
import com.rohitthebest.manageyourrenters.data.SupportingDocumentHelperModel
import com.rohitthebest.manageyourrenters.database.model.Borrower
import com.rohitthebest.manageyourrenters.databinding.FragmentBorrowerHomeBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.fragments.CustomMenuItems
import com.rohitthebest.manageyourrenters.ui.fragments.SupportingDocumentDialogFragment
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "BorrowerHomeFragment"

@AndroidEntryPoint
class BorrowerHomeFragment : Fragment(R.layout.fragment_borrower_home),
    BorrowerAdapter.OnClickListener, CustomMenuItems.OnItemClickListener,
    SupportingDocumentDialogFragment.OnBottomSheetDismissListener {

    private var _binding: FragmentBorrowerHomeBinding? = null
    private val binding get() = _binding!!

    private val borrowerViewModel by viewModels<BorrowerViewModel>()
    private lateinit var borrowerAdapter: BorrowerAdapter

    private var rvStateParcelable: Parcelable? = null

    private var searchView: SearchView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBorrowerHomeBinding.bind(view)


        borrowerAdapter = BorrowerAdapter()
        setUpRecyclerView()

        getRecyclerViewState()

        initListeners()

        binding.refreshLayout.isRefreshing = true

        lifecycleScope.launch {

            delay(400)
            getAllBorrowers()
        }

        binding.individualRentersRV.changeVisibilityOfFABOnScrolled(
            binding.addIndividualRenterFAB
        )
    }

    private fun initListeners() {

        binding.addIndividualRenterFAB.setOnClickListener {

            findNavController().navigate(R.id.action_borrowerHomeFragment_to_addBorrowerFragment)
        }
        binding.individualRenterToolbar.setNavigationOnClickListener {

            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.refreshLayout.setOnRefreshListener {

            getAllBorrowers()
        }
    }

    private fun getRecyclerViewState() {

        borrowerViewModel.borrowerRvState.observe(viewLifecycleOwner) { parcelable ->

            parcelable?.let {

                rvStateParcelable = it
            }
        }
    }

    private fun getAllBorrowers() {

        binding.refreshLayout.isRefreshing = true

        borrowerViewModel.getAllBorrower()

        borrowerViewModel.allBorrowersList.observe(viewLifecycleOwner) { borrowers ->

            if (searchView != null && searchView!!.query.toString().isValid()) {
                initSearchViewMenu(borrowers)
            } else {
                if (borrowers.isNotEmpty()) {

                    showNoBorrowersAddedTV(false)
                    initSearchViewMenu(borrowers)
                } else {
                    binding.noBorrowersAddedMessageTV.text =
                        getString(R.string.no_borrowers_message)
                    showNoBorrowersAddedTV(true)
                }
                borrowerAdapter.submitList(borrowers)
            }
            binding.individualRentersRV.layoutManager?.onRestoreInstanceState(rvStateParcelable)
            binding.refreshLayout.isRefreshing = false
        }
    }

    private var searchTextDelayJob: Job? = null
    private fun initSearchViewMenu(borrowerList: List<Borrower>) {

        searchView =
            binding.individualRenterToolbar.menu.findItem(R.id.menu_search).actionView as SearchView

        if (searchView != null) {

            if (searchView!!.query.toString().isValid()) {
                Log.d(TAG, "setUpSearchEditText: query : ${searchView!!.query}")
                searchBorrower(searchView!!.query.toString(), borrowerList)
            }

            searchView!!.onTextSubmit { newText ->
                searchBorrower(newText, borrowerList)
            }

            searchView!!.onTextChanged { newText ->

                searchTextDelayJob = lifecycleScope.launch {
                    searchTextDelayJob?.executeAfterDelay {
                        searchBorrower(newText, borrowerList)
                    }
                }
            }
        }
    }

    private fun searchBorrower(query: String?, borrowerList: List<Borrower>) {

        if (query?.isEmpty()!!) {

            binding.individualRentersRV.scrollToPosition(0)
            borrowerAdapter.submitList(borrowerList)
            showNoBorrowersAddedTV(false)
        } else {

            val filteredList = borrowerList.filter { borrower ->

                borrower.name.lowercase(Locale.ROOT)
                    .contains(query.toString().trim().lowercase(Locale.ROOT))
            }
            if (filteredList.isNotEmpty()) {

                showNoBorrowersAddedTV(false)
            } else {
                binding.noBorrowersAddedMessageTV.text =
                    getString(R.string.no_matching_results_found_message)
                showNoBorrowersAddedTV(true)
            }

            borrowerAdapter.submitList(filteredList)
        }
    }

    private fun setUpRecyclerView() {

        binding.individualRentersRV.apply {

            setHasFixedSize(true)
            adapter = borrowerAdapter
            layoutManager = LinearLayoutManager(requireContext())
            changeVisibilityOfFABOnScrolled(binding.addIndividualRenterFAB)
        }

        borrowerAdapter.setOnClickListener(this)
    }

    override fun onBorrowerClicked(borrowerKey: String) {

        val action =
            BorrowerHomeFragmentDirections.actionBorrowerHomeFragmentToBorrowerPaymentFragment(
                borrowerKey
            )

        findNavController().navigate(action)
    }

    private lateinit var borrowerForMenus: Borrower
    private var currentAdapterPosition = -1

    override fun onMenuButtonClicked(borrower: Borrower, position: Int) {

        borrowerForMenus = borrower
        currentAdapterPosition = position

        requireActivity().supportFragmentManager.let {

            val bundle = Bundle()
            bundle.putBoolean(Constants.SHOW_SYNC_MENU, !borrowerForMenus.isSynced)
            bundle.putBoolean(Constants.SHOW_DELETE_MENU, true)
            bundle.putBoolean(Constants.SHOW_DOCUMENTS_MENU, true)
            bundle.putBoolean(Constants.SHOW_EDIT_MENU, true)

            CustomMenuItems.newInstance(
                bundle
            ).apply {
                show(it, TAG)
            }.setOnClickListener(this)
        }
    }

    override fun onDetailsButtonClicked(borrowerKey: String) {
        val action =
            BorrowerHomeFragmentDirections.actionBorrowerHomeFragmentToBorrowerDetailBottomSheetDialog(
                borrowerKey
            )
        findNavController().navigate(action)
    }

    //[START OF MENU CLICK LISTENERS]

    override fun onEditMenuClick() {

        if (::borrowerForMenus.isInitialized) {
            val action =
                BorrowerHomeFragmentDirections.actionBorrowerHomeFragmentToAddBorrowerFragment(
                    borrowerForMenus.key
                )

            findNavController().navigate(action)
        }
    }

    override fun onCopyMenuClick() {}
    override fun onMoveMenuClick() {}

    override fun onDeleteMenuClick() {

        showAlertDialogForDeletion(
            requireContext(),
            positiveButtonListener = {

                if (isInternetAvailable(requireContext())) {

                    borrowerViewModel.deleteBorrower(
                        borrowerForMenus
                    )
                } else {
                    showNoInternetMessage(requireContext())
                }

                it.dismiss()
            },
            negativeButtonListener = {

                it.dismiss()
            }
        )

    }

    private fun checkSupportingDocumentValidation(): Boolean {

        if (!borrowerForMenus.isSupportingDocAdded) {

            showToast(requireContext(), getString(R.string.no_supporting_doc_added))
            return false
        } else if (borrowerForMenus.isSupportingDocAdded && borrowerForMenus.supportingDocument == null) {

            showToast(requireContext(), getString(R.string.uploading_doc_progress_message))
            return false
        }

        return true
    }


    override fun onViewSupportingDocumentMenuClick() {

        if (::borrowerForMenus.isInitialized && checkSupportingDocumentValidation()) {

            borrowerForMenus.supportingDocument?.let { supportingDoc ->

                Functions.onViewOrDownloadSupportingDocument(
                    requireActivity(),
                    supportingDoc
                )
            }

        }
    }

    override fun onReplaceSupportingDocumentClick() {

        if (isInternetAvailable(requireContext())) {
            if (::borrowerForMenus.isInitialized) {

                val supportingDocumentHelperModel = SupportingDocumentHelperModel()
                supportingDocumentHelperModel.modelName = getString(R.string.borrowers)

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
                borrowerViewModel.addOrReplaceBorrowerSupportingDocument(
                    borrowerForMenus,
                    supportingDocumentHelperModel
                )
            }
        } else {

            showNoInternetMessage(requireContext())
        }
        getAllBorrowers()
    }

    override fun onDeleteSupportingDocumentClick() {

        if (::borrowerForMenus.isInitialized
            && checkSupportingDocumentValidation()
            && borrowerForMenus.supportingDocument != null
        ) {

            showAlertDialogForDeletion(
                requireContext(),
                {
                    if (borrowerForMenus.supportingDocument?.documentType != DocumentType.URL) {

                        if (!isInternetAvailable(requireContext())) {

                            showToast(
                                requireContext(),
                                getString(R.string.network_required_for_deleting_file_from_cloud)
                            )
                            return@showAlertDialogForDeletion
                        } else {

                            deleteFileFromFirebaseStorage(
                                requireContext(),
                                borrowerForMenus.supportingDocument?.documentUrl!!
                            )
                        }
                    }
                    borrowerForMenus.supportingDocument = null
                    borrowerForMenus.isSupportingDocAdded = false
                    borrowerViewModel.updateBorrower(borrowerForMenus)
                    borrowerViewModel.getAllBorrower()

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

        if (::borrowerForMenus.isInitialized) {
            if (isInternetAvailable(requireContext())) {

                if (borrowerForMenus.isSynced) {

                    showToast(requireContext(), "Already Synced")
                } else {

                    borrowerForMenus.isSynced = true
                    borrowerViewModel.insertBorrower(borrowerForMenus)
                    borrowerAdapter.notifyItemChanged(currentAdapterPosition)
                }

            } else {

                showNoInternetMessage(requireContext())
            }
        }

    }

    //[END OF MENU CLICK LISTENERS]


    private fun showNoBorrowersAddedTV(isVisible: Boolean) {

        binding.noBorrowersAddedMessageTV.isVisible = isVisible
        binding.individualRentersRV.isVisible = !isVisible
    }

    override fun onDestroyView() {
        super.onDestroyView()

        borrowerViewModel.saveBorrowerRvState(
            binding.individualRentersRV.layoutManager?.onSaveInstanceState()
        )

        _binding = null
    }
}
