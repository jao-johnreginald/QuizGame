package com.johnreg.quizgame

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.johnreg.quizgame.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    // Reach the child 'scores' under the main database
    private val database = FirebaseDatabase.getInstance()
    private val databaseReference = database.reference.child("scores")

    // Access user information
    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setButtonListeners()
        retrieveData()
    }

    private fun setButtonListeners() {
        // Return to the home page
        binding.btnPlayAgain.setOnClickListener { finish() }
        // Close the application
        binding.btnExit.setOnClickListener { finishAffinity() }
    }

    private fun retrieveData() {
        // Retrieve the data using the databaseReference object
        databaseReference.addValueEventListener(object : ValueEventListener {
            // Retrieve data from the database in the onDataChange function
            override fun onDataChange(snapshot: DataSnapshot) {
                // Check whether the user object is null with the 'let' keyword
                user?.let { user ->
                    // Create a new variable to hold the userUID code
                    val userUID = user.uid

                    // Retrieve the data using the snapshot object
                    val userCorrect = snapshot.child(userUID).child("correct").value.toString()
                    val userWrong = snapshot.child(userUID).child("wrong").value.toString()

                    // Write these values on the TextView
                    binding.tvScoreCorrect.text = userCorrect
                    binding.tvScoreWrong.text = userWrong
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_LONG).show()
            }
        })
    }

}