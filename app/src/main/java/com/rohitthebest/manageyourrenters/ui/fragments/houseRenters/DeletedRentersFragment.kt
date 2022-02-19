package com.rohitthebest.manageyourrenters.ui.fragments.houseRenters

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.houseRenterAdapters.DeletedRentersAdapter
import com.rohitthebest.manageyourrenters.databinding.FragmentDeletedRentersBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.DeletedRenterViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeletedRentersFragment : Fragment(R.layout.fragment_deleted_renters) {

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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
