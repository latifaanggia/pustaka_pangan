package com.example.pustakapangan

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class KoleksiActivity : AppCompatActivity() {

    private val prefs by lazy { getSharedPreferences("koleksi_prefs", MODE_PRIVATE) }
    private val KEY_OFFLINE_VOL06 = "is_offline_vol06"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!SessionManager.isLoggedIn(this)) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_koleksi)

        val majalahVol06 = MajalahRepository.getById(6)!!
        val majalahVol05 = MajalahRepository.getById(5)!!

        // NAVIGASI
        findViewById<RelativeLayout>(R.id.navBeranda).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java)); finish()
        }
        findViewById<RelativeLayout>(R.id.navAkun).setOnClickListener {
            startActivity(Intent(this, AkunActivity::class.java)); finish()
        }
        findViewById<RelativeLayout>(R.id.navMajalah).setOnClickListener {
            startActivity(Intent(this, MajalahActivity::class.java)); finish()
        }
        findViewById<ImageView>(R.id.btnNotifikasi).setOnClickListener {
            startActivity(Intent(this, NotifikasiActivity::class.java))
        }

        // MAJALAH VOL 06 (status dibaca dari SharedPreferences saat halaman dibuka)
        val btnBacaVol06 = findViewById<MaterialCardView>(R.id.btnBacaVol06)
        val btnDownloadVol06 = findViewById<MaterialCardView>(R.id.btnDownloadVol06)
        val tvBacaVol06 = findViewById<TextView>(R.id.tvBacaVol06)
        val iconDownloadVol06 = findViewById<ImageView>(R.id.iconDownloadVol06)

        if (prefs.getBoolean(KEY_OFFLINE_VOL06, false)) {
            tampilkanVol06SudahDiunduh(btnBacaVol06, tvBacaVol06, iconDownloadVol06)
        }

        btnBacaVol06.setOnClickListener {
            bukaEReader(majalahVol06.judul, majalahVol06.namaFilePdf, prefs.getBoolean(KEY_OFFLINE_VOL06, false))
        }

        btnDownloadVol06.setOnClickListener {
            prefs.edit().putBoolean(KEY_OFFLINE_VOL06, true).apply()
            tampilkanVol06SudahDiunduh(btnBacaVol06, tvBacaVol06, iconDownloadVol06)
            Toast.makeText(this, "Majalah berhasil diunduh!", Toast.LENGTH_SHORT).show()
        }

        // MAJALAH VOL 05 (Kondisi Awal: Offline / Hijau)
        findViewById<MaterialCardView>(R.id.btnBacaVol05).setOnClickListener {
            bukaEReader(majalahVol05.judul, majalahVol05.namaFilePdf, true)
        }
    }

    override fun onResume() {
        super.onResume()
        if (SessionManager.isLoggedIn(this)) {
            findViewById<View>(R.id.dotNotifBell).visibility = if (NotifikasiState.adaNotifBelumDibaca(this)) View.VISIBLE else View.GONE
        }
    }

    private fun tampilkanVol06SudahDiunduh(card: MaterialCardView, tvBaca: TextView, iconDownload: ImageView) {
        card.setCardBackgroundColor(Color.parseColor("#EAF6EC"))
        card.strokeColor = Color.parseColor("#00A859")
        tvBaca.setTextColor(Color.parseColor("#00A859"))
        iconDownload.setImageResource(R.drawable.ic_check_green)
        iconDownload.setColorFilter(Color.parseColor("#00A859"))
    }

    private fun bukaEReader(judulMajalah: String, namaFilePdf: String, isOffline: Boolean) {
        val intent = Intent(this, EReaderActivity::class.java)
        intent.putExtra("JUDUL_MAJALAH", judulMajalah)
        intent.putExtra("NAMA_FILE_PDF", namaFilePdf)
        intent.putExtra("IS_OFFLINE", isOffline) // ⬅️ Ini kunci pelemparan datanya
        startActivity(intent)
    }
}