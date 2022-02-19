package com.rohitthebest.manageyourrenters.ui.fragments.houseRenters

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.houseRenterAdapters.DeletedRentersAdapter
import com.rohitthebest.manageyourrenters.database.model.DeletedRenter
import com.rohitthebest.manageyourrenters.database.model.Renter
import com.rohitthebest.manageyourrenters.database.model.RenterPayment
import com.rohitthebest.manageyourrenters.databinding.FragmentDeletedRentersBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.DeletedRenterViewModel
import dagger.hilt.android.AndroidEntryPoint

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
        //TODO("Not yet implemented")
    }

    override fun onLastPaymentInfoBtnClicked(deletedRenter: RenterPayment) {
        //TODO("Not yet implemented")
    }

    override fun onDeleteBtnClicked(deletedRenter: DeletedRenter) {
        //TODO("Not yet implemented")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
