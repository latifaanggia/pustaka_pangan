package com.example.pustakapangan

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class TopUpActivity : AppCompatActivity() {
    private var metodeTerpilih = "BCA"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_top_up)

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
            metodeTerpilih = "BCA" // Catat di ingatan kalau user pilih BCA
        }

        btnQris.setOnClickListener {
            ubahMetodeAktif(btnQris, tvQris, btnBca, tvBca)
            metodeTerpilih = "QRIS" // Catat di ingatan kalau user pilih QRIS
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
        }

        btnNominal20.setOnClickListener { pilihNominal(0) }
        btnNominal40.setOnClickListener { pilihNominal(1) }
        btnNominal100.setOnClickListener { pilihNominal(2) }
        btnNominal200.setOnClickListener { pilihNominal(3) }
        btnNominalLainnya.setOnClickListener { pilihNominal(4) }

        // Flow bca dan qris
        val btnProsesTopUp = findViewById<MaterialButton>(R.id.btnProsesTopUp)
        btnProsesTopUp.setOnClickListener {

            if (metodeTerpilih == "BCA") {
                val intent = Intent(this, KonfirmasiBankActivity::class.java)
                startActivity(intent)

            } else if (metodeTerpilih == "QRIS") {
                val intent = Intent(this, KonfirmasiQrisActivity::class.java)
                startActivity(intent)
            }

        }
    }
}