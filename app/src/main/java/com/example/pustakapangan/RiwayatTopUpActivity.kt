package com.example.pustakapangan
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class RiwayatTopUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat_topup)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btnKonfirmasiWa1).setOnClickListener {
            val template = """
                Halo Admin, saya sudah melakukan top up namun belum ada konfirmasi:

                No. Invoice: INV-20260807-002
                Metode: BCA Transfer
                Nominal: Rp 20.000

                Mohon dicek dan diproses kembali.
                *Silakan lampirkan foto bukti transfer di chat ini*
            """.trimIndent()

            try {
                val encodedPesan = java.net.URLEncoder.encode(template, "UTF-8")
                val nomorAdmin = "628111190039"
                val url = "https://api.whatsapp.com/send?phone=$nomorAdmin&text=$encodedPesan"
                startActivity(Intent(Intent.ACTION_VIEW).apply { data = android.net.Uri.parse(url) })
            } catch (e: Exception) {
                Toast.makeText(this, "Aplikasi WhatsApp tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }
    }
}