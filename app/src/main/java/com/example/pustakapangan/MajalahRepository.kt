package com.example.pustakapangan

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

    suspend fun muatDariSupabase() {
        val json = SupabaseConfig.get("majalah?select=*")
        val array = JSONArray(json)
        val hasil = mutableListOf<Majalah>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            hasil.add(
                Majalah(
                    id = obj.getInt("id"),
                    kategoriId = obj.optInt("kategori_id"),
                    judul = obj.getString("judul"),
                    tahun = obj.optInt("tahun"),
                    harga = obj.getInt("harga"),
                    urlCover = obj.optString("url_cover"), // ⬅️ FIX: langsung URL asli dari database, gak perlu jembatan lokal lagi
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