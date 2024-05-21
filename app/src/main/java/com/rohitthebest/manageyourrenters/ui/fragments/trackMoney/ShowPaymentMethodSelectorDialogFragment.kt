package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney

import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.SelectPaymentMethodAdapter
import com.rohitthebest.manageyourrenters.data.filter.ExpenseFilterDto
import com.rohitthebest.manageyourrenters.database.model.PaymentMethod
import com.rohitthebest.manageyourrenters.databinding.DialogPaymentMethodSelectorBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.viewModels.PaymentMethodViewModel
import com.rohitthebest.manageyourrenters.utils.convertJsonToObject
import com.rohitthebest.manageyourrenters.utils.isValid
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShowPaymentMethodSelectorDialogFragment :
    BottomSheetDialogFragment(R.layout.dialog_payment_method_selector),
    SelectPaymentMethodAdapter.OnClickListener {

    private var _binding: DialogPaymentMethodSelectorBinding? = null
    private val binding get() = _binding!!

    private val paymentMethodViewModel by viewModels<PaymentMethodViewModel>()

    private lateinit var selectPaymentMethodAdapter: SelectPaymentMethodAdapter

    private var mListener: OnClickListener? = null

    private var selectedPaymentMethods: List<String> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = DialogPaymentMethodSelectorBinding.bind(view)

        selectPaymentMethodAdapter = SelectPaymentMethodAdapter()
        setUpRecyclerView()
        getMessage()
        getAllPaymentMethods()
        initListeners()
    }


    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            arguments?.let { bundle ->

                if (bundle.getString(Constants.EXPENSE_FILTER_KEY).isValid()) {

                    val expenseFilterDto = bundle.getString(Constants.EXPENSE_FILTER_KEY)!!
                        .convertJsonToObject(ExpenseFilterDto::class.java)!!

                    selectedPaymentMethods = expenseFilterDto.paymentMethods
                    getAllPaymentMethods()
                }
            }
        }
    }

    private fun getAllPaymentMethods() {

        paymentMethodViewModel.getAllPaymentMethods()
            .observe(viewLifecycleOwner) { paymentMethods ->

                if (selectedPaymentMethods.isNotEmpty()) {
                    paymentMethods.forEach { pm ->
                        if (selectedPaymentMethods.contains(pm.key)) {
                            pm.isSelected = true
                        }
                    }
                }

                selectPaymentMethodAdapter.submitList(paymentMethods)
            }
    }

    private fun setUpRecyclerView() {

        binding.paymentMethodsRV.apply {
            setHasFixedSize(true)
            adapter = selectPaymentMethodAdapter
            layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        }

        selectPaymentMethodAdapter.setOnClickListener(this)
    }

    override fun onItemClick(paymentMethod: PaymentMethod, position: Int) {

        paymentMethod.isSelected = !paymentMethod.isSelected
        selectPaymentMethodAdapter.notifyItemChanged(position)
    }

    private fun initListeners() {

        binding.toolbar.menu.findItem(R.id.menu_item_filter_apply).apply {

            setOnMenuItemClickListener {

                if (mListener != null) {
                    mListener!!.onFilterApply(
                        selectPaymentMethodAdapter.currentList.filter { it.isSelected }
                            .map { it.key }
                    )
                }

                dismiss()

                true
            }
        }

        binding.toolbar.menu.findItem(R.id.menu_item_filter_clear).setOnMenuItemClickListener {

            if (mListener != null) {
                mListener!!.onFilterApply(emptyList())
            }

            dismiss()

            true
        }

    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }

    interface OnClickListener {

        // method
        fun onFilterApply(selectedPaymentMethods: List<String>?)
    }

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): ShowPaymentMethodSelectorDialogFragment {
            val fragment = ShowPaymentMethodSelectorDialogFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        _binding = null
    }

}