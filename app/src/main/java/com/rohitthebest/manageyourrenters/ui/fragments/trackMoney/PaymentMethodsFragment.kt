package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.PaymentMethodsAdapter
import com.rohitthebest.manageyourrenters.data.CustomDateRange
import com.rohitthebest.manageyourrenters.data.ShowExpenseBottomSheetTagsEnum
import com.rohitthebest.manageyourrenters.database.model.PaymentMethod
import com.rohitthebest.manageyourrenters.databinding.FragmentPaymentMethodsBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.fragments.AddEditPaymentMethodBottomSheetFragment
import com.rohitthebest.manageyourrenters.ui.fragments.CustomMenuItems
import com.rohitthebest.manageyourrenters.ui.viewModels.PaymentMethodViewModel
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.changeVisibilityOfFABOnScrolled
import com.rohitthebest.manageyourrenters.utils.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.showAlertDialogForDeletion
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "PaymentMethodsFragment"

@AndroidEntryPoint
class PaymentMethodsFragment : Fragment(R.layout.fragment_payment_methods),
    PaymentMethodsAdapter.OnClickListener, CustomMenuItems.OnItemClickListener {

    private var _binding: FragmentPaymentMethodsBinding? = null
    private val binding get() = _binding!!

    private val paymentMethodsViewModel by viewModels<PaymentMethodViewModel>()
    private lateinit var paymentMethodsAdapter: PaymentMethodsAdapter

    private var paymentMethodForMenu: PaymentMethod? = null
    private var paymentMethodRVPositionForMenu = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentPaymentMethodsBinding.bind(view)

        paymentMethodsAdapter = PaymentMethodsAdapter()
        initListeners()
        setUpRecyclerView()

        lifecycleScope.launch {
            delay(300)
            getAllPaymentMethods()
        }
    }

    private fun getAllPaymentMethods() {

        paymentMethodsViewModel.getAllPaymentMethods().observe(viewLifecycleOwner) {

            paymentMethodsAdapter.submitList(it)
        }
    }

    private fun setUpRecyclerView() {

        binding.paymentMethodsRV.apply {
            adapter = paymentMethodsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            changeVisibilityOfFABOnScrolled(binding.addPaymentMethodFAB)
            hasFixedSize()
        }

        paymentMethodsAdapter.setOnClickListener(this)
    }

    override fun onItemClick(paymentMethod: PaymentMethod, position: Int) {

        val action =
            PaymentMethodsFragmentDirections.actionPaymentMethodsFragmentToShowExpenseBottomSheetFragment(
                paymentMethodKey = paymentMethod.key,
                callingFragementTag = ShowExpenseBottomSheetTagsEnum.PAYMENT_METHOD_FRAGMENT,
                dateRangeMessage = CustomDateRange.ALL_TIME,
                date1 = 0L,
                date2 = 0L
            )

        findNavController().navigate(action)
    }

    override fun onMenuBtnClicked(paymentMethod: PaymentMethod, position: Int) {

        paymentMethodForMenu = paymentMethod
        paymentMethodRVPositionForMenu = position

        val defaultPaymentMethods = listOf(
            Constants.PAYMENT_METHOD_OTHER_KEY,
            Constants.PAYMENT_METHOD_CASH_KEY,
            Constants.PAYMENT_METHOD_DEBIT_CARD_KEY,
            Constants.PAYMENT_METHOD_CREDIT_CARD_KEY
        )

        requireActivity().supportFragmentManager.let { fm ->

            val bundle = Bundle()
            bundle.putBoolean(
                Constants.SHOW_EDIT_MENU,
                !defaultPaymentMethods.contains(paymentMethod.key)
            )
            bundle.putBoolean(
                Constants.SHOW_DELETE_MENU,
                !defaultPaymentMethods.contains(paymentMethod.key)
            )
            bundle.putBoolean(Constants.SHOW_DOCUMENTS_MENU, false)
            bundle.putBoolean(Constants.SHOW_COPY_MENU, false)
            bundle.putBoolean(Constants.SHOW_MOVE_MENU, false)
            bundle.putBoolean(Constants.SHOW_SYNC_MENU, !paymentMethod.isSynced)
            bundle.putString(Constants.COPY_MENU_TEXT, "")

            CustomMenuItems.newInstance(
                bundle
            ).apply {

                show(fm, TAG)
            }.setOnClickListener(this)
        }
    }


    override fun onEditMenuClick() {

        paymentMethodForMenu?.let {
            showAddEditPaymentMethodBottomSheet(true)
        }
    }

    override fun onSyncMenuClick() {

        paymentMethodForMenu?.let { paymentMethod ->
            if (paymentMethod.isSynced) {
                requireContext().showToast(getString(R.string.already_synced))
            } else {
                if (requireContext().isInternetAvailable()) {
                    paymentMethod.isSynced = true
                    paymentMethodsViewModel.syncPaymentWithCloud(paymentMethod)
                } else {
                    requireContext().showToast(Constants.NO_INTERNET_MESSAGE)
                }
            }
        }
    }

    override fun onDeleteMenuClick() {

        paymentMethodForMenu?.let { paymentMethod ->
            showAlertDialogForDeletion(
                requireContext(),
                {
                    paymentMethodsViewModel.deletePaymentMethod(paymentMethod = paymentMethod)
                },
                {
                    it.dismiss()
                }
            )
        }
    }

    override fun onCopyMenuClick() {}
    override fun onMoveMenuClick() {}
    override fun onViewSupportingDocumentMenuClick() {}
    override fun onReplaceSupportingDocumentClick() {}
    override fun onDeleteSupportingDocumentClick() {}


    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.addPaymentMethodFAB.setOnClickListener {
            showAddEditPaymentMethodBottomSheet()
        }
    }

    private fun showAddEditPaymentMethodBottomSheet(isForEdit: Boolean = false) {

        requireActivity().supportFragmentManager.let { fm ->

            val bundle = Bundle()
            bundle.putBoolean(Constants.IS_FOR_EDIT, isForEdit)

            if (isForEdit) {
                paymentMethodForMenu?.let { paymentMethod ->
                    bundle.putString(Constants.PAYMENT_METHOD_KEY_FOR_EDIT, paymentMethod.key)
                }
            }

            AddEditPaymentMethodBottomSheetFragment.newInstance(bundle)
                .apply {
                    show(fm, TAG)
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}