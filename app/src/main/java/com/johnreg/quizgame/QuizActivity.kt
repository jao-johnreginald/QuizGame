package com.johnreg.quizgame

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
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

    // These will be assigned after an answer is chosen by the user
    private var userAnswer = ""
    private var userCorrect = 0
    private var userWrong = 0

    private lateinit var timer: CountDownTimer
    private var timerContinue = false
    private var leftTime = TOTAL_TIME

    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser
    private val scoreRef = database.reference

    companion object {
        const val TOTAL_TIME = 25000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gameLogic()
        setButtonListeners()
    }

    private fun setButtonListeners() {
        binding.btnFinish.setOnClickListener { sendScore() }
        binding.btnNext.setOnClickListener {
            resetTimer()
            gameLogic()
        }
        binding.tvA.setOnClickListener { buttonLogic(binding.tvA, "a") }
        binding.tvB.setOnClickListener { buttonLogic(binding.tvB, "b") }
        binding.tvC.setOnClickListener { buttonLogic(binding.tvC, "c") }
        binding.tvD.setOnClickListener { buttonLogic(binding.tvD, "d") }
    }

    private fun buttonLogic(tvClicked: TextView, chosenAnswer: String) {
        disableClickableOfOptions()
        pauseTimer()
        userAnswer = chosenAnswer
        if (correctAnswer == userAnswer) {
            tvClicked.setBackgroundColor(Color.GREEN)
            userCorrect++
            binding.tvCorrect.text = userCorrect.toString()
        } else {
            tvClicked.setBackgroundColor(Color.RED)
            userWrong++
            binding.tvWrong.text = userWrong.toString()
            findAnswer()
        }
    }

    private fun gameLogic() {
        restoreOptions()

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

                    binding.tvQuestion.text = question
                    binding.tvA.text = answerA
                    binding.tvB.text = answerB
                    binding.tvC.text = answerC
                    binding.tvD.text = answerD
                    binding.pbQuiz.visibility = View.INVISIBLE
                    binding.layoutInfo.visibility = View.VISIBLE
                    binding.layoutQuestion.visibility = View.VISIBLE
                    binding.layoutButtons.visibility = View.VISIBLE

                    startTimer()
                } else {
                    AlertDialog.Builder(this@QuizActivity)
                        .setTitle("Quiz Game")
                        .setMessage("Congratulations!!!\nYou have answered all the questions. Do you want to see the result?")
                        .setCancelable(false)
                        .setPositiveButton("See Result") { _, _ ->
                            sendScore()
                        }
                        .setNegativeButton("Play Again") { _, _ ->
                            val intent = Intent(this@QuizActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        .create()
                        .show()
                }

                questionNumber++
            }
            // State if there is any action to be taken when data cannot be retrieved
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun findAnswer() {
        when (correctAnswer) {
            "a" -> binding.tvA.setBackgroundColor(Color.GREEN)
            "b" -> binding.tvB.setBackgroundColor(Color.GREEN)
            "c" -> binding.tvC.setBackgroundColor(Color.GREEN)
            "d" -> binding.tvD.setBackgroundColor(Color.GREEN)
        }
    }

    private fun disableClickableOfOptions() {
        binding.tvA.isClickable = false
        binding.tvB.isClickable = false
        binding.tvC.isClickable = false
        binding.tvD.isClickable = false
    }

    private fun restoreOptions() {
        binding.tvA.setBackgroundColor(Color.WHITE)
        binding.tvB.setBackgroundColor(Color.WHITE)
        binding.tvC.setBackgroundColor(Color.WHITE)
        binding.tvD.setBackgroundColor(Color.WHITE)

        binding.tvA.isClickable = true
        binding.tvB.isClickable = true
        binding.tvC.isClickable = true
        binding.tvD.isClickable = true
    }

    private fun startTimer() {
        timer = object : CountDownTimer(leftTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                leftTime = millisUntilFinished
                updateCountDownText()
            }

            override fun onFinish() {
                disableClickableOfOptions()
                resetTimer()
                updateCountDownText()
                val message = "Sorry, Time is up! Continue with the next question."
                binding.tvQuestion.text = message
                timerContinue = false
            }
        }.start()

        timerContinue = true
    }

    private fun updateCountDownText() {
        val remainingTime: Int = (leftTime / 1000).toInt()
        binding.tvTime.text = remainingTime.toString()
    }

    private fun pauseTimer() {
        timer.cancel()
        timerContinue = false
    }

    private fun resetTimer() {
        pauseTimer()
        leftTime = TOTAL_TIME
        updateCountDownText()
    }

    private fun sendScore() {
        user?.let {
            val userUID = it.uid
            scoreRef.child("scores").child(userUID).child("correct").setValue(userCorrect)
            scoreRef.child("scores").child(userUID).child("wrong").setValue(userWrong).addOnSuccessListener {
                Toast.makeText(applicationContext, "Scores sent to database successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@QuizActivity, ResultActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

}