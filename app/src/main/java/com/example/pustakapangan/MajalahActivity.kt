package com.example.pustakapangan

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.Typeface
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class MajalahActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_majalah)

        setupKatalogPerTahun()

        // Hero
        val viewPager = findViewById<ViewPager2>(R.id.viewPagerHero)
        val dot1 = findViewById<MaterialCardView>(R.id.dot1)
        val dot2 = findViewById<MaterialCardView>(R.id.dot2)
        val dot3 = findViewById<MaterialCardView>(R.id.dot3)
        val dots = listOf(dot1, dot2, dot3)

        // Slide 1: edisi terbaru, slide 2: pembeli terbanyak (bukan edisi yang sama), slide 3: edisi terbaru berikutnya
        val terbaru = MajalahRepository.getTerbaru(3)
        val populer = MajalahRepository.getPopuler(5).firstOrNull { it.id != terbaru.firstOrNull()?.id }
        val pilihan = terbaru.drop(1).firstOrNull { it.id != populer?.id }
        val bannerMajalahData = listOfNotNull(terbaru.firstOrNull()?.let { "TERBARU" to it }, populer?.let { "TERPOPULER" to it }, pilihan?.let { "PILIHAN" to it })
            .map { (tag, m) -> BannerMajalahItem(m.id, m.urlCover, tag, m.judul, "Beli - Rp${"%,d".format(m.harga).replace(',', '.')}") }

        viewPager.adapter = MajalahHeroAdapter(bannerMajalahData) { majalahId -> bukaDetailMajalah(majalahId) }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                for (i in dots.indices) {
                    val params = dots[i].layoutParams
                    if (i == position) {
                        params.width = (24 * resources.displayMetrics.density).toInt()
                        dots[i].setCardBackgroundColor(Color.parseColor("#E53935"))
                    } else {
                        params.width = (8 * resources.displayMetrics.density).toInt()
                        dots[i].setCardBackgroundColor(Color.parseColor("#E0E0E0"))
                    }
                    dots[i].layoutParams = params
                }
            }
        })

        // Navbar
        val navBeranda = findViewById<RelativeLayout>(R.id.navBeranda)
        navBeranda.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        val navKoleksi = findViewById<RelativeLayout>(R.id.navKoleksi)
        navKoleksi.setOnClickListener {
            val sudahLogin = SessionManager.isLoggedIn(this)
            startActivity(Intent(this, if (sudahLogin) KoleksiActivity::class.java else SignInActivity::class.java))
            if (sudahLogin) finish()
        }

        val navAkun = findViewById<RelativeLayout>(R.id.navAkun)
        navAkun.setOnClickListener {
            val sudahLogin = SessionManager.isLoggedIn(this)
            startActivity(Intent(this, if (sudahLogin) AkunActivity::class.java else SignInActivity::class.java))
            if (sudahLogin) finish()
        }

        // Tombol Notifikasi
        val btnNotifikasi = findViewById<ImageView>(R.id.btnNotifikasi)
        btnNotifikasi.setOnClickListener {
            val intent = Intent(this, NotifikasiActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupKatalogPerTahun() {
        val scroll = findViewById<ScrollView>(R.id.mainScrollView)
        val layoutChip = findViewById<LinearLayout>(R.id.layoutChipTahun); val layoutSection = findViewById<LinearLayout>(R.id.layoutSectionTahun)
        val chips = mutableListOf<Pair<MaterialCardView, TextView>>()
        fun aktifkanChip(index: Int) = chips.forEachIndexed { i, (card, tv) ->
            val aktif = i == index
            card.setCardBackgroundColor(Color.parseColor(if (aktif) "#D32F2F" else "#FFFFFF"))
            card.strokeWidth = if (aktif) 0 else (1 * resources.displayMetrics.density).toInt(); card.strokeColor = Color.parseColor("#E5E7EB")
            tv.setTextColor(Color.parseColor(if (aktif) "#FFFFFF" else "#6B7280")); tv.setTypeface(null, if (aktif) Typeface.BOLD else Typeface.NORMAL)
        }
        MajalahRepository.getDaftarTahun().forEachIndexed { index, tahun ->
            val section = layoutInflater.inflate(R.layout.item_majalah_tahun, layoutSection, false)
            section.findViewById<TextView>(R.id.tvSectionTahun).text = "Tahun $tahun"
            section.findViewById<RecyclerView>(R.id.rvSectionTahun).apply {
                layoutManager = LinearLayoutManager(this@MajalahActivity, LinearLayoutManager.HORIZONTAL, false)
                adapter = MajalahGridAdapter(MajalahRepository.getByTahun(tahun)) { majalahId -> bukaDetailMajalah(majalahId) }
            }
            layoutSection.addView(section)
            val chip = layoutInflater.inflate(R.layout.item_chip_tahun, layoutChip, false) as MaterialCardView
            val tvChip = chip.findViewById<TextView>(R.id.tvChipTahun).apply { text = tahun.toString() }
            chips.add(chip to tvChip); layoutChip.addView(chip)
            // top section dihitung relatif ke ScrollView: posisi container + posisi section di dalamnya
            chip.setOnClickListener { aktifkanChip(index); scroll.post { scroll.smoothScrollTo(0, layoutSection.top + section.top - 20) } }
        }
        aktifkanChip(0)
    }

    override fun onResume() {
        super.onResume()
        NotifikasiRepository.perbaruiBadge(this, findViewById(R.id.dotNotifBell))
    }

    private fun bukaDetailMajalah(majalahId: Int) {
        startActivity(Intent(this, DetailMajalahActivity::class.java).apply {
            putExtra("MAJALAH_ID", majalahId)
        })
    }

    // Adapter Hero
    class MajalahHeroAdapter(
        private val items: List<BannerMajalahItem>,
        private val onBeliClick: (Int) -> Unit
    ) : RecyclerView.Adapter<MajalahHeroAdapter.HeroViewHolder>() {

        class HeroViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val bgImage: ImageView = view.findViewById(R.id.imgBannerBgMajalah)
            val tvTag: TextView = view.findViewById(R.id.tvTagHero)
            val tvTitle: TextView = view.findViewById(R.id.tvJudulMajalahHero)
            val btnBeli: MaterialButton = view.findViewById(R.id.btnBeliMajalahHero)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeroViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.hero_majalah, parent, false)
            return HeroViewHolder(view)
        }

        override fun onBindViewHolder(holder: HeroViewHolder, position: Int) {
            val currentItem = items[position]
            Glide.with(holder.itemView).load(currentItem.image).into(holder.bgImage)
            holder.tvTag.text = currentItem.tagText
            holder.tvTitle.text = currentItem.title
            holder.btnBeli.text = currentItem.buttonText
            holder.btnBeli.setOnClickListener { onBeliClick(currentItem.majalahId) }
        }

        override fun getItemCount() = items.size
    }

    class MajalahGridAdapter(
        private val items: List<Majalah>,
        private val onItemClick: (Int) -> Unit
    ) : RecyclerView.Adapter<MajalahGridAdapter.GridViewHolder>() {

        class GridViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imgCover: ImageView = view.findViewById(R.id.imgCoverItem)
            val tvHarga: TextView = view.findViewById(R.id.tvHargaItem)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_majalah_card, parent, false)
            return GridViewHolder(view)
        }

        override fun onBindViewHolder(holder: GridViewHolder, position: Int) {
            val majalah = items[position]
            Glide.with(holder.itemView).load(majalah.urlCover).into(holder.imgCover)
            // Label: belum ada PDF = "Segera Hadir" (abu-abu), harga 0 = "Gratis", selain itu harga
            holder.tvHarga.text = when { !majalah.tersedia -> "Segera Hadir"; majalah.gratis -> "Gratis"; else -> "Rp${"%,d".format(majalah.harga).replace(',', '.')}" }
            holder.tvHarga.setBackgroundColor(Color.parseColor(if (majalah.tersedia) "#00A859" else "#6B7280"))
            holder.itemView.setOnClickListener { onItemClick(majalah.id) }
        }

        override fun getItemCount() = items.size
    }
}

// hero majalah
data class BannerMajalahItem(
    val majalahId: Int,
    val image: String,
    val tagText: String,
    val title: String,
    val buttonText: String
)