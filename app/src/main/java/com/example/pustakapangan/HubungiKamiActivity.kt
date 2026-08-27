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

            val btnWa = findViewById<MaterialButton>(R.id.btnWa)
            btnWa.setOnClickListener {
                try {
                    val nomorAdmin = "628111190039"
                    val pesan = "Halo Admin FoodReview, saya butuh bantuan."
                    val encodedPesan = java.net.URLEncoder.encode(pesan, "UTF-8")
                    val url = "https://api.whatsapp.com/send?phone=$nomorAdmin&text=$encodedPesan"

                    val intentWa = Intent(Intent.ACTION_VIEW)
                    intentWa.data = android.net.Uri.parse(url)
                    startActivity(intentWa)
                } catch (e: Exception) {
                    Toast.makeText(this, "Aplikasi WhatsApp tidak ditemukan", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            val etNama = findViewById<EditText>(R.id.etNamaLengkap)
            val etEmail = findViewById<EditText>(R.id.etEmail)
            val etWaForm =
                findViewById<EditText>(R.id.etWa) // Diubah sedikit namanya biar tidak bentrok
            val etPesan = findViewById<EditText>(R.id.etPesan)
            val btnKirim = findViewById<MaterialButton>(R.id.btnKirimPesan)

            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val isAllFilled = etNama.text.isNotEmpty() &&
                            etEmail.text.isNotEmpty() &&
                            etWaForm.text.isNotEmpty() &&
                            etPesan.text.isNotEmpty()

                    if (isAllFilled) {
                        btnKirim.isEnabled = true
                        btnKirim.setTextColor(Color.parseColor("#00A859"))
                        btnKirim.strokeColor = ColorStateList.valueOf(Color.parseColor("#00A859"))
                    } else {
                        btnKirim.isEnabled = false
                        btnKirim.setTextColor(Color.parseColor("#6B7280"))
                        btnKirim.strokeColor = ColorStateList.valueOf(Color.parseColor("#9CA3AF"))
                    }
                }
            }

            etNama.addTextChangedListener(textWatcher)
            etEmail.addTextChangedListener(textWatcher)
            etWaForm.addTextChangedListener(textWatcher)
            etPesan.addTextChangedListener(textWatcher)

            btnKirim.setOnClickListener {
                Toast.makeText(this, "Pesan Anda berhasil dikirim!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}