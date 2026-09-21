package com.example.pustakapangan

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        val etNamaDepan = findViewById<EditText>(R.id.etNamaDepan)
        val etNamaBelakang = findViewById<EditText>(R.id.etNamaBelakang)
        val etEmailReg = findViewById<EditText>(R.id.etEmailReg)
        val etNomorHp = findViewById<EditText>(R.id.etNomorHp)
        val etAlamat = findViewById<EditText>(R.id.etAlamat)
        val etKota = findViewById<EditText>(R.id.etKota)
        val dropdownNegara = findViewById<AutoCompleteTextView>(R.id.dropdownNegara)
        val etPasswordReg = findViewById<TextInputEditText>(R.id.etPasswordReg)
        val etKonfirmasiPassword = findViewById<TextInputEditText>(R.id.etKonfirmasiPassword)
        val btnDaftarAkun = findViewById<MaterialButton>(R.id.btnDaftarAkun)

        val daftarNegara = listOf("Indonesia", "Malaysia", "Singapura", "Brunei Darussalam", "Timor Leste")
        dropdownNegara.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, daftarNegara))

        fun periksaForm() {
            val semuaTerisi = listOf(
                etNamaDepan, etNamaBelakang, etEmailReg, etNomorHp, etAlamat, etKota
            ).all { it.text.isNotEmpty() } && dropdownNegara.text.isNotEmpty() &&
                    etPasswordReg.text?.isNotEmpty() == true && etKonfirmasiPassword.text?.isNotEmpty() == true

            val passwordCocok = etPasswordReg.text.toString() == etKonfirmasiPassword.text.toString()
            val valid = semuaTerisi && passwordCocok

            btnDaftarAkun.isEnabled = valid
            btnDaftarAkun.backgroundTintList = ColorStateList.valueOf(
                Color.parseColor(if (valid) "#00A859" else "#9CA3AF")
            )
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) = periksaForm()
        }
        listOf(etNamaDepan, etNamaBelakang, etEmailReg, etNomorHp, etAlamat, etKota, dropdownNegara,
            etPasswordReg, etKonfirmasiPassword).forEach { it.addTextChangedListener(textWatcher) }

        // Tombol Daftar Akun
        btnDaftarAkun.setOnClickListener {
            CustomerRepository.daftarkanCustomerBaru(
                Customer(
                    id = 1,
                    namaDepan = etNamaDepan.text.toString(),
                    namaBelakang = etNamaBelakang.text.toString(),
                    email = etEmailReg.text.toString(),
                    saldo = 0
                )
            )
            SessionManager.setLoggedIn(this, true)
            Toast.makeText(this, "Akun berhasil dibuat!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        findViewById<MaterialCardView>(R.id.btnGoogleReg).setOnClickListener {
            CustomerRepository.daftarkanCustomerBaru(
                Customer(id = 1, namaDepan = "Pengguna", namaBelakang = "Google", email = "user@gmail.com", saldo = 0, provider = "google")
            )
            SessionManager.setLoggedIn(this, true)
            Toast.makeText(this, "Berhasil daftar dengan Google!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        // Sudah punya akun? Kembali ke Sign In
        findViewById<TextView>(R.id.tvMasuk).setOnClickListener { finish() }
    }
}