package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.monthlyPayment

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.monthlyPaymentAdapters.MonthlyPaymentAdapter
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPayment
import com.rohitthebest.manageyourrenters.database.model.apiModels.MonthlyPaymentCategory
import com.rohitthebest.manageyourrenters.databinding.FragmentMonthlyPaymentBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.CustomMenuItems
import com.rohitthebest.manageyourrenters.ui.viewModels.MonthlyPaymentCategoryViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.MonthlyPaymentViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getMonthList
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import dagger.hilt.android.AndroidEntryPoint
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMonthlyPaymentBinding.bind(view)

        binding.progressbar.show()
        getMessage()

        initListeners()
        monthlyPaymentAdapter = MonthlyPaymentAdapter()

        setUpRecyclerView()

        // todo : save recycler view position
        // todo : in expense graph fragment save the sort type used by the user
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

    override fun onDeleteMenuClick() {

        if (this::monthlyPaymentForMenus.isInitialized) {

            showAlertDialogForDeletion(
                requireContext(),
                { dialog ->

                    if (isInternetAvailable(requireContext())) {

                        monthlyPaymentViewModel.deleteMonthlyPayment(
                            requireContext(), monthlyPaymentForMenus
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
                    requireContext(), monthlyPaymentForMenus
                )
                monthlyPaymentAdapter.notifyItemChanged(monthlyPaymentForMenusAdapterPosition)

                receivedMonthlyPaymentCategory.modified = System.currentTimeMillis()

                monthlyPaymentCategoryViewModel.updateMonthlyPaymentCategory(
                    requireContext(), receivedMonthlyPaymentCategory, false
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
            requireActivity().onBackPressed()
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

                if (payments.isNotEmpty()) {
                    binding.noMonthlyPaymentCategoryTV.hide()
                    setUpSearchView(payments)
                } else {

                    binding.noMonthlyPaymentCategoryTV.show()
                }

                monthlyPaymentAdapter.submitList(payments)

                binding.progressbar.hide()
            }
    }

    private fun setUpSearchView(payments: List<MonthlyPayment>?) {

        val searchView = binding.toolbar.menu.findItem(R.id.menu_search).actionView as SearchView

        searchView.clearFocus()
        searchView.setQuery("", true)

        searchView.searchText { s ->

            if (s?.isEmpty()!!) {

                binding.monthlyPaymentRV.scrollToPosition(0)
                monthlyPaymentAdapter.submitList(payments)
            } else {

                val filteredList = payments?.filter { monthlyPayment ->

                    val periodString =
                        monthlyPaymentViewModel.buildMonthlyPaymentPeriodString(
                            monthlyPayment.monthlyPaymentDateTimeInfo,
                            getMonthList(requireContext())
                        )

                    periodString.lowercase(Locale.ROOT).contains(
                        s.toString().trim().lowercase(Locale.ROOT)
                    ) || monthlyPayment.amount.toString().contains(s.toString().trim())
                }

                monthlyPaymentAdapter.submitList(filteredList)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()

        hideKeyBoard(requireActivity())

        _binding = null
    }
}
