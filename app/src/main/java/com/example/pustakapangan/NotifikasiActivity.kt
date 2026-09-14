package com.example.pustakapangan

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class NotifikasiActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifikasi)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        val cardNotifUnread = findViewById<MaterialCardView>(R.id.cardNotifUnread)
        val dotUnread = findViewById<View>(R.id.dotUnread)
        val iconUnread = findViewById<MaterialCardView>(R.id.iconUnread)
        cardNotifUnread.setOnClickListener {
            cardNotifUnread.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            cardNotifUnread.strokeWidth = 0
            cardNotifUnread.cardElevation = 2 * resources.displayMetrics.density
            iconUnread.setCardBackgroundColor(Color.parseColor("#F4F6F8"))
            dotUnread.visibility = View.GONE
        }
    }
}