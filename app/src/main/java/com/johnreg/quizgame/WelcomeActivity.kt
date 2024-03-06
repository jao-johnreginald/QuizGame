package com.johnreg.quizgame

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.johnreg.quizgame.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Animation
        val alphaAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.splash_anim)
        binding.tvSplash.startAnimation(alphaAnimation)

        // After applying this animation, the WelcomeActivity should be automatically closed
        // And the MainActivity should be opened, we do this by creating a Handler
        val handler = Handler(Looper.getMainLooper())
        // This line of code will hold the process to be 5000 milliseconds, 5 seconds
        // And then execute the codes in the run function
        handler.postDelayed({
            val intent = Intent(this@WelcomeActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 5000)
    }
}