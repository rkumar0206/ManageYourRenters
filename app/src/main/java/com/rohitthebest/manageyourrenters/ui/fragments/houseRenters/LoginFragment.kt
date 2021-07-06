package com.rohitthebest.manageyourrenters.ui.fragments.houseRenters

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.rohitthebest.manageyourrenters.R
import com.rohitthebest.manageyourrenters.database.entity.Payment
import com.rohitthebest.manageyourrenters.database.entity.Renter
import com.rohitthebest.manageyourrenters.databinding.FragmentLoginBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.others.Constants.IS_SYNCED_SHARED_PREF_KEY
import com.rohitthebest.manageyourrenters.others.Constants.IS_SYNCED_SHARED_PREF_NAME
import com.rohitthebest.manageyourrenters.ui.viewModels.PaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.RenterViewModel
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hide
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.isInternetAvailable
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.loadBooleanFromSharedPreference
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.saveBooleanToSharedPreference
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.show
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showNoInternetMessage
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val renterViewModel by viewModels<RenterViewModel>()
    private val paymentViewModel by viewModels<PaymentViewModel>()

    private val TAG = "LoginFragment"
    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private var isSynced = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = Firebase.auth

        isSynced = loadBooleanFromSharedPreference(
            requireActivity(),
            IS_SYNCED_SHARED_PREF_NAME,
            IS_SYNCED_SHARED_PREF_KEY
        )

        if (mAuth.currentUser != null && !isSynced) {

            checkSyncAndNavigateToHomeFragment()
        }

        initListeners()
    }

    override fun onStart() {
        super.onStart()

        isSynced = loadBooleanFromSharedPreference(
            requireActivity(),
            IS_SYNCED_SHARED_PREF_NAME,
            IS_SYNCED_SHARED_PREF_KEY
        )


        if (mAuth.currentUser != null && isSynced) {

            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun checkSyncAndNavigateToHomeFragment() {

        Log.i(TAG, "checkSyncAndNavigateToHomeFragment: ")

        if (!isSynced) {

            binding.loginCL.hide()
            binding.syncingCL.show()

            FirebaseFirestore.getInstance()
                .collection(getString(R.string.renters))
                .whereEqualTo("uid", Functions.getUid())
                .get()
                .addOnSuccessListener {

                    if (it.size() != 0) {

                        try {

                            binding.showSyncingInfoTV.text = "syncing renters..."

                            renterViewModel.deleteRenterByIsSynced(getString(R.string.t))

                            lifecycleScope.launch {

                                delay(150)

                                withContext(Dispatchers.Main) {

                                    val listOfRenters = it.toObjects(Renter::class.java)
                                    renterViewModel.insertRenters(listOfRenters)

                                    syncPayments()
                                }
                            }
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        } catch (e: IllegalStateException) {
                            e.printStackTrace()
                        }

                    } else {

                        showToast(requireContext(), "You have not added any renters yet!!")

                        saveIsSyncedValueAndNavigateToHomeFragment()
                    }

                }
                .addOnFailureListener {

                    showToast(requireContext(), it.message!!)

                    signIn()
                }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun syncPayments() {

        try {

            FirebaseFirestore.getInstance()
                .collection(getString(R.string.payments))
                .whereEqualTo("uid", Functions.getUid())
                .get()
                .addOnSuccessListener {

                    if (it.size() != 0) {

                        try {

                            binding.showSyncingInfoTV.text = "syncing payments..."

                            paymentViewModel.deleteAllPaymentsByIsSynced(getString(R.string.t))

                            lifecycleScope.launch {

                                delay(150)

                                withContext(Dispatchers.Main) {

                                    paymentViewModel.insertPayments(it.toObjects(Payment::class.java))
                                    Log.i(TAG, "syncPayments: inserted")

                                    saveIsSyncedValueAndNavigateToHomeFragment()

                                }
                            }

                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        } catch (e: IllegalStateException) {
                            e.printStackTrace()
                        }
                    } else {

                        saveIsSyncedValueAndNavigateToHomeFragment()
                    }
                }.addOnFailureListener {

                    showToast(requireContext(), it.message.toString())

                    syncPayments()
                }

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    private fun saveIsSyncedValueAndNavigateToHomeFragment() {

        showProgressBar()

        lifecycleScope.launch {

            delay(200)

            withContext(Dispatchers.Main) {

                isSynced = true

                saveBooleanToSharedPreference(
                    requireActivity(),
                    IS_SYNCED_SHARED_PREF_NAME,
                    IS_SYNCED_SHARED_PREF_KEY,
                    isSynced
                )

                hideProgressBar()

                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)

            }
        }
    }

    private fun initListeners() {

        binding.signInButton.setOnClickListener {

            if (isInternetAvailable(requireContext())) {
                signIn()
            } else {
                showNoInternetMessage(requireContext())
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mAuth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }


    // [START signin]
    private fun signIn() {

        showProgressBar()

        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(
            signInIntent,
            Constants.RC_SIGN_IN
        )
    }
    // [END signin]

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == Constants.RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)

                firebaseAuthWithGoogle(account.idToken!!)

            } catch (e: ApiException) {
                try {
                    // Google Sign In failed, update UI appropriately
                    Log.w(TAG, "Google sign in failed", e)
                    // [START_EXCLUDE]
                    showToast(requireContext(), "SignIn Un-successful")
                    hideProgressBar()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        // [START_EXCLUDE silent]
        showProgressBar()
        // [END_EXCLUDE]
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        try {
            mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")

                        showToast(requireContext(), "SignIn successful")

                        checkSyncAndNavigateToHomeFragment()

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        showToast(requireContext(), "Authentication Failed.")
                        hideProgressBar()

                    }
                    // [START_EXCLUDE]
                    hideProgressBar()
                    // [END_EXCLUDE]
                }
        } catch (e: Exception) {
        }
    }

    private fun showProgressBar() {

        try{

            binding.progressBar.show()
        }catch (e : Exception) {
            e.printStackTrace()
        }
    }

    private fun hideProgressBar() {

        try{

            binding.progressBar.hide()
        }catch (e : Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

}