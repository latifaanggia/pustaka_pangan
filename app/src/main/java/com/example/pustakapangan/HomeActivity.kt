package com.example.pustakapangan

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.card.MaterialCardView

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // 1. Panggil komponen dari XML
        val viewPager = findViewById<ViewPager2>(R.id.viewPagerHero)
        val dot1 = findViewById<MaterialCardView>(R.id.dot1)
        val dot2 = findViewById<MaterialCardView>(R.id.dot2)
        val dot3 = findViewById<MaterialCardView>(R.id.dot3)
        val dots = listOf(dot1, dot2, dot3)

        // 2. Siapkan 3 Gambar Dummy untuk Slider (Pastikan gambar ini ada di drawable)
        val dummyImages = listOf(
            R.drawable.img_vol_04, // Slide 1
            R.drawable.img_vol_05, // Slide 2 (ganti dengan gambar lain)
            R.drawable.img_vol_06  // Slide 3 (ganti dengan gambar lain)
        )

        // 3. Masukkan gambar ke dalam ViewPager (Proyektor)
        viewPager.adapter = HeroAdapter(dummyImages)

        // 4. Logika ketika Banner digeser (Update warna & ukuran titik)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                for (i in dots.indices) {
                    val params = dots[i].layoutParams
                    if (i == position) {
                        // Jika aktif: Panjang 24dp, warna Merah
                        params.width = (24 * resources.displayMetrics.density).toInt()
                        dots[i].setCardBackgroundColor(Color.parseColor("#E53935"))
                    } else {
                        // Jika tidak aktif: Panjang 8dp, warna Abu-abu
                        params.width = (8 * resources.displayMetrics.density).toInt()
                        dots[i].setCardBackgroundColor(Color.parseColor("#E0E0E0"))
                    }
                    dots[i].layoutParams = params
                }
            }
        })
    }

    // =======================================================
    // ADAPTER: Ini adalah mesin pencetak slide template-nya
    // =======================================================
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