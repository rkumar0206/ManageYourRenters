package com.rohitthebest.manageyourrenters.ui.fragments.borrower

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.borrowerAdapters.BorrowerPaymentAdapter
import com.rohitthebest.manageyourrenters.data.Interest
import com.rohitthebest.manageyourrenters.data.InterestCalculatorFields
import com.rohitthebest.manageyourrenters.data.InterestTimeSchedule
import com.rohitthebest.manageyourrenters.data.InterestType
import com.rohitthebest.manageyourrenters.database.model.Borrower
import com.rohitthebest.manageyourrenters.database.model.BorrowerPayment
import com.rohitthebest.manageyourrenters.databinding.FragmentBorrowerPaymentBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.fragments.trackMoney.CustomMenuItems
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerPaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.onViewOrDownloadSupportingDocument
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "BorrowerPaymentFragment"

@AndroidEntryPoint
class BorrowerPaymentFragment : Fragment(R.layout.fragment_borrower_payment),
    BorrowerPaymentAdapter.OnClickListener, CustomMenuItems.OnItemClickListener {

    private var _binding: FragmentBorrowerPaymentBinding? = null
    private val binding get() = _binding!!

    private var receivedBorrower: Borrower? = null
    private var receivedBorrowerKey: String = ""

    private val borrowerViewModel by viewModels<BorrowerViewModel>()
    private val borrowerPaymentViewModel by viewModels<BorrowerPaymentViewModel>()

    private lateinit var borrowerPaymentAdapter: BorrowerPaymentAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBorrowerPaymentBinding.bind(view)


        binding.borrowerPaymentToolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

        binding.addPaymentFAB.setOnClickListener {

            if (receivedBorrowerKey != "") {

                val action =
                    BorrowerPaymentFragmentDirections.actionBorrowerPaymentFragmentToAddBorrowerPaymentFragment(
                        receivedBorrowerKey, ""
                    )
                findNavController().navigate(action)
            }
        }

        getMessage()

        borrowerPaymentAdapter = BorrowerPaymentAdapter()

        setUpRecyclerView()

        binding.progressbar.show()
    }

    private fun getMessage() {

        try {

            if (!arguments?.isEmpty!!) {

                val args = arguments?.let {

                    BorrowerPaymentFragmentArgs.fromBundle(it)
                }

                receivedBorrowerKey = args?.borrowerKeyMessage!!

                lifecycleScope.launch {

                    delay(300)

                    getBorrower()
                    getBorrowerPayments()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getBorrower() {

        borrowerViewModel.getBorrowerByKey(receivedBorrowerKey).observe(viewLifecycleOwner) {

            if (it != null) {
                receivedBorrower = it
                binding.borrowerPaymentToolbar.title = it.name
            }
        }
    }

    private fun getBorrowerPayments() {

        borrowerPaymentViewModel.getPaymentsByBorrowerKey(receivedBorrowerKey)
            .observe(viewLifecycleOwner) { borrowerPayments ->

                borrowerPaymentAdapter.submitList(borrowerPayments)

                if (borrowerPayments.isNotEmpty()) {

                    binding.borrowerPaymentRV.show()
                    binding.noBorrowerPaymentTV.hide()
                } else {

                    binding.borrowerPaymentRV.hide()
                    binding.noBorrowerPaymentTV.show()
                }

                binding.progressbar.hide()
            }

    }

    private fun setUpRecyclerView() {

        binding.borrowerPaymentRV.apply {

            adapter = borrowerPaymentAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            changeVisibilityOfFABOnScrolled(binding.addPaymentFAB)
        }

        borrowerPaymentAdapter.setOnClickListener(this)

    }

    //[START OF ADAPTER CLICK LISTENER]
    override fun onItemClick(borrowerPayment: BorrowerPayment) {

        val action =
            BorrowerPaymentFragmentDirections.actionBorrowerPaymentFragmentToAddPartialPaymentFragment(
                borrowerPayment.key
            )

        findNavController().navigate(action)
    }

    override fun onSyncBtnClick(borrowerPayment: BorrowerPayment, position: Int) {

        if (borrowerPayment.isSynced) {

            showToast(requireContext(), "Already synced")
        } else {

            if (isInternetAvailable(requireContext())) {

                borrowerPayment.isSynced = true

                uploadDocumentToFireStore(
                    requireContext(),
                    getString(R.string.borrowerPayments),
                    borrowerPayment.key
                )

                borrowerPaymentViewModel.updateBorrowerPayment(borrowerPayment)
                borrowerPaymentAdapter.notifyItemChanged(position)

            } else {

                showNoInternetMessage(requireContext())
            }
        }
    }

    override fun onShowMessageBtnClick(message: String) {

        var m = message

        if (!message.isValid()) {

            m = "No message added!!!"
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Message")
            .setMessage(m)
            .setPositiveButton("Ok") { dialog, _ ->

                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onInterestBtnClick(borrowerPayment: BorrowerPayment) {

        if (!borrowerPayment.isInterestAdded) {

            borrowerPayment.interest =
                Interest(InterestType.SIMPLE_INTEREST, 0.0, InterestTimeSchedule.ANNUALLY)
            showToast(requireContext(), "No interest added!!!")
        }

        val interestCalculatorFields = InterestCalculatorFields(
            borrowerPayment.created,
            borrowerPayment.amountTakenOnRent,
            borrowerPayment.interest!!
        )

        val action =
            BorrowerPaymentFragmentDirections.actionBorrowerPaymentFragmentToCalculateInterestBottomSheetFragment(
                interestCalcualatorFields = interestCalculatorFields.convertToJsonString()
            )

        findNavController().navigate(action)
    }

    private var borrowerPaymentForMenus: BorrowerPayment? = null
    private var adapterItemPosition = 0

    override fun onMenuBtnClick(borrowerPayment: BorrowerPayment, position: Int) {

        borrowerPaymentForMenus = borrowerPayment
        adapterItemPosition = position

        requireActivity().supportFragmentManager.let {

            val bundle = Bundle()
            bundle.putBoolean(Constants.SHOW_SYNC_MENU, false)

            CustomMenuItems.newInstance(
                bundle
            ).apply {
                show(it, TAG)
            }.setOnClickListener(this)
        }

    }

    override fun onEditMenuClick() {

        if (borrowerPaymentForMenus != null) {


            // checking if the borrower payment already contains some partial payment or not
            // if it contains at least one partial payment then user cannot edit the payment

            if (borrowerPaymentForMenus!!.dueLeftAmount < borrowerPaymentForMenus!!.amountTakenOnRent) {

                showToast(
                    requireContext(),
                    getString(R.string.this_payment_already_has_some_partial_payments),
                    Toast.LENGTH_LONG
                )
            } else {

                val action =
                    BorrowerPaymentFragmentDirections.actionBorrowerPaymentFragmentToAddBorrowerPaymentFragment(
                        receivedBorrowerKey, borrowerPaymentForMenus?.key
                    )
                findNavController().navigate(action)
            }
        }

    }

    override fun onDeleteMenuClick() {

        showAlertDialogForDeletion(
            requireContext(),
            {

                if (borrowerPaymentForMenus != null) {

                    if (!borrowerPaymentForMenus!!.isSynced) {

                        borrowerPaymentViewModel.deleteBorrowerPayment(
                            requireContext(),
                            borrowerPaymentForMenus!!
                        )

                    } else {

                        if (isInternetAvailable(requireContext())) {

                            borrowerPaymentViewModel.deleteBorrowerPayment(
                                requireContext(),
                                borrowerPaymentForMenus!!
                            )

                        } else {

                            showNoInternetMessage(requireContext())
                        }
                    }
                }

                it.dismiss()
            },
            {
                it.dismiss()
            }
        )

    }

    override fun onViewSupportingDocumentMenuClick() {

        if (borrowerPaymentForMenus != null) {
            if (!borrowerPaymentForMenus!!.isSupportingDocAdded) {

                showToast(requireContext(), getString(R.string.no_supporting_doc_added))
            } else {

                borrowerPaymentForMenus!!.supportingDocument?.let { supportingDoc ->

                    onViewOrDownloadSupportingDocument(
                        requireActivity(),
                        supportingDoc
                    )
                }
            }
        }

    }

    override fun onReplaceSupportingDocumentClick() {
        //TODO("Not yet implemented")
    }

    override fun onDeleteSupportingDocumentClick() {
        //TODO("Not yet implemented")
    }

    override fun onSyncMenuClick() {}

    //[END OF ADAPTER CLICK LISTENER]

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
