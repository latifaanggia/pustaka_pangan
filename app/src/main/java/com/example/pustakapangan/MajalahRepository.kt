package com.example.pustakapangan

import android.content.Context
import org.json.JSONArray

data class Majalah(
    val id: Int,
    val kategoriId: Int,
    val judul: String,
    val tahun: Int,
    val harga: Int,
    val urlCover: String,
    val namaFilePdf: String,
    val daftarIsi: String
)

object MajalahRepository {
    private var daftarMajalah: List<Majalah> = emptyList()
    var sudahDimuat = false
        private set

    var dariCache = false // true = data diambil dari penyimpanan HP karena offline
        private set

    // Online: ambil dari Supabase lalu simpan salinannya
    // Offline: pakai salinan terakhir, supaya majalah yang sudah diunduh tetap bisa dibaca tanpa internet
    // Kalau belum pernah online sama sekali, lempar error
    suspend fun muat(context: Context) {
        val prefs = context.getSharedPreferences("majalah_cache", Context.MODE_PRIVATE)
        val json = try {
            SupabaseConfig.get("majalah?select=*").also {
                prefs.edit().putString("json", it).apply(); dariCache = false
            }
        }
        catch (e: Exception) {
            prefs.getString("json", null)?.also {
                dariCache = true
            } ?: throw e
        }
        val array = JSONArray(json)
        daftarMajalah = (0 until array.length()).map { i ->
            array.getJSONObject(i).let { obj ->
                Majalah(
                    id = obj.getInt("id"),
                    kategoriId = obj.optInt("kategori_id"),
                    judul = obj.getString("judul"),
                    tahun = obj.optInt("tahun"),
                    harga = obj.getInt("harga"),
                    urlCover = obj.optString("url_cover"),
                    namaFilePdf = obj.optString("url_pdf"),
                    daftarIsi = obj.optString("daftar_isi")
                )
            }
        }
        sudahDimuat = true
    }

    fun getByNamaFile(namaFile: String): Majalah? = daftarMajalah.find {
        it.namaFilePdf == namaFile
    }

    fun getSemuaMajalah(): List<Majalah> = daftarMajalah

    fun getByTahun(tahun: Int): List<Majalah> = daftarMajalah.filter {
        it.tahun == tahun
    }

    fun getTerbaru(jumlah: Int = 3): List<Majalah> =
        daftarMajalah.sortedWith(compareByDescending<Majalah> {
            it.tahun
        }.thenByDescending { it.id }).take(jumlah)

    fun getById(id: Int): Majalah? = daftarMajalah.find {
        it.id == id
    }
}