package com.rohitthebest.manageyourrenters.ui.fragments.borrower

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.borrowerAdapters.BorrowerAdapter
import com.rohitthebest.manageyourrenters.database.model.Borrower
import com.rohitthebest.manageyourrenters.databinding.FragmentBorrowerHomeBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.BorrowerViewModel
import com.rohitthebest.manageyourrenters.utils.ConversionWithGson
import com.rohitthebest.manageyourrenters.utils.FirebaseServiceHelper
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showAlertDialogForDeletion
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showSnackbarWithActionAndDismissListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAG = "BorrowerHomeFragment"

@AndroidEntryPoint
class BorrowerHomeFragment : Fragment(R.layout.fragment_borrower_home),
    BorrowerAdapter.OnClickListener {

    private var _binding: FragmentBorrowerHomeBinding? = null
    private val binding get() = _binding!!

    private val borrowerViewModel by viewModels<BorrowerViewModel>()
    private lateinit var borrowerAdapter: BorrowerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentBorrowerHomeBinding.bind(view)

        binding.addIndividualRenterFAB.setOnClickListener {

            findNavController().navigate(R.id.action_borrowerHomeFragment_to_addBorrowerFragment)
        }
        binding.individualRenterToolbar.setNavigationOnClickListener {

            requireActivity().onBackPressed()
        }

        showHideProgressBar(true)

        borrowerAdapter = BorrowerAdapter()
        setUpRecyclerView()

        lifecycleScope.launch {

            delay(250)
            getAllBorrowers()
        }
    }

    private fun getAllBorrowers() {

        borrowerViewModel.getAllBorrower().observe(viewLifecycleOwner, {

            borrowerAdapter.submitList(it)

            showHideProgressBar(false)
        })
    }

    private fun setUpRecyclerView() {

        binding.individualRentersRV.apply {

            setHasFixedSize(true)
            adapter = borrowerAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        borrowerAdapter.setOnClickListener(this)
    }

    override fun onBorrowerClicked(borrower: Borrower?) {
        //todo : open borrower payment fragment
    }

    override fun onSyncButtonClicked(borrower: Borrower?) {

        if (isInternetAvailable(requireContext())) {

            if (borrower?.isSynced!!) {

                Functions.showToast(requireContext(), "Already Synced")
            } else {

                borrower.isSynced = true

                FirebaseServiceHelper.uploadDocumentToFireStore(
                    requireContext(),
                    ConversionWithGson.fromBorrowerToString(borrower),
                    getString(R.string.borrowers),
                    borrower.key
                )

                borrowerViewModel.insertBorrower(borrower)
            }

        } else {

            showNoInternetMessage(requireContext())
        }

    }

    override fun onDeleteClicked(borrower: Borrower?) {

        showAlertDialogForDeletion(
            requireContext(),
            positiveButtonListener = {

                if (!borrower?.isSynced!!) {

                    deleteBorrower(borrower)
                } else {

                    if (isInternetAvailable(requireContext())) {

                        deleteBorrower(borrower)
                    } else {
                        showNoInternetMessage(requireContext())
                    }
                }

                it.dismiss()

            },
            negativeButtonListener = {

                it.dismiss()
            }
        )
    }

    private fun deleteBorrower(borrower: Borrower) {

        borrowerViewModel.deleteBorrower(borrower)

        var isUndoClicked = false

        binding.root.showSnackbarWithActionAndDismissListener(
            text = "Borrower deleted",
            actionText = "Undo",
            action = {

                isUndoClicked = true

                borrowerViewModel.insertBorrower(borrower)
                Functions.showToast(requireContext(), "Borrower restored...")
            },
            dismissListener = {

                if (!isUndoClicked && borrower.isSynced) {

                    FirebaseServiceHelper.deleteDocumentFromFireStore(
                        context = requireContext(),
                        collection = getString(R.string.borrowers),
                        documentKey = borrower.key
                    )
                }

                deleteAllPaymentsOfThisBorrower(borrower)
            }
        )
    }

    private fun deleteAllPaymentsOfThisBorrower(borrower: Borrower) {

        // todo : delete all the payments related to this borrower
    }

    override fun onEditClicked(borrower: Borrower?) {

        val action = BorrowerHomeFragmentDirections.actionBorrowerHomeFragmentToAddBorrowerFragment(
            borrower?.key
        )

        findNavController().navigate(action)
    }

    private fun showHideProgressBar(isVisible: Boolean) {

        binding.individualRentersRV.isVisible = !isVisible
        binding.progressBar.isVisible = isVisible
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
