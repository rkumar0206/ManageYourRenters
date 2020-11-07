package com.rohitthebest.manageyourrenters.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rohitthebest.manageyourrenters.adapters.ShowPaymentAdapter
import com.rohitthebest.manageyourrenters.database.entity.Payment
import com.rohitthebest.manageyourrenters.database.entity.Renter
import com.rohitthebest.manageyourrenters.databinding.FragmentPaymentBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.PaymentViewModel
import com.rohitthebest.manageyourrenters.utils.ConversionWithGson.Companion.convertJSONtoRenter
import com.rohitthebest.manageyourrenters.utils.ConversionWithGson.Companion.convertRenterToJSONString
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hide
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class PaymentFragment : Fragment(), View.OnClickListener {

    private val paymentViewModel: PaymentViewModel by viewModels()

    private var _binding: FragmentPaymentBinding? = null
    private val binding get() = _binding!!

    private var receivedRenter: Renter? = null

    private lateinit var mAdapter: ShowPaymentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getMessage()
        initListener()

        showProgressBar()

        GlobalScope.launch {

            delay(300)

            withContext(Dispatchers.Main) {

                getPaymentListOfRenter()
            }
        }
    }

    private fun getPaymentListOfRenter() {

        try {

            paymentViewModel.getAllPaymentsListOfRenter(receivedRenter?.key!!)
                .observe(viewLifecycleOwner) {

                    if (it.isNotEmpty()) {

                        hideNoPaymentsTV()
                        initializeSearchView(it)
                    } else {

                        showNoPaymentsTV()
                    }

                    setUpRecyclerView(it)
                }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initializeSearchView(it: List<Payment>?) {


    }

    private fun setUpRecyclerView(paymentList: List<Payment>?) {

        try {

            paymentList?.let {

                mAdapter = ShowPaymentAdapter()

                mAdapter.submitList(it)

                binding.paymentRV.apply {

                    setHasFixedSize(true)
                    adapter = mAdapter
                    layoutManager = LinearLayoutManager(requireContext())
                }
            }

        } catch (e: java.lang.Exception) {

            e.printStackTrace()
        }

        hideProgressBar()
    }


    private fun getMessage() {

        try {
            if (!arguments?.isEmpty!!) {

                val args = arguments?.let {

                    PaymentFragmentArgs.fromBundle(it)
                }

                receivedRenter = convertJSONtoRenter(args?.renterInfoMessage)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initListener() {

        binding.addPyamentFAB.setOnClickListener(this)
        binding.deleteAllPaymentsBtn.setOnClickListener(this)
        binding.paymentBackBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            binding.addPyamentFAB.id -> {

                try {

                    val action =
                        PaymentFragmentDirections.actionPaymentFragmentToAddPaymentFragment(
                            convertRenterToJSONString(receivedRenter!!)
                        )
                    findNavController().navigate(action)
                } catch (e: Exception) {

                    e.printStackTrace()
                }
            }

            binding.paymentBackBtn.id -> {

                requireActivity().onBackPressed()
            }
        }
    }

    private fun showNoPaymentsTV() {

        try {

            binding.noPaymentsTV.show()
            binding.paymentRV.hide()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideNoPaymentsTV() {

        try {

            binding.noPaymentsTV.hide()
            binding.paymentRV.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showProgressBar() {

        try {

            binding.paymentFragProgressBar.show()
            binding.paymentRV.hide()
        } catch (e: java.lang.Exception) {

            e.printStackTrace()
        }
    }

    private fun hideProgressBar() {

        try {

            binding.paymentFragProgressBar.hide()
            binding.paymentRV.show()
        } catch (e: java.lang.Exception) {

            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

}