package com.johnreg.quizgame

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.johnreg.quizgame.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSignup.setOnClickListener {
            val email = binding.etSignupEmail.text.toString()
            val password = binding.etSignupPassword.text.toString()
            signupWithFirebase(email, password)
        }
    }

    private fun signupWithFirebase(email: String, password: String) {
        // Make the ProgressBar visible
        binding.pbSignup.visibility = View.VISIBLE

        // The user should be able to click the Signup button only once
        binding.btnSignup.isClickable = false

        // At this stage the account is created
        val authResultTask = auth.createUserWithEmailAndPassword(email, password)

        // Add a Listener to this function to track the result of this operation
        authResultTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Snackbar.make(binding.root, "Your account has been created", Snackbar.LENGTH_LONG).show()
                finish()
                // Make the ProgressBar invisible and the signup Button clickable
                binding.pbSignup.visibility = View.INVISIBLE
                binding.btnSignup.isClickable = true
            } else {
                // Show the reason for the error
                Snackbar.make(binding.root, "${task.exception?.localizedMessage}", Snackbar.LENGTH_LONG).show()
            }
        }
    }

}