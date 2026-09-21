package com.example.pustakapangan

data class Langganan(
    val topUpId: Int,
    val customerId: Int,
    val kategoriId: Int,
    val namaLangganan: String, // ⬅️ langganan
    val tglAwal: String,       // ⬅️ tgl_awal
    val tglAkhir: String,      // ⬅️ tgl_akhir
    val status: String         // ⬅️ "Aktif" / "Berakhir"
)

object LanggananRepository {
    private val daftarLangganan = mutableListOf(
        Langganan(1, 1, 1, "Paket Bulanan", "01 Agustus 2026", "01 September 2026", "Aktif")
    )

    fun getLanggananAktif(customerId: Int): List<Langganan> =
        daftarLangganan.filter { it.customerId == customerId && it.status == "Aktif" }

    fun isPunyaAksesMajalah(customerId: Int, majalahId: Int): Boolean {
        return getLanggananAktif(customerId).isNotEmpty()
    }
}