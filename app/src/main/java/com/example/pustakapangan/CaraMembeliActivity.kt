package com.example.pustakapangan

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class CaraMembeliActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Menyambungkan file Kotlin ini dengan desain XML yang sudah kita buat
        setContentView(R.layout.activity_cara_membeli)

        // 2. Mengenalkan tombol panah kembali (Back) berdasarkan ID di XML
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        // 3. Memberikan perintah saat tombol diklik
        btnBack.setOnClickListener {
            // Membuang/menutup halaman saat ini agar kembali ke halaman Akun
            finish()
        }
    }
}