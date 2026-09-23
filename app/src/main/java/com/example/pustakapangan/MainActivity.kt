package com.example.pustakapangan

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch {
            try {
                MajalahRepository.muatDariSupabase()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Gagal memuat data: cek koneksi internet", Toast.LENGTH_LONG).show()
            }
            startActivity(Intent(this@MainActivity, HomeActivity::class.java))
            finish()
        }
    }
}
