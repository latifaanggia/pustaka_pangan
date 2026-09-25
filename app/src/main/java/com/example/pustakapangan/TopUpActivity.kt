package com.example.pustakapangan

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class TopUpActivity : AppCompatActivity() {
    private var metodeTerpilih = "BCA"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top_up)

        tampilkanSaldo(CustomerRepository.getUserAktif(this)?.saldo ?: 0)

        // Tombol Kembali
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        // Logika metode pembayaran
        val btnBca = findViewById<MaterialCardView>(R.id.btnBca)
        val tvBca = findViewById<TextView>(R.id.tvBca)
        val btnQris = findViewById<MaterialCardView>(R.id.btnQris)
        val tvQris = findViewById<TextView>(R.id.tvQris)

        fun ubahMetodeAktif(aktifCard: MaterialCardView, aktifText: TextView, pasifCard: MaterialCardView, pasifText: TextView) {

            aktifCard.strokeColor = Color.parseColor("#00A859")
            aktifCard.setCardBackgroundColor(Color.parseColor("#EAF6EC"))
            aktifText.setTextColor(Color.parseColor("#00A859"))

            pasifCard.strokeColor = Color.parseColor("#E5E7EB")
            pasifCard.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            pasifText.setTextColor(Color.parseColor("#1E1E1E"))
        }

        btnBca.setOnClickListener {
            ubahMetodeAktif(btnBca, tvBca, btnQris, tvQris)
            metodeTerpilih = "BCA"
        }

        btnQris.setOnClickListener {
            ubahMetodeAktif(btnQris, tvQris, btnBca, tvBca)
            metodeTerpilih = "QRIS"
        }

        // Nominal
        val btnNominal20 = findViewById<MaterialCardView>(R.id.btnNominal20)
        val tvNominal20 = findViewById<TextView>(R.id.tvNominal20)
        val btnNominal40 = findViewById<MaterialCardView>(R.id.btnNominal40)
        val tvNominal40 = findViewById<TextView>(R.id.tvNominal40)
        val btnNominal100 = findViewById<MaterialCardView>(R.id.btnNominal100)
        val tvNominal100 = findViewById<TextView>(R.id.tvNominal100)
        val btnNominal200 = findViewById<MaterialCardView>(R.id.btnNominal200)
        val tvNominal200 = findViewById<TextView>(R.id.tvNominal200)
        val btnNominalLainnya = findViewById<MaterialCardView>(R.id.btnNominalLainnya)
        val tvNominalLainnya = findViewById<TextView>(R.id.tvNominalLainnya)

        val layoutInputManual = findViewById<LinearLayout>(R.id.layoutInputManual)

        val daftarBtn = listOf(btnNominal20, btnNominal40, btnNominal100, btnNominal200, btnNominalLainnya)
        val daftarText = listOf(tvNominal20, tvNominal40, tvNominal100, tvNominal200, tvNominalLainnya)
        val daftarNominalRupiah = listOf("Rp 20.000", "Rp 40.000", "Rp 100.000", "Rp 200.000", "")
        var nominalTerpilih = daftarNominalRupiah[0]

        fun pilihNominal(indexTerpilih: Int) {
            for (i in daftarBtn.indices) {
                if (i == indexTerpilih) {
                    daftarBtn[i].strokeColor = Color.parseColor("#00A859")
                    daftarBtn[i].setCardBackgroundColor(Color.parseColor("#EAF6EC"))
                    daftarText[i].setTextColor(Color.parseColor("#00A859"))
                } else {
                    daftarBtn[i].strokeColor = Color.parseColor("#E5E7EB")
                    daftarBtn[i].setCardBackgroundColor(Color.parseColor("#FFFFFF"))
                    daftarText[i].setTextColor(Color.parseColor("#1E1E1E"))
                }
            }

            if (indexTerpilih == 4) {
                layoutInputManual.visibility = View.VISIBLE
            } else {
                layoutInputManual.visibility = View.GONE
            }
            nominalTerpilih = daftarNominalRupiah[indexTerpilih]
        }

        pilihNominal(0)
        btnNominal20.setOnClickListener { pilihNominal(0) }
        btnNominal40.setOnClickListener { pilihNominal(1) }
        btnNominal100.setOnClickListener { pilihNominal(2) }
        btnNominal200.setOnClickListener { pilihNominal(3) }
        btnNominalLainnya.setOnClickListener { pilihNominal(4) }

        // Flow bca dan qris
        val etInputNominal = findViewById<android.widget.EditText>(R.id.etInputNominal)
        val btnProsesTopUp = findViewById<MaterialButton>(R.id.btnProsesTopUp)
        btnProsesTopUp.setOnClickListener {

            val nominalInt = if (nominalTerpilih.isEmpty()) {
                etInputNominal.text.toString().filter { it.isDigit() }.toIntOrNull() ?: 0
            } else {
                nominalTerpilih.filter { it.isDigit() }.toIntOrNull() ?: 0
            }
            val namaMetode = if (metodeTerpilih == "BCA") "BCA Transfer" else "QRIS"

            val user = CustomerRepository.getUserAktif(this)
            if (user == null) {
                android.widget.Toast.makeText(
                    this,
                    "Sesi login habis, silakan masuk lagi.",
                    android.widget.Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            btnProsesTopUp.isEnabled = false
            btnProsesTopUp.text = "Memproses..."
            lifecycleScope.launch {
                try {
                    TopUpRepository.tambahRiwayat(
                        CustomerRepository.getTokenValid(this@TopUpActivity),
                        user.id,
                        nominalInt, namaMetode)
                    android.widget.Toast.makeText(
                        this@TopUpActivity,
                        "Saldo akan bertambah setelah pembayaran dikonfirmasi admin",
                        android.widget.Toast.LENGTH_LONG).show()

                    val tujuan =
                        if (metodeTerpilih == "BCA") KonfirmasiBankActivity::class.java
                        else KonfirmasiQrisActivity::class.java

                    startActivity(Intent(
                        this@TopUpActivity,
                        tujuan).apply { putExtra("NOMINAL_TOPUP",
                        "Rp${"%,d".format(nominalInt).replace(',', '.')}") })

                }
                catch (e: Exception) {
                    android.widget.Toast.makeText(
                        this@TopUpActivity,
                        "Gagal top up: ${e.message}",
                        android.widget.Toast.LENGTH_LONG).show()

                    btnProsesTopUp.isEnabled = true
                    btnProsesTopUp.text = "Top Up Sekarang"
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            try { tampilkanSaldo(CustomerRepository.refreshSaldo(this@TopUpActivity)) }
            catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            }
            catch (e: Exception) { } }
    }

    private fun tampilkanSaldo(saldo: Int) {
        findViewById<TextView>(R.id.tvSaldoTopUp).text =
            "Rp${"%,d".format(saldo).replace(',', '.')}" }
}
