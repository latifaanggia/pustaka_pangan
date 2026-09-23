package com.example.pustakapangan

data class Langganan(
    val topUpId: Int,
    val customerId: String,
    val kategoriId: Int,
    val namaLangganan: String,
    val tglAwal: String,
    val tglAkhir: String,
    val status: String
)

object LanggananRepository {
    private val daftarLangganan = mutableListOf(
        Langganan(1, "dummy-1", 1, "Paket Bulanan", "01 Agustus 2026", "01 September 2026", "Aktif")
    )

    fun getLanggananAktif(customerId: String): List<Langganan> =
        daftarLangganan.filter { it.customerId == customerId && it.status == "Aktif" }

    fun isPunyaAksesMajalah(customerId: String, majalahId: Int): Boolean {
        return getLanggananAktif(customerId).isNotEmpty()
    }
}
