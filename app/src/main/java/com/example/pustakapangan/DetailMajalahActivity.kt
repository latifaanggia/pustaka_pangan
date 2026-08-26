package com.example.pustakapangan

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class DetailMajalahActivity : AppCompatActivity() {
    private var halamanSaatIni = 1
    private val totalHalaman = 6

    private val daftarGambar = listOf(
        R.drawable.img_2026_vol_07,       // Halaman 1 (Cover)
        R.drawable.img_2026_vol_07_hal2,  // Halaman 2
        R.drawable.img_2026_vol_07_hal3,  // Halaman 3
        R.drawable.img_2026_vol_07_hal4,  // Halaman 4
        R.drawable.img_2026_vol_07_hal5,  // Halaman 5
        R.drawable.img_2026_vol_07_hal6   // Halaman 6
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_majalah)

        setupPratinjauEditorial()
        setupTombolAksi()
    }

    private fun setupPratinjauEditorial() {
        val imgPratinjau = findViewById<ImageView>(R.id.imgPratinjau)
        val tvHalamanPratinjau = findViewById<TextView>(R.id.tvHalamanPratinjau)
        val btnPanahKiri = findViewById<MaterialCardView>(R.id.btnPanahKiri)
        val btnPanahKanan = findViewById<MaterialCardView>(R.id.btnPanahKanan)

        // Logika saat panah kanan diklik
        btnPanahKanan.setOnClickListener {
            if (halamanSaatIni < totalHalaman) {
                halamanSaatIni++

                // Update teks dan gambar seketika
                tvHalamanPratinjau.text = "$halamanSaatIni / $totalHalaman"
                imgPratinjau.setImageResource(daftarGambar[halamanSaatIni - 1])
            }
        }

        // Logika saat panah kiri diklik
        btnPanahKiri.setOnClickListener {
            if (halamanSaatIni > 1) {
                halamanSaatIni--

                // Update teks dan gambar seketika
                tvHalamanPratinjau.text = "$halamanSaatIni / $totalHalaman"
                imgPratinjau.setImageResource(daftarGambar[halamanSaatIni - 1])
            }
        }
    }

    private fun setupTombolAksi() {
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnAction = findViewById<MaterialButton>(R.id.btnAction)

        // Tombol kembali (tutup halaman ini)
        btnBack.setOnClickListener {
            finish()
        }

        // Tombol Beli (Hanya efek sementara/dummy)
        btnAction.setOnClickListener {
            Toast.makeText(this, "Membuka halaman pembayaran...", Toast.LENGTH_SHORT).show()
        }
    }

    // Fungsi untuk mengubah tampilan jika user sudah membeli majalah ini
    private fun aturStatusPembelian(sudahDibeli: Boolean) {
        val btnAction = findViewById<MaterialButton>(R.id.btnAction)

        if (sudahDibeli) {
            // Ubah jadi State: SUDAH BAYAR
            btnAction.text = "Baca Sekarang"
            btnAction.backgroundTintList = getColorStateList(android.R.color.holo_orange_dark) // Atau pakai Color.parseColor("#FF8C00")

            btnAction.setOnClickListener {
                Toast.makeText(this, "Membuka halaman E-Reader...", Toast.LENGTH_SHORT).show()
                // Nanti kodenya diarahkan ke halaman baca majalah
            }
        } else {
            // State default: BELUM BAYAR (Tetap Hijau)
            btnAction.text = "Beli Sekarang"
            // Warnanya otomatis mengikuti XML (Hijau)
        }
    }
}