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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import com.google.android.material.card.MaterialCardView

class KoleksiActivity : AppCompatActivity() {

    private var jobUnduhVol06: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!SessionManager.isLoggedIn(this)) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_koleksi)

        if (MajalahRepository.getSemuaMajalah().isEmpty()) {
            Toast.makeText(this, "Gagal memuat data majalah. Cek koneksi internet lalu coba lagi.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val majalahVol07 = MajalahRepository.getById(7)!!
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

        // MAJALAH VOL 07
        findViewById<MaterialCardView>(R.id.btnBacaVol07).setOnClickListener {
            bukaEReader(majalahVol07.judul, majalahVol07.namaFilePdf, PdfDownloader.sudahDiunduh(this, majalahVol07.namaFilePdf))
        }

        // MAJALAH VOL 06
        val btnBacaVol06 = findViewById<MaterialCardView>(R.id.btnBacaVol06)
        val btnDownloadVol06 = findViewById<MaterialCardView>(R.id.btnDownloadVol06)
        val tvBacaVol06 = findViewById<TextView>(R.id.tvBacaVol06)
        val iconDownloadVol06 = findViewById<ImageView>(R.id.iconDownloadVol06)
        if (PdfDownloader.sudahDiunduh(this, majalahVol06.namaFilePdf)) tampilkanVol06SudahDiunduh(btnBacaVol06, tvBacaVol06, iconDownloadVol06)

        btnBacaVol06.setOnClickListener {
            if (jobUnduhVol06?.isActive == true) { Toast.makeText(this, "Tunggu unduhan selesai dulu ya", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            bukaEReader(majalahVol06.judul, majalahVol06.namaFilePdf, PdfDownloader.sudahDiunduh(this, majalahVol06.namaFilePdf))
        }

        btnDownloadVol06.setOnClickListener {
            when {
                PdfDownloader.sudahDiunduh(this, majalahVol06.namaFilePdf) -> Toast.makeText(this, "Majalah sudah tersedia offline", Toast.LENGTH_SHORT).show()
                jobUnduhVol06?.isActive == true -> Toast.makeText(this, "Sedang mengunduh...", Toast.LENGTH_SHORT).show()
                else -> lifecycleScope.launch {
                    btnDownloadVol06.alpha = 0.5f; tvBacaVol06.text = "0%"
                    try {
                        PdfDownloader.unduh(majalahVol06.namaFilePdf, PdfDownloader.fileOffline(this@KoleksiActivity, majalahVol06.namaFilePdf)) { tvBacaVol06.text = "$it%" }
                        tampilkanVol06SudahDiunduh(btnBacaVol06, tvBacaVol06, iconDownloadVol06)
                        Toast.makeText(this@KoleksiActivity, "Majalah berhasil diunduh!", Toast.LENGTH_SHORT).show()
                    } catch (e: CancellationException) { throw e
                    } catch (e: Exception) { Toast.makeText(this@KoleksiActivity, e.message ?: "Gagal mengunduh majalah", Toast.LENGTH_LONG).show()
                    } finally { tvBacaVol06.text = "Baca"; btnDownloadVol06.alpha = 1f }
                }.also { jobUnduhVol06 = it }
            }
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
        intent.putExtra("IS_OFFLINE", isOffline)
        startActivity(intent)
    }
}