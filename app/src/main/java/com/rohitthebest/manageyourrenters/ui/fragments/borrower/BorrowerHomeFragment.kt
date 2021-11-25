package com.rohitthebest.manageyourrenters.ui.fragments.borrower

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
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
import com.rohitthebest.manageyourrenters.utils.*
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

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

            delay(400)
            getAllBorrowers()
        }

        binding.individualRentersRV.changeVisibilityOfFABOnScrolled(
            binding.addIndividualRenterFAB
        )
    }

    private fun getAllBorrowers() {

        borrowerViewModel.getAllBorrower().observe(viewLifecycleOwner, {

            if (it.isEmpty()) {

                showNoBorrowersAddedTV(true)
            } else {

                showNoBorrowersAddedTV(false)
            }

            borrowerAdapter.submitList(it)

            initSearchViewMenu(it)

            showHideProgressBar(false)
        })
    }

    private fun initSearchViewMenu(borrowerList: List<Borrower>) {

        val searchView =
            binding.individualRenterToolbar.menu.findItem(R.id.menu_search_home).actionView as SearchView

        searchView.searchText { newText ->

            if (borrowerList.isEmpty()) {

                binding.individualRentersRV.scrollToPosition(0)
                borrowerAdapter.submitList(borrowerList)
            } else {

                val filteredList = borrowerList.filter { borrower ->

                    borrower.name.lowercase(Locale.ROOT)
                        .contains(newText.toString().trim().lowercase(Locale.ROOT))
                }

                borrowerAdapter.submitList(filteredList)
            }

        }
    }

    private fun setUpRecyclerView() {

        binding.individualRentersRV.apply {

            setHasFixedSize(true)
            adapter = borrowerAdapter
            layoutManager = LinearLayoutManager(requireContext())
            changeVisibilityOfFABOnScrolled(binding.addIndividualRenterFAB)
        }

        borrowerAdapter.setOnClickListener(this)
    }

    override fun onBorrowerClicked(borrowerKey: String) {

        val action =
            BorrowerHomeFragmentDirections.actionBorrowerHomeFragmentToBorrowerPaymentFragment(
                borrowerKey
            )

        findNavController().navigate(action)
    }

    override fun onSyncButtonClicked(borrower: Borrower?, position: Int) {

        if (isInternetAvailable(requireContext())) {

            if (borrower?.isSynced!!) {

                Functions.showToast(requireContext(), "Already Synced")
            } else {

                borrower.isSynced = true

                uploadDocumentToFireStore(
                    requireContext(),
                    fromBorrowerToString(borrower),
                    getString(R.string.borrowers),
                    borrower.key
                )

                borrowerViewModel.updateBorrower(borrower)

                borrowerAdapter.notifyItemChanged(position)
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

                    borrowerViewModel.deleteBorrower(
                        requireContext(),
                        borrower
                    )
                } else {

                    if (isInternetAvailable(requireContext())) {

                        borrowerViewModel.deleteBorrower(
                            requireContext(),
                            borrower
                        )
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

    override fun onEditClicked(borrowerKey: String) {

        val action = BorrowerHomeFragmentDirections.actionBorrowerHomeFragmentToAddBorrowerFragment(
            borrowerKey
        )

        findNavController().navigate(action)
    }

    override fun onMobileNumberClicked(mobileNumber: String, view: View) {

        Functions.showMobileNumberOptionMenu(
            requireActivity(),
            view,
            mobileNumber
        )
    }

    private fun showHideProgressBar(isVisible: Boolean) {

        binding.individualRentersRV.isVisible = !isVisible
        binding.progressBar.isVisible = isVisible
    }

    private fun showNoBorrowersAddedTV(isVisible: Boolean) {

        binding.noBorrowersAddedMessageTV.isVisible = isVisible
        binding.individualRentersRV.isVisible = !isVisible
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
