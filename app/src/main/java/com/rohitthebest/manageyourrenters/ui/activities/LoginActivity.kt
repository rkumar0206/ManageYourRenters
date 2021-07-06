package com.rohitthebest.manageyourrenters.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
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
import com.rohitthebest.manageyourrenters.databinding.ActivityLoginBinding
import com.rohitthebest.manageyourrenters.others.Constants
import com.rohitthebest.manageyourrenters.ui.viewModels.PaymentViewModel
import com.rohitthebest.manageyourrenters.ui.viewModels.RenterViewModel
import com.rohitthebest.manageyourrenters.utils.Functions
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.hide
import com.rohitthebest.manageyourrenters.utils.Functions.Companion.show
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "LoginActivity"

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val renterViewModel by viewModels<RenterViewModel>()
    private val paymentViewModel by viewModels<PaymentViewModel>()

    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private var isSynced = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        isSynced = Functions.loadBooleanFromSharedPreference(
            this,
            Constants.IS_SYNCED_SHARED_PREF_NAME,
            Constants.IS_SYNCED_SHARED_PREF_KEY
        )

        if (mAuth.currentUser != null && !isSynced) {

            syncSavedDataFromFirebase()
        }

        initListeners()
    }


    private fun initListeners() {

        binding.signInButton.setOnClickListener {

            if (Functions.isInternetAvailable(this)) {
                signIn()
            } else {
                Functions.showNoInternetMessage(this)
            }
        }
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
                    Functions.showToast(this, "SignIn Un-successful")
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
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")

                        Functions.showToast(this, "SignIn successful")

                        syncSavedDataFromFirebase()

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        Functions.showToast(this, "Authentication Failed.")
                        hideProgressBar()

                    }
                    // [START_EXCLUDE]
                    hideProgressBar()
                    // [END_EXCLUDE]
                }
        } catch (e: Exception) {
        }
    }

    @SuppressLint("SetTextI18n")
    private fun syncSavedDataFromFirebase() {

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

                        Functions.showToast(
                            this,
                            "You have not added any renters yet!!"
                        )

                        saveIsSyncedValueAndNavigateToHomeActivity()
                    }

                }
                .addOnFailureListener {

                    Functions.showToast(this, it.message!!)

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

                                    saveIsSyncedValueAndNavigateToHomeActivity()

                                }
                            }

                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        } catch (e: IllegalStateException) {
                            e.printStackTrace()
                        }
                    } else {

                        saveIsSyncedValueAndNavigateToHomeActivity()
                    }
                }.addOnFailureListener {

                    Functions.showToast(this, it.message.toString())

                    syncPayments()
                }

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    private fun saveIsSyncedValueAndNavigateToHomeActivity() {

        showProgressBar()

        isSynced = true

        Functions.saveBooleanToSharedPreference(
            this@LoginActivity,
            Constants.IS_SYNCED_SHARED_PREF_NAME,
            Constants.IS_SYNCED_SHARED_PREF_KEY,
            isSynced
        )

        hideProgressBar()

        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)

    }

    private fun showProgressBar() {

        try {

            binding.progressBar.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideProgressBar() {

        try {

            binding.progressBar.hide()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}