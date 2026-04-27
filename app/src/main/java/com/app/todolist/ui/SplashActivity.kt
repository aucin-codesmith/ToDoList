package com.app.todolist.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.app.todolist.R
import com.app.todolist.MainActivity   // adjust if MainActivity is in a sub-package
import com.app.todolist.ui.auth.LoginActivity
import kotlinx.coroutines.*

class SplashActivity : AppCompatActivity() {

    private val splashDuration = 2500L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val ivLogo    = findViewById<ImageView>(R.id.ivLogo)
        val tvName    = findViewById<TextView>(R.id.tvAppName)
        val tvTagline = findViewById<TextView>(R.id.tvTagline)

        ivLogo.animate()
            .alpha(1f)
            .translationY(-10f)
            .setDuration(700)
            .start()

        tvName.animate()
            .alpha(1f)
            .setStartDelay(300)
            .setDuration(700)
            .start()

        tvTagline.animate()
            .alpha(1f)
            .setStartDelay(600)
            .setDuration(700)
            .start()

        CoroutineScope(Dispatchers.Main).launch {
            delay(splashDuration)
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}