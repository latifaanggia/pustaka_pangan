package com.example.pustakapangan

import android.content.Intent
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

class HubungiKamiActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hubungi_kami)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        val btnWa = findViewById<MaterialButton>(R.id.btnWa)
        btnWa.setOnClickListener { bukaWhatsApp("Halo Admin FoodReview, saya butuh bantuan.") }

        val etNama = findViewById<EditText>(R.id.etNamaLengkap)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etWaForm = findViewById<EditText>(R.id.etWa)
        val etPesan = findViewById<EditText>(R.id.etPesan)
        val btnKirim = findViewById<MaterialButton>(R.id.btnKirimPesan)

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val isAllFilled = etNama.text.isNotEmpty() && etEmail.text.isNotEmpty() &&
                        etWaForm.text.isNotEmpty() && etPesan.text.isNotEmpty()
                btnKirim.isEnabled = isAllFilled
                val warna = if (isAllFilled) "#00A859" else "#6B7280"
                val stroke = if (isAllFilled) "#00A859" else "#9CA3AF"
                btnKirim.setTextColor(Color.parseColor(warna))
                btnKirim.strokeColor = ColorStateList.valueOf(Color.parseColor(stroke))
            }
        }
        listOf(etNama, etEmail, etWaForm, etPesan).forEach { it.addTextChangedListener(textWatcher) }

        btnKirim.setOnClickListener {
            val template = """
                Halo Admin, saya ingin bertanya:

                Nama: ${etNama.text}
                Email: ${etEmail.text}
                No. WhatsApp: ${etWaForm.text}
                Pesan: ${etPesan.text}
            """.trimIndent()
            bukaWhatsApp(template)
        }
    }

    private fun bukaWhatsApp(pesan: String) {
        try {
            val nomorAdmin = "628111190039"
            val encodedPesan = java.net.URLEncoder.encode(pesan, "UTF-8")
            val url = "https://api.whatsapp.com/send?phone=$nomorAdmin&text=$encodedPesan"
            startActivity(Intent(Intent.ACTION_VIEW).apply { data = android.net.Uri.parse(url) })
        } catch (e: Exception) {
            Toast.makeText(this, "Aplikasi WhatsApp tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }
}