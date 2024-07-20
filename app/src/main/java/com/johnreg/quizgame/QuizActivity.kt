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

    // Create the variables that we will assign when we retrieve the data out of the database
    private var question = ""
    private var answerA = ""
    private var answerB = ""
    private var answerC = ""
    private var answerD = ""
    private var correctAnswer = ""
    private var questionCount = 0
    private var questionNumber = 0

    // Assign the user's answer to this variable
    private var userAnswer = ""

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

    companion object {
        // Create a Long container to determine the initial value of time
        // Define time in milliseconds in kotlin, in programming time is usually defined by Long
        const val TOTAL_TIME = 30000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeHashSet()
        // Call the gameLogic function in the onCreate function
        gameLogic()
        setListenersAndTexts()
    }

    private fun initializeHashSet() {
        // Generate random numbers until the number of elements in the 'questions' array is 5
        do {
            // Generate a random number between 1 and 10
            val number = Random.nextInt(1, 11)
            // Pass this number to the HashSet of questions
            questions.add(number)
            Log.d("NumberGenerated", number.toString())
        } while (questions.size < 5)
        Log.d("NumberHashSet", questions.toString())
    }

    private fun setListenersAndTexts() {
        binding.btnFinish.setOnClickListener { sendScore() }
        binding.btnNext.setOnClickListener {
            // Before the gameLogic (startTimer) function, the timer needs to be reset
            resetTimer()
            // Call the gameLogic function again when the user clicks the 'next' Button
            gameLogic()
        }

        // Print the initial value of userCorrect and userWrong
        binding.tvCorrect.text = userCorrect.toString()
        binding.tvWrong.text = userWrong.toString()

        binding.tvA.setOnClickListener { onAnswerClicked(binding.tvA, "a") }
        binding.tvB.setOnClickListener { onAnswerClicked(binding.tvB, "b") }
        binding.tvC.setOnClickListener { onAnswerClicked(binding.tvC, "c") }
        binding.tvD.setOnClickListener { onAnswerClicked(binding.tvD, "d") }
    }

    private fun onAnswerClicked(textViewAnswer: TextView, answer: String) {
        // When the user answers a question before the time runs out, the timer should stop
        timer.cancel()
        // Receive the answer from the user
        userAnswer = answer
        // Check to see if these are correct
        if (correctAnswer == userAnswer) {
            // Make the background of the textViewAnswer GREEN with the setBackgroundColor function
            textViewAnswer.setBackgroundColor(Color.GREEN)
            // Increase the value of the userCorrect
            userCorrect++
            // Print the user's correct value on the TextView 'correct'
            binding.tvCorrect.text = userCorrect.toString()
        } else {
            // Make the background of this TextView RED
            textViewAnswer.setBackgroundColor(Color.RED)
            // Increase the value of the userWrong
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
        // Once the user has answered, he should not select any options again until he clicks the 'next' Button
        // After the user selects an option, we must disable the clicking feature of each option
        disableClickableOfOptions()
    }

    // Retrieve the data in this function
    private fun gameLogic() {
        // The marking colors must be restored and the options must be clickable
        restoreOptions()

        // Use the databaseReference object we created above and the ValueEventListener interface
        dataRefQuestions.addValueEventListener(object : ValueEventListener {
            // Perform data retrieving in this method, this method also constantly monitors the database live
            // When there's a change to the database, it instantly reflects to the application
            override fun onDataChange(snapshot: DataSnapshot) {
                // Learn the total number of questions, using the snapshot object
                questionCount = snapshot.childrenCount.toInt()

                // Create an if condition here, the quiz should continue until the value of
                // this questionNumber variable equals the number of questions in the database,
                // that is the value of the questionCount variable, otherwise the quiz will end
                if (questionNumber < questions.size) {
                    // Reach the elements of the HashSet array using the questionNumber variable
                    val element = questions.elementAt(questionNumber)

                    // Retrieve all the data under the 1st question
                    question = snapshot.child("$element").child("q").value.toString()
                    answerA = snapshot.child("$element").child("a").value.toString()
                    answerB = snapshot.child("$element").child("b").value.toString()
                    answerC = snapshot.child("$element").child("c").value.toString()
                    answerD = snapshot.child("$element").child("d").value.toString()
                    correctAnswer = snapshot.child("$element").child("answer").value.toString()

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
            }
        }.start()
    }

    private fun updateCountDownText() {
        // This will give us the value of the remainingTime in seconds, convert this value to an Integer
        val remainingTime: Int = (leftTime / 1000).toInt()
        // Write this remainingTime Integer value on the 'time' text, every second the text will update
        binding.tvTime.text = remainingTime.toString()
    }

    private fun resetTimer() {
        // When the timer is reset, pause the timer, and the leftTime should equal the TOTAL_TIME value again
        // This will set the timer for 30 seconds again, also the text should be updated
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