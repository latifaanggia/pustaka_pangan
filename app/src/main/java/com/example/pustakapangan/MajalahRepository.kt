package com.example.pustakapangan

import org.json.JSONArray

data class Majalah(
    val id: Int,
    val kategoriId: Int,
    val judul: String,
    val tahun: Int,
    val harga: Int,
    val urlCover: Int,
    val namaFilePdf: String,
    val daftarIsi: String
)

object MajalahRepository {
    private var daftarMajalah: List<Majalah> = emptyList()
    var sudahDimuat = false
        private set

    private val petaCoverLokal = mapOf(
        "img_2026_vol_07.png" to R.drawable.img_2026_vol_07,
        "img_2026_vol_06.png" to R.drawable.img_2026_vol_06,
        "img_2026_vol_05.png" to R.drawable.img_2026_vol_05,
        "img_2026_vol_04.png" to R.drawable.img_2026_vol_04,
        "img_2025_vol_12.png" to R.drawable.img_2025_vol_12,
        "img_2025_vol_11.png" to R.drawable.img_2025_vol_11,
        "img_2025_vol_10.png" to R.drawable.img_2025_vol_10
    )

    suspend fun muatDariSupabase() {
        val json = SupabaseConfig.get("majalah?select=*")
        val array = JSONArray(json)
        val hasil = mutableListOf<Majalah>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val namaCover = obj.optString("url_cover")
            hasil.add(
                Majalah(
                    id = obj.getInt("id"),
                    kategoriId = obj.optInt("kategori_id"),
                    judul = obj.getString("judul"),
                    tahun = obj.optInt("tahun"),
                    harga = obj.getInt("harga"),
                    urlCover = petaCoverLokal[namaCover] ?: R.drawable.img_2026_vol_07,
                    namaFilePdf = obj.optString("url_pdf"),
                    daftarIsi = obj.optString("daftar_isi")
                )
            )
        }
        daftarMajalah = hasil
        sudahDimuat = true
    }

    fun getSemuaMajalah(): List<Majalah> = daftarMajalah

    fun getByTahun(tahun: Int): List<Majalah> = daftarMajalah.filter { it.tahun == tahun }

    fun getTerbaru(jumlah: Int = 3): List<Majalah> =
        daftarMajalah.sortedWith(compareByDescending<Majalah> { it.tahun }.thenByDescending { it.id }).take(jumlah)

    fun getById(id: Int): Majalah? = daftarMajalah.find { it.id == id }
}
