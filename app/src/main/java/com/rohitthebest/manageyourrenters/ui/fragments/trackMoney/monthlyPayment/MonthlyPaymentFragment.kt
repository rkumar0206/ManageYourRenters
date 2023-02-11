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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.monthlyPaymentAdapters.MonthlyPaymentAdapter
import com.rohitthebest.manageyourrenters.database.model.MonthlyPayment
import com.rohitthebest.manageyourrenters.database.model.MonthlyPaymentCategory
import com.rohitthebest.manageyourrenters.databinding.FragmentMonthlyPaymentBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.fragments.CustomMenuItems
import com.rohitthebest.manageyourrenters.ui.viewModels.MonthlyPaymentCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.MonthlyPaymentViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getMonthList
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "MonthlyPaymentFragment"

@AndroidEntryPoint
class MonthlyPaymentFragment : Fragment(R.layout.fragment_monthly_payment),
    MonthlyPaymentAdapter.OnClickListener, CustomMenuItems.OnItemClickListener {

    private var _binding: FragmentMonthlyPaymentBinding? = null
    private val binding get() = _binding!!

    private val monthlyPaymentViewModel by viewModels<MonthlyPaymentViewModel>()
    private val monthlyPaymentCategoryViewModel by viewModels<MonthlyPaymentCategoryViewModel>()

    private lateinit var receivedMonthlyPaymentCategoryKey: String
    private lateinit var receivedMonthlyPaymentCategory: MonthlyPaymentCategory

    private lateinit var monthlyPaymentAdapter: MonthlyPaymentAdapter

    private var rvStateParcelable: Parcelable? = null
    private var searchView: SearchView? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMonthlyPaymentBinding.bind(view)

        binding.progressbar.show()
        getMessage()

        initListeners()
        monthlyPaymentAdapter = MonthlyPaymentAdapter()

        getMonthlyPaymentRvState()

        setUpRecyclerView()
    }

    private fun getMonthlyPaymentRvState() {

        monthlyPaymentViewModel.monthlyPaymentRvState.observe(viewLifecycleOwner) { rvStateParcelable ->

            rvStateParcelable?.let {

                this.rvStateParcelable = it
            }
        }
    }

    private fun setUpRecyclerView() {

        binding.monthlyPaymentRV.apply {

            setHasFixedSize(true)
            adapter = monthlyPaymentAdapter
            layoutManager = LinearLayoutManager(requireContext())
            changeVisibilityOfFABOnScrolled(binding.addMonthlyPaymentsFAB)
        }

        monthlyPaymentAdapter.setOnClickListener(this)
    }


    override fun onItemClick(monthlyPayment: MonthlyPayment) {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Payment info")
            .setMessage(
                monthlyPaymentViewModel.buildMonthlyPaymentInfoStringForAlertDialogMessage(
                    monthlyPayment,
                    receivedMonthlyPaymentCategory,
                    getMonthList(requireContext())
                )
            )
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onMessageBtnClicked(message: String) {

        val msg = if (message.isValid()) message else "No message added for this payment"

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Payment message")
            .setMessage(msg)
            .setPositiveButton("Ok") { dialogInterface, _ ->

                dialogInterface.dismiss()
            }
            .create()
            .show()

    }

    private lateinit var monthlyPaymentForMenus: MonthlyPayment
    private var monthlyPaymentForMenusAdapterPosition: Int = 0

    override fun onMenuBtnClicked(monthlyPayment: MonthlyPayment, position: Int) {

        monthlyPaymentForMenus = monthlyPayment
        monthlyPaymentForMenusAdapterPosition = position

        requireActivity().supportFragmentManager.let { fm ->

            val bundle = Bundle()
            bundle.putBoolean(Constants.SHOW_EDIT_MENU, true)
            bundle.putBoolean(Constants.SHOW_DELETE_MENU, true)
            bundle.putBoolean(Constants.SHOW_DOCUMENTS_MENU, false)

            if (!monthlyPayment.isSynced) {

                bundle.putBoolean(Constants.SHOW_SYNC_MENU, true)
            }

            CustomMenuItems.newInstance(
                bundle
            ).apply {

                show(fm, TAG)
            }.setOnClickListener(this)

        }
    }

    override fun onEditMenuClick() {

        val action =
            MonthlyPaymentFragmentDirections.actionMonthlyPaymentFragmentToAddEditMonthlyPaymentFragment(
                receivedMonthlyPaymentCategoryKey, monthlyPaymentForMenus.key
            )

        findNavController().navigate(action)
    }

    override fun onCopyMenuClick() {}

    override fun onMoveMenuClick() {}

    override fun onDeleteMenuClick() {

        if (this::monthlyPaymentForMenus.isInitialized) {

            showAlertDialogForDeletion(
                requireContext(),
                { dialog ->

                    if (isInternetAvailable(requireContext())) {

                        monthlyPaymentViewModel.deleteMonthlyPayment(
                            monthlyPaymentForMenus
                        )
                    } else {

                        showNoInternetMessage(requireContext())
                    }

                    dialog.dismiss()
                },
                {
                    it.dismiss()
                }
            )
        }
    }

    override fun onSyncMenuClick() {

        if (this::monthlyPaymentForMenus.isInitialized && !monthlyPaymentForMenus.isSynced) {

            if (isInternetAvailable(requireContext())) {

                monthlyPaymentViewModel.insertMonthlyPayment(
                    monthlyPaymentForMenus
                )
                monthlyPaymentAdapter.notifyItemChanged(monthlyPaymentForMenusAdapterPosition)

                val oldValue = receivedMonthlyPaymentCategory.copy()
                receivedMonthlyPaymentCategory.modified = System.currentTimeMillis()
                monthlyPaymentCategoryViewModel.updateMonthlyPaymentCategory(
                    oldValue, receivedMonthlyPaymentCategory
                )

            } else {

                showNoInternetMessage(requireContext())
            }
        }
    }

    override fun onViewSupportingDocumentMenuClick() {}

    override fun onReplaceSupportingDocumentClick() {}

    override fun onDeleteSupportingDocumentClick() {}

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.addMonthlyPaymentsFAB.setOnClickListener {

            if (receivedMonthlyPaymentCategoryKey.isValid()) {

                val action =
                    MonthlyPaymentFragmentDirections.actionMonthlyPaymentFragmentToAddEditMonthlyPaymentFragment(
                        receivedMonthlyPaymentCategoryKey, ""
                    )

                findNavController().navigate(action)
            } else {

                Functions.showToast(requireContext(), "No category chosen!!!")
            }
        }

    }

    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            val args = arguments?.let { bundle ->

                MonthlyPaymentFragmentArgs.fromBundle(bundle)
            }

            receivedMonthlyPaymentCategoryKey = args?.monthlyPaymentCategoryKey!!

            lifecycleScope.launch {

                delay(300)
                getMonthlyPaymentCategory()
            }

        }
    }

    private fun getMonthlyPaymentCategory() {

        monthlyPaymentCategoryViewModel.getMonthlyPaymentCategoryUsingKey(
            receivedMonthlyPaymentCategoryKey
        )
            .observe(viewLifecycleOwner) { category ->

                receivedMonthlyPaymentCategory = category
                binding.toolbar.title = "${category.categoryName} payments"

                observeMonthlyPayments()
            }
    }

    private fun observeMonthlyPayments() {

        monthlyPaymentViewModel.getAllMonthlyPaymentsByCategoryKey(receivedMonthlyPaymentCategoryKey)
            .observe(viewLifecycleOwner) { payments ->

                if (searchView != null && searchView!!.query.toString().isValid()) {

                    setUpSearchView(payments)
                } else {

                    if (payments.isNotEmpty()) {
                        setNoMonthlyPaymentMessageTvVisibility(false)
                        setUpSearchView(payments)
                    } else {

                        binding.noMonthlyPaymentAddedTV.text =
                            getString(R.string.no_monthly_payment_added_message)
                        setNoMonthlyPaymentMessageTvVisibility(true)
                    }

                    monthlyPaymentAdapter.submitList(payments)
                }
                binding.monthlyPaymentRV.layoutManager?.onRestoreInstanceState(rvStateParcelable)

                binding.progressbar.hide()
            }
    }

    private var searchTextDelayJob: Job? = null
    private fun setUpSearchView(payments: List<MonthlyPayment>) {

        searchView = binding.toolbar.menu.findItem(R.id.menu_search).actionView as SearchView

        searchView?.let { sv ->

            if (sv.query.toString().isValid()) {
                searchMonthlyPayments(sv.query.toString(), payments)
            }

            sv.onTextSubmit { query -> searchMonthlyPayments(query, payments) }

            sv.onTextChanged { query ->
                searchTextDelayJob = lifecycleScope.launch {
                    searchTextDelayJob?.executeAfterDelay {
                        searchMonthlyPayments(query, payments)
                    }
                }
            }
        }
    }

    private fun searchMonthlyPayments(query: String?, payments: List<MonthlyPayment>) {

        if (query?.isEmpty()!!) {

            binding.monthlyPaymentRV.scrollToPosition(0)
            monthlyPaymentAdapter.submitList(payments)

            if (payments.isNotEmpty()) {

                setNoMonthlyPaymentMessageTvVisibility(false)
            } else {

                binding.noMonthlyPaymentAddedTV.text =
                    getString(R.string.no_monthly_payment_added_message)
                setNoMonthlyPaymentMessageTvVisibility(true)
            }

        } else {

            val filteredList = payments.filter { monthlyPayment ->

                val periodString =
                    monthlyPaymentViewModel.buildMonthlyPaymentPeriodString(
                        monthlyPayment.monthlyPaymentDateTimeInfo,
                        getMonthList(requireContext())
                    )

                periodString.lowercase(Locale.ROOT).contains(
                    query.toString().trim().lowercase(Locale.ROOT)
                ) || monthlyPayment.amount.toString().contains(query.toString().trim())
            }

            if (filteredList.isNotEmpty()) {

                setNoMonthlyPaymentMessageTvVisibility(false)
            } else {

                binding.noMonthlyPaymentAddedTV.text = getString(R.string.no_records_found)
                setNoMonthlyPaymentMessageTvVisibility(true)
            }

            monthlyPaymentAdapter.submitList(filteredList)
        }
    }

    private fun setNoMonthlyPaymentMessageTvVisibility(isVisible: Boolean) {

        binding.monthlyPaymentRV.isVisible = !isVisible
        binding.noMonthlyPaymentAddedTV.isVisible = isVisible
    }


    override fun onDestroyView() {
        super.onDestroyView()

        monthlyPaymentViewModel.saveMonthlyPaymentRvState(
            binding.monthlyPaymentRV.layoutManager?.onSaveInstanceState()
        )

        hideKeyBoard(requireActivity())

        _binding = null
    }
}
