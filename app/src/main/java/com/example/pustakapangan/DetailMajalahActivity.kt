package com.example.pustakapangan

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
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

        btnPanahKanan.setOnClickListener {
            if (halamanSaatIni < totalHalaman) {
                halamanSaatIni++

                tvHalamanPratinjau.text = "$halamanSaatIni / $totalHalaman"
                imgPratinjau.setImageResource(daftarGambar[halamanSaatIni - 1])
            }
        }

        btnPanahKiri.setOnClickListener {
            if (halamanSaatIni > 1) {
                halamanSaatIni--

                tvHalamanPratinjau.text = "$halamanSaatIni / $totalHalaman"
                imgPratinjau.setImageResource(daftarGambar[halamanSaatIni - 1])
            }
        }
    }

    private fun setupTombolAksi() {
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnAction = findViewById<MaterialButton>(R.id.btnAction)

        // Tombol kembali
        btnBack.setOnClickListener {
            finish()
        }

        // Tombol Beli
        btnAction.setOnClickListener { tampilkanKonfirmasiPembelian() }
    }

    // =========================
    // BOTTOM SHEET: KONFIRMASI PEMBELIAN
    // =========================
    private fun tampilkanKonfirmasiPembelian() {
        val sheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_konfirmasi_pembelian, null)
        sheet.setContentView(view)

        view.findViewById<ImageView>(R.id.btnCloseSheet).setOnClickListener { sheet.dismiss() }

        view.findViewById<MaterialButton>(R.id.btnKonfirmasiBeli).setOnClickListener {
            sheet.dismiss()
            tampilkanPembelianBerhasil()
        }

        sheet.show()
    }

    // DIALOG: PEMBELIAN BERHASIL
    private fun tampilkanPembelianBerhasil() {
        val dialog = Dialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_pembelian_berhasil, null)
        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        view.findViewById<MaterialButton>(R.id.btnMulaiMembaca).setOnClickListener {
            dialog.dismiss()
            bukaEReader()
            finish()
        }
        view.findViewById<MaterialButton>(R.id.btnTutupSukses).setOnClickListener {
            dialog.dismiss()
            aturStatusPembelian(true) //
        }

        dialog.show()
    }

    private fun bukaEReader() {
        startActivity(Intent(this, EReaderActivity::class.java).apply {
            putExtra("JUDUL_MAJALAH", "FRI VOL XXI/07 2026")
            putExtra("NAMA_FILE_PDF", "2026_vol_07.pdf")
        })
    }

    private fun aturStatusPembelian(sudahDibeli: Boolean) {
        val btnAction = findViewById<MaterialButton>(R.id.btnAction)

        if (sudahDibeli) {
            // Ubah jadi State: SUDAH BAYAR
            btnAction.text = "Baca Sekarang"
            btnAction.backgroundTintList = getColorStateList(android.R.color.holo_orange_dark) // Atau pakai Color.parseColor("#FF8C00")
            btnAction.setOnClickListener { bukaEReader() } // ⬅️ FIX: dulu cuma Toast dummy, sekarang beneran buka E-Reader
        } else {
            // State default: BELUM BAYAR (Tetap Hijau)
            btnAction.text = "Beli Sekarang"
        }
    }
}