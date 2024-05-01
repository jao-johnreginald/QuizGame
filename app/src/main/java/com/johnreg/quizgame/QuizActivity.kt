package com.johnreg.quizgame

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
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

        binding.apply {
            gameLogic()

            btnNext.setOnClickListener {
                resetTimer()
                gameLogic()
            }
            btnFinish.setOnClickListener { sendScore() }

            tvA.setOnClickListener {
                pauseTimer()
                userAnswer = "a"
                if (correctAnswer == userAnswer) {
                    tvA.setBackgroundColor(Color.GREEN)
                    userCorrect++
                    tvCorrect.text = userCorrect.toString()
                } else {
                    tvA.setBackgroundColor(Color.RED)
                    userWrong++
                    tvWrong.text = userWrong.toString()
                    findAnswer()
                }
                disableClickableOfOptions()
            }

            tvB.setOnClickListener {
                pauseTimer()
                userAnswer = "b"
                if (correctAnswer == userAnswer) {
                    tvB.setBackgroundColor(Color.GREEN)
                    userCorrect++
                    tvCorrect.text = userCorrect.toString()
                } else {
                    tvB.setBackgroundColor(Color.RED)
                    userWrong++
                    tvWrong.text = userWrong.toString()
                    findAnswer()
                }
                disableClickableOfOptions()
            }

            tvC.setOnClickListener {
                pauseTimer()
                userAnswer = "c"
                if (correctAnswer == userAnswer) {
                    tvC.setBackgroundColor(Color.GREEN)
                    userCorrect++
                    tvCorrect.text = userCorrect.toString()
                } else {
                    tvC.setBackgroundColor(Color.RED)
                    userWrong++
                    tvWrong.text = userWrong.toString()
                    findAnswer()
                }
                disableClickableOfOptions()
            }

            tvD.setOnClickListener {
                pauseTimer()
                userAnswer = "d"
                if (correctAnswer == userAnswer) {
                    tvD.setBackgroundColor(Color.GREEN)
                    userCorrect++
                    tvCorrect.text = userCorrect.toString()
                } else {
                    tvD.setBackgroundColor(Color.RED)
                    userWrong++
                    tvWrong.text = userWrong.toString()
                    findAnswer()
                }
                disableClickableOfOptions()
            }
        }
    }

    private fun ActivityQuizBinding.gameLogic() {
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

                    tvQuestion.text = question
                    tvA.text = answerA
                    tvB.text = answerB
                    tvC.text = answerC
                    tvD.text = answerD
                    pbQuiz.visibility = View.INVISIBLE
                    layoutInfo.visibility = View.VISIBLE
                    layoutQuestion.visibility = View.VISIBLE
                    layoutButtons.visibility = View.VISIBLE

                    startTimer()
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

    private fun ActivityQuizBinding.findAnswer() {
        when (correctAnswer) {
            "a" -> tvA.setBackgroundColor(Color.GREEN)
            "b" -> tvB.setBackgroundColor(Color.GREEN)
            "c" -> tvC.setBackgroundColor(Color.GREEN)
            "d" -> tvD.setBackgroundColor(Color.GREEN)
        }
    }

    private fun ActivityQuizBinding.disableClickableOfOptions() {
        tvA.isClickable = false
        tvB.isClickable = false
        tvC.isClickable = false
        tvD.isClickable = false
    }

    private fun ActivityQuizBinding.restoreOptions() {
        tvA.setBackgroundColor(Color.WHITE)
        tvB.setBackgroundColor(Color.WHITE)
        tvC.setBackgroundColor(Color.WHITE)
        tvD.setBackgroundColor(Color.WHITE)

        tvA.isClickable = true
        tvB.isClickable = true
        tvC.isClickable = true
        tvD.isClickable = true
    }

    private fun ActivityQuizBinding.startTimer() {
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
                tvQuestion.text = message
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