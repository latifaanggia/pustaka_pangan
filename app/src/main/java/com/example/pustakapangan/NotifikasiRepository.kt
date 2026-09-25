package com.example.pustakapangan

import android.content.Context
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class Notifikasi(
    val id: Int,
    val tipe: String,
    val judul: String,
    val pesan: String,
    var sudahDibaca: Boolean,
    val waktu: Date)

object NotifikasiRepository {
    private var jumlahBelumDibacaTerakhir = 0

    suspend fun getSemua(context: Context): List<Notifikasi> {
        val array = JSONArray(SupabaseConfig.get(
            "notifikasi?select=*&order=created_at.desc,id.desc&limit=50",
            CustomerRepository.getTokenValid(context)))

        return (0 until array.length()).map {
            i -> array.getJSONObject(i).let {
            Notifikasi(it.getInt("id"),
                it.getString("tipe"),
                it.getString("judul"),
                it.getString("pesan"),
                it.getBoolean("sudah_dibaca"),
                parseWaktu(it.getString("created_at")))
            }
        }.also {
            daftar -> jumlahBelumDibacaTerakhir = daftar.count {
                !it.sudahDibaca
            }
        }
    }

    suspend fun jumlahBelumDibaca(context: Context): Int =
        JSONArray(SupabaseConfig.get(
            "notifikasi?sudah_dibaca=eq.false&select=id",
            CustomerRepository.getTokenValid(context))).length().also {
                jumlahBelumDibacaTerakhir = it
            }

    suspend fun tandaiDibaca(context: Context, idNotif: Int? = null) {
        SupabaseConfig.postRpc(
            "tandai_notifikasi_dibaca",
            JSONObject().put("p_id", idNotif ?: JSONObject.NULL).toString(),
            CustomerRepository.getTokenValid(context)
        )
        jumlahBelumDibacaTerakhir =
            if (idNotif == null) 0
            else (jumlahBelumDibacaTerakhir - 1).coerceAtLeast(0)
    }

    fun perbaruiBadge(activity: AppCompatActivity, dot: View) {
        if (!SessionManager.isLoggedIn(activity)) {
            dot.visibility = View.GONE;
            return
        }
        dot.visibility = if (jumlahBelumDibacaTerakhir > 0) View.VISIBLE else View.GONE
        activity.lifecycleScope.launch {
            try {
                dot.visibility = if (jumlahBelumDibaca(activity) > 0) View.VISIBLE
                else View.GONE
            }
            catch (e: CancellationException) {
                throw e
            }
            catch (e: Exception) { }
        }
    }

    // "2026-09-25T07:35:43.123456+00:00" -> Date
    private fun parseWaktu(iso: String): Date = try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).parse(
            iso.replace(Regex("\\.\\d+"),
            "").replace(Regex("([+-]\\d{2}):(\\d{2})$"), "$1$2")
        )!!
    }
    catch (e: Exception) {
        Date()
    }

    fun formatWaktu(waktu: Date): String {
        val selisihMenit = (System.currentTimeMillis() - waktu.time) / 60_000
        val id = Locale("in", "ID");
        val jam = SimpleDateFormat("HH:mm", id).format(waktu)
        val hariIni = Calendar.getInstance();
        val cal = Calendar.getInstance().apply {
            time = waktu
        }
        val kemarin = (hariIni.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }
        fun samaHari(a: Calendar, b: Calendar) = a.get(Calendar.YEAR) ==
                b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) ==
                b.get(Calendar.DAY_OF_YEAR)
        return when {
            selisihMenit < 1 -> "Baru saja"
            selisihMenit < 60 -> "$selisihMenit menit lalu"
            samaHari(cal, hariIni) -> "Hari ini, $jam"
            samaHari(cal, kemarin) -> "Kemarin, $jam"
            else -> SimpleDateFormat("dd MMMM yyyy", id).format(waktu)
        }
    }
}
