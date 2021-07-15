package com.rohitthebest.manageyourrenters.ui.fragments.borrower

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.borrowerAdapters.BorrowerPaymentAdapter
import com.rohitthebest.manageyourrenters.database.model.Borrower
import com.rohitthebest.manageyourrenters.database.model.BorrowerPayment
import com.rohitthebest.manageyourrenters.databinding.FragmentBorrowerPaymentBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerPaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerViewModel
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "BorrowerPaymentFragment"

@AndroidEntryPoint
class BorrowerPaymentFragment : Fragment(R.layout.fragment_borrower_payment),
    BorrowerPaymentAdapter.OnClickListener {

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
                        receivedBorrowerKey
                    )
                findNavController().navigate(action)
            }
        }

        getMessage()

        borrowerPaymentAdapter = BorrowerPaymentAdapter()

        setUpRecyclerView()
    }


    private fun getMessage() {

        try {

            if (!arguments?.isEmpty!!) {

                val args = arguments?.let {

                    BorrowerPaymentFragmentArgs.fromBundle(it)
                }

                receivedBorrowerKey = args?.borrowerKeyMessage!!

                getBorrower()
                getBorrowerPayments()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun getBorrower() {

        borrowerViewModel.getBorrowerByKey(receivedBorrowerKey).observe(viewLifecycleOwner, {

            if (it != null) {
                receivedBorrower = it
                Log.d(TAG, "getBorrower: received borrower : $receivedBorrower")
            }
        })
    }

    private fun getBorrowerPayments() {


        borrowerPaymentViewModel.getPaymentsByBorrowerKey(receivedBorrowerKey)
            .observe(viewLifecycleOwner, { borrowerPayments ->

                borrowerPaymentAdapter.submitList(borrowerPayments)

            })
    }


    private fun setUpRecyclerView() {

        binding.borrowerPaymentRV.apply {

            adapter = borrowerPaymentAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }

        borrowerPaymentAdapter.setOnClickListener(this)

    }

    override fun onItemClick(borrowerPayment: BorrowerPayment) {


    }

    override fun onDeleteBtnClick(borrowerPayment: BorrowerPayment) {
        //TODO("Not yet implemented")
    }

    override fun onSyncBtnClick(borrowerPayment: BorrowerPayment) {
        // TODO("Not yet implemented")
    }

    override fun onShowMessageBtnClick(message: String) {
        //TODO("Not yet implemented")
    }

    override fun onShowDocumentBtnClick(borrowerPayment: BorrowerPayment) {
        //TODO("Not yet implemented")
    }

    override fun onInterestBtnClick(borrowerPayment: BorrowerPayment) {
        //TODO("Not yet implemented")
    }

    override fun onEditBtnClick(borrowerPayment: BorrowerPayment) {
        //TODO("Not yet implemented")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
