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
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.getUid
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import com.rohitthebest.manageyourrenters.utils.isValid
import com.rohitthebest.manageyourrenters.utils.onTextChangedListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditTextBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: EditTextBottomSheetLayoutBinding? = null
    private val binding get() = _binding!!

    private val paymentMethodViewModel by viewModels<PaymentMethodViewModel>()
    private lateinit var paymentMethodNameList: List<String>

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

        lifecycleScope.launch {
            delay(100)
            getAllPaymentMethods()
        }

        initListeners()
        textWatchers()
    }

    private fun getAllPaymentMethods() {

        paymentMethodViewModel.getAllPaymentMethods()
            .observe(viewLifecycleOwner) { paymentMethods ->

                paymentMethodNameList = paymentMethods.map { pm -> pm.paymentMethod.lowercase() }
            }
    }

    private fun initListeners() {

        binding.editText.editText?.requestFocus()
        Functions.showKeyboard(requireActivity(), binding.editText)

        binding.toolbar.setNavigationOnClickListener {
            showToast(requireContext(), getString(R.string.cancelled))
            dismiss()
        }

        binding.toolbar.menu.findItem(R.id.menu_save_btn).setOnMenuItemClickListener {

            if (binding.editText.editText?.error == null) {

                savePaymentMethod()
            }

            true
        }
    }

    private fun savePaymentMethod() {

        val paymentMethod = PaymentMethod(
            key = Functions.generateKey("_${getUid()}"),
            paymentMethod = binding.editText.editText?.text.toString().trim(),
            uid = getUid()!!,
            isSynced = isInternetAvailable(requireContext()),
            isSelected = true
        )

        paymentMethodViewModel.insertPaymentMethod(paymentMethod)
        showToast(requireContext(), getString(R.string.payment_method_saved))
        dismiss()
    }

    private fun textWatchers() {

        binding.editText.editText?.onTextChangedListener { s ->

            if (!s.toString().isValid()) {

                binding.editText.error = Constants.EDIT_TEXT_EMPTY_MESSAGE
            } else {

                if (paymentMethodNameList.contains(s?.toString()?.trim()?.lowercase())) {

                    binding.editText.error = getString(R.string.this_payment_method_already_exists)
                } else {
                    binding.editText.error = null
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): EditTextBottomSheetFragment {
            val fragment = EditTextBottomSheetFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}