package com.johnreg.quizgame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.johnreg.quizgame.databinding.ActivityQuizBinding

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding

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