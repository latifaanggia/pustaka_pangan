package com.example.pustakapangan

import android.content.Context

object RiwayatBaca {
    private fun prefs(ctx: Context) = ctx.getSharedPreferences("riwayat_baca", Context.MODE_PRIVATE)
    private fun uid(ctx: Context) = CustomerRepository.getUserAktif(ctx)?.id ?: "tamu"

    fun simpan(ctx: Context, namaFile: String, halaman: Int) =
        prefs(ctx).edit().putInt("hal_${uid(ctx)}_$namaFile", halaman).putString("terakhir_${uid(ctx)}", namaFile).apply()
    fun halaman(ctx: Context, namaFile: String) =
        prefs(ctx).getInt("hal_${uid(ctx)}_$namaFile", 0) // 0-based
    fun terakhir(ctx: Context): String? =
        prefs(ctx).getString("terakhir_${uid(ctx)}", null)
}
