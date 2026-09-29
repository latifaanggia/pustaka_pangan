package com.example.pustakapangan

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

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

        // Simpan password: validasi lokal dulu, baru kirim ke Supabase
        btnSimpan.setOnClickListener {
            val lama = etPassLama.text.toString(); val baru = etPassBaru.text.toString(); val konfirm = etPassKonfirm.text.toString()
            val error = when {
                baru.length < 8 -> "Password baru minimal 8 karakter"
                !baru.any { it.isDigit() } || !baru.any { it.isLetter() } -> "Password harus kombinasi angka dan huruf"
                baru != konfirm -> "Konfirmasi password tidak sama"
                baru == lama -> "Password baru harus berbeda dari password lama"
                else -> null
            }
            if (error != null) { Toast.makeText(this, error, Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            btnSimpan.isEnabled = false; btnSimpan.text = "Menyimpan..."
            lifecycleScope.launch {
                try {
                    CustomerRepository.ubahPassword(this@UbahPasswordActivity, lama, baru)
                    Toast.makeText(this@UbahPasswordActivity, "Password berhasil diubah", Toast.LENGTH_SHORT).show(); finish()
                } catch (e: CancellationException) { throw e
                } catch (e: Exception) {
                    val pesan = if (e is java.io.IOException) "Tidak ada koneksi internet" else e.message ?: "Gagal mengubah password"
                    Toast.makeText(this@UbahPasswordActivity, pesan, Toast.LENGTH_LONG).show()
                    btnSimpan.isEnabled = true; btnSimpan.text = "Simpan Perubahan"
                }
            }
        }
    }
}