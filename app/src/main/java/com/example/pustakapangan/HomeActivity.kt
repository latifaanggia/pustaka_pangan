package com.example.pustakapangan

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.card.MaterialCardView

class HomeActivity : AppCompatActivity() {

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

        // Banner Slider
        val viewPager = findViewById<ViewPager2>(R.id.viewPagerHero)
        val dot1 = findViewById<MaterialCardView>(R.id.dot1)
        val dot2 = findViewById<MaterialCardView>(R.id.dot2)
        val dot3 = findViewById<MaterialCardView>(R.id.dot3)
        val dots = listOf(dot1, dot2, dot3)

        val dummyImages = listOf(
            R.drawable.img_vol_04, // Slide 1
            R.drawable.img_vol_05, // Slide 2
            R.drawable.img_vol_06  // Slide 3
        )

        viewPager.adapter = HeroAdapter(dummyImages)

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

    class HeroAdapter(private val images: List<Int>) : RecyclerView.Adapter<HeroAdapter.HeroViewHolder>() {

        class HeroViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val bgImage: ImageView = view.findViewById(R.id.imgBannerBg)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeroViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hero_banner, parent, false)
            return HeroViewHolder(view)
        }

        override fun onBindViewHolder(holder: HeroViewHolder, position: Int) {
            holder.bgImage.setImageResource(images[position])
        }

        override fun getItemCount() = images.size
    }
}