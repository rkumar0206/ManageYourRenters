package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense.budgetAndIncome

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.Budget
import com.rohitthebest.manageyourrenters.databinding.EditTextBottomSheetLayoutBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.viewModels.BudgetViewModel
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.isNotValid
import com.rohitthebest.manageyourrenters.utils.isTextValid
import com.rohitthebest.manageyourrenters.utils.isValid
import com.rohitthebest.manageyourrenters.utils.onTextChangedListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddBudgetLimitBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: EditTextBottomSheetLayoutBinding? = null
    private val binding get() = _binding!!

    private val budgetViewModel by viewModels<BudgetViewModel>()
    private var receivedBudgetKey = ""
    private lateinit var receivedBudget: Budget

    private var isForEdit = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.edit_text_bottom_sheet_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = EditTextBottomSheetLayoutBinding.bind(view)

        binding.toolbar.title = getString(R.string.add_budget_limit)
        setUpEditText()

        getMessage()
        initListeners()
        textWatchers()
    }

    private fun setUpEditText() {

        binding.editText.counterMaxLength = 12
        binding.editText.hint = getString(R.string.enter_budget_limit)

        binding.editText.editText?.apply {

            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

            val maxLengthFilter: Array<InputFilter> = arrayOf(InputFilter.LengthFilter(12))
            filters = maxLengthFilter

            hint = getString(R.string.enter_budget_limit)
        }

        binding.editText.editText?.requestFocus()
        Functions.showKeyboard(requireActivity(), binding.editText.editText!!)

    }

    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            arguments?.let { bundle ->

                try {

                    isForEdit = bundle.getBoolean(Constants.IS_FOR_EDIT, false)

                    if (isForEdit) {

                        receivedBudgetKey = bundle.getString(Constants.DOCUMENT_KEY, "")
                        if (receivedBudgetKey.isNotValid()) {
                            requireContext().showToast(getString(R.string.something_went_wrong))
                            dismiss()
                        } else {

                            getBudgetByKey()
                        }
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }

    }

    private fun getBudgetByKey() {

        budgetViewModel.getBudgetByKey(receivedBudgetKey).observe(viewLifecycleOwner) { budget ->

            receivedBudget = budget
            updateUI()
        }
    }

    private fun updateUI() {

        if (this::receivedBudget.isInitialized) {
            binding.editText.editText?.setText(receivedBudget.budgetLimit.toString())
        }
    }

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {
            showToast(requireContext(), getString(R.string.cancelled))
            Functions.hideKeyBoard(requireActivity())
            dismiss()
        }

        binding.toolbar.menu.findItem(R.id.menu_save_btn).setOnMenuItemClickListener {


            if (isFormValid()) {

                if (isForEdit) {

                }

                // todo: save payment method
            }

            true
        }
    }

    private fun isFormValid(): Boolean {

        if (!binding.editText.editText?.isTextValid()!!) {
            binding.editText.error = Constants.EDIT_TEXT_EMPTY_MESSAGE
            return false
        }

        return binding.editText.error == null
    }

    private fun textWatchers() {

        binding.editText.editText?.onTextChangedListener { s ->

            if (!s.toString().isValid()) {

                binding.editText.error = Constants.EDIT_TEXT_EMPTY_MESSAGE
            } else {

                if (this::receivedBudget.isInitialized &&
                    s?.toString()?.trim()?.toDouble()!! <= 0.0
                ) {
                    binding.editText.error =
                        getString(R.string.budget_limit_should_be_grater_than_0)
                } else {
                    binding.editText.error = null
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): AddBudgetLimitBottomSheetFragment {
            val fragment = AddBudgetLimitBottomSheetFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

}