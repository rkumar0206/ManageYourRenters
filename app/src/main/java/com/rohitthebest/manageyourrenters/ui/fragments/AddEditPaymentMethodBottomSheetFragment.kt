package com.rohitthebest.manageyourrenters.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.model.PaymentMethod
import com.rohitthebest.manageyourrenters.databinding.EditTextBottomSheetLayoutBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.viewModels.PaymentMethodViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getUid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddEditPaymentMethodBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: EditTextBottomSheetLayoutBinding? = null
    private val binding get() = _binding!!

    private val paymentMethodViewModel by viewModels<PaymentMethodViewModel>()
    private lateinit var paymentMethodNameList: List<String>

    private var isForEdit = false
    private lateinit var receivedPaymentMethod: PaymentMethod

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

        binding.editText.editText?.requestFocus()
        Functions.showKeyboard(requireActivity(), binding.editText.editText!!)

        getMessage()

        lifecycleScope.launch {
            delay(100)
            getAllPaymentMethods()
        }

        initListeners()
        textWatchers()
    }

    private fun getMessage() {

        if (!arguments?.isEmpty!!) {

            arguments?.let { bundle ->

                try {

                    isForEdit = bundle.getBoolean(Constants.IS_FOR_EDIT, false)

                    if (isForEdit) {
                        getPaymentMethod(
                            bundle.getString(
                                Constants.PAYMENT_METHOD_KEY_FOR_EDIT,
                                ""
                            )
                        )
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getPaymentMethod(key: String?) {

        if (key.isValid()) {

            paymentMethodViewModel.getPaymentMethodByKey(key!!)
                .observe(viewLifecycleOwner) { paymentMethod ->
                    receivedPaymentMethod = paymentMethod
                    binding.editText.editText?.setText(paymentMethod.paymentMethod)
                }
        } else {
            requireContext().showToast(getString(R.string.something_went_wrong))
            dismiss()
        }
    }

    private fun getAllPaymentMethods() {

        paymentMethodViewModel.getAllPaymentMethods()
            .observe(viewLifecycleOwner) { paymentMethods ->

                paymentMethodNameList = paymentMethods.map { pm -> pm.paymentMethod.lowercase() }
            }
    }

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {
            showToast(requireContext(), getString(R.string.cancelled))
            hideKeyBoard(requireActivity())
            dismiss()
        }

        binding.toolbar.menu.findItem(R.id.menu_save_btn).setOnMenuItemClickListener {

            if (isForEdit) {
                if (binding.editText.editText?.text.toString().trim().lowercase()
                    == receivedPaymentMethod.paymentMethod.lowercase()
                ) {
                    requireContext().showToast(getString(R.string.payment_method_saved))
                    dismiss()
                }
            }

            if (isFormValid()) {
                savePaymentMethod()
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

    private fun savePaymentMethod() {

        if (!isForEdit) {
            val paymentMethod = PaymentMethod(
                key = Functions.generateKey("_${getUid()}"),
                paymentMethod = binding.editText.editText?.text.toString().trim(),
                uid = getUid()!!,
                isSynced = isInternetAvailable(requireContext()),
                isSelected = false
            )
            paymentMethodViewModel.insertPaymentMethod(paymentMethod)
        } else {

            val paymentMethod = receivedPaymentMethod.copy()
            paymentMethod.paymentMethod = binding.editText.editText?.text.toString().trim()
            paymentMethod.isSynced = requireContext().isInternetAvailable()

            // if oldValue has isSynced value as true then update, else insert
            if (receivedPaymentMethod.isSynced) {
                paymentMethodViewModel.updatePaymentMethod(receivedPaymentMethod, paymentMethod)
            } else {
                paymentMethodViewModel.insertPaymentMethod(paymentMethod)
            }
        }

        showToast(requireContext(), getString(R.string.payment_method_saved))
        dismiss()
    }

    private fun textWatchers() {

        binding.editText.editText?.onTextChangedListener { s ->

            if (!s.toString().isValid()) {

                binding.editText.error = Constants.EDIT_TEXT_EMPTY_MESSAGE
            } else {

                if (this::paymentMethodNameList.isInitialized &&
                    paymentMethodNameList.contains(s?.toString()?.trim()?.lowercase())
                ) {

                    binding.editText.error = getString(R.string.this_payment_method_already_exists)
                } else {
                    binding.editText.error = null
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): AddEditPaymentMethodBottomSheetFragment {
            val fragment = AddEditPaymentMethodBottomSheetFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}