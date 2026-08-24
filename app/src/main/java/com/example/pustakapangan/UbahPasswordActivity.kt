package com.example.pustakapangan

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class UbahPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ubah_password)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        val etPassLama = findViewById<TextInputEditText>(R.id.etPasswordLama)
        val etPassBaru = findViewById<TextInputEditText>(R.id.etPasswordBaru)
        val etPassKonfirm = findViewById<TextInputEditText>(R.id.etKonfirmasiPassword)
        val btnSimpan = findViewById<MaterialButton>(R.id.btnSimpanPassword)

        // Perubahan Warna Tombol
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val isAllFilled = etPassLama.text.toString().isNotEmpty() &&
                        etPassBaru.text.toString().isNotEmpty() &&
                        etPassKonfirm.text.toString().isNotEmpty()

                if (isAllFilled) {
                    // Tombol aktif & Hijau
                    btnSimpan.isEnabled = true
                    btnSimpan.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#00A859"))
                } else {
                    // Tombol mati & Abu-abu
                    btnSimpan.isEnabled = false
                    btnSimpan.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#9CA3AF"))
                }
            }
        }

        etPassLama.addTextChangedListener(textWatcher)
        etPassBaru.addTextChangedListener(textWatcher)
        etPassKonfirm.addTextChangedListener(textWatcher)
    }
}