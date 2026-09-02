package com.example.pustakapangan

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class KoleksiActivity : AppCompatActivity() {

    // Ingatan untuk status unduhan (Mewakili Vol 06 yang masih putih)
    private var isOfflineVol06 = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_koleksi)

        // =========================
        // NAVIGASI
        // =========================
        findViewById<RelativeLayout>(R.id.navBeranda).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        findViewById<RelativeLayout>(R.id.navAkun).setOnClickListener {
            startActivity(Intent(this, AkunActivity::class.java))
            finish()
        }

        findViewById<RelativeLayout>(R.id.navMajalah).setOnClickListener {
            startActivity(Intent(this, MajalahActivity::class.java))
            finish()
        }

        findViewById<ImageView>(R.id.btnNotifikasi).setOnClickListener {
            startActivity(Intent(this, NotifikasiActivity::class.java))
        }

        // =========================
        // MAJALAH VOL 06 (Kondisi Awal: Online / Putih)
        // =========================
        val btnBacaVol06 = findViewById<MaterialCardView>(R.id.btnBacaVol06)

        btnBacaVol06.setOnClickListener {
            bukaEReader("FRI VOL XXI/06 2026", "2026_vol_06.pdf", isOfflineVol06)
        }

        val btnDownloadVol06 = findViewById<MaterialCardView>(R.id.btnDownloadVol06)
        val tvBacaVol06 = findViewById<TextView>(R.id.tvBacaVol06)
        val iconDownloadVol06 = findViewById<ImageView>(R.id.iconDownloadVol06)

        btnDownloadVol06.setOnClickListener {
            isOfflineVol06 = true

            btnBacaVol06.setCardBackgroundColor(Color.parseColor("#EAF6EC"))
            btnBacaVol06.strokeColor = Color.parseColor("#00A859")
            tvBacaVol06.setTextColor(Color.parseColor("#00A859"))

            iconDownloadVol06.setImageResource(R.drawable.ic_check_green)
            iconDownloadVol06.setColorFilter(Color.parseColor("#00A859"))

            Toast.makeText(this, "Majalah berhasil diunduh!", Toast.LENGTH_SHORT).show()
        }

        // =========================
        // MAJALAH VOL 05 (Kondisi Awal: Offline / Hijau)
        // =========================
        val btnBacaVol05 = findViewById<MaterialCardView>(R.id.btnBacaVol05)
        btnBacaVol05.setOnClickListener {
            bukaEReader("FRI VOL XXI/05 2026", "2026_vol_05.pdf", true)
        }
    }

    // =========================
    // FUNGSI BUKA E-READER
    // =========================
    private fun bukaEReader(judulMajalah: String, namaFilePdf: String, isOffline: Boolean) {
        val intent = Intent(this, EReaderActivity::class.java)
        intent.putExtra("JUDUL_MAJALAH", judulMajalah)
        intent.putExtra("NAMA_FILE_PDF", namaFilePdf)
        intent.putExtra("IS_OFFLINE", isOffline) // ⬅️ Ini kunci pelemparan datanya
        startActivity(intent)
    }
}