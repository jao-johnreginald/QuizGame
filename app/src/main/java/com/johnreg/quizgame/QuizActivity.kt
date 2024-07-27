package com.johnreg.quizgame

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
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
import kotlin.random.Random

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding

    // Get the FirebaseDatabase instance
    private val data = FirebaseDatabase.getInstance()

    // Reach the data under the 'questions' and 'scores' child using the FirebaseDatabase instance
    private val dataRefQuestions = data.reference.child("questions")
    private val dataRefScores = data.reference.child("scores")

    private lateinit var dataSnapshot: DataSnapshot

    // Create the variables that we will assign when we retrieve the data out of the database
    private var correctAnswer = ""

    // Create other containers that hold the number of correct and incorrect answers of the user
    private var userCorrect = 0
    private var userWrong = 0

    // There is an abstract class in kotlin that we can use for the timer
    // Its name is CountDownTimer class, call this class and create an object from this class
    private lateinit var timer: CountDownTimer

    // Use the leftTime value in the code, for now equal the initial value
    private var leftTime = TOTAL_TIME

    // Reach some information such as the email and the userUID of the user who logs in to the application
    private val auth = FirebaseAuth.getInstance()
    private val user = auth.currentUser

    // HashSet only considers one of the same elements ignoring all others
    // Create an array from the HashSet class and transfer the randomly generated numbers to this array
    private val questions: HashSet<Int> = HashSet()
    private var index = 0

    companion object {
        const val QUESTIONS_TO_SHOW = 5
        // Create a Long container to determine the initial value of time
        // Define time in milliseconds in kotlin, in programming time is usually defined by Long
        const val TOTAL_TIME = 30000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeDataSnapshot()
    }

    private fun initializeDataSnapshot() {
        // Use the DatabaseReference object created above and the ValueEventListener interface
        dataRefQuestions.addValueEventListener(object : ValueEventListener {
            // Perform data retrieving, also constantly monitors the database live
            override fun onDataChange(snapshot: DataSnapshot) {
                // Retrieve the data through the snapshot object created from the DataSnapshot class
                dataSnapshot = snapshot
                initializeHashSet()

                // Call the retrieveData function after initializing the DataSnapshot and HashSet
                retrieveData()

                // The ProgressBar should now disappear and the components should be VISIBLE
                hidePbAndShowLayouts()
                setListenersAndTexts()
            }
            // State if there's any action to be taken when data cannot be retrieved or an error occurs
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun initializeHashSet() {
        // Learn the total number of questions, using the dataSnapshot object
        val questionCount = dataSnapshot.childrenCount.toInt()

        // Continue to generate random numbers until the questions.size is QUESTIONS_TO_SHOW
        do {
            // Generate a random number between 1 and questionCount
            val number = Random.nextInt(1, questionCount + 1)
            // Pass this number to the HashSet of questions
            questions.add(number)
            Log.d("NumberGenerated", number.toString())
        } while (questions.size < QUESTIONS_TO_SHOW)
        Log.d("NumberHashSet", questions.toString())
    }

    private fun setListenersAndTexts() {
        // Print the initial value of userCorrect and userWrong
        binding.tvCorrect.text = userCorrect.toString()
        binding.tvWrong.text = userWrong.toString()

        binding.tvA.setOnClickListener { onAnswerClicked("a", binding.tvA) }
        binding.tvB.setOnClickListener { onAnswerClicked("b", binding.tvB) }
        binding.tvC.setOnClickListener { onAnswerClicked("c", binding.tvC) }
        binding.tvD.setOnClickListener { onAnswerClicked("d", binding.tvD) }

        binding.btnNext.setOnClickListener { onNextButtonClicked() }
        binding.btnFinish.setOnClickListener { sendScore() }
    }

    private fun onAnswerClicked(userAnswer: String, textViewAnswer: TextView) {
        // Stop the timer, disable the clicking feature of each option until the 'next' Button is clicked
        timer.cancel()
        disableClickableOfOptions()
        // Receive the answer from the user, check to see if these are correct
        if (userAnswer == correctAnswer) {
            // Make the background of the textViewAnswer GREEN, increase the value of the userCorrect
            textViewAnswer.setBackgroundColor(Color.GREEN)
            userCorrect++
            // Print the userCorrect value on the TextView 'correct'
            binding.tvCorrect.text = userCorrect.toString()
        } else {
            // Make the background of the textViewAnswer RED, increase the value of the userWrong
            textViewAnswer.setBackgroundColor(Color.RED)
            userWrong++
            // Print the userWrong value on the TextView 'wrong'
            binding.tvWrong.text = userWrong.toString()
            // Show the correct answer in green
            when (correctAnswer) {
                "a" -> binding.tvA.setBackgroundColor(Color.GREEN)
                "b" -> binding.tvB.setBackgroundColor(Color.GREEN)
                "c" -> binding.tvC.setBackgroundColor(Color.GREEN)
                "d" -> binding.tvD.setBackgroundColor(Color.GREEN)
            }
        }
    }

    private fun onNextButtonClicked() {
        // Before the retrieveData (startTimer) function, the timer needs to be reset
        resetTimer()

        // The marking colors must be restored and the options must be clickable
        restoreOptions()

        // Continue the quiz until the index equals the questions.size, otherwise end the quiz
        if (index < questions.size) {
            // Call the retrieveData function again when the user clicks the 'next' Button
            retrieveData()
        } else {
            // When all the questions are finished, show a dialog window to the user
            showDialog()
        }
    }

    private fun retrieveData() {
        // Reach the elements of the HashSet array using the index variable
        val element = questions.elementAt(index)

        // Retrieve all the data under the element
        val question = dataSnapshot.child("$element").child("q").value.toString()
        val answerA = dataSnapshot.child("$element").child("a").value.toString()
        val answerB = dataSnapshot.child("$element").child("b").value.toString()
        val answerC = dataSnapshot.child("$element").child("c").value.toString()
        val answerD = dataSnapshot.child("$element").child("d").value.toString()
        correctAnswer = dataSnapshot.child("$element").child("answer").value.toString()

        // Print the data under the element
        binding.tvQuestion.text = question
        binding.tvA.text = answerA
        binding.tvB.text = answerB
        binding.tvC.text = answerC
        binding.tvD.text = answerD

        // The timer will start only after the data is done being retrieved
        startTimer()

        // Increase the index by 1 until it's equal to the number of elements in the HashSet
        index++
    }

    private fun hidePbAndShowLayouts() {
        binding.pbQuiz.visibility = View.INVISIBLE
        binding.layoutInfo.visibility = View.VISIBLE
        binding.layoutQuestion.visibility = View.VISIBLE
        binding.layoutButtons.visibility = View.VISIBLE
    }

    private fun showDialog() {
        AlertDialog.Builder(this@QuizActivity)
            .setTitle("Quiz Game")
            .setMessage("Congratulations!!!\nYou have answered all the questions. Do you want to see the result?")
            .setCancelable(false)
            .setPositiveButton("See Result") { _, _ ->
                // Save the user's score in the database and open the ResultActivity
                sendScore()
            }
            .setNegativeButton("Play Again") { _, _ ->
                // Close this page and open the MainActivity
                val intent = Intent(this@QuizActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            .create()
            .show()
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
        Also add the .start() end of the scope of the CountDownTimer class
         */
        timer = object : CountDownTimer(leftTime, 1000) {
            // This is what the timer does every second
            override fun onTick(millisUntilFinished: Long) {
                // The onTick method will work until the TOTAL_TIME seconds are complete
                leftTime = millisUntilFinished
                // Update the TextView showing the duration
                updateCountDownText()
            }
            // Write what to do once the timer finishes
            override fun onFinish() {
                // The user cannot select an option after the time is up
                disableClickableOfOptions()
                // The timer should reset, also the 'time' text needs to be updated
                resetTimer()
                // Write this message to the user on the 'question' TextView
                val message = "Sorry, Time is up! Continue with the next question."
                binding.tvQuestion.text = message
            }
        }.start()
    }

    private fun updateCountDownText() {
        // Get the value of the remainingTime in seconds, convert this value to an Integer
        val remainingTime: Int = (leftTime / 1000).toInt()
        // Write this remainingTime Integer value on the 'time' text, every second the text will update
        binding.tvTime.text = remainingTime.toString()
    }

    private fun resetTimer() {
        // Pause the timer, set the timer for TOTAL_TIME seconds again, update the text
        timer.cancel()
        leftTime = TOTAL_TIME
        updateCountDownText()
    }

    private fun sendScore() {
        // Check that the user object is not null with the 'let' keyword
        user?.let { user ->
            // Get the UID code of the user who logged in to the application with the user.uid
            val uid = user.uid
            // Use .setValue() to send data (userCorrect, userWrong) to the database in Firebase
            dataRefScores.child(uid).child("correct").setValue(userCorrect).addOnSuccessListener {
                dataRefScores.child(uid).child("wrong").setValue(userWrong).addOnSuccessListener {
                    Toast.makeText(
                        applicationContext,
                        "Scores sent to database successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Close this page and open a new page when the scores will be displayed
                    val intent = Intent(this@QuizActivity, ResultActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

}