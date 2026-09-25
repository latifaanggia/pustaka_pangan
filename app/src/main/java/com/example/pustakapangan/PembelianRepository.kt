package com.example.pustakapangan

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object PembelianRepository {

    suspend fun beli(context: Context, majalahId: Int): Int {
        val hasil =
            JSONObject(SupabaseConfig.postRpc("beli_majalah",
                JSONObject().put("p_majalah_id", majalahId).toString(),
                CustomerRepository.getTokenValid(context)))
        return hasil.getInt("saldo_baru").also {
            CustomerRepository.updateSaldoLokal(context, it)
        }
    }

    suspend fun sudahDibeli(context: Context, majalahId: Int): Boolean =
        JSONArray(
            SupabaseConfig.get("pembelian?majalah_id=eq.$majalahId&select=id",
                CustomerRepository.getTokenValid(context))).length() > 0

    suspend fun getIdMajalahDibeli(context: Context): Set<Int> {
        val array = JSONArray(
            SupabaseConfig.get("pembelian?select=majalah_id&order=tanggal.desc",
                CustomerRepository.getTokenValid(context)))
        return (0 until array.length()).map {
            array.getJSONObject(it).getInt("majalah_id")
        }.toSet()
    }
}
