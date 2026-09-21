package com.example.pustakapangan

data class RiwayatTopUp(
    val id: Int,           // ⬅️ topup_id
    val customerId: Int,
    val saldo: Int,        // ⬅️ balance
    val status: String,    // ⬅️ "Menunggu Konfirmasi" / "Berhasil" / "Ditolak"
    val tanggal: String,   // ⬅️ tanggal
    val metode: String,    // ⬅️ method: "BCA Transfer" / "QRIS"
    val noInvoice: String  // ⬅️ custom
)

object TopUpRepository {
    private val daftarRiwayat = listOf(
        RiwayatTopUp(2, 1, 20000, "Menunggu Konfirmasi", "07 Agustus 2026", "BCA Transfer", "INV-20260807-002"),
        RiwayatTopUp(1, 1, 100000, "Berhasil", "01 Agustus 2026", "QRIS", "INV-20260801-001")
    )

    fun getRiwayatByCustomer(customerId: Int): List<RiwayatTopUp> =
        daftarRiwayat.filter { it.customerId == customerId }.sortedByDescending { it.id }

    fun getMenungguKonfirmasi(customerId: Int): List<RiwayatTopUp> =
        getRiwayatByCustomer(customerId).filter { it.status == "Menunggu Konfirmasi" }
}