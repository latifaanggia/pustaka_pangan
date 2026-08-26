package com.example.pustakapangan // JANGAN DIHAPUS: Sesuaikan dengan namamu

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class AkunActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_akun)

        // Menu Ubah Password
        val menuUbahPassword = findViewById<MaterialCardView>(R.id.menuUbahPassword)
        menuUbahPassword.setOnClickListener {
            val intent = Intent(this, UbahPasswordActivity::class.java)
            startActivity(intent)
        }

        // Menu Cara Membeli
        val menuCaraMembeli = findViewById<MaterialCardView>(R.id.menuCaraMembeli)
        menuCaraMembeli.setOnClickListener {
            val intent = Intent(this, CaraMembeliActivity::class.java)
            startActivity(intent)
        }

        // Menu Riwayat Top Up
        val menuRiwayatTopup = findViewById<MaterialCardView>(R.id.menuRiwayatTopup)
        menuRiwayatTopup.setOnClickListener {
            val intent = Intent(this, RiwayatTopUpActivity::class.java)
            startActivity(intent)
        }

        // Menu Hubungi Kami
        val menuHubungiKami = findViewById<MaterialCardView>(R.id.menuHubungiKami)
        menuHubungiKami.setOnClickListener {
            val intent = Intent(this, HubungiKamiActivity::class.java)
            startActivity(intent)
        }

        val btnTopup = findViewById<MaterialCardView>(R.id.btnTopup)
        btnTopup.setOnClickListener {
            Toast.makeText(this, "Halaman Top Up belum dibuat", Toast.LENGTH_SHORT).show()
        }

        val menuLogout = findViewById<MaterialCardView>(R.id.menuLogout)
        menuLogout.setOnClickListener {
            Toast.makeText(this, "Berhasil Logout", Toast.LENGTH_SHORT).show()
        }

        // Menu Beranda
        val navBeranda = findViewById<RelativeLayout>(R.id.navBeranda)
        navBeranda.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Menu Koleksi
        val navKoleksi = findViewById<RelativeLayout>(R.id.navKoleksi)
        navKoleksi.setOnClickListener {
            val intent = Intent(this, KoleksiActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Menu Majalah
        val navMajalah = findViewById<RelativeLayout>(R.id.navMajalah)
        navMajalah.setOnClickListener {
            startActivity(Intent(this, MajalahActivity::class.java))
            finish()
        }
    }
}