package com.example.pustakapangan

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val splashHandler = Handler(Looper.getMainLooper())
    private val splashRunnable = Runnable {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        splashHandler.postDelayed(splashRunnable, 3000)
    }

    override fun onDestroy() {
        super.onDestroy()
        splashHandler.removeCallbacks(splashRunnable)
    }
}