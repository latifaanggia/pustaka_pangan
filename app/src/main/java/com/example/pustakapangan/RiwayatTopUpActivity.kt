package com.example.pustakapangan

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class RiwayatTopUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat_topup)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        val user = CustomerRepository.getUserAktif(this)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewRiwayat)
        recyclerView.layoutManager = LinearLayoutManager(this)

        if (user == null) {
            Toast.makeText(this, "Sesi login habis, silakan masuk lagi.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                val daftarRiwayat = TopUpRepository.getRiwayatByCustomer(user.accessToken, user.id)
                recyclerView.adapter = RiwayatTopUpAdapter(daftarRiwayat) { pesanWa -> bukaWhatsApp(pesanWa) }
            } catch (e: Exception) {
                Toast.makeText(this@RiwayatTopUpActivity, "Gagal memuat riwayat: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun bukaWhatsApp(pesan: String) {
        try {
            val encodedPesan = java.net.URLEncoder.encode(pesan, "UTF-8")
            val nomorAdmin = "628111190039"
            val url = "https://api.whatsapp.com/send?phone=$nomorAdmin&text=$encodedPesan"
            startActivity(Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(url) })
        } catch (e: Exception) {
            Toast.makeText(this, "Aplikasi WhatsApp tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    class RiwayatTopUpAdapter(
        private val items: List<RiwayatTopUp>,
        private val onKonfirmasiWaClick: (String) -> Unit
    ) : RecyclerView.Adapter<RiwayatTopUpAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvInvoice: TextView = view.findViewById(R.id.tvInvoiceItem)
            val tvTanggal: TextView = view.findViewById(R.id.tvTanggalItem)
            val cardStatus: MaterialCardView = view.findViewById(R.id.cardStatusItem)
            val tvStatus: TextView = view.findViewById(R.id.tvStatusItem)
            val tvMetode: TextView = view.findViewById(R.id.tvMetodeItem)
            val tvNominal: TextView = view.findViewById(R.id.tvNominalItem)
            val btnKonfirmasiWa: MaterialButton = view.findViewById(R.id.btnKonfirmasiWaItem)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_riwayat_topup, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.tvInvoice.text = item.noInvoice
            holder.tvTanggal.text = item.tanggal
            holder.tvMetode.text = item.metode
            holder.tvNominal.text = "Rp${"%,d".format(item.saldo).replace(',', '.')}"
            holder.tvStatus.text = item.status.replace(" Konfirmasi", "\nKonfirmasi")

            when (item.status) {
                "Berhasil" -> {
                    holder.cardStatus.setCardBackgroundColor(Color.parseColor("#E8F5E9"))
                    holder.cardStatus.strokeColor = Color.parseColor("#00A859")
                    holder.tvStatus.setTextColor(Color.parseColor("#00A859"))
                    holder.btnKonfirmasiWa.visibility = View.GONE
                }
                "Ditolak" -> {
                    holder.cardStatus.setCardBackgroundColor(Color.parseColor("#FFEBEE"))
                    holder.cardStatus.strokeColor = Color.parseColor("#D32F2F")
                    holder.tvStatus.setTextColor(Color.parseColor("#D32F2F"))
                    holder.btnKonfirmasiWa.visibility = View.GONE
                }
                else -> {
                    holder.cardStatus.setCardBackgroundColor(Color.parseColor("#FFF3E0"))
                    holder.cardStatus.strokeColor = Color.parseColor("#F28E35")
                    holder.tvStatus.setTextColor(Color.parseColor("#F28E35"))
                    holder.btnKonfirmasiWa.visibility = View.VISIBLE
                }
            }

            holder.btnKonfirmasiWa.setOnClickListener {
                val template = """
                    Halo Admin, saya sudah melakukan top up namun belum ada konfirmasi:

                    No. Invoice: ${item.noInvoice}
                    Metode: ${item.metode}
                    Nominal: Rp${"%,d".format(item.saldo).replace(',', '.')}

                    Mohon dicek dan diproses kembali.
                    *Silakan lampirkan foto bukti transfer di chat ini*
                """.trimIndent()
                onKonfirmasiWaClick(template)
            }
        }

        override fun getItemCount() = items.size
    }
}
