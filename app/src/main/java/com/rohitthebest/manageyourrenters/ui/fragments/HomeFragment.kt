package com.rohitthebest.manageyourrenters.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.ShowRentersAdapter
import com.rohitthebest.manageyourrenters.database.entity.Renter
import com.rohitthebest.manageyourrenters.databinding.FragmentHomeBinding
import com.rohitthebest.manageyourrenters.ui.viewModels.RenterViewModel
import com.rohitthebest.manageyourrenters.utils.ConversionWithGson.Companion.convertRenterToJSONString
import com.rohitthebest.manageyourrenters.utils.FirebaseServiceHelper.Companion.deleteDocumentFromFireStore
import com.rohitthebest.manageyourrenters.utils.FirebaseServiceHelper.Companion.uploadDocumentToFireStore
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.closeKeyboard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hide
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.show
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showKeyboard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.*

@AndroidEntryPoint
class HomeFragment : Fragment(), View.OnClickListener, ShowRentersAdapter.OnClickListener {

    private val renterViewModel: RenterViewModel by viewModels()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var isSearchViewVisible = false
    private var mAuth: FirebaseAuth? = null

    private lateinit var mAdapter: ShowRentersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = Firebase.auth

        mAdapter = ShowRentersAdapter()

        GlobalScope.launch {

            delay(100)

            withContext(Dispatchers.Main) {

                getAllRentersList()
            }
        }

        initListeners()
    }

    private fun getAllRentersList() {

        try {

            renterViewModel.getAllRentersList().observe(viewLifecycleOwner, {

                if (it.isNotEmpty()) {

                    hideNoRentersAddedTV()
                    setUpSearchEditText(it)
                } else {

                    showNoRentersAddedTV()
                }

                setupRecyclerView(it)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setUpSearchEditText(it: List<Renter>?) {

        binding.searchET.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (s?.isEmpty()!!) {

                    setupRecyclerView(it)
                } else {

                    val filteredList = it?.filter { renter ->

                        renter.name.toLowerCase(Locale.ROOT).contains(
                            s.toString().trim().toLowerCase(Locale.ROOT)
                        )
                                ||
                                renter.roomNumber.toLowerCase(Locale.ROOT).contains(
                                    s.toString().trim().toLowerCase(Locale.ROOT)
                                )
                    }

                    setupRecyclerView(filteredList)

                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupRecyclerView(renterList: List<Renter>?) {

        try {

            renterList?.let {

                mAdapter.submitList(it)

                binding.rentersRV.apply {

                    adapter = mAdapter
                    layoutManager = LinearLayoutManager(requireContext())
                    setHasFixedSize(true)
                }
            }

            mAdapter.setOnClickListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRenterClicked(renter: Renter) {

        showToast(requireContext(), "item click")
        //todo : open payment fragment
    }

    override fun onSyncButtonClicked(renter: Renter) {

        if (isInternetAvailable(requireContext())) {

            if (renter.isSynced == getString(R.string.t)) {

                showToast(requireContext(), "Already Synced")
            } else {

                renter.isSynced = getString(R.string.t)

                uploadDocumentToFireStore(
                    requireContext(),
                    convertRenterToJSONString(renter),
                    getString(R.string.renters),
                    renter.key!!
                )

                renterViewModel.insertRenter(renter)
            }

        } else {

            showNoInternetMessage(requireContext())
        }
    }

    override fun onDeleteClicked(renter: Renter) {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Are you sure?")
            .setMessage(getString(R.string.delete_warning_message))
            .setPositiveButton("Delete") { dialogInterface, _ ->

                if (renter.isSynced == getString(R.string.f)) {

                    deleteRenter(renter)
                } else {

                    if (isInternetAvailable(requireContext())) {

                        deleteRenter(renter)
                    } else {
                        showNoInternetMessage(requireContext())
                    }
                }

                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->

                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun deleteRenter(renter: Renter) {

        renterViewModel.deleteRenter(renter)

        var isUndoClicked = false
        Snackbar.make(binding.homeCoordinatorL, "Renter deleted", Snackbar.LENGTH_LONG)
            .setAction("Undo") {

                isUndoClicked = true

                renterViewModel.insertRenter(renter)
                showToast(requireContext(), "Renter restored...")
            }
            .addCallback(object : Snackbar.Callback() {

                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {

                    if (!isUndoClicked && renter.isSynced == getString(R.string.t)) {

                        deleteDocumentFromFireStore(
                            context = requireContext(),
                            collection = getString(R.string.renters),
                            documentKey = renter.key!!
                        )
                    }
                }
            })
            .show()
    }

    override fun onEditClicked(renter: Renter) {

    val action = HomeFragmentDirections.actionHomeFragmentToAddRenterFragment(
        convertRenterToJSONString(renter)
    )

        findNavController().navigate(action)

/*
        MaterialDialog(requireContext(), BottomSheet())
            .show {

                title(text = "Edit ${renter.name} Info")

                customView(
                    R.layout.add_renter_layout,
                    noVerticalPadding = true,
                    scrollable = true
                )

                fillDocument(this.getCustomView(), renter)
            }
            .positiveButton(text = "Save Changes") {

                //todo : edit renter
            }
            .negativeButton {

                it.dismiss()
            }
*/
    }

   /* private fun fillDocument(customView: View, renter: Renter) {

        customView.findViewById<TextInputLayout>(R.id.renterNameET).editText?.setText(
            renter.name
        )

        customView.findViewById<EditText>(R.id.renterMobileNumberET).setText(
            renter.mobileNumber
        )

        customView.findViewById<TextInputLayout>(R.id.renterEmailET).editText?.setText(
            renter.emailId
        )

        customView.findViewById<EditText>(R.id.otherDocumentNameET).setText(
            renter.otherDocumentName
        )

        customView.findViewById<EditText>(R.id.otherDocumentNumber).setText(
            renter.otherDocumentNumber
        )

        customView.findViewById<TextInputLayout>(R.id.renterRoomNumberET).editText?.setText(
            renter.roomNumber
        )

        customView.findViewById<TextInputLayout>(R.id.renterAddressET).editText?.setText(
            renter.address
        )
    }
    */
    private fun hideNoRentersAddedTV() {

        try {

            binding.rentersRV.show()
            binding.noRentersAddedTV.hide()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun showNoRentersAddedTV() {

        try {

            binding.rentersRV.hide()
            binding.noRentersAddedTV.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStart() {
        super.onStart()

        if (mAuth?.currentUser != null) {

            if (isInternetAvailable(requireContext())) {

                if (mAuth?.currentUser!!.photoUrl != null) {

                    Glide.with(this)
                        .load(mAuth?.currentUser!!.photoUrl)
                        .into(binding.profileImage)
                }

            } else {

                showNoInternetMessage(requireContext())
            }

        }
    }

    private fun initListeners() {

        binding.searchRenterBtn.setOnClickListener(this)
        binding.addRenterFAB.setOnClickListener(this)
        binding.profileImage.setOnClickListener(this)

        binding.rentersRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                try {
                    if (dy > 0 && binding.addRenterFAB.visibility == View.VISIBLE) {

                        binding.addRenterFAB.hide()
                    } else if (dy < 0 && binding.addRenterFAB.visibility != View.VISIBLE) {

                        binding.addRenterFAB.show()

                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        })

    }

    override fun onClick(v: View?) {

        when (v?.id) {

            binding.searchRenterBtn.id -> {

                if (!isSearchViewVisible) {

                    showSearchView()
                } else {

                    hideSearchView()
                }
            }

            binding.addRenterFAB.id -> {

                findNavController().navigate(R.id.action_homeFragment_to_addRenterFragment)
            }
        }
    }

    private fun showSearchView() {

        isSearchViewVisible = !isSearchViewVisible

        binding.renterSV.show()
        binding.renterSV.animate().translationY(0f).alpha(1f).setDuration(350).start()

        binding.searchET.requestFocus()

        showKeyboard(requireActivity(), binding.searchET)
    }

    private fun hideSearchView() {

        isSearchViewVisible = !isSearchViewVisible

        binding.renterSV.animate().translationY(-50f).alpha(0f).setDuration(350).start()

        GlobalScope.launch {

            delay(360)

            closeKeyboard(requireActivity())
            withContext(Dispatchers.Main) {

                binding.renterSV.hide()
                binding.searchET.setText("")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        hideKeyBoard(requireActivity())

        _binding = null
    }

}