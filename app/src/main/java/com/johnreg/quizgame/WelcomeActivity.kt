package com.johnreg.quizgame

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.johnreg.quizgame.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setAnimation()
        setHandler()
    }

    private fun setAnimation() {
        val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.splash_anim)
        binding.ivSplash.startAnimation(animation)
    }

    // After the animation, WelcomeActivity should close and MainActivity should open
    // Create a Handler, process takes DELAY_MILLIS long, then execute the codes
    private fun setHandler() {
        Handler(Looper.getMainLooper()).postDelayed({
            // If user is logged out -> start LoginActivity, else -> start MainActivity
            if (auth.currentUser == null) {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(applicationContext, "Welcome to Quiz Game", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, DELAY_MILLIS)
    }

    companion object {
        private const val DELAY_MILLIS = 3000L
    }

}