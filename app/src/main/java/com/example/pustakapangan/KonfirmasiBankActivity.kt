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

class KonfirmasiBankActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_konfirmasi_bank)

        // Tombol Kembali
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        val etNamaLengkapBank = findViewById<EditText>(R.id.etNamaLengkap)
        val etEmailBank = findViewById<EditText>(R.id.etEmail)
        val btnKonfirmasiWaBank = findViewById<MaterialButton>(R.id.btnKonfirmasiWaBank)

        fun periksaForm() {
            val nama = etNamaLengkapBank.text.toString().trim()
            val email = etEmailBank.text.toString().trim()

            if (nama.isNotEmpty() && email.isNotEmpty()) {
                btnKonfirmasiWaBank.isEnabled = true
                btnKonfirmasiWaBank.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#00A859")) // Jadi Hijau
            } else {
                btnKonfirmasiWaBank.isEnabled = false
                btnKonfirmasiWaBank.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#9CA3AF")) // Balik Abu-abu
            }
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                periksaForm()
            }
        }

        etNamaLengkapBank.addTextChangedListener(textWatcher)
        etEmailBank.addTextChangedListener(textWatcher)

        // Ke wa
        val nominalDariTopUp = intent.getStringExtra("NOMINAL_TOPUP") ?: "Rp 0"

        btnKonfirmasiWaBank.setOnClickListener {
            val nama = etNamaLengkapBank.text.toString().trim()
            val email = etEmailBank.text.toString().trim()

            val nominal = nominalDariTopUp
            val metode = "BCA Transfer"

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