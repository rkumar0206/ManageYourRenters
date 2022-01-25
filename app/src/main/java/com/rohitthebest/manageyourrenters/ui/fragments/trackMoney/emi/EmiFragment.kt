package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.emi

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.emiAdapters.EMIAdapter
import com.rohitthebest.manageyourrenters.data.DocumentType
import com.rohitthebest.manageyourrenters.database.model.EMI
import com.rohitthebest.manageyourrenters.databinding.FragmentEmiBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.IS_DOCUMENT_FOR_EDITING_KEY
import com.rohitthebest.manageyourrenters.ui.fragments.AddSupportingDocumentBottomSheetFragment
import com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.CustomMenuItems
import com.rohitthebest.manageyourrenters.ui.viewModels.EMIViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.onViewOrDownloadSupportingDocument
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "EmiFragment"

@AndroidEntryPoint
class EmiFragment : Fragment(R.layout.fragment_emi), EMIAdapter.OnClickListener,
    CustomMenuItems.OnItemClickListener {

    private var _binding: FragmentEmiBinding? = null
    private val binding get() = _binding!!

    private val emiViewModel by viewModels<EMIViewModel>()

    private lateinit var emiAdapter: EMIAdapter

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

    override fun onMenuBtnClicked(emi: EMI, position: Int) {

        emiForMenuItems = emi

        requireActivity().supportFragmentManager.let {

            val bundle = Bundle()
            bundle.putBoolean(Constants.SHOW_SYNC_MENU, false)

            CustomMenuItems.newInstance(
                bundle
            ).apply {
                show(it, "emi_menu_bottomsheet_tag")
            }.setOnClickListener(this)
        }
    }

    override fun onSyncBtnClicked(emi: EMI, position: Int) {

        if (emi.isSynced) {

            requireContext().showToast("Already synced")
        } else {

            if (isInternetAvailable(requireContext())) {

                emi.isSynced = true

                uploadDocumentToFireStore(
                    requireContext(),
                    getString(R.string.emis),
                    emi.key
                )

                emiViewModel.updateEMI(emi)

                emiAdapter.notifyItemChanged(position)
            } else {

                showNoInternetMessage(requireContext())
            }
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

    override fun onDeleteMenuClick() {

        if (this::emiForMenuItems.isInitialized) {

            showAlertDialogForDeletion(
                requireContext(),
                {

                    if (!emiForMenuItems.isSynced) {

                        emiViewModel.deleteEMI(
                            requireContext(),
                            emiForMenuItems
                        )
                    } else {

                        if (isInternetAvailable(requireContext())) {

                            emiViewModel.deleteEMI(
                                requireContext(),
                                emiForMenuItems
                            )
                        } else {

                            showNoInternetMessage(requireContext())
                        }
                    }
                },
                { dialog ->

                    dialog.dismiss()
                }
            )
        }

    }

    override fun onViewSupportingDocumentMenuClick() {

        if (this::emiForMenuItems.isInitialized) {

            if (!emiForMenuItems.isSupportingDocumentAdded) {

                requireContext().showToast(getString(R.string.no_supporting_doc_added))
            } else {

                emiForMenuItems.supportingDocument?.let { supportingDoc ->

                    onViewOrDownloadSupportingDocument(
                        requireActivity(),
                        supportingDoc
                    )
                }
            }
        }

    }

    override fun onReplaceSupportingDocumentClick() {

        requireActivity().supportFragmentManager.let {

            val bundle = Bundle()
            bundle.putString(Constants.COLLECTION_TAG_KEY, getString(R.string.emis))
            bundle.putString(Constants.DOCUMENT_KEY, fromEMIToString(emiForMenuItems))
            bundle.putBoolean(IS_DOCUMENT_FOR_EDITING_KEY, true)

            AddSupportingDocumentBottomSheetFragment.newInstance(
                bundle
            ).apply {
                show(it, "AddSupportingDocTag")
            }
        }

    }

    override fun onDeleteSupportingDocumentClick() {

        if (this::emiForMenuItems.isInitialized) {

            if (emiForMenuItems.isSupportingDocumentAdded) {

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Are you sure?")
                    .setMessage("After deleting you cannot retrieve it again.")
                    .setPositiveButton("Yes") { dialog, _ ->

                        if (emiForMenuItems.supportingDocument?.documentType != DocumentType.URL) {

                            deleteFileFromFirebaseStorage(
                                requireContext(),
                                emiForMenuItems.supportingDocument?.documentUrl!!
                            )

                        }

                        emiForMenuItems.isSupportingDocumentAdded = false
                        emiForMenuItems.supportingDocument = null

                        val map = HashMap<String, Any?>()
                        map["supportingDocumentAdded"] = false
                        map["supportingDocument"] = null

                        updateDocumentOnFireStore(
                            requireContext(),
                            map,
                            getString(R.string.emis),
                            emiForMenuItems.key
                        )

                        emiViewModel.updateEMI(emiForMenuItems)

                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->

                        dialog.dismiss()
                    }
                    .create()
                    .show()

            } else {

                requireContext().showToast(
                    "No supporting document added!!!"
                )
            }
        }


    }

    override fun onSyncMenuClick() {}
    //[END OF MENUS]

    private fun getEMIs() {

        emiViewModel.getAllEMIs().observe(viewLifecycleOwner, { emiList ->

            Log.d(TAG, "getEMIs: $emiList")

            if (emiList.isEmpty()) {

                setNoEMIAddedMessageTVVisibility(true)
            } else {

                setNoEMIAddedMessageTVVisibility(false)
            }

            emiAdapter.submitList(emiList)

            setUpSearchView(emiList)

            setProgressBarVisibility(false)
        })
    }

    private fun setUpSearchView(emiList: List<EMI>?) {

        val searchView =
            binding.emiFragmentToolbar.menu.findItem(R.id.menu_search_home).actionView as SearchView

        searchView.searchText { s ->

            if (s?.trim()?.isEmpty()!!) {

                binding.emiRV.scrollToPosition(0)
                emiAdapter.submitList(emiList)
            } else {

                val filteredList = emiList?.filter { emi ->

                    emi.emiName.lowercase().contains(s.lowercase().trim())
                }

                emiAdapter.submitList(filteredList)

            }

        }

    }

    private fun initListeners() {

        binding.emiFragmentToolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
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
        _binding = null
    }

}
