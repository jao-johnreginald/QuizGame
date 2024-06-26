package com.johnreg.quizgame

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.johnreg.quizgame.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private val googleSignInClient: GoogleSignInClient by lazy {
        // Choose the standard google sign in method with the Builder method
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // This method specifies that an id token for authenticating users is requested
            .requestIdToken(getString(R.string.request_id_token))
            // Ask the user for their email
            .requestEmail().build()
        // Create instance with the gso
        GoogleSignIn.getClient(this, gso)
    }

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeActivityResultLauncher()
        setGoogleButtonText()
        setButtonListeners()
    }

    private fun initializeActivityResultLauncher() {
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                // Create a thread using the Task class
                // This thread will get the data from google in the background
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                // Start the login process using the task object
                firebaseSignInWithGoogle(task)
            }
        }
    }

    private fun setGoogleButtonText() {
        // Access the TextView inside this button
        val textOfGoogleButton = binding.btnGoogleSignIn.getChildAt(0) as TextView
        textOfGoogleButton.apply {
            setText(R.string.continue_with_google)
            setTextColor(Color.BLACK)
            textSize = 18F
        }
    }

    private fun setButtonListeners() {
        binding.btnSignIn.setOnClickListener { signInUser() }

        binding.btnGoogleSignIn.setOnClickListener {
            // Google sign in, can't run intent with startActivity, must use activityResultLauncher
            val signInIntent = googleSignInClient.signInIntent
            activityResultLauncher.launch(signInIntent)
        }

        binding.tvSignUp.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        binding.tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signInUser() {
        // Get the EditText values
        val userEmail = binding.etLoginEmail.text.toString()
        val userPassword = binding.etLoginPassword.text.toString()
        // After signInWithEmailAndPassword() is completed, define the transactions to be performed
        auth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener { task ->
            // If the email and password are correct -> start MainActivity, else -> show error message
            if (task.isSuccessful) {
                startMainActivity()
            } else {
                Toast.makeText(
                    applicationContext,
                    task.exception?.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun firebaseSignInWithGoogle(task: Task<GoogleSignInAccount>) {
        try {
            // Do the sign in operation, do the sign in process using the Google API
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
            // Create an object from the AuthCredential class
            // The idToken has a unique code for each device, we can tell which device is logged in
            val authCredential = GoogleAuthProvider.getCredential(account.idToken, null)
            // Do the authentication process using the auth object created from the FirebaseAuth class
            auth.signInWithCredential(authCredential)
            // start MainActivity
            startMainActivity()
        } catch (error: ApiException) {
            // If there is a problem while receiving data from the API, take this error inside the catch block
            Toast.makeText(applicationContext, error.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

    private fun startMainActivity() {
        Toast.makeText(applicationContext, "Welcome to Quiz Game", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}