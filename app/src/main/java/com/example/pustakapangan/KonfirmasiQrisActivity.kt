package com.example.pustakapangan

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class KonfirmasiQrisActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_konfirmasi_qris)

        // Tombol Kembali
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        val etNamaLengkapQris = findViewById<EditText>(R.id.etNamaLengkapQris)
        val etEmailQris = findViewById<EditText>(R.id.etEmailQris)
        val btnKonfirmasiWaQris = findViewById<MaterialButton>(R.id.btnKonfirmasiWaQris)

        // Cek form
        fun periksaForm() {
            val nama = etNamaLengkapQris.text.toString().trim()
            val email = etEmailQris.text.toString().trim()

            if (nama.isNotEmpty() && email.isNotEmpty()) {
                btnKonfirmasiWaQris.isEnabled = true
                btnKonfirmasiWaQris.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#00A859")) // Hijau
            } else {
                btnKonfirmasiWaQris.isEnabled = false
                btnKonfirmasiWaQris.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#9CA3AF")) // Abu-abu
            }
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { periksaForm() }
        }

        etNamaLengkapQris.addTextChangedListener(textWatcher)
        etEmailQris.addTextChangedListener(textWatcher)

        // Ke wa
        val nominalDariTopUp = intent.getStringExtra("NOMINAL_TOPUP") ?: "Rp 0"

        btnKonfirmasiWaQris.setOnClickListener {
            val nama = etNamaLengkapQris.text.toString().trim()
            val email = etEmailQris.text.toString().trim()

            val nominal = nominalDariTopUp
            val metode = "QRIS"

            val pesanTemplate = """
                Halo Admin, saya sudah melakukan top up:

                Nama: $nama
                Email: $email
                Nominal: $nominal
                Metode Pembayaran: $metode

                Mohon diproses.

                *Silakan lampirkan foto bukti transfer di chat ini*
            """.trimIndent()

            try {
                val encodedPesan = java.net.URLEncoder.encode(pesanTemplate, "UTF-8")
                val nomorAdmin = "628111190039"
                val url = "https://api.whatsapp.com/send?phone=$nomorAdmin&text=$encodedPesan"

                val intentWa = android.content.Intent(android.content.Intent.ACTION_VIEW)
                intentWa.data = android.net.Uri.parse(url)
                startActivity(intentWa)

            } catch (e: Exception) {
                Toast.makeText(this, "Gagal membuka WhatsApp", Toast.LENGTH_SHORT).show()
            }
        }
    }
}