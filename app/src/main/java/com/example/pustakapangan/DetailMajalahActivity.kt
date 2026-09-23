package com.example.pustakapangan

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class DetailMajalahActivity : AppCompatActivity() {
    private var halamanSaatIni = 1
    private lateinit var majalah: Majalah
    private var daftarGambarLokal: List<Int> = emptyList()
    private val pakaiPratinjauLokal get() = daftarGambarLokal.isNotEmpty()
    private val totalHalaman get() = if (pakaiPratinjauLokal) daftarGambarLokal.size else 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_majalah)

        if (MajalahRepository.getSemuaMajalah().isEmpty()) {
            android.widget.Toast.makeText(this, "Gagal memuat data majalah. Cek koneksi internet lalu coba lagi.", android.widget.Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        val majalahId = intent.getIntExtra("MAJALAH_ID", 7)
        majalah = MajalahRepository.getById(majalahId) ?: MajalahRepository.getSemuaMajalah().first()

        daftarGambarLokal = if (majalah.id == 7) {
            listOf(
                R.drawable.img_2026_vol_07, R.drawable.img_2026_vol_07_hal2, R.drawable.img_2026_vol_07_hal3,
                R.drawable.img_2026_vol_07_hal4, R.drawable.img_2026_vol_07_hal5, R.drawable.img_2026_vol_07_hal6
            )
        } else {
            emptyList()
        }

        tampilkanDataMajalah()
        setupPratinjauEditorial()
        setupTombolAksi()
    }

    private fun tampilkanDataMajalah() {
        Glide.with(this).load(majalah.urlCover).into(findViewById<ImageView>(R.id.imgCover))
        findViewById<TextView>(R.id.tvJudulMajalah).text = majalah.judul
        findViewById<TextView>(R.id.tvHarga).text = "Rp ${formatRupiah(majalah.harga)}"
        tampilkanHalamanPratinjau(1)
        findViewById<TextView>(R.id.tvHalamanPratinjau).text = "1 / $totalHalaman"
    }

    private fun tampilkanHalamanPratinjau(halaman: Int) {
        val imgPratinjau = findViewById<ImageView>(R.id.imgPratinjau)
        if (pakaiPratinjauLokal) {
            imgPratinjau.setImageResource(daftarGambarLokal[halaman - 1])
        } else {
            Glide.with(this).load(majalah.urlCover).into(imgPratinjau)
        }
    }

    private fun formatRupiah(angka: Int): String =
        java.text.NumberFormat.getNumberInstance(java.util.Locale("in", "ID")).format(angka)

    private fun setupPratinjauEditorial() {
        val tvHalamanPratinjau = findViewById<TextView>(R.id.tvHalamanPratinjau)
        val btnPanahKiri = findViewById<MaterialCardView>(R.id.btnPanahKiri)
        val btnPanahKanan = findViewById<MaterialCardView>(R.id.btnPanahKanan)

        btnPanahKanan.setOnClickListener {
            if (halamanSaatIni < totalHalaman) {
                halamanSaatIni++
                tvHalamanPratinjau.text = "$halamanSaatIni / $totalHalaman"
                tampilkanHalamanPratinjau(halamanSaatIni)
            }
        }

        btnPanahKiri.setOnClickListener {
            if (halamanSaatIni > 1) {
                halamanSaatIni--
                tvHalamanPratinjau.text = "$halamanSaatIni / $totalHalaman"
                tampilkanHalamanPratinjau(halamanSaatIni)
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

        btnAction.setOnClickListener {
            if (SessionManager.isLoggedIn(this)) {
                tampilkanKonfirmasiPembelian()
            } else {
                startActivity(Intent(this, SignInActivity::class.java))
            }
        }
    }

    // BOTTOM SHEET: KONFIRMASI PEMBELIAN
    private fun tampilkanKonfirmasiPembelian() {
        val sheet = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_konfirmasi_pembelian, null)
        sheet.setContentView(view)

        Glide.with(this).load(majalah.urlCover).into(view.findViewById<ImageView>(R.id.imgCoverSheet))
        view.findViewById<TextView>(R.id.tvJudulSheet).text = majalah.judul
        view.findViewById<TextView>(R.id.tvHargaSheet).text = "Rp${formatRupiah(majalah.harga)}"
        view.findViewById<TextView>(R.id.tvHargaPotong).text = "- Rp${formatRupiah(majalah.harga)}"

        val saldoUser = CustomerRepository.getUserAktif(this)?.saldo ?: 0
        val sisaSaldo = saldoUser - majalah.harga
        view.findViewById<TextView>(R.id.tvSaldoSaatIni).text = "Rp${formatRupiah(saldoUser)}"
        view.findViewById<TextView>(R.id.tvSisaSaldo).text = "Rp${formatRupiah(sisaSaldo)}"

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
            aturStatusPembelian(true)
        }

        dialog.show()
    }

    private fun bukaEReader() {
        startActivity(Intent(this, EReaderActivity::class.java).apply {
            putExtra("JUDUL_MAJALAH", majalah.judul)
            putExtra("NAMA_FILE_PDF", majalah.namaFilePdf)
        })
    }

    private fun aturStatusPembelian(sudahDibeli: Boolean) {
        val btnAction = findViewById<MaterialButton>(R.id.btnAction)

        if (sudahDibeli) {
            // Ubah jadi State: SUDAH BAYAR
            btnAction.text = "Baca Sekarang"
            btnAction.backgroundTintList = getColorStateList(android.R.color.holo_orange_dark)
            btnAction.setOnClickListener { bukaEReader() }
        } else {
            // State default: BELUM BAYAR (Tetap Hijau)
            btnAction.text = "Beli Sekarang"
        }
    }
}