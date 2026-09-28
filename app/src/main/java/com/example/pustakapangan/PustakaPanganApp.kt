package com.example.pustakapangan

import android.app.Application

class PustakaPanganApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotifikasiPopup.pasang(this)
    }
}
