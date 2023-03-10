package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.monthlyPayment

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
import com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.monthlyPaymentAdapters.MonthlyPaymentCategoryAdapter
import com.rohitthebest.manageyourrenters.database.model.MonthlyPaymentCategory
import com.rohitthebest.manageyourrenters.databinding.FragmentMonthlyPaymentCategoryBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.fragments.CustomMenuItems
import com.rohitthebest.manageyourrenters.ui.viewModels.MonthlyPaymentCategoryViewModel
import com.rohitthebest.manageyourrenters.utils.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "MonthlyPaymentCategoryFragment"

@AndroidEntryPoint
class MonthlyPaymentCategoryFragment : Fragment(R.layout.fragment_monthly_payment_category),
    MonthlyPaymentCategoryAdapter.OnClickListener, CustomMenuItems.OnItemClickListener {

    private var _binding: FragmentMonthlyPaymentCategoryBinding? = null
    private val binding get() = _binding!!
    private val monthlyPaymentCategoryViewModel by viewModels<MonthlyPaymentCategoryViewModel>()

    private var rvStateParcelable: Parcelable? = null

    private lateinit var monthlyPaymentCategoryAdapter: MonthlyPaymentCategoryAdapter
    private var searchView: SearchView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMonthlyPaymentCategoryBinding.bind(view)

        monthlyPaymentCategoryAdapter = MonthlyPaymentCategoryAdapter()

        initListeners()
        binding.progressbar.show()
        getMonthlyPaymentCategoryRvState()
        lifecycleScope.launch {
            delay(300)
            observeMonthlyPaymentCategories()
        }
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {

        binding.monthlyPaymentCategoryRV.apply {

            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = monthlyPaymentCategoryAdapter
            changeVisibilityOfFABOnScrolled(binding.addMonthlyPaymentCategoryFAB)
        }

        monthlyPaymentCategoryAdapter.setOnClickListener(this)
    }

    override fun onItemClick(monthlyPaymentCategory: MonthlyPaymentCategory) {

        val action =
            MonthlyPaymentCategoryFragmentDirections.actionMonthlyPaymentCategoryFragmentToMonthlyPaymentFragment(
                monthlyPaymentCategory.key
            )

        findNavController().navigate(action)

    }

    private lateinit var monthlyPaymentCategoryForMenus: MonthlyPaymentCategory
    private var itemPosition = 0

    override fun onMenuBtnClicked(monthlyPaymentCategory: MonthlyPaymentCategory, position: Int) {

        monthlyPaymentCategoryForMenus = monthlyPaymentCategory
        itemPosition = position

        requireActivity().supportFragmentManager.let { fm ->

            val bundle = Bundle()
            bundle.putBoolean(Constants.SHOW_EDIT_MENU, true)
            bundle.putBoolean(Constants.SHOW_DELETE_MENU, true)
            bundle.putBoolean(Constants.SHOW_DOCUMENTS_MENU, false)

            if (!monthlyPaymentCategory.isSynced) {

                bundle.putBoolean(Constants.SHOW_SYNC_MENU, true)
            }

            CustomMenuItems.newInstance(bundle).apply {
                show(fm, TAG)
            }.setOnClickListener(this)
        }
    }

    override fun onEditMenuClick() {

        val action =
            MonthlyPaymentCategoryFragmentDirections.actionMonthlyPaymentCategoryFragmentToAddEditMonthlyPaymentCategory(
                monthlyPaymentCategoryForMenus.key
            )

        findNavController().navigate(action)
    }

    override fun onCopyMenuClick() {}

    override fun onMoveMenuClick() {}

    override fun onDeleteMenuClick() {
        showAlertDialogForDeletion(
            requireContext(),
            { dialog ->

                if (this::monthlyPaymentCategoryForMenus.isInitialized) {

                    // internet is always required to delete any object,
                    // even if it is not synced to the cloud, as it may have been inserted to the
                    // cloud database and been updated later and is not synced now
                    if (Functions.isInternetAvailable(requireContext())) {

                        monthlyPaymentCategoryViewModel.deleteMonthlyPaymentCategory(
                            monthlyPaymentCategoryForMenus
                        )
                    } else {
                        Functions.showNoInternetMessage(requireContext())
                    }
                }

                dialog.dismiss()
            },
            { dialog ->

                dialog.dismiss()
            }
        )
    }

    override fun onSyncMenuClick() {

        if (this::monthlyPaymentCategoryForMenus.isInitialized && !monthlyPaymentCategoryForMenus.isSynced) {

            if (Functions.isInternetAvailable(requireContext())) {

                monthlyPaymentCategoryViewModel.insertMonthlyPaymentCategory(
                    monthlyPaymentCategoryForMenus
                )
                monthlyPaymentCategoryAdapter.notifyItemChanged(itemPosition)
            } else {

                Functions.showNoInternetMessage(requireContext())
            }
        }
    }

    override fun onViewSupportingDocumentMenuClick() {}

    override fun onReplaceSupportingDocumentClick() {}

    override fun onDeleteSupportingDocumentClick() {}

    private fun getMonthlyPaymentCategoryRvState() {

        monthlyPaymentCategoryViewModel.monthlyPaymentCategoryRvState.observe(viewLifecycleOwner) { parcelable ->

            parcelable?.let {

                rvStateParcelable = it
            }
        }
    }

    private fun observeMonthlyPaymentCategories() {

        monthlyPaymentCategoryViewModel.getAllMonthlyPaymentCategories()
            .observe(viewLifecycleOwner) { categories ->

                if (searchView != null && searchView!!.query.toString().isValid()) {

                    setUpSearchView(categories)
                } else {

                    if (categories.isNotEmpty()) {

                        setNoMonthlyPaymentCategoryMessageTvVisibility(false)
                        setUpSearchView(categories)
                    } else {

                        binding.noMonthlyPaymentCategoryAddedTV.text =
                            getString(R.string.no_monthly_payment_category_added_message)
                        setNoMonthlyPaymentCategoryMessageTvVisibility(true)
                    }
                    monthlyPaymentCategoryAdapter.submitList(categories)
                }
                binding.monthlyPaymentCategoryRV.layoutManager?.onRestoreInstanceState(
                    rvStateParcelable
                )
                binding.progressbar.hide()
            }
    }

    private var searchTextDelayJob: Job? = null
    private fun setUpSearchView(monthlyPaymentCategories: List<MonthlyPaymentCategory>) {

        searchView =
            binding.toolbar.menu.findItem(R.id.menu_search_monthly_payment_category).actionView as SearchView

        searchView?.let { sv ->

            if (sv.query.toString().isValid()) {
                searchMonthlyPaymentCategory(sv.query.toString(), monthlyPaymentCategories)
            }

            sv.onTextSubmit { query ->
                searchMonthlyPaymentCategory(
                    query,
                    monthlyPaymentCategories
                )
            }
            sv.onTextChanged { query ->

                searchTextDelayJob = lifecycleScope.launch {
                    searchTextDelayJob?.executeAfterDelay {
                        searchMonthlyPaymentCategory(query, monthlyPaymentCategories)
                    }
                }
            }
        }
    }

    private fun searchMonthlyPaymentCategory(
        query: String?,
        monthlyPaymentCategories: List<MonthlyPaymentCategory>
    ) {

        if (query?.isEmpty()!!) {

            binding.monthlyPaymentCategoryRV.scrollToPosition(0)
            monthlyPaymentCategoryAdapter.submitList(monthlyPaymentCategories)

            if (monthlyPaymentCategories.isNotEmpty()) {

                setNoMonthlyPaymentCategoryMessageTvVisibility(false)
            } else {

                binding.noMonthlyPaymentCategoryAddedTV.text =
                    getString(R.string.no_monthly_payment_category_added_message)
                setNoMonthlyPaymentCategoryMessageTvVisibility(true)
            }

        } else {

            val filteredList = monthlyPaymentCategories.filter { monthlyPaymentCategory ->

                monthlyPaymentCategory.categoryName.lowercase(Locale.ROOT).contains(
                    query.toString().trim().lowercase(Locale.ROOT)
                )
            }

            if (filteredList.isNotEmpty()) {

                setNoMonthlyPaymentCategoryMessageTvVisibility(false)
            } else {

                binding.noMonthlyPaymentCategoryAddedTV.text = getString(R.string.no_records_found)
                setNoMonthlyPaymentCategoryMessageTvVisibility(true)
            }
            monthlyPaymentCategoryAdapter.submitList(filteredList)
        }

    }

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.addMonthlyPaymentCategoryFAB.setOnClickListener {

            findNavController().navigate(R.id.action_monthlyPaymentCategoryFragment_to_addEditMonthlyPaymentCategory)
        }
    }

    private fun setNoMonthlyPaymentCategoryMessageTvVisibility(isVisible: Boolean) {

        binding.monthlyPaymentCategoryRV.isVisible = !isVisible
        binding.noMonthlyPaymentCategoryAddedTV.isVisible = isVisible
    }


    override fun onDestroyView() {
        super.onDestroyView()

        monthlyPaymentCategoryViewModel.saveMonthlyPaymentCategoryRvState(
            binding.monthlyPaymentCategoryRV.layoutManager?.onSaveInstanceState()
        )

        Functions.hideKeyBoard(requireActivity())

        _binding = null
    }
}
