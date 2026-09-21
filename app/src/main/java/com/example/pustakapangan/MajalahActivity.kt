package com.example.pustakapangan

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class MajalahActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_majalah)

        // Chip tahun
        val mainScrollView = findViewById<ScrollView>(R.id.mainScrollView)
        val chip2026 = findViewById<MaterialCardView>(R.id.chip2026)
        val tvChip2026 = findViewById<TextView>(R.id.tvChip2026)
        val section2026 = findViewById<TextView>(R.id.section2026)
        val chip2025 = findViewById<MaterialCardView>(R.id.chip2025)
        val tvChip2025 = findViewById<TextView>(R.id.tvChip2025)
        val section2025 = findViewById<TextView>(R.id.section2025)

        fun ubahChipAktif(aktifCard: MaterialCardView, aktifText: TextView, pasifCard: MaterialCardView, pasifText: TextView) {
            aktifCard.setCardBackgroundColor(Color.parseColor("#D32F2F"))
            aktifCard.strokeWidth = 0
            aktifText.setTextColor(Color.parseColor("#FFFFFF"))

            pasifCard.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            pasifCard.strokeColor = Color.parseColor("#E5E7EB")
            pasifCard.strokeWidth = (1 * resources.displayMetrics.density).toInt()
            pasifText.setTextColor(Color.parseColor("#6B7280"))
        }

        chip2026.setOnClickListener {
            ubahChipAktif(chip2026, tvChip2026, chip2025, tvChip2025)
            mainScrollView.post { mainScrollView.smoothScrollTo(0, section2026.top) }
        }
        chip2025.setOnClickListener {
            ubahChipAktif(chip2025, tvChip2025, chip2026, tvChip2026)
            mainScrollView.post { mainScrollView.smoothScrollTo(0, section2025.top - 20) }
        }

        // Hero
        val viewPager = findViewById<ViewPager2>(R.id.viewPagerHero)
        val dot1 = findViewById<MaterialCardView>(R.id.dot1)
        val dot2 = findViewById<MaterialCardView>(R.id.dot2)
        val dot3 = findViewById<MaterialCardView>(R.id.dot3)
        val dots = listOf(dot1, dot2, dot3)

        val majalahTerbaru = MajalahRepository.getTerbaru(3)
        val bannerMajalahData = majalahTerbaru.mapIndexed { index, m ->
            val tag = when (index) { 0 -> "TERBARU"; 1 -> "TERPOPULER"; else -> "PILIHAN" }
            BannerMajalahItem(m.id, m.urlCover, tag, m.judul, "Beli - Rp${"%,d".format(m.harga).replace(',', '.')}")
        }

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

        findViewById<RecyclerView>(R.id.recyclerView2026).apply {
            layoutManager = LinearLayoutManager(this@MajalahActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = MajalahGridAdapter(MajalahRepository.getByTahun(2026)) { majalahId -> bukaDetailMajalah(majalahId) }
        }
        findViewById<RecyclerView>(R.id.recyclerView2025).apply {
            layoutManager = LinearLayoutManager(this@MajalahActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = MajalahGridAdapter(MajalahRepository.getByTahun(2025)) { majalahId -> bukaDetailMajalah(majalahId) }
        }

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

    override fun onResume() {
        super.onResume()
        findViewById<View>(R.id.dotNotifBell).visibility = if (NotifikasiState.adaNotifBelumDibaca(this)) View.VISIBLE else View.GONE
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
            holder.bgImage.setImageResource(currentItem.image)
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
            holder.imgCover.setImageResource(majalah.urlCover)
            holder.tvHarga.text = "Rp${"%,d".format(majalah.harga).replace(',', '.')}"
            holder.itemView.setOnClickListener { onItemClick(majalah.id) }
        }

        override fun getItemCount() = items.size
    }
}

// hero majalah
data class BannerMajalahItem(
    val majalahId: Int,
    val image: Int,
    val tagText: String,
    val title: String,
    val buttonText: String
)