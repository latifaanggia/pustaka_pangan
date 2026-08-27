package com.example.pustakapangan

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class SignInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        val etEmailSignIn = findViewById<EditText>(R.id.etEmailSignIn)
        val etPasswordSignIn = findViewById<EditText>(R.id.etPasswordSignIn)
        val btnMasuk = findViewById<MaterialButton>(R.id.btnMasuk)

        fun periksaForm() {
            val email = etEmailSignIn.text.toString().trim()
            val password = etPasswordSignIn.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                btnMasuk.isEnabled = true
                btnMasuk.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#00A859")) // Hijau
            } else {
                btnMasuk.isEnabled = false
                btnMasuk.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#9CA3AF")) // Abu-abu
            }
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { periksaForm() }
        }

        etEmailSignIn.addTextChangedListener(textWatcher)
        etPasswordSignIn.addTextChangedListener(textWatcher)

        // Tombol Masuk
        btnMasuk.setOnClickListener {
            // Nanti di sini ditambahkan logika authentikasi sungguhan (ke server/Firebase)
            Toast.makeText(this, "Berhasil Masuk!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        val tvDaftar = findViewById<TextView>(R.id.tvDaftar)
        tvDaftar.setOnClickListener {
            Toast.makeText(this, "Menuju halaman pendaftaran...", Toast.LENGTH_SHORT).show()
        }

        // Aksi Google Login
        val btnGoogle = findViewById<MaterialCardView>(R.id.btnGoogle)
        btnGoogle.setOnClickListener {
            Toast.makeText(this, "Membuka Google Login...", Toast.LENGTH_SHORT).show()
        }
    }
}