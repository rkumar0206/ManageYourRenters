package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.databinding.ChooseExpenseCategoryBottomSheetLayoutBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChooseExpenseCategoryBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: ChooseExpenseCategoryBottomSheetLayoutBinding? = null
    private val binding get() = _binding!!

    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(
            R.layout.choose_expense_category_bottom_sheet_layout,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = ChooseExpenseCategoryBottomSheetLayoutBinding.bind(view)


    }

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): ChooseExpenseCategoryBottomSheetFragment {

            val fragment = ChooseExpenseCategoryBottomSheetFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}