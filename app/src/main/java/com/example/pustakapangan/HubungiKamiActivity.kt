package com.example.pustakapangan

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class HubungiKamiActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hubungi_kami)

        val etNama = findViewById<EditText>(R.id.etNamaLengkap)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etWa = findViewById<EditText>(R.id.etWa)
        val etPesan = findViewById<EditText>(R.id.etPesan)
        val btnKirim = findViewById<MaterialButton>(R.id.btnKirimPesan)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hubungi_kami)

        // 1. Kenalkan tombol Back dari XML
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        // 2. Beri perintah ketika tombol diklik
        btnBack.setOnClickListener {
            // Menutup halaman ini dan otomatis kembali ke halaman sebelumnya (Akun)
            finish()
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val isAllFilled = etNama.text.isNotEmpty() &&
                        etEmail.text.isNotEmpty() &&
                        etWa.text.isNotEmpty() &&
                        etPesan.text.isNotEmpty()

                if (isAllFilled) {
                    // Tombol Aktif & Berubah Hijau
                    btnKirim.isEnabled = true
                    btnKirim.setTextColor(Color.parseColor("#00A859"))
                    btnKirim.strokeColor = ColorStateList.valueOf(Color.parseColor("#00A859"))
                } else {
                    // Tombol Mati & Kembali Abu-abu
                    btnKirim.isEnabled = false
                    btnKirim.setTextColor(Color.parseColor("#6B7280"))
                    btnKirim.strokeColor = ColorStateList.valueOf(Color.parseColor("#9CA3AF"))
                }
            }
        }

        etNama.addTextChangedListener(textWatcher)
        etEmail.addTextChangedListener(textWatcher)
        etWa.addTextChangedListener(textWatcher)
        etPesan.addTextChangedListener(textWatcher)
    }
}