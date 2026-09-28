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
                CustomerRepository.getTokenValid(context)
            )
        ).length() > 0

    var dariCache = false // true = daftar pembelian dari penyimpanan HP (offline)
        private set

    // Daftar id majalah yang dibeli user
    suspend fun getIdMajalahDibeli(context: Context): Set<Int> {
        val prefs = context.getSharedPreferences("pembelian_cache", Context.MODE_PRIVATE)
        val key = "ids_${CustomerRepository.getUserAktif(context)?.id}"
        return try {
            val array = JSONArray(SupabaseConfig.get("pembelian?select=majalah_id&order=tanggal.desc", CustomerRepository.getTokenValid(context)))
            (0 until array.length()).map {
                array.getJSONObject(it).getInt("majalah_id")
            }.toSet()
                .also { ids -> prefs.edit().putStringSet(key, ids.map {
                    it.toString()
                }
                    .toSet()).apply(); dariCache = false
                }
        } catch (e: Exception) {
            if (e.message?.contains("Sesi berakhir") == true) throw e
            prefs.getStringSet(key, null)?.mapNotNull {
                it.toIntOrNull()
            }?.toSet()?.also {
                dariCache = true
            } ?: throw e
        }
    }
}
