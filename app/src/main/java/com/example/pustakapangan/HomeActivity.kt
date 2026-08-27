package com.example.pustakapangan

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.card.MaterialCardView

class HomeActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Detail Majalah
        val cardMajalahTerbaru1 = findViewById<MaterialCardView>(R.id.cardMajalahTerbaru1)
        cardMajalahTerbaru1.setOnClickListener {
            val intent = Intent(this, DetailMajalahActivity::class.java)
            startActivity(intent)
        }

        // Menu Akun
        val navAkun = findViewById<RelativeLayout>(R.id.navAkun)
        navAkun.setOnClickListener {
            val intent = Intent(this, AkunActivity::class.java)
            startActivity(intent)
        }

        // Menu Koleksi
        val navKoleksi = findViewById<RelativeLayout>(R.id.navKoleksi)
        navKoleksi.setOnClickListener {
            val intent = Intent(this, KoleksiActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Menu Majalah
        val navMajalah = findViewById<RelativeLayout>(R.id.navMajalah)
        navMajalah.setOnClickListener {
            startActivity(Intent(this, MajalahActivity::class.java))
            finish()
        }

        // Tombol Notifikasi
        val btnNotifikasi = findViewById<ImageView>(R.id.btnNotifikasi)
        btnNotifikasi.setOnClickListener {
            val intent = Intent(this, NotifikasiActivity::class.java)
            startActivity(intent)
        }

        val viewPager = findViewById<ViewPager2>(R.id.viewPagerHero)
        val dot1 = findViewById<MaterialCardView>(R.id.dot1)
        val dot2 = findViewById<MaterialCardView>(R.id.dot2)
        val dot3 = findViewById<MaterialCardView>(R.id.dot3)
        val dots = listOf(dot1, dot2, dot3)

        val bannerData = listOf(
            BannerItem(
                R.drawable.img_2026_vol_07,
                "Akses Semua Majalah Digital",
                "Mulai dari Rp 15.000 / Edisi",
                "Beli Sekarang"
            ),
            BannerItem(
                R.drawable.img_2026_vol_06,
                "Edisi Khusus Industri Susu",
                "Baca ulasan lengkapnya di sini",
                "Beli - Rp 20.000"
            ),
            BannerItem(
                R.drawable.img_2026_vol_05,
                "Inovasi Kemasan Ramah Lingkungan",
                "Tantangan Industri Pangan Indonesia",
                "Baca Sekarang"
            )
        )

        viewPager.adapter = HeroAdapter(bannerData)

        // Banner Digeser
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
    }

    class HeroAdapter(private val items: List<BannerItem>) : RecyclerView.Adapter<HeroAdapter.HeroViewHolder>() {

        class HeroViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            // Kenalkan semua komponen dari XML
            val bgImage: ImageView = view.findViewById(R.id.imgBannerBgBeranda)
            val tvTitle: TextView = view.findViewById(R.id.tvBannerTitle)
            val tvSubtitle: TextView = view.findViewById(R.id.tvBannerSubtitle)
            val btnBeli: com.google.android.material.button.MaterialButton = view.findViewById(R.id.btnBeliSekarang)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeroViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.hero_beranda, parent, false)
            return HeroViewHolder(view)
        }

        override fun onBindViewHolder(holder: HeroViewHolder, position: Int) {
            val currentItem = items[position]

            // Ganti isi komponen sesuai data di urutan saat ini
            holder.bgImage.setImageResource(currentItem.image)
            holder.tvTitle.text = currentItem.title
            holder.tvSubtitle.text = currentItem.subtitle
            holder.btnBeli.text = currentItem.buttonText
        }

        override fun getItemCount() = items.size
    }
}

// hero banner
data class BannerItem(
    val image: Int,
    val title: String,
    val subtitle: String,
    val buttonText: String
)