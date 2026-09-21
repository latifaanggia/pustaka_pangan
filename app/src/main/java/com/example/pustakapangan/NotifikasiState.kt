package com.example.pustakapangan

import android.content.Context

object NotifikasiState {
    private const val PREF_NAME = "notifikasi_prefs"
    private const val KEY_ADA_BELUM_DIBACA = "ada_belum_dibaca"

    fun adaNotifBelumDibaca(context: Context): Boolean =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_ADA_BELUM_DIBACA, true)

    fun tandaiSudahDibaca(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_ADA_BELUM_DIBACA, false).apply()
    }
}