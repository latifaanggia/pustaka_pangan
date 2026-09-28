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
    val namaFilePdf: String, // kosong = baru ada cover, versi digital (PDF) belum tersedia
    val daftarIsi: String,
    val edisi: Int = 0,
    val jumlahPembeli: Int = 0, // dasar urutan section "Populer"
    val jumlahPratinjau: Int = 0 // jumlah gambar Pratinjau Editorial di bucket pratinjau-majalah (0 = pakai cover)
) {
    // 2026_vol_07.pdf -> .../pratinjau-majalah/2026_vol_07_hal3.jpg
    fun urlPratinjau(halaman: Int) = "${SupabaseConfig.PROJECT_URL}/storage/v1/object/public/pratinjau-majalah/${namaFilePdf.removeSuffix(".pdf")}_hal$halaman.jpg"
    val tersedia get() = namaFilePdf.isNotBlank()
    val gratis get() = harga == 0 // majalah gratis (Kulinologi, Food For Kids)
}

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
                    namaFilePdf =
                        if (obj.isNull("url_pdf")) ""
                        else obj.getString("url_pdf"),
                    daftarIsi = obj.optString("daftar_isi"),
                    edisi = obj.optInt("edisi"),
                    jumlahPembeli = obj.optInt("jumlah_pembeli"),
                    jumlahPratinjau = obj.optInt("jumlah_pratinjau")
                )
            }
        }
        sudahDimuat = true
    }

    fun getByNamaFile(namaFile: String): Majalah? = daftarMajalah.find {
        it.namaFilePdf == namaFile
    }

    fun getSemuaMajalah(): List<Majalah> = daftarMajalah

    // Urutan standar katalog: tahun terbaru dulu, lalu edisi terbesar (Vol 08 sebelum Vol 07, dst)
    private val urutanTerbaru = compareByDescending<Majalah> { it.tahun }.thenByDescending { it.edisi }

    fun getByTahun(tahun: Int): List<Majalah> = daftarMajalah.filter { it.tahun == tahun }.sortedWith(urutanTerbaru)

    fun getDaftarTahun(): List<Int> = daftarMajalah.map { it.tahun }.distinct().sortedDescending()

    // Banner/Terbaru/Populer
    private fun bisaDibeli() = daftarMajalah.filter { it.tersedia && !it.gratis }

    fun getTerbaru(jumlah: Int = 3): List<Majalah> = bisaDibeli().sortedWith(urutanTerbaru).take(jumlah)

    // Pembeli terbanyak dulu; kalau jumlahnya sama (misal sama-sama 0), yang terbaru dulu
    fun getPopuler(jumlah: Int = 5): List<Majalah> =
        bisaDibeli().sortedWith(compareByDescending<Majalah> {
            it.jumlahPembeli
        }.then(urutanTerbaru)).take(jumlah)

    fun getGratis(): List<Majalah> = daftarMajalah.filter {
        it.tersedia && it.gratis
    }.sortedWith(urutanTerbaru)

    fun getById(id: Int): Majalah? = daftarMajalah.find {
        it.id == id
    }
}