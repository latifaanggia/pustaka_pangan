package com.example.pustakapangan

data class RiwayatTopUp(
    val id: Int,            // topup_id
    val customerId: String,
    val saldo: Int,
    val status: String,     // "Menunggu Konfirmasi" / "Berhasil" / "Ditolak"
    val tanggal: String,
    val metode: String,     // method: "BCA Transfer" / "QRIS"
    val noInvoice: String   // custom
)

object TopUpRepository {
    private val daftarRiwayat = mutableListOf(
        RiwayatTopUp(2, "dummy-1", 20000, "Menunggu Konfirmasi", "07 Agustus 2026, 11.45", "BCA Transfer", "INV-20260807-002"),
        RiwayatTopUp(1, "dummy-1", 100000, "Berhasil", "20 Juli 2026, 14.10", "BCA Transfer", "INV-20260720-091")
    )

    fun getRiwayatByCustomer(customerId: String): List<RiwayatTopUp> =
        daftarRiwayat.filter { it.customerId == customerId }.sortedByDescending { it.id }

    fun getMenungguKonfirmasi(customerId: String): List<RiwayatTopUp> =
        getRiwayatByCustomer(customerId).filter { it.status == "Menunggu Konfirmasi" }

    fun tambahRiwayat(customerId: String, saldo: Int, metode: String): RiwayatTopUp {
        val idBaru = (daftarRiwayat.maxOfOrNull { it.id } ?: 0) + 1
        val sekarang = java.text.SimpleDateFormat("dd MMMM yyyy, HH.mm", java.util.Locale("in", "ID")).format(java.util.Date())
        val noInvoice = "INV-${java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US).format(java.util.Date())}-${(100..999).random()}"
        val riwayatBaru = RiwayatTopUp(idBaru, customerId, saldo, "Menunggu Konfirmasi", sekarang, metode, noInvoice)
        daftarRiwayat.add(riwayatBaru)
        return riwayatBaru
    }
}
