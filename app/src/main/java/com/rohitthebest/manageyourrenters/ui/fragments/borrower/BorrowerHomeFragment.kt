package com.rohitthebest.manageyourrenters.ui.fragments.borrower

import android.os.Bundle
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
import com.rohitthebest.manageyourrenters.ui.fragments.SupportingDocumentDialogFragment
import com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.CustomMenuItems
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBorrowerHomeBinding.bind(view)

        binding.addIndividualRenterFAB.setOnClickListener {

            findNavController().navigate(R.id.action_borrowerHomeFragment_to_addBorrowerFragment)
        }
        binding.individualRenterToolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

        showHideProgressBar(true)

        borrowerAdapter = BorrowerAdapter()
        setUpRecyclerView()

        lifecycleScope.launch {

            delay(400)
            getAllBorrowers()
        }

        binding.individualRentersRV.changeVisibilityOfFABOnScrolled(
            binding.addIndividualRenterFAB
        )
    }

    private fun getAllBorrowers() {

        borrowerViewModel.getAllBorrower().observe(viewLifecycleOwner) {

            if (it.isEmpty()) {

                showNoBorrowersAddedTV(true)
            } else {

                showNoBorrowersAddedTV(false)
            }

            borrowerAdapter.submitList(it)

            initSearchViewMenu(it)

            showHideProgressBar(false)
        }
    }

    private fun initSearchViewMenu(borrowerList: List<Borrower>) {

        val searchView =
            binding.individualRenterToolbar.menu.findItem(R.id.menu_search).actionView as SearchView

        searchView.searchText { newText ->

            if (borrowerList.isEmpty()) {

                binding.individualRentersRV.scrollToPosition(0)
                borrowerAdapter.submitList(borrowerList)
            } else {

                val filteredList = borrowerList.filter { borrower ->

                    borrower.name.lowercase(Locale.ROOT)
                        .contains(newText.toString().trim().lowercase(Locale.ROOT))
                }

                borrowerAdapter.submitList(filteredList)
            }

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

    override fun onDeleteMenuClick() {

        showAlertDialogForDeletion(
            requireContext(),
            positiveButtonListener = {

                if (!borrowerForMenus.isSynced) {

                    borrowerViewModel.deleteBorrower(
                        borrowerForMenus
                    )
                } else {

                    if (isInternetAvailable(requireContext())) {

                        borrowerViewModel.deleteBorrower(
                            borrowerForMenus
                        )
                    } else {
                        showNoInternetMessage(requireContext())
                    }
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

                    uploadDocumentToFireStore(
                        requireContext(),
                        getString(R.string.borrowers),
                        borrowerForMenus.key
                    )

                    borrowerViewModel.updateBorrower(borrowerForMenus)

                    borrowerAdapter.notifyItemChanged(currentAdapterPosition)
                }

            } else {

                showNoInternetMessage(requireContext())
            }
        }

    }


    //[END OF MENU CLICK LISTENERS]

    override fun onMobileNumberClicked(mobileNumber: String, view: View) {

        Functions.showMobileNumberOptionMenu(
            requireActivity(),
            view,
            mobileNumber
        )
    }

    private fun showHideProgressBar(isVisible: Boolean) {

        binding.progressBar.isVisible = isVisible
    }

    private fun showNoBorrowersAddedTV(isVisible: Boolean) {

        binding.noBorrowersAddedMessageTV.isVisible = isVisible
        binding.individualRentersRV.isVisible = !isVisible
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
