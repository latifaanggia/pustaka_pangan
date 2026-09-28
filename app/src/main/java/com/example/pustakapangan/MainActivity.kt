package com.example.pustakapangan

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        muatData()
    }

    private fun muatData() {
        lifecycleScope.launch {
            try { MajalahRepository.muat(this@MainActivity) } catch (e: CancellationException) { throw e } catch (e: Exception) { }

            // Tanpa data sama sekali (belum pernah online) -> jangan lanjut ke Home
            if (MajalahRepository.getSemuaMajalah().isEmpty()) {
                AlertDialog.Builder(this@MainActivity).setTitle("Tidak Ada Koneksi")
                    .setMessage("Gagal memuat data majalah. Periksa koneksi internet kamu lalu coba lagi.")
                    .setCancelable(false).setPositiveButton("Coba Lagi") {
                        _, _ -> muatData() }.setNegativeButton("Keluar") {
                        _, _ -> finish()
                    }.show()
                return@launch
            }
            if (MajalahRepository.dariCache)
                Toast.makeText(
                    this@MainActivity,
                    "Mode offline: menampilkan data tersimpan",
                    Toast.LENGTH_LONG).show()
            startActivity(Intent(this@MainActivity, HomeActivity::class.java))
            finish()
        }
    }
}
