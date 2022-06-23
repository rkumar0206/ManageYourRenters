package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.trackMoneyAdapters.expenseAdapters.ChooseExpenseCategoryAdapter
import com.rohitthebest.manageyourrenters.database.model.apiModels.ExpenseCategory
import com.rohitthebest.manageyourrenters.databinding.ChooseExpenseCategoryBottomSheetLayoutBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.ExpenseCategoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChooseExpenseCategoryBottomSheetFragment : BottomSheetDialogFragment(),
    ChooseExpenseCategoryAdapter.OnClickListener {

    private var _binding: ChooseExpenseCategoryBottomSheetLayoutBinding? = null
    private val binding get() = _binding!!

    private val expenseCategoryViewModel by viewModels<ExpenseCategoryViewModel>()

    private lateinit var chooseExpenseCategoryAdapter: ChooseExpenseCategoryAdapter

    private var mListener: OnItemClicked? = null

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

        chooseExpenseCategoryAdapter = ChooseExpenseCategoryAdapter()

        setUpRecyclerView()

        expenseCategoryViewModel.getAllExpenseCategories()
            .observe(viewLifecycleOwner) { categories ->

                chooseExpenseCategoryAdapter.submitList(categories)
            }

        binding.toolbar.setNavigationOnClickListener {

            dismiss()
        }
    }

    private fun setUpRecyclerView() {

        binding.chooseExpenseCategoryRv.apply {

            adapter = chooseExpenseCategoryAdapter
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 3)
        }

        chooseExpenseCategoryAdapter.setOnClickListener(this)
    }

    override fun onItemClick(expenseCategory: ExpenseCategory) {

        mListener?.onCategoryClicked(expenseCategory)
        dismiss()
    }

    interface OnItemClicked {

        fun onCategoryClicked(expenseCategory: ExpenseCategory)
    }

    fun setOnItemClickedListener(listener: OnItemClicked) {

        mListener = listener
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