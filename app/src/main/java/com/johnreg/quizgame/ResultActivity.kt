package com.johnreg.quizgame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.johnreg.quizgame.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding

    private val database = FirebaseDatabase.getInstance()
    private val databaseReference = database.reference.child("scores")

    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPlayAgain.setOnClickListener {

        }
        binding.btnExit.setOnClickListener {

        }
    }
}