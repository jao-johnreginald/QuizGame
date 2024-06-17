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

    // Create the database object
    private val database = FirebaseDatabase.getInstance()

    // Reach the data under the 'questions' child
    private val databaseReference = database.reference.child("questions")

    // Reach the data under the 'scores' child
    private val scoreRef = database.reference

    // Create the variables that we will assign when we retrieve the data out of the database
    private var question = ""
    private var answerA = ""
    private var answerB = ""
    private var answerC = ""
    private var answerD = ""
    private var correctAnswer = ""
    private var questionCount = 0
    private var questionNumber = 1

    // Assign the user's answer to this variable
    private var userAnswer = ""

    // Create other containers that hold the number of correct and incorrect answers of the user
    private var userCorrect = 0
    private var userWrong = 0

    private lateinit var timer: CountDownTimer
    private var timerContinue = false
    private var leftTime = TOTAL_TIME

    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser

    companion object {
        const val TOTAL_TIME = 25000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Call the gameLogic function in the onCreate function
        gameLogic()
        setButtonListeners()
    }

    private fun setButtonListeners() {
        binding.btnFinish.setOnClickListener { sendScore() }
        binding.btnNext.setOnClickListener {
            resetTimer()
            // Call the gameLogic function again when the user clicks the 'next' Button
            gameLogic()
        }
        binding.tvA.setOnClickListener { buttonLogic(binding.tvA, "a") }
        binding.tvB.setOnClickListener { buttonLogic(binding.tvB, "b") }
        binding.tvC.setOnClickListener { buttonLogic(binding.tvC, "c") }
        binding.tvD.setOnClickListener { buttonLogic(binding.tvD, "d") }
    }

    private fun buttonLogic(tvClicked: TextView, chosenAnswer: String) {
        pauseTimer()
        // Receive the answer from the user
        userAnswer = chosenAnswer
        // Check to see if these are correct
        if (correctAnswer == userAnswer) {
            // Make the background of the TextView clicked GREEN with the setBackgroundColor function
            tvClicked.setBackgroundColor(Color.GREEN)
            // Increase the value of the userCorrect
            userCorrect++
            // Print the user's correct value on the TextView 'correct'
            binding.tvCorrect.text = userCorrect.toString()
        } else {
            // Make the background of this TextView RED
            tvClicked.setBackgroundColor(Color.RED)
            // Increase the value of the userWrong
            userWrong++
            // Print the userWrong value on the TextView 'wrong'
            binding.tvWrong.text = userWrong.toString()
            // Call the findAnswer function in the else block and show the correct answer in green
            findAnswer()
        }
        // Once the user has answered, he should not select any options again until he clicks the 'next' Button
        // After the user selects an option, we must disable the clicking feature of each option
        disableClickableOfOptions()
    }

    // Retrieve the data in this function
    private fun gameLogic() {
        restoreOptions()

        // Use the databaseReference object we created above and the ValueEventListener interface
        databaseReference.addValueEventListener(object : ValueEventListener {
            // Perform data retrieving in this method, this method also constantly monitors the database live
            // When there's a change to the database, it instantly reflects to the application
            override fun onDataChange(snapshot: DataSnapshot) {
                // Learn the total number of questions, using the snapshot object
                questionCount = snapshot.childrenCount.toInt()

                // Create an if condition here, the quiz should continue until the value of
                // this questionNumber variable equals the number of questions in the database,
                // that is the value of the questionCount variable, otherwise the quiz will end
                if (questionNumber <= questionCount) {
                    // Retrieve all the data under the 1st question
                    question = snapshot.child("$questionNumber").child("q").value.toString()
                    answerA = snapshot.child("$questionNumber").child("a").value.toString()
                    answerB = snapshot.child("$questionNumber").child("b").value.toString()
                    answerC = snapshot.child("$questionNumber").child("c").value.toString()
                    answerD = snapshot.child("$questionNumber").child("d").value.toString()
                    correctAnswer = snapshot.child("$questionNumber").child("answer").value.toString()

                    // Print the 1st question
                    binding.tvQuestion.text = question
                    binding.tvA.text = answerA
                    binding.tvB.text = answerB
                    binding.tvC.text = answerC
                    binding.tvD.text = answerD

                    // After successfully retrieving the data, the ProgressBar
                    // should now disappear and the components should be VISIBLE
                    binding.pbQuiz.visibility = View.INVISIBLE
                    binding.layoutInfo.visibility = View.VISIBLE
                    binding.layoutQuestion.visibility = View.VISIBLE
                    binding.layoutButtons.visibility = View.VISIBLE

                    startTimer()
                } else {
                    // When all the questions are finished, we will show a dialog window to the user
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

                // Each time the gameLogic function is called, the questionNumber
                // will be increased by 1 until it's equal to the number of questions
                questionNumber++
            }
            // State if there's any action to be taken when data cannot be retrieved or an error occurs
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