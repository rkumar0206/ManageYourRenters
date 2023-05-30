package com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.expense.budgetAndIncome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.SelectPaymentMethodAdapter
import com.rohitthebest.manageyourrenters.database.model.Income
import com.rohitthebest.manageyourrenters.database.model.PaymentMethod
import com.rohitthebest.manageyourrenters.databinding.FragmentAddIncomeBootmsheetBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.EDIT_TEXT_EMPTY_MESSAGE
import com.rohitthebest.manageyourrenters.ui.fragments.AddEditPaymentMethodBottomSheetFragment
import com.rohitthebest.manageyourrenters.ui.viewModels.IncomeViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.PaymentMethodViewModel
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.isTextValid
import com.rohitthebest.manageyourrenters.utils.isValid
import com.rohitthebest.manageyourrenters.utils.onTextChangedListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "AddIncomeBottomSheetFra"

@AndroidEntryPoint
class AddIncomeBottomSheetFragment : BottomSheetDialogFragment(),
    SelectPaymentMethodAdapter.OnClickListener {

    private var _binding: FragmentAddIncomeBootmsheetBinding? = null
    private val binding get() = _binding!!

    private val incomeViewModel by viewModels<IncomeViewModel>()
    private val paymentMethodViewModel by viewModels<PaymentMethodViewModel>()

    private var isForEdit = false
    private var receivedMonth: Int = 0
    private var receivedYear: Int = 0
    private var receivedIncomeKey = ""
    private lateinit var receivedIncome: Income

    private var mListener: OnIncomeBottomSheetDismissListener? = null
    private lateinit var selectPaymentMethodAdapter: SelectPaymentMethodAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_add_income_bootmsheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddIncomeBootmsheetBinding.bind(view)

        selectPaymentMethodAdapter = SelectPaymentMethodAdapter()

        getMessage()
        initListener()
        textWatchers()
        setUpSourceAutoTextView()
        setUpPaymentMethodRecyclerView()
    }

    private fun setUpPaymentMethodRecyclerView() {

        binding.linkPaymentMethodsRV.apply {

            adapter = selectPaymentMethodAdapter
            layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
            setHasFixedSize(true)
        }

        selectPaymentMethodAdapter.setOnClickListener(this)
    }

    override fun onItemClick(paymentMethod: PaymentMethod, position: Int) {

        if (paymentMethod.key != Constants.ADD_PAYMENT_METHOD_KEY) {

            paymentMethod.isSelected = !paymentMethod.isSelected
            selectPaymentMethodAdapter.notifyItemChanged(position)
        } else {

            requireActivity().supportFragmentManager.let {

                AddEditPaymentMethodBottomSheetFragment.newInstance(Bundle())
                    .apply {
                        show(it, TAG)
                    }
            }
        }
    }

    private fun setUpSourceAutoTextView() {

        incomeViewModel.getAllIncomeSources().observe(viewLifecycleOwner) { sources ->

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                sources
            )

            binding.insideEditTextSource.setAdapter(adapter)
        }
    }

    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            arguments?.let { bundle ->

                try {

                    isForEdit = bundle.getBoolean(Constants.IS_FOR_EDIT, false)

                    if (isForEdit) {

                        receivedIncomeKey = bundle.getString(Constants.DOCUMENT_KEY, "")
                        if (receivedIncomeKey.isValid()) {

                            getIncomeByKey()
                        } else {
                            requireContext().showToast(getString(R.string.something_went_wrong))
                            dismiss()
                        }
                    } else {

                        receivedMonth = bundle.getInt(Constants.INCOME_MONTH_KEY, -1)
                        receivedYear = bundle.getInt(Constants.INCOME_YEAR_KEY, -1)

                        if (receivedMonth == -1 || receivedYear == -1) {

                            showToast(
                                requireContext(),
                                getString(R.string.no_month_or_year_received_for_which_income_needs_to_be_added)
                            )
                            dismiss()
                        }
                    }

                    lifecycleScope.launch {

                        delay(300)
                        getAllPaymentMethods()
                    }


                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    requireContext().showToast(getString(R.string.something_went_wrong))
                    dismiss()
                }
            }
        }

    }

    private fun getAllPaymentMethods() {

        paymentMethodViewModel.getAllPaymentMethods()
            .observe(viewLifecycleOwner) { paymentMethods ->

                if (isForEdit && selectPaymentMethodAdapter.currentList.isEmpty()) {

                    receivedIncome.linkedPaymentMethods?.let { alreadySelectedPM ->
                        paymentMethods.forEach { pm ->
                            if (alreadySelectedPM.contains(pm.key)) {
                                pm.isSelected = true
                            }
                        }
                    }
                }

                val addPaymentMethod = PaymentMethod(
                    key = Constants.ADD_PAYMENT_METHOD_KEY,
                    paymentMethod = getString(R.string.add),
                    uid = "",
                    isSynced = false,
                    isSelected = false
                )

                selectPaymentMethodAdapter.submitList(paymentMethods + listOf(addPaymentMethod))
            }

    }

    private fun getIncomeByKey() {

        incomeViewModel.getIncomeByKey(receivedIncomeKey).observe(viewLifecycleOwner) { income ->
            receivedIncome = income
            receivedMonth = receivedIncome.month
            receivedYear = receivedIncome.year
            updateUI()
        }
    }

    private fun updateUI() {

        if (this::receivedIncome.isInitialized) {

            binding.apply {
                insideEditTextIncome.setText(receivedIncome.income.toString())
                insideEditTextSource.setText(receivedIncome.source)
            }
        }
    }


    private fun initListener() {

        binding.toolbar.setNavigationOnClickListener {

            if (mListener != null) {
                mListener!!.onIncomeBottomSheetDismissed(false)
            }
            dismiss()
        }

        binding.toolbar.menu.findItem(R.id.menu_save_btn).setOnMenuItemClickListener {

            if (isFormValid()) {

                initIncome()
            }
            true
        }
    }

    private fun initIncome() {

        var income = Income()

        if (isForEdit) {

            income = receivedIncome.copy()
        } else {

            income.apply {
                this.created = System.currentTimeMillis()
                this.modified = System.currentTimeMillis()
                this.uid = Functions.getUid()!!
                this.key = Functions.generateKey("_${Functions.getUid()}")
                this.month = receivedMonth
                this.year = receivedYear
                this.monthYearString = this.generateMonthYearString()
                this.isSynced = isInternetAvailable(requireContext())
            }
        }

        income.source = binding.insideEditTextSource.text.toString().trim()
        income.income = binding.insideEditTextIncome.text.toString().trim().toDouble()

        val selectedPaymentMethods =
            selectPaymentMethodAdapter.currentList.filter { pm -> pm.isSelected }
                .map { pm -> pm.key }

        income.linkedPaymentMethods =
            selectedPaymentMethods.ifEmpty { listOf(Constants.PAYMENT_METHOD_OTHER_KEY) }

        if (!isForEdit) {
            incomeViewModel.insertIncome(income)
        } else {
            incomeViewModel.updateIncome(
                receivedIncome, income
            )
        }

        if (mListener != null) {

            mListener!!.onIncomeBottomSheetDismissed(true)
        }

        dismiss()
    }

    private fun isFormValid(): Boolean {

        if (!binding.insideEditTextIncome.isTextValid()) {

            binding.incomeET.error = EDIT_TEXT_EMPTY_MESSAGE
            return false
        }

        if (!binding.insideEditTextSource.isTextValid()) {
            binding.sourceET.error = EDIT_TEXT_EMPTY_MESSAGE
            return false
        }

        return binding.incomeET.error == null && binding.sourceET.error == null
    }

    private fun textWatchers() {

        binding.insideEditTextIncome.onTextChangedListener { s ->

            if (!s?.toString().isValid()) {

                binding.incomeET.error = EDIT_TEXT_EMPTY_MESSAGE
            } else {

                if (s?.toString()?.trim()?.toDouble()!! <= 0.0) {
                    binding.incomeET.error = getString(R.string.income_should_be_grater_than_0)
                } else {
                    binding.incomeET.error = null
                }
            }
        }

        binding.insideEditTextSource.onTextChangedListener { s ->

            if (!s?.toString().isValid()) {

                binding.sourceET.error = EDIT_TEXT_EMPTY_MESSAGE
            } else {

                binding.sourceET.error = null
            }
        }
    }

    interface OnIncomeBottomSheetDismissListener {

        fun onIncomeBottomSheetDismissed(
            isIncomeAdded: Boolean
        )
    }

    fun setOnBottomSheetDismissListener(listener: OnIncomeBottomSheetDismissListener) {

        mListener = listener
    }


    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): AddIncomeBottomSheetFragment {
            val fragment = AddIncomeBottomSheetFragment()
            fragment.arguments = bundle
            return fragment
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
