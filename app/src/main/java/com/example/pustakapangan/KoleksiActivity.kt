package com.example.pustakapangan

import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class KoleksiActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_koleksi)

        // Menu Beranda
        val navBeranda = findViewById<RelativeLayout>(R.id.navBeranda)
        navBeranda.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Menu Majalah
        val navMajalah = findViewById<RelativeLayout>(R.id.navMajalah)
        navMajalah.setOnClickListener {
            Toast.makeText(this, "Halaman Majalah belum dibuat", Toast.LENGTH_SHORT).show()
        }

        // Menu
        val navAkun = findViewById<RelativeLayout>(R.id.navAkun)
        navAkun.setOnClickListener {
            val intent = Intent(this, AkunActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}