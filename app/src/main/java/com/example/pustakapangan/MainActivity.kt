package com.example.pustakapangan

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home) // Ini file Splash Screen

        // Jembatan (Delay 3 detik, lalu pindah ke Home)
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish() // Menutup Splash Screen supaya tidak bisa balik lagi
        }, 3000) // 3000 = 3 detik
    }
}