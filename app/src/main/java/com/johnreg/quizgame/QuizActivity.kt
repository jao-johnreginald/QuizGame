package com.johnreg.quizgame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.johnreg.quizgame.databinding.ActivityQuizBinding

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding

    // Reach the data under the 'questions' child
    private val database = FirebaseDatabase.getInstance()
    private val databaseReference = database.reference.child("questions")

    // These will be assigned after the data gets retrieved
    private var question = ""
    private var answerA = ""
    private var answerB = ""
    private var answerC = ""
    private var answerD = ""
    private var correctAnswer = ""
    private var questionCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            btnNext.setOnClickListener {  }
            btnFinish.setOnClickListener {  }
            tvA.setOnClickListener {  }
            tvB.setOnClickListener {  }
            tvC.setOnClickListener {  }
            tvD.setOnClickListener {  }
        }
    }
}