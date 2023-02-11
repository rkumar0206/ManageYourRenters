package com.rohitthebest.manageyourrenters.ui.fragments.houseRenters

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.houseRenterAdapters.DeletedRentersAdapter
import com.rohitthebest.manageyourrenters.database.model.DeletedRenter
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.databinding.FragmentDeletedRentersBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.DeletedRenterViewModel
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//private const val TAG = "DeletedRentersFragment"

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

        lifecycleScope.launch {

            delay(200)
            getAllDeletedRenter()
        }

        initListeners()

    }

    private fun initListeners() {

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.toolbar.menu.findItem(R.id.menu_info_deleted_renter_fragment)
            .setOnMenuItemClickListener {

                MaterialAlertDialogBuilder(requireContext())
                    .setMessage(
                        "This history about deleted renters is saved locally and will be deleted " +
                                "after you uninstall / logout / re-sync the data."
                    )
                    .setPositiveButton("Ok") { dialog, _ ->

                        dialog.dismiss()
                    }
                    .create()
                    .show()

                true
            }

        binding.toolbar.menu.findItem(R.id.menu_delete_all_deleted_renters)
            .setOnMenuItemClickListener {

                if (!binding.noRecordsFoundTV.isVisible) {

                    showAlertDialogForDeletion(
                        requireContext(),
                        {

                            showToast(requireContext(), "All records deleted")

                            deletedRenterViewModel.deleteAllDeletedRenters()

                            it.dismiss()
                        },
                        {
                            it.dismiss()
                        }
                    )
                } else {

                    showToast(requireContext(), "No records available to be deleted.")
                }

                true
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

        var message = "Name : ${deletedRenter.name}\n\n" +
                "Added on : ${
                    WorkingWithDateAndTime.convertMillisecondsToDateAndTimePattern(
                        deletedRenter.timeStamp
                    )
                }\n\n" +
                "Address : ${deletedRenter.address}\n\n" +
                "Room no : ${deletedRenter.roomNumber}\n\n" +
                "Mobile no. : ${deletedRenter.mobileNumber}\n\n"

        if (deletedRenter.emailId.isValid()) {
            message += "Email id : ${deletedRenter.emailId}\n\n"
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

        if (deletedRenterKey.isValid()) {

            val action =
                DeletedRentersFragmentDirections.actionDeletedRentersFragmentToRenterBillFragment(
                    deletedRenterKey, true
                )

            findNavController().navigate(action)
        } else {

            showToast(requireContext(), "No payment added")
        }

    }

    override fun onDeleteBtnClicked(deletedRenter: DeletedRenter) {

        showAlertDialogForDeletion(
            requireContext(),
            {


            deletedRenterViewModel.deleteDeletedRenter(deletedRenter)

                it.dismiss()
            },
            {
                it.dismiss()
            }
        )

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
