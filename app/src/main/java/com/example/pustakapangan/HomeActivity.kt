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

        val duaTerbaru = MajalahRepository.getTerbaru(2)
        val majalahTerbaru1 = duaTerbaru[0]
        val majalahTerbaru2 = duaTerbaru[1]

        findViewById<ImageView>(R.id.imgTerbaru1).setImageResource(majalahTerbaru1.urlCover)
        findViewById<TextView>(R.id.tvJudulTerbaru1).text = majalahTerbaru1.judul
        findViewById<TextView>(R.id.tvHargaTerbaru1).text = "Rp${"%,d".format(majalahTerbaru1.harga).replace(',', '.')}"

        findViewById<ImageView>(R.id.imgTerbaru2).setImageResource(majalahTerbaru2.urlCover)
        findViewById<TextView>(R.id.tvJudulTerbaru2).text = majalahTerbaru2.judul
        findViewById<TextView>(R.id.tvHargaTerbaru2).text = "Rp${"%,d".format(majalahTerbaru2.harga).replace(',', '.')}"

        // Detail Majalah
        val cardMajalahTerbaru1 = findViewById<MaterialCardView>(R.id.cardMajalahTerbaru1)
        cardMajalahTerbaru1.setOnClickListener {
            startActivity(Intent(this, DetailMajalahActivity::class.java).apply { putExtra("MAJALAH_ID", majalahTerbaru1.id) })
        }
        findViewById<MaterialCardView>(R.id.cardMajalahTerbaru2).setOnClickListener {
            startActivity(Intent(this, DetailMajalahActivity::class.java).apply { putExtra("MAJALAH_ID", majalahTerbaru2.id) })
        }

        findViewById<TextView>(R.id.tvLihatSemua).setOnClickListener {
            startActivity(Intent(this, MajalahActivity::class.java))
            finish()
        }

        // Menu Akun
        val navAkun = findViewById<RelativeLayout>(R.id.navAkun)
        navAkun.setOnClickListener {
            val tujuan = if (SessionManager.isLoggedIn(this)) AkunActivity::class.java else SignInActivity::class.java
            startActivity(Intent(this, tujuan))
        }

        // Menu Koleksi
        val navKoleksi = findViewById<RelativeLayout>(R.id.navKoleksi)
        navKoleksi.setOnClickListener {
            val tujuan = if (SessionManager.isLoggedIn(this)) KoleksiActivity::class.java else SignInActivity::class.java
            startActivity(Intent(this, tujuan))
            if (SessionManager.isLoggedIn(this)) finish()
        }

        // Menu Majalah
        val navMajalah = findViewById<RelativeLayout>(R.id.navMajalah)
        navMajalah.setOnClickListener {
            startActivity(Intent(this, MajalahActivity::class.java))
            finish()
        }

        // Tombol Notifikasi
        findViewById<ImageView>(R.id.btnNotifikasi).setOnClickListener {
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

    override fun onResume() {
        super.onResume()
        findViewById<View>(R.id.dotNotifBell).visibility = if (NotifikasiState.adaNotifBelumDibaca(this)) View.VISIBLE else View.GONE
    }

    class HeroAdapter(private val items: List<BannerItem>) : RecyclerView.Adapter<HeroAdapter.HeroViewHolder>() {

        class HeroViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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