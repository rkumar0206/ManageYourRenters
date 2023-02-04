package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.RadioGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.SelectPaymentMethodAdapter
import com.rohitthebest.manageyourrenters.data.filter.*
import com.rohitthebest.manageyourrenters.database.model.PaymentMethod
import com.rohitthebest.manageyourrenters.databinding.ExpenseFilterLayoutBinding
import com.rohitthebest.manageyourrenters.databinding.FragmentExpenseFilterBinding
import com.rohitthebest.manageyourrenters.others.Constants.EXPENSE_FILTER_KEY
import com.rohitthebest.manageyourrenters.ui.viewModels.PaymentMethodViewModel
import com.rohitthebest.manageyourrenters.utils.convertJsonToObject
import com.rohitthebest.manageyourrenters.utils.isTextValid
import com.rohitthebest.manageyourrenters.utils.isValid
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "ExpenseFilterBottomShee"

@AndroidEntryPoint
class ExpenseFilterBottomSheetFragment :
    BottomSheetDialogFragment(R.layout.fragment_expense_filter),
    CompoundButton.OnCheckedChangeListener, SelectPaymentMethodAdapter.OnClickListener {

    private var _binding: FragmentExpenseFilterBinding? = null
    private val binding get() = _binding!!
    private lateinit var includeBinding: ExpenseFilterLayoutBinding

    private val paymentMethodViewModel by viewModels<PaymentMethodViewModel>()

    private var mListener: OnClickListener? = null
    private lateinit var expenseFilterDto: ExpenseFilterDto
    private lateinit var selectPaymentMethodAdapter: SelectPaymentMethodAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentExpenseFilterBinding.bind(view)

        includeBinding = binding.include

        expenseFilterDto = ExpenseFilterDto()
        selectPaymentMethodAdapter = SelectPaymentMethodAdapter()

        initListeners()
        getAllPaymentMethods()
        setUpPaymentMethodsRecyclerView()
        getMessage()
    }

    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            arguments?.let { bundle ->

                Log.d(TAG, "getMessage: ${bundle.getString(EXPENSE_FILTER_KEY)}")

                if (bundle.getString(EXPENSE_FILTER_KEY).isValid()) {
                    expenseFilterDto = bundle.getString(EXPENSE_FILTER_KEY)!!
                        .convertJsonToObject(ExpenseFilterDto::class.java)!!

                    updateUI()
                }
            }
        }
    }

    private fun updateUI() {

        includeBinding.apply {

            expenseFilterAmountCB.isChecked = expenseFilterDto.isAmountEnabled
            expenseFilterSpentOnCB.isChecked = expenseFilterDto.isSpentOnEnabled
            expenseFilterSortByCB.isChecked = expenseFilterDto.isSortByEnabled
            expenseFilterPaymentMethodCB.isChecked = expenseFilterDto.isPaymentMethodEnabled

            if (expenseFilterDto.isAmountEnabled) {

                amountRG.check(
                    when (expenseFilterDto.selectedAmountFilter) {

                        IntFilterOptions.isLessThan -> isLessThanRB.id
                        IntFilterOptions.isGreaterThan -> isGreaterThanRB.id
                        IntFilterOptions.isEqualsTo -> isEqualToRB.id
                        IntFilterOptions.isBetween -> isBetweenRB.id
                    }
                )

                if (expenseFilterDto.selectedAmountFilter == IntFilterOptions.isBetween) {

                    includeBinding.amountET2.setText(expenseFilterDto.amount.toString())
                    includeBinding.amountET3.setText(expenseFilterDto.amount2.toString())
                }

                amountET.setText(expenseFilterDto.amount.toString())
            }

            if (expenseFilterDto.isSpentOnEnabled) {

                spentOnRG.check(
                    when (expenseFilterDto.selectedSpentOnFilter) {

                        StringFilterOptions.startsWith -> startsWithRB.id
                        StringFilterOptions.endsWith -> endsWithRB.id
                        StringFilterOptions.containsWith -> containsRB.id
                        StringFilterOptions.regex -> regexRB.id
                    }
                )

                spentOnET.setText(expenseFilterDto.spentOnText)
            }

            if (expenseFilterDto.isSortByEnabled) {

                sortByRG.check(
                    when (expenseFilterDto.sortByFilter) {

                        SortFilter.amount -> amountRB.id
                        SortFilter.dateCreated -> dateCreatedRB.id
                        SortFilter.dateModified -> dateModifiedRB.id
                    }
                )

                sortOrderRG.check(
                    when (expenseFilterDto.sortOrder) {

                        SortOrder.ascending -> ascendingRB.id
                        SortOrder.descending -> descendingRB.id
                    }
                )
            }

            if (expenseFilterDto.isPaymentMethodEnabled) {
                getAllPaymentMethods()
            }
        }
    }

    private fun setUpPaymentMethodsRecyclerView() {

        includeBinding.paymentMethodFilterRV.apply {

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

    private fun getAllPaymentMethods() {

        paymentMethodViewModel.getAllPaymentMethods()
            .observe(viewLifecycleOwner) { paymentMethods ->

                paymentMethods.forEach { pm ->
                    if (expenseFilterDto.paymentMethods.contains(pm.key)) {
                        pm.isSelected = true
                    }
                }

                selectPaymentMethodAdapter.submitList(paymentMethods)
            }
    }

    private fun initListeners() {

        includeBinding.expenseFilterAmountCB.setOnCheckedChangeListener(this)
        includeBinding.expenseFilterSortByCB.setOnCheckedChangeListener(this)
        includeBinding.expenseFilterSpentOnCB.setOnCheckedChangeListener(this)
        includeBinding.expenseFilterPaymentMethodCB.setOnCheckedChangeListener(this)
        includeBinding.isBetweenRB.setOnCheckedChangeListener(this)

        binding.toolbar.setNavigationOnClickListener {
            dismiss()
        }

        binding.toolbar.menu.findItem(R.id.menu_item_filter_apply).setOnMenuItemClickListener {

            handleApplyMenuClicked()
            true
        }

        binding.toolbar.menu.findItem(R.id.menu_item_filter_clear).setOnMenuItemClickListener {

            if (mListener != null) {
                mListener!!.onFilterApply(null)
                dismiss()
            }
            true
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {

        when (buttonView?.id) {

            includeBinding.expenseFilterAmountCB.id -> {

                expenseFilterDto.isAmountEnabled = isChecked

                if (isChecked) {
                    includeBinding.amountRG.check(includeBinding.isEqualToRB.id)
                    handleAmountETVisibility(false)
                } else {
                    includeBinding.amountRG.check(RadioGroup.NO_ID)
                    includeBinding.amountET.setText("")
                }
            }

            includeBinding.expenseFilterSortByCB.id -> {

                expenseFilterDto.isSortByEnabled = isChecked

                if (isChecked) {
                    includeBinding.sortByRG.check(includeBinding.dateCreatedRB.id)
                    includeBinding.sortOrderRG.check(includeBinding.descendingRB.id)
                } else {
                    includeBinding.sortByRG.check(RadioGroup.NO_ID)
                    includeBinding.sortOrderRG.check(RadioGroup.NO_ID)
                }
            }

            includeBinding.expenseFilterSpentOnCB.id -> {

                expenseFilterDto.isSpentOnEnabled = isChecked

                if (isChecked) {

                    includeBinding.spentOnRG.check(includeBinding.containsRB.id)
                } else {
                    includeBinding.spentOnRG.check(RadioGroup.NO_ID)
                    includeBinding.spentOnET.setText("")
                }
            }

            includeBinding.expenseFilterPaymentMethodCB.id -> {

                expenseFilterDto.isPaymentMethodEnabled = isChecked
            }

            includeBinding.isBetweenRB.id -> {

                handleAmountETVisibility(isChecked)
            }
        }
    }

    private fun handleAmountETVisibility(isBetweenChecked: Boolean) {

        includeBinding.amountET2.isVisible = isBetweenChecked
        includeBinding.amountET3.isVisible = isBetweenChecked
        includeBinding.andTV.isVisible = isBetweenChecked
        includeBinding.amountET.isVisible = !isBetweenChecked
    }

    private fun handleApplyMenuClicked() {

        expenseFilterDto.apply {

            if (isAmountEnabled) {

                if (includeBinding.amountRG.checkedRadioButtonId == includeBinding.isBetweenRB.id) {

                    amount =
                        if (includeBinding.amountET2.isTextValid()) includeBinding.amountET2.text.toString()
                            .trim().toDouble() else 0.0

                    amount2 =
                        if (includeBinding.amountET3.isTextValid()) includeBinding.amountET3.text.toString()
                            .trim().toDouble() else 0.0
                } else {

                    amount =
                        if (includeBinding.amountET.isTextValid()) includeBinding.amountET.text.toString()
                            .trim().toDouble()
                        else
                            0.0
                }

                selectedAmountFilter = when (includeBinding.amountRG.checkedRadioButtonId) {

                    includeBinding.isLessThanRB.id -> IntFilterOptions.isLessThan
                    includeBinding.isGreaterThanRB.id -> IntFilterOptions.isGreaterThan
                    includeBinding.isEqualToRB.id -> IntFilterOptions.isEqualsTo
                    includeBinding.isBetweenRB.id -> IntFilterOptions.isBetween
                    else -> IntFilterOptions.isEqualsTo
                }
            }

            if (isSpentOnEnabled) {

                selectedSpentOnFilter = when (includeBinding.spentOnRG.checkedRadioButtonId) {

                    includeBinding.startsWithRB.id -> StringFilterOptions.startsWith
                    includeBinding.endsWithRB.id -> StringFilterOptions.endsWith
                    includeBinding.containsRB.id -> StringFilterOptions.containsWith
                    includeBinding.regexRB.id -> StringFilterOptions.regex
                    else -> StringFilterOptions.containsWith
                }

                spentOnText =
                    if (includeBinding.spentOnET.isTextValid()) includeBinding.spentOnET.text.toString()
                        .trim()
                    else
                        ""
            }

            if (isPaymentMethodEnabled) {

                paymentMethods =
                    if (this@ExpenseFilterBottomSheetFragment::selectPaymentMethodAdapter.isInitialized)
                        selectPaymentMethodAdapter.currentList.filter { pm -> pm.isSelected }
                            .map { pm -> pm.key } else emptyList()
            }

            if (isSortByEnabled) {

                sortByFilter = when (includeBinding.sortByRG.checkedRadioButtonId) {

                    includeBinding.amountRB.id -> SortFilter.amount
                    includeBinding.dateCreatedRB.id -> SortFilter.dateCreated
                    includeBinding.dateModifiedRB.id -> SortFilter.dateModified
                    else -> SortFilter.dateCreated
                }

                sortOrder = when (includeBinding.sortOrderRG.checkedRadioButtonId) {

                    includeBinding.ascendingRB.id -> SortOrder.ascending
                    includeBinding.descendingRB.id -> SortOrder.descending
                    else -> SortOrder.descending
                }
            }
        }

        if (mListener != null) {
            mListener!!.onFilterApply(expenseFilterDto)
            dismiss()
        }
    }

    fun setOnClickListener(listener: OnClickListener) {
        mListener = listener
    }

    interface OnClickListener {

        // method
        fun onFilterApply(expenseFilterDto: ExpenseFilterDto?)
    }

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): ExpenseFilterBottomSheetFragment {
            val fragment = ExpenseFilterBottomSheetFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}