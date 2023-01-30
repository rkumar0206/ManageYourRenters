package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.PaymentMethodsAdapter
import com.rohitthebest.manageyourrenters.database.model.PaymentMethod
import com.rohitthebest.manageyourrenters.databinding.FragmentPaymentMethodsBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.PaymentMethodViewModel
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.changeVisibilityOfFABOnScrolled
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PaymentMethodsFragment : Fragment(R.layout.fragment_payment_methods),
    PaymentMethodsAdapter.OnClickListener {

    private var _binding: FragmentPaymentMethodsBinding? = null
    private val binding get() = _binding!!

    private val paymentMethodsViewModel by viewModels<PaymentMethodViewModel>()
    private lateinit var paymentMethodsAdapter: PaymentMethodsAdapter

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
        requireContext().showToast("item : ${paymentMethod.paymentMethod}")
    }

    override fun onEditBtnClicked(paymentMethod: PaymentMethod, position: Int) {
        requireContext().showToast("item edit: $paymentMethod")
    }

    override fun onSyncBtnClicked(paymentMethod: PaymentMethod, position: Int) {
        requireContext().showToast("item sync: ${paymentMethod.isSynced}")
    }

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}