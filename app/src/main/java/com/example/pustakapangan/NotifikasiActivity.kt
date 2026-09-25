package com.example.pustakapangan

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class NotifikasiActivity : AppCompatActivity() {
    private lateinit var rvNotifikasi: RecyclerView
    private lateinit var progress: ProgressBar
    private lateinit var tvKosong: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!SessionManager.isLoggedIn(this)) {
            startActivity(Intent(this, SignInActivity::class.java));
            finish(); return
        }
        setContentView(R.layout.activity_notifikasi)
        findViewById<ImageView>(R.id.btnBack).setOnClickListener {
            finish()
        }
        rvNotifikasi = findViewById<RecyclerView>(R.id.rvNotifikasi).apply {
            layoutManager = LinearLayoutManager(this@NotifikasiActivity)
        }
        progress = findViewById(R.id.progressNotifikasi);
        tvKosong = findViewById(R.id.tvNotifKosong)
    }

    override fun onResume() {
        super.onResume(); if (::rvNotifikasi.isInitialized) muatNotifikasi()
    }

    private fun muatNotifikasi() {
        if (rvNotifikasi.adapter == null) progress.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val daftar = NotifikasiRepository.getSemua(this@NotifikasiActivity)
                rvNotifikasi.adapter = NotifikasiAdapter(daftar) {
                    notif, posisi -> onNotifDiklik(notif, posisi)
                }
                tvKosong.text = "Belum ada notifikasi";
                tvKosong.visibility = if (daftar.isEmpty()) View.VISIBLE else View.GONE
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                if (rvNotifikasi.adapter == null) {
                    tvKosong.text = "Gagal memuat notifikasi.\n${e.message}";
                    tvKosong.visibility = View.VISIBLE
                }
                if (e.message?.contains("Sesi berakhir") == true) {
                    startActivity(Intent(this@NotifikasiActivity, SignInActivity::class.java));
                    finish()
                }
            } finally { progress.visibility = View.GONE }
        }
    }

    // Tap: tandai dibaca
    private fun onNotifDiklik(notif: Notifikasi, posisi: Int) {
        if (!notif.sudahDibaca) {
            notif.sudahDibaca = true; rvNotifikasi.adapter?.notifyItemChanged(posisi)
            lifecycleScope.launch {
                try {
                    NotifikasiRepository.tandaiDibaca(this@NotifikasiActivity, notif.id)
                }
                catch (e: CancellationException) {
                    throw e
                }
                catch (e: Exception) { }
            }
        }
        val tujuan = when (notif.tipe) {
            "edisi_baru" -> MajalahActivity::class.java;
            "topup_berhasil", "topup_ditolak" -> RiwayatTopUpActivity::class.java; else -> null
        }
        tujuan?.let {
            startActivity(Intent(this, it))
        }
    }

    class NotifikasiAdapter(
        private val items: List<Notifikasi>,
        private val onKlik: (Notifikasi, Int) -> Unit) : RecyclerView.Adapter<NotifikasiAdapter.VH>() {
        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val card: MaterialCardView = v.findViewById(R.id.cardNotif);
            val iconBg: MaterialCardView = v.findViewById(R.id.iconNotifBg)
            val icon: ImageView = v.findViewById(R.id.imgIconNotif);
            val dot: View = v.findViewById(R.id.dotUnread)
            val judul: TextView = v.findViewById(R.id.tvJudulNotif);
            val pesan: TextView = v.findViewById(R.id.tvPesanNotif);
            val waktu: TextView = v.findViewById(R.id.tvWaktuNotif)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(LayoutInflater.from(parent.context).inflate(R.layout.item_notifikasi, parent, false))
        override fun getItemCount() = items.size
        override fun onBindViewHolder(h: VH, position: Int) {
            val n = items[position];
            val density = h.itemView.resources.displayMetrics.density

            h.judul.text = n.judul; h.pesan.text = n.pesan;
            h.waktu.text = NotifikasiRepository.formatWaktu(n.waktu)

            h.icon.setImageResource(when (n.tipe) {
                "edisi_baru" -> R.drawable.ic_open_book;
                "selamat_datang" -> R.drawable.ic_popper;
                else -> R.drawable.ic_card
            })
            h.card.setCardBackgroundColor(Color.parseColor(if (n.sudahDibaca) "#FFFFFF" else "#FFF5F5"))

            h.card.strokeColor = Color.parseColor("#FFCDD2");
            h.card.strokeWidth = if (n.sudahDibaca) 0 else density.toInt()

            h.card.cardElevation = if (n.sudahDibaca) 2 * density else 0f
            h.iconBg.setCardBackgroundColor(Color.parseColor(if (n.sudahDibaca && n.tipe != "topup_ditolak") "#F4F6F8" else "#FFEBEE"))
            h.dot.visibility = if (n.sudahDibaca) View.GONE else View.VISIBLE
            h.card.setOnClickListener {
                h.adapterPosition.takeIf {
                    it != RecyclerView.NO_POSITION
                }?.let {
                    onKlik(items[it], it)
                }
            }
        }
    }
}
