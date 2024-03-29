package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.emi

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.emiAdapters.EMIAdapter
import com.rohitthebest.manageyourrenters.data.DocumentType
import com.rohitthebest.manageyourrenters.data.SupportingDocumentHelperModel
import com.rohitthebest.manageyourrenters.database.model.EMI
import com.rohitthebest.manageyourrenters.databinding.FragmentEmiBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.activities.ShowImageActivity
import com.rohitthebest.manageyourrenters.ui.fragments.CustomMenuItems
import com.rohitthebest.manageyourrenters.ui.fragments.SupportingDocumentDialogFragment
import com.rohitthebest.manageyourrenters.ui.viewModels.EMIViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.onViewOrDownloadSupportingDocument
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "EmiFragment"

@AndroidEntryPoint
class EmiFragment : Fragment(R.layout.fragment_emi), EMIAdapter.OnClickListener,
    CustomMenuItems.OnItemClickListener,
    SupportingDocumentDialogFragment.OnBottomSheetDismissListener {

    private var _binding: FragmentEmiBinding? = null
    private val binding get() = _binding!!

    private val emiViewModel by viewModels<EMIViewModel>()

    private lateinit var emiAdapter: EMIAdapter

    private var rvStateParcelable: Parcelable? = null
    private var searchView: SearchView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentEmiBinding.bind(view)

        initListeners()

        emiAdapter = EMIAdapter()

        setProgressBarVisibility(true)

        lifecycleScope.launch {

            delay(400)
            getEMIs()
        }

        setUpRecyclerView()
        getRvState()
    }

    private fun getRvState() {

        emiViewModel.emiRvState.observe(viewLifecycleOwner) { parcelable ->

            parcelable?.let {

                rvStateParcelable = it
            }
        }
    }

    private fun setUpRecyclerView() {

        binding.emiRV.apply {

            adapter = emiAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            changeVisibilityOfFABOnScrolled(binding.addEmiCategoryFAB)
        }

        emiAdapter.setOnClickListener(this)
    }

    override fun onItemClick(emi: EMI) {

        val action = EmiFragmentDirections.actionEmiFragmentToEMIPaymentFragment(emi.key)

        findNavController().navigate(action)
    }

    private lateinit var emiForMenuItems: EMI
    private var adapterPosition = -1

    override fun onMenuBtnClicked(emi: EMI, position: Int) {

        emiForMenuItems = emi
        adapterPosition = position

        requireActivity().supportFragmentManager.let {

            val bundle = Bundle()
            bundle.putBoolean(Constants.SHOW_SYNC_MENU, !emi.isSynced)

            CustomMenuItems.newInstance(
                bundle
            ).apply {
                show(it, TAG)
            }.setOnClickListener(this)
        }
    }

    //[START OF MENUS]
    override fun onEditMenuClick() {

        if (this::emiForMenuItems.isInitialized) {

            val action = EmiFragmentDirections.actionEmiFragmentToAddEditEMIFragment(
                emiForMenuItems.key
            )

            findNavController().navigate(action)
        }
    }

    override fun onCopyMenuClick() {}

    override fun onMoveMenuClick() {}

    override fun onDeleteMenuClick() {

        if (this::emiForMenuItems.isInitialized) {

            showAlertDialogForDeletion(
                requireContext(),
                {

                    if (isInternetAvailable(requireContext())) {

                        emiViewModel.deleteEMI(emiForMenuItems)
                    } else {

                        showNoInternetMessage(requireContext())
                    }

                },
                { dialog ->

                    dialog.dismiss()
                }
            )
        }
    }

    private fun checkSupportingDocumentValidation(): Boolean {

        if (!emiForMenuItems.isSupportingDocAdded) {

            showToast(requireContext(), getString(R.string.no_supporting_doc_added))
            return false
        } else if (emiForMenuItems.isSupportingDocAdded && emiForMenuItems.supportingDocument == null) {

            showToast(requireContext(), getString(R.string.uploading_doc_progress_message))
            return false
        }

        return true
    }

    override fun onViewSupportingDocumentMenuClick() {

        if (this::emiForMenuItems.isInitialized && checkSupportingDocumentValidation()) {

            emiForMenuItems.supportingDocument?.let { supportingDoc ->

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
            if (this::emiForMenuItems.isInitialized) {

                val supportingDocumentHelperModel = SupportingDocumentHelperModel()
                supportingDocumentHelperModel.modelName = getString(R.string.emis)

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
                emiViewModel.addOrReplaceBorrowerSupportingDocument(
                    emiForMenuItems,
                    supportingDocumentHelperModel
                )
            }
        } else {

            showNoInternetMessage(requireContext())
        }
    }

    override fun onDeleteSupportingDocumentClick() {

        if (this::emiForMenuItems.isInitialized
            && checkSupportingDocumentValidation()
            && emiForMenuItems.supportingDocument != null
        ) {

            showAlertDialogForDeletion(
                requireContext(),
                {
                    if (emiForMenuItems.supportingDocument?.documentType != DocumentType.URL) {

                        if (!isInternetAvailable(requireContext())) {

                            showToast(
                                requireContext(),
                                getString(R.string.network_required_for_deleting_file_from_cloud)
                            )
                            return@showAlertDialogForDeletion
                        } else {

                            deleteFileFromFirebaseStorage(
                                requireContext(),
                                emiForMenuItems.supportingDocument?.documentUrl!!
                            )
                        }
                    }

                    val emi = emiForMenuItems.copy()

                    emi.supportingDocument = null
                    emi.isSupportingDocAdded = false

                    emiViewModel.updateEMI(emiForMenuItems, emi)
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

        if (emiForMenuItems.isSynced) {

            requireContext().showToast("Already synced")
        } else {

            if (isInternetAvailable(requireContext())) {

                emiViewModel.updateEMI(emiForMenuItems, emiForMenuItems)
                emiAdapter.notifyItemChanged(adapterPosition)
            } else {

                showNoInternetMessage(requireContext())
            }
        }

    }
    //[END OF MENUS]

    private fun getEMIs() {

        emiViewModel.getAllEMIs().observe(viewLifecycleOwner) { emiList ->

            if (searchView != null && searchView!!.query.toString().isValid()) {

                setUpSearchView(emiList)
            } else {

                if (emiList.isNotEmpty()) {

                    setNoEMIAddedMessageTVVisibility(false)
                    setUpSearchView(emiList)
                } else {

                    binding.noEmiAddedMessageTV.text = getString(R.string.no_EMI_added_message)
                    setNoEMIAddedMessageTVVisibility(true)
                }
                emiAdapter.submitList(emiList)
            }
            binding.emiRV.layoutManager?.onRestoreInstanceState(rvStateParcelable)

            setProgressBarVisibility(false)
        }
    }

    private var searchTextDelayJob: Job? = null
    private fun setUpSearchView(emiList: List<EMI>) {

        searchView =
            binding.emiFragmentToolbar.menu.findItem(R.id.menu_search).actionView as SearchView

        searchView?.let { sv ->

            if (sv.query.toString().isValid()) {
                searchEMI(sv.query.toString(), emiList)
            }
            sv.onTextSubmit { query -> searchEMI(query, emiList) }

            sv.onTextChanged { query ->
                searchTextDelayJob = lifecycleScope.launch {

                    searchTextDelayJob?.executeAfterDelay {
                        searchEMI(query, emiList)
                    }
                }
            }
        }
    }

    private fun searchEMI(query: String?, emiList: List<EMI>) {

        if (query?.trim()?.isEmpty()!!) {

            binding.emiRV.scrollToPosition(0)
            emiAdapter.submitList(emiList)
            if (emiList.isNotEmpty()) {
                setNoEMIAddedMessageTVVisibility(false)
            } else {
                binding.noEmiAddedMessageTV.text = getString(R.string.no_EMI_added_message)
                setNoEMIAddedMessageTVVisibility(true)
            }
        } else {

            val filteredList = emiList.filter { emi ->

                emi.emiName.lowercase().contains(query.lowercase().trim())
            }

            if (filteredList.isNotEmpty()) {
                setNoEMIAddedMessageTVVisibility(false)
            } else {
                binding.noEmiAddedMessageTV.text =
                    getString(R.string.no_matching_results_found_message)
                setNoEMIAddedMessageTVVisibility(true)
            }

            emiAdapter.submitList(filteredList)
        }

    }

    private fun initListeners() {

        binding.emiFragmentToolbar.setNavigationOnClickListener {

            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.addEmiCategoryFAB.setOnClickListener {

            findNavController().navigate(R.id.action_emiFragment_to_addEditEMIFragment)
        }

        binding.emiRV.changeVisibilityOfFABOnScrolled(
            binding.addEmiCategoryFAB
        )
    }

    private fun setProgressBarVisibility(isVisible: Boolean) {

        binding.emiRV.isVisible = !isVisible
        binding.progressBar.isVisible = isVisible
    }

    private fun setNoEMIAddedMessageTVVisibility(isVisible: Boolean) {

        binding.emiRV.isVisible = !isVisible
        binding.noEmiAddedMessageTV.isVisible = isVisible
    }

    override fun onDestroyView() {
        super.onDestroyView()

        emiViewModel.saveEmiRvState(
            binding.emiRV.layoutManager?.onSaveInstanceState()
        )

        _binding = null
    }

}
