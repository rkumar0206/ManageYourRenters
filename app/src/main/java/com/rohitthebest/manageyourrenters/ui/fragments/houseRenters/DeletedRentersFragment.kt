package com.rohitthebest.manageyourrenters.ui.fragments.houseRenters

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.houseRenterAdapters.DeletedRentersAdapter
import com.rohitthebest.manageyourrenters.database.model.DeletedRenter
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.databinding.FragmentDeletedRentersBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.DeletedRenterViewModel
import com.rohitthebest.manageyourrenters.utils.WorkingWithDateAndTime
import com.rohitthebest.manageyourrenters.utils.hide
import com.rohitthebest.manageyourrenters.utils.isValid
import com.rohitthebest.manageyourrenters.utils.show
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "DeletedRentersFragment"

@AndroidEntryPoint
class DeletedRentersFragment : Fragment(R.layout.fragment_deleted_renters),
    DeletedRentersAdapter.OnClickListener {

    private var _binding: FragmentDeletedRentersBinding? = null
    private val binding get() = _binding!!

    private val deletedRenterViewModel by viewModels<DeletedRenterViewModel>()

    private lateinit var deletedRentersAdapter: DeletedRentersAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDeletedRentersBinding.bind(view)

        deletedRentersAdapter = DeletedRentersAdapter()

        setUpRecyclerView()

        getAllDeletedRenter()

        initListeners()

    }

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun getAllDeletedRenter() {

        deletedRenterViewModel.getAllDeletedRenters()
            .observe(viewLifecycleOwner) { deletedRenters ->

                if (deletedRenters.isNotEmpty()) {

                    binding.noRecordsFoundTV.hide()
                } else {

                    binding.noRecordsFoundTV.show()
                }

                deletedRentersAdapter.submitList(deletedRenters)
            }
    }

    private fun setUpRecyclerView() {

        binding.deletedRentersRV.apply {

            adapter = deletedRentersAdapter
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext())
        }

        deletedRentersAdapter.setOnClickListener(this)
    }

    override fun onRenterInfoBtnClicked(deletedRenter: Renter) {

        var message = "Name : ${deletedRenter.name}\n" +
                "Added on : ${
                    WorkingWithDateAndTime().convertMillisecondsToDateAndTimePattern(
                        deletedRenter.timeStamp
                    )
                }\n" +
                "Address : ${deletedRenter.address}\n" +
                "Room no : ${deletedRenter.roomNumber}\n" +
                "Mobile no. : ${deletedRenter.mobileNumber}\n"

        if (deletedRenter.emailId.isValid()) {
            message += "Email id : ${deletedRenter.emailId}\n"
        }

        if (deletedRenter.otherDocumentName.isValid()) {

            message += "${deletedRenter.otherDocumentName} : ${deletedRenter.otherDocumentNumber}\n"
        }


        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Renter information")
            .setMessage(message)
            .setPositiveButton("Ok") { dialog, _ ->

                dialog.dismiss()
            }
            .create()
            .show()

    }

    override fun onLastPaymentInfoBtnClicked(deletedRenterKey: String) {

        val action =
            DeletedRentersFragmentDirections.actionDeletedRentersFragmentToRenterBillFragment(
                deletedRenterKey, true
            )

        findNavController().navigate(action)
    }

    override fun onDeleteBtnClicked(deletedRenter: DeletedRenter) {

        // todo : show dialog
        // todo : delete the renter
        Log.d(TAG, "onDeleteBtnClicked: ${deletedRenter.paymentHistory}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
