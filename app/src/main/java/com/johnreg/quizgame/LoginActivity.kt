package com.johnreg.quizgame

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.johnreg.quizgame.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Access the TextView inside this button
        val textOfGoogleButton = binding.btnGoogleSignIn.getChildAt(0) as TextView
        textOfGoogleButton.apply {
            setText(R.string.continue_with_google)
            setTextColor(Color.BLACK)
            textSize = 18F
        }

        // Register
        registerActivityForGoogleSignIn()

        binding.btnSignIn.setOnClickListener {
            val userEmail = binding.etLoginEmail.text.toString()
            val userPassword = binding.etLoginPassword.text.toString()
            signInUser(userEmail, userPassword)
        }

        binding.btnGoogleSignIn.setOnClickListener { signInGoogle() }

        binding.tvSignUp.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        binding.tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()

        // If the user is already logged in, write the operation to be done
        if (auth.currentUser != null) {
            startMainActivity()
        }
    }

    private fun signInUser(userEmail: String, userPassword: String) {
        // Compare the previously created account with the account currently entered
        auth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener { task ->
            // If the transaction is completed, define the transactions to be performed
            if (task.isSuccessful) {
                startMainActivity()
            } else {
                Toast.makeText(this, "${task.exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startMainActivity() {
        Toast.makeText(this, "Welcome to Quiz Game", Toast.LENGTH_LONG).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun signInGoogle() {
        // Choose the standard google sign in method with the Builder method
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // This method specifies that an id token for authenticating users is requested
            .requestIdToken(getString(R.string.request_id_token))
            // Ask the user for their email
            .requestEmail().build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        signIn()
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        // Can't run this intent with startActivity(), use activityResultLauncher
        activityResultLauncher.launch(signInIntent)
    }

    private fun registerActivityForGoogleSignIn() {}
}