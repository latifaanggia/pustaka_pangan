package com.example.pustakapangan

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class KoleksiActivity : AppCompatActivity() {
    private val progressUnduh = mutableMapOf<Int, Int>() // majalahId -> persen
    private val adapters = mutableListOf<KoleksiAdapter>()
    private var idsTampil: List<Int>? = null
    private var jobMuat: Job? = null
    private var infoOfflineDitampilkan = false
    private var aksiKosong: () -> Unit = {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!SessionManager.isLoggedIn(this)) {
            startActivity(Intent(this, SignInActivity::class.java));
            finish();
            return
        }
        setContentView(R.layout.activity_koleksi)
        if (MajalahRepository.getSemuaMajalah().isEmpty()) {
            Toast.makeText(this, "Gagal memuat data majalah. Cek koneksi internet lalu coba lagi.", Toast.LENGTH_LONG).show()
            startActivity(Intent(this, MainActivity::class.java));
            finish();
            return
        }

        // NAVIGASI
        findViewById<RelativeLayout>(R.id.navBeranda).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java)); finish()
        }
        findViewById<RelativeLayout>(R.id.navAkun).setOnClickListener {
            startActivity(Intent(this, AkunActivity::class.java));
            finish()
        }
        findViewById<RelativeLayout>(R.id.navMajalah).setOnClickListener {
            startActivity(Intent(this, MajalahActivity::class.java));
            finish()
        }
        findViewById<ImageView>(R.id.btnNotifikasi).setOnClickListener {
            startActivity(Intent(this, NotifikasiActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnAksiKoleksiKosong).setOnClickListener {
            aksiKosong()
        }
    }

    // habis beli majalah atau balik dari E-Reader, isi koleksi & "Terakhir Dibaca" ikut diperbarui
    override fun onResume() {
        super.onResume()
        if (isFinishing || !SessionManager.isLoggedIn(this)) return
        NotifikasiRepository.perbaruiBadge(this, findViewById(R.id.dotNotifBell))
        muatKoleksi()
    }

    private fun muatKoleksi() {
        if (jobMuat?.isActive == true) return
        val progress = findViewById<ProgressBar>(R.id.progressKoleksi)
        if (idsTampil == null) progress.visibility = View.VISIBLE
        jobMuat = lifecycleScope.launch {
            try {
                val ids = PembelianRepository.getIdMajalahDibeli(this@KoleksiActivity)
                tampilkanKoleksi(MajalahRepository.getSemuaMajalah().filter { it.id in ids })
                if (PembelianRepository.dariCache && !infoOfflineDitampilkan) {
                    infoOfflineDitampilkan = true
                    Toast.makeText(this@KoleksiActivity, "Mode offline: majalah yang sudah diunduh tetap bisa dibaca", Toast.LENGTH_LONG).show()
                }
            } catch (e: CancellationException) { throw e
            } catch (e: Exception) {
                if (e.message?.contains("Sesi berakhir") == true) {
                    Toast.makeText(this@KoleksiActivity, "Sesi berakhir, silakan login ulang", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@KoleksiActivity, SignInActivity::class.java)); finish(); return@launch
                }
                if (idsTampil == null) tampilkanKosong("Gagal memuat koleksi.\nCek koneksi internet kamu lalu coba lagi.", "Coba Lagi") { muatKoleksi() }
            } finally { progress.visibility = View.GONE }
        }
    }

    private fun tampilkanKoleksi(dibeli: List<Majalah>) {
        val ids = dibeli.map { it.id }.sorted()
        if (ids == idsTampil) adapters.forEach { it.notifyDataSetChanged() }
        else {
            idsTampil = ids
            val container = findViewById<LinearLayout>(R.id.layoutKoleksiTahun).apply {
                removeAllViews()
            };
            adapters.clear()
            dibeli.groupBy { it.tahun }.toSortedMap(reverseOrder()).forEach {
                (tahun, daftar) ->
                val section = layoutInflater.inflate(R.layout.item_koleksi_tahun, container, false)
                section.findViewById<TextView>(R.id.tvTahunKoleksi).text = "Tahun $tahun"
                val adapter = KoleksiAdapter(daftar.sortedByDescending {
                    it.id
                }).also {
                    adapters.add(it)
                }
                section.findViewById<RecyclerView>(R.id.rvMajalahTahun).apply {
                    layoutManager = LinearLayoutManager(this@KoleksiActivity, LinearLayoutManager.HORIZONTAL, false);
                    this.adapter = adapter
                }
                container.addView(section)
            }
        }
        if (dibeli.isEmpty()) tampilkanKosong("Koleksimu masih kosong.\nBeli majalah untuk mulai membaca.", "Jelajahi Majalah") {
            startActivity(Intent(this, MajalahActivity::class.java));
            finish()
        }
        else findViewById<View>(R.id.layoutKoleksiKosong).visibility = View.GONE
        tampilkanTerakhirDibaca(dibeli)
    }

    private fun tampilkanKosong(pesan: String, teksTombol: String, aksi: () -> Unit) {
        findViewById<View>(R.id.layoutKoleksiKosong).visibility = View.VISIBLE
        findViewById<TextView>(R.id.tvKoleksiKosong).text = pesan
        findViewById<MaterialButton>(R.id.btnAksiKoleksiKosong).text = teksTombol; aksiKosong = aksi
    }

    // Kartu "Terakhir Dibaca"
    private fun tampilkanTerakhirDibaca(dibeli: List<Majalah>) {
        val majalah = RiwayatBaca.terakhir(this)?.let {
            file -> dibeli.find {
                it.namaFilePdf == file
            }
        }
        val card = findViewById<MaterialCardView>(R.id.cardTerakhirDibaca)
        listOf(card, findViewById<View>(R.id.tvLabelTerakhirDibaca)).forEach {
            it.visibility = if (majalah != null) View.VISIBLE else View.GONE
        }
        if (majalah == null) return
        Glide.with(this).load(majalah.urlCover).into(findViewById<ImageView>(R.id.imgCoverTerakhir))
        findViewById<TextView>(R.id.tvJudulTerakhir).text = majalah.judul
        findViewById<TextView>(R.id.tvLanjutTerakhir).text = "Lanjutkan Membaca · Hal. ${RiwayatBaca.halaman(this, majalah.namaFilePdf) + 1}"
        card.setOnClickListener {
            bukaEReader(majalah)
        }
    }

    private fun bukaEReader(majalah: Majalah) {
        if (majalah.id in progressUnduh) {
            Toast.makeText(this, "Tunggu unduhan selesai dulu ya", Toast.LENGTH_SHORT).show(); return
        }
        startActivity(Intent(this, EReaderActivity::class.java).apply {
            putExtra("JUDUL_MAJALAH", majalah.judul); putExtra("NAMA_FILE_PDF", majalah.namaFilePdf)
            putExtra("IS_OFFLINE", PdfDownloader.sudahDiunduh(this@KoleksiActivity, majalah.namaFilePdf))
        })
    }

    private fun unduh(majalah: Majalah) {
        if (majalah.id in progressUnduh) {
            Toast.makeText(this, "Sedang mengunduh...", Toast.LENGTH_SHORT).show();
            return
        }
        progressUnduh[majalah.id] = 0;
        refreshItem(majalah.id)

        lifecycleScope.launch {
            try {
                // Sudah pernah dibaca online -> file ada di cache
                if (PdfDownloader.adaDiCache(this@KoleksiActivity, majalah.namaFilePdf))
                    PdfDownloader.simpanDariCache(this@KoleksiActivity, majalah.namaFilePdf)
                else PdfDownloader.unduh(
                    majalah.namaFilePdf,
                    PdfDownloader.fileOffline(this@KoleksiActivity, majalah.namaFilePdf)
                ) {
                    progressUnduh[majalah.id] = it; refreshItem(majalah.id)
                }
                Toast.makeText(this@KoleksiActivity, "${majalah.judul} berhasil diunduh!", Toast.LENGTH_SHORT).show()
            }
            catch (e: CancellationException) {
                throw e
            }
            catch (e: Exception) {
                Toast.makeText(
                    this@KoleksiActivity,
                    e.message ?: "Gagal mengunduh majalah",
                    Toast.LENGTH_LONG).show()
            }
            finally {
                progressUnduh.remove(majalah.id);
                refreshItem(majalah.id)
            }
        }
    }

    private fun hapusUnduhan(majalah: Majalah) {
        AlertDialog.Builder(this).setTitle("Hapus Unduhan?")
            .setMessage("${majalah.judul} akan dihapus dari penyimpanan HP. Kamu tetap bisa membaca online atau mengunduhnya lagi kapan saja.")
            .setPositiveButton("Hapus") { _, _ ->
                PdfDownloader.hapusFile(PdfDownloader.fileOffline(this, majalah.namaFilePdf));
                refreshItem(majalah.id)
                Toast.makeText(this, "Unduhan dihapus", Toast.LENGTH_SHORT).show()
            }.setNegativeButton("Batal", null).show()
    }

    private fun refreshItem(majalahId: Int) = adapters.forEach { it.refresh(majalahId) }

    private fun bindTombol(h: KoleksiVH, m: Majalah) {
        val persen = progressUnduh[m.id];
        val offline = persen == null && PdfDownloader.sudahDiunduh(this, m.namaFilePdf)
        val hijau = Color.parseColor("#00A859");
        val abu = Color.parseColor("#E5E7EB");
        val hitam = Color.parseColor("#1E1E1E")

        h.btnBaca.setCardBackgroundColor(Color.parseColor(if (offline) "#E8F5E9" else "#FFFFFF"));
        h.btnBaca.strokeColor = if (offline) hijau else abu
        h.tvBaca.text = if (persen != null) "$persen%" else "Baca";
        h.tvBaca.setTextColor(if (offline) hijau else hitam)

        h.btnUnduh.strokeColor = if (offline) hijau else abu;
        h.btnUnduh.alpha = if (persen != null) 0.5f else 1f
        h.iconUnduh.setImageResource(
            if (offline) R.drawable.ic_check_green
            else R.drawable.ic_download
        )
        if (offline) h.iconUnduh.clearColorFilter()
        else h.iconUnduh.setColorFilter(hitam)
    }

    class KoleksiVH(v: View) : RecyclerView.ViewHolder(v) {
        val cover: ImageView = v.findViewById(R.id.imgCoverKoleksi);
        val judul: TextView = v.findViewById(R.id.tvJudulKoleksi)
        val btnBaca: MaterialCardView = v.findViewById(R.id.btnBacaKoleksi);
        val tvBaca: TextView = v.findViewById(R.id.tvBacaKoleksi)
        val btnUnduh: MaterialCardView = v.findViewById(R.id.btnUnduhKoleksi);
        val iconUnduh: ImageView = v.findViewById(R.id.iconUnduhKoleksi)
    }

    inner class KoleksiAdapter(private val items: List<Majalah>) : RecyclerView.Adapter<KoleksiVH>() {
        override fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int) = KoleksiVH(LayoutInflater.from(parent.context).inflate(R.layout.item_koleksi_majalah, parent, false))
        override fun getItemCount() = items.size
        override fun onBindViewHolder(h: KoleksiVH, position: Int) {
            val m = items[position]
            Glide.with(h.itemView).load(m.urlCover).into(h.cover);
            h.judul.text = m.judul; bindTombol(h, m)
            h.btnBaca.setOnClickListener {
                bukaEReader(m)
            }
            h.btnUnduh.setOnClickListener {
                if (PdfDownloader.sudahDiunduh(
                        this@KoleksiActivity,
                        m.namaFilePdf)
                    )
                    hapusUnduhan(m)
                else unduh(m)
            }
        }
        override fun onBindViewHolder(h: KoleksiVH, position: Int, payloads: MutableList<Any>) {
            if (payloads.isEmpty()) super.onBindViewHolder(h, position, payloads)
            else bindTombol(h, items[position])
        }
        fun refresh(majalahId: Int) {
            items.indexOfFirst {
                it.id == majalahId
            }.takeIf { it >= 0 }?.let {
                notifyItemChanged(it, "tombol")
            }
        }
    }
}
