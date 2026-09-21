package com.example.pustakapangan

data class Majalah(
    val id: Int,             // ⬅️ product_id
    val kategoriId: Int,     // ⬅️ category_id
    val judul: String,       // ⬅️ name
    val tahun: Int,
    val harga: Int,          // ⬅️ price
    val urlCover: Int,       // ⬅️ image
    val namaFilePdf: String, // ⬅️ pdf
    val daftarIsi: String    // ⬅️ daftarisi
)

object MajalahRepository {
    private val daftarMajalah = listOf(
        Majalah(7, 1, "FRI VOL XXI/07 2026", 2026, 20000, R.drawable.img_2026_vol_07, "2026_vol_07.pdf",
            "Sains, Data, dan Analisis: Fondasi Transformasi Sistem Pangan"),
        Majalah(6, 1, "FRI VOL XXI/06 2026", 2026, 20000, R.drawable.img_2026_vol_06, "2026_vol_06.pdf",
            "Masa Depan Pangan & Diet Sehat: Rethinking Food Science di Era Digital"),
        Majalah(5, 1, "FRI VOL XXI/05 2026", 2026, 20000, R.drawable.img_2026_vol_05, "2026_vol_05.pdf",
            "Industri Pengolahan Susu Masa Depan"),
        Majalah(4, 1, "FRI VOL XXI/04 2026", 2026, 20000, R.drawable.img_2026_vol_04, "2026_vol_04.pdf",
            "Ketahanan Pangan Lokal di Tengah Perubahan Iklim"),
        Majalah(12, 1, "FRI VOL XX/12 2025", 2025, 20000, R.drawable.img_2025_vol_12, "2025_vol_12.pdf",
            "Refleksi Akhir Tahun: Inovasi Pangan Sepanjang 2025"),
        Majalah(11, 1, "FRI VOL XX/11 2025", 2025, 20000, R.drawable.img_2025_vol_11, "2025_vol_11.pdf",
            "Food Processing for a Sustainable Future"),
        Majalah(10, 1, "FRI VOL XX/10 2025", 2025, 20000, R.drawable.img_2025_vol_10, "2025_vol_10.pdf",
            "Keamanan Pangan: Dinamika, Risiko, dan Tantangan")
    )

    fun getSemuaMajalah(): List<Majalah> = daftarMajalah

    fun getByTahun(tahun: Int): List<Majalah> = daftarMajalah.filter { it.tahun == tahun }

    fun getTerbaru(jumlah: Int = 3): List<Majalah> =
        daftarMajalah.sortedWith(compareByDescending<Majalah> { it.tahun }.thenByDescending { it.id }).take(jumlah)

    fun getById(id: Int): Majalah? = daftarMajalah.find { it.id == id }
}