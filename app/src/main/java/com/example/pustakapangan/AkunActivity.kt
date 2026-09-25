package com.example.pustakapangan

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import com.google.android.material.card.MaterialCardView

class AkunActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!SessionManager.isLoggedIn(this)) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_akun)

        val user = CustomerRepository.getUserAktif(this)
        if (user != null) {
            findViewById<TextView>(R.id.tvNamaUser).text = "${user.namaDepan} ${user.namaBelakang}".trim()
            findViewById<TextView>(R.id.tvInisialAvatar).text = user.inisial()
            tampilkanSaldo(user.saldo)
        }

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
            val intent = Intent(this, TopUpActivity::class.java)
            startActivity(intent)
        }

        val menuLogout = findViewById<MaterialCardView>(R.id.menuLogout)
        menuLogout.setOnClickListener {
            SessionManager.logout(this)
            CustomerRepository.logout(this)
            Toast.makeText(this, "Berhasil Logout", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, SignInActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
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

        // Tombol Notifikasi
        val btnNotifikasi = findViewById<ImageView>(R.id.btnNotifikasi)
        btnNotifikasi.setOnClickListener {
            val intent = Intent(this, NotifikasiActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        if (SessionManager.isLoggedIn(this)) {
            NotifikasiRepository.perbaruiBadge(this, findViewById(R.id.dotNotifBell))
            lifecycleScope.launch { try { tampilkanSaldo(CustomerRepository.refreshSaldo(this@AkunActivity)) } catch (e: CancellationException) { throw e } catch (e: Exception) { } }
        }
    }

    private fun tampilkanSaldo(saldo: Int) {
        findViewById<TextView>(R.id.tvSaldo).text = "Rp ${java.text.NumberFormat.getNumberInstance(java.util.Locale("in", "ID")).format(saldo)}"
    }
}