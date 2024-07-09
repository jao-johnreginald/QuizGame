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
    private val scoreRef = database.reference.child("scores")

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

    // There is an abstract class in kotlin that we can use for the timer
    // Its name is CountDownTimer class, call this class and create an object from this class
    private lateinit var timer: CountDownTimer

    // This variable will show as false when the timer is not running and true when it is running
    private var timerContinue = false

    // Use the leftTime value in the code, for now equal the initial value
    private var leftTime = TOTAL_TIME

    // Reach some information such as the email and the userUID of the user who logs in to the application
    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser

    companion object {
        // Create a Long container to determine the initial value of time
        // Define time in milliseconds in kotlin, in programming time is usually defined by Long
        const val TOTAL_TIME = 30000L
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
            // Before the gameLogic (startTimer) function, the timer needs to be reset
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
        // When the user answers a question before the time runs out, the timer should stop
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
        // The marking colors must be restored and the options must be clickable
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

                    // The timer will start whenever the gameLogic function is called
                    startTimer()
                } else {
                    // When all the questions are finished, we will show a dialog window to the user
                    AlertDialog.Builder(this@QuizActivity)
                        .setTitle("Quiz Game")
                        .setMessage("Congratulations!!!\nYou have answered all the questions. Do you want to see the result?")
                        .setCancelable(false)
                        .setPositiveButton("See Result") { _, _ ->
                            // The user's score should have been saved in the database
                            // And then the ResultActivity should be opened
                            sendScore()
                        }
                        .setNegativeButton("Play Again") { _, _ ->
                            // This page should close and the MainActivity should open
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
        // Change the background color of the TextViews to WHITE
        binding.tvA.setBackgroundColor(Color.WHITE)
        binding.tvB.setBackgroundColor(Color.WHITE)
        binding.tvC.setBackgroundColor(Color.WHITE)
        binding.tvD.setBackgroundColor(Color.WHITE)

        // Make those options clickable
        binding.tvA.isClickable = true
        binding.tvB.isClickable = true
        binding.tvC.isClickable = true
        binding.tvD.isClickable = true
    }

    private fun startTimer() {
        /*
        Initialize the CountDownTimer object, the CountDownTimer class takes 2 parameters
        The first one is the initial value, it will be the leftTime
        The second one is the countDownInterval value, write 1000
        That means it will count down 1 second by 1 second
        Also we have to add the .start() end of the scope of the CountDownTimer class
         */
        timer = object : CountDownTimer(leftTime, 1000) {
            // This is what we want the timer to do every second
            override fun onTick(millisUntilFinished: Long) {
                // The onTick method will work until the 30 seconds are complete
                leftTime = millisUntilFinished
                // This method will update the TextView showing the duration
                updateCountDownText()
            }
            // Write what you want to do once the timer finishes
            override fun onFinish() {
                // The user cannot select an option after the time is up
                disableClickableOfOptions()
                /*
                The timer should reset, the 'time' text needs to be updated, present that to the user
                Write this message to the user on the 'question' TextView
                Also the timerContinue boolean value must be false
                 */
                resetTimer()
                updateCountDownText()
                val message = "Sorry, Time is up! Continue with the next question."
                binding.tvQuestion.text = message
                timerContinue = false
            }
        }.start()

        // Set the time 'continue' value to true here (when the timer starts)
        timerContinue = true
    }

    private fun updateCountDownText() {
        // This will give us the value of the remainingTime in seconds, convert this value to an Integer
        val remainingTime: Int = (leftTime / 1000).toInt()
        // Write this remainingTime Integer value on the 'time' text, every second the text will update
        binding.tvTime.text = remainingTime.toString()
    }

    private fun pauseTimer() {
        // Pause the timer in this method, use the cancel function
        timer.cancel()
        // Set the timerContinue value to true when the timer is running and false when it is not
        timerContinue = false
    }

    private fun resetTimer() {
        // When the timer is reset, pause the timer, and the leftTime should equal the TOTAL_TIME value again
        // This will set the timer for 30 seconds again, also the text should be updated
        pauseTimer()
        leftTime = TOTAL_TIME
        updateCountDownText()
    }

    private fun sendScore() {
        // Check that the user object is not null with the 'let' keyword
        user?.let { user ->
            // Get the UID code of the user who logged in to the application with the user.uid
            val userUID = user.uid
            // Use the scoreRef object to send data (userCorrect, userWrong) to the database in Firebase
            scoreRef.child(userUID).child("correct").setValue(userCorrect)
            scoreRef.child(userUID).child("wrong").setValue(userWrong).addOnSuccessListener {
                Toast.makeText(applicationContext, "Scores sent to database successfully", Toast.LENGTH_SHORT).show()
                // Close this page and open a new page when the scores will be displayed
                val intent = Intent(this@QuizActivity, ResultActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

}