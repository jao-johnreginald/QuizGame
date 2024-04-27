package com.johnreg.quizgame

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
    private var questionNumber = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gameLogic()

        binding.apply {
            btnNext.setOnClickListener { gameLogic() }
            btnFinish.setOnClickListener {  }
            tvA.setOnClickListener {  }
            tvB.setOnClickListener {  }
            tvC.setOnClickListener {  }
            tvD.setOnClickListener {  }
        }
    }

    private fun gameLogic() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            // Perform data retrieving, constantly monitors the database live
            override fun onDataChange(snapshot: DataSnapshot) {
                questionCount = snapshot.childrenCount.toInt()

                if (questionNumber <= questionCount) {
                    question = snapshot.child("$questionNumber").child("q").value.toString()
                    answerA = snapshot.child("$questionNumber").child("a").value.toString()
                    answerB = snapshot.child("$questionNumber").child("b").value.toString()
                    answerC = snapshot.child("$questionNumber").child("c").value.toString()
                    answerD = snapshot.child("$questionNumber").child("d").value.toString()
                    correctAnswer = snapshot.child("$questionNumber").child("answer").value.toString()

                    binding.apply {
                        tvQuestion.text = question
                        tvA.text = answerA
                        tvB.text = answerB
                        tvC.text = answerC
                        tvD.text = answerD

                        pbQuiz.visibility = View.INVISIBLE
                        layoutInfo.visibility = View.VISIBLE
                        layoutQuestion.visibility = View.VISIBLE
                        layoutButtons.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(applicationContext, "You answered all the questions", Toast.LENGTH_SHORT).show()
                }

                questionNumber++
            }
            // State if there is any action to be taken when data cannot be retrieved
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_LONG).show()
            }
        })
    }

}