package com.johnreg.quizgame

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.johnreg.quizgame.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignIn.setOnClickListener {
            val userEmail = binding.etLoginEmail.text.toString()
            val userPassword = binding.etLoginPassword.text.toString()
            signInUser(userEmail, userPassword)
        }
        binding.btnGoogleSignIn.setOnClickListener {  }
        binding.tvSignUp.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
        binding.tvForgotPassword.setOnClickListener {  }
    }

    private fun signInUser(userEmail: String, userPassword: String) {
        // Compare the previously created account with the account currently entered
        auth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener { task ->
            // If the transaction is completed, define the transactions to be performed
            if (task.isSuccessful) {
                Snackbar.make(binding.root, "Welcome to Quiz Game", Snackbar.LENGTH_LONG).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Snackbar.make(binding.root, "${task.exception?.localizedMessage}", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}