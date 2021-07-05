package com.rohitthebest.manageyourrenters.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.adapters.ShowRentersAdapter
import com.rohitthebest.manageyourrenters.database.entity.Renter
import com.rohitthebest.manageyourrenters.databinding.FragmentHomeBinding
import com.rohitthebest.manageyourrenters.others.Constants.IS_SYNCED_SHARED_PREF_KEY
import com.rohitthebest.manageyourrenters.others.Constants.IS_SYNCED_SHARED_PREF_NAME
import com.rohitthebest.manageyourrenters.ui.viewModels.PaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.RenterViewModel
import com.rohitthebest.manageyourrenters.utils.ConversionWithGson
import com.rohitthebest.manageyourrenters.utils.ConversionWithGson.Companion.convertRenterToJSONString
import com.rohitthebest.manageyourrenters.utils.FirebaseServiceHelper
import com.rohitthebest.manageyourrenters.utils.FirebaseServiceHelper.Companion.deleteDocumentFromFireStore
import com.rohitthebest.manageyourrenters.utils.FirebaseServiceHelper.Companion.uploadDocumentToFireStore
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.closeKeyboard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hide
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hideKeyBoard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.saveBooleanToSharedPreference
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.show
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showKeyboard
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

@AndroidEntryPoint
class HomeFragment : Fragment(), View.OnClickListener, ShowRentersAdapter.OnClickListener {

    private val TAG = "HomeFragment"

    private val renterViewModel: RenterViewModel by viewModels()
    private val paymentViewModel: PaymentViewModel by viewModels()

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var isSearchViewVisible = false
    private var mAuth: FirebaseAuth? = null

    private lateinit var mAdapter: ShowRentersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = Firebase.auth

        mAdapter = ShowRentersAdapter()

        showProgressBar()

        lifecycleScope.launch {

            delay(350)

            withContext(Dispatchers.Main) {

                updateUI()
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

        hideProgressBar()
    }

    override fun onRenterClicked(renter: Renter) {

        val action = HomeFragmentDirections.actionHomeFragmentToPaymentFragment(
            renter.key
            //convertRenterToJSONString(renter)
        )
        findNavController().navigate(action)
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

                    deleteAllPaymentsOfThisRenter(renter)
                }
            })
            .show()
    }

    private fun deleteAllPaymentsOfThisRenter(renter: Renter) {

        paymentViewModel.deleteAllPaymentsOfRenter(renter.key!!)

        var paymentKeyList: List<String>

        paymentViewModel.getAllPaymentsListOfRenter(renterKey = renter.key!!)
            .observe(viewLifecycleOwner) {

                if (it.isNotEmpty()) {

                    paymentKeyList =
                        it.filter { payment -> payment.isSynced == getString(R.string.t) }
                            .map { pay ->

                                pay.key
                            }

                    if (paymentKeyList.isNotEmpty()) {

                        FirebaseServiceHelper.deleteAllDocumentsUsingKey(
                            requireContext(),
                            getString(R.string.payments),
                            ConversionWithGson.convertStringListToJSON(paymentKeyList)
                        )

                    }
                }

            }

    }

    override fun onEditClicked(renter: Renter) {

        val action = HomeFragmentDirections.actionHomeFragmentToAddRenterFragment(
            convertRenterToJSONString(renter)
        )

        findNavController().navigate(action)
    }

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

    private fun updateUI() {

        if (mAuth?.currentUser != null) {

            try {

                if (mAuth?.currentUser!!.photoUrl != null) {

                    Glide.with(this)
                        .load(mAuth?.currentUser!!.photoUrl)
                        .into(binding.profileImage)
                }
            } catch (e: Exception) {

                e.printStackTrace()
            }

        }
    }

    private fun initListeners() {

        binding.searchRenterBtn.setOnClickListener(this)
        binding.addRenterFAB.setOnClickListener(this)
        binding.profileImage.setOnClickListener(this)
        binding.downloadChangesBtn.setOnClickListener(this)

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

            binding.profileImage.id -> {

                showBottomSheetProfileDialog()
            }

            binding.downloadChangesBtn.id -> {

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Sync changes from cloud?")
                    .setMessage("If any changes has been done on cloud this will sync them with your phone.")
                    .setPositiveButton("Request sync") { d, _ ->

                        if (isInternetAvailable(requireContext())) {

                            saveBooleanToSharedPreference(
                                requireActivity(),
                                IS_SYNCED_SHARED_PREF_NAME,
                                IS_SYNCED_SHARED_PREF_KEY,
                                false
                            )

                            showProgressBar()

                            lifecycleScope.launch {

                                delay(200)

                                withContext(Dispatchers.Main) {

                                    hideProgressBar()

                                    findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
                                }
                            }

                            d.dismiss()
                        } else {

                            showNoInternetMessage(requireContext())
                        }
                    }.setNegativeButton("Cancel") { d, _ ->

                        d.dismiss()
                    }
                    .create()
                    .show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showBottomSheetProfileDialog() {

        MaterialDialog(requireActivity(), BottomSheet(layoutMode = LayoutMode.WRAP_CONTENT)).show {

            customView(
                R.layout.user_info_with_sign_out_layout,
                scrollable = true
            )

            val userName = getCustomView().findViewById<TextView>(R.id.userNameTV)
            val emailId = getCustomView().findViewById<TextView>(R.id.userEmailTV)
            val noOfRenters = getCustomView().findViewById<TextView>(R.id.noOfRentersTV)
            val signOutBtn = getCustomView().findViewById<MaterialCardView>(R.id.signOutBtn)

            userName.text = mAuth?.currentUser?.displayName
            emailId.text = mAuth?.currentUser?.email
            renterViewModel.getRenterCount().observe(viewLifecycleOwner) {

                noOfRenters.text = "Number of renters : $it"
            }

            signOutBtn.setOnClickListener {

                if (isInternetAvailable(requireContext())) {

                    signOut()

                    dismiss()
                } else {

                    showNoInternetMessage(requireContext())
                }
            }
        }
    }

    private fun signOut() {

        try {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Are You Sure?")
                .setPositiveButton("Yes") { _, _ ->

                    mAuth?.signOut()

                    //[Google Sign Out]
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

                    googleSignInClient?.signOut()?.addOnCompleteListener {
                        Log.i(TAG, "Google signOut Successful")

                        try {
                            Log.i(TAG, "signOut: Google signOut Successful")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    showToast(requireContext(), "SignOut Successful")

                    //deleting everything saved on SQLite
                    deleteEverythingFromSQLite()

                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteEverythingFromSQLite() {

        try {
            renterViewModel.deleteAllRenter()
            paymentViewModel.deleteAllPayments()
            changeIsSyncedValue()

        } catch (e: Exception) {
        }
    }

    private fun changeIsSyncedValue() {

        try {

            saveBooleanToSharedPreference(
                requireActivity(),
                IS_SYNCED_SHARED_PREF_NAME,
                IS_SYNCED_SHARED_PREF_KEY,
                false
            )

            Log.i(TAG, "saveIsSyncedValue: changed the value of isSynced to false")

            lifecycleScope.launch {

                delay(200)

                withContext(Dispatchers.Main) {

                    findNavController().navigate(R.id.action_homeFragment_to_loginFragment)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "saveData: ${e.message}")
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

        lifecycleScope.launch {

            closeKeyboard(requireActivity())

            delay(360)

            withContext(Dispatchers.Main) {

                binding.renterSV.hide()
                binding.searchET.setText("")
            }
        }
    }

    private fun showProgressBar() {

        try {

            binding.homeProgressBar.show()
            binding.rentersRV.hide()
        } catch (e: java.lang.Exception) {

            e.printStackTrace()
        }
    }

    private fun hideProgressBar() {

        try {

            binding.homeProgressBar.hide()
            binding.rentersRV.show()
        } catch (e: java.lang.Exception) {

            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()

        try {

            hideKeyBoard(requireActivity())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        hideKeyBoard(requireActivity())

        _binding = null
    }

}