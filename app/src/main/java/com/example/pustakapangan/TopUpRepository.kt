package com.example.pustakapangan

import org.json.JSONArray
import org.json.JSONObject

data class RiwayatTopUp(
    val id: Int,
    val customerId: String,
    val saldo: Int,
    val status: String,
    val tanggal: String,
    val metode: String,
    val noInvoice: String
)

object TopUpRepository {
    suspend fun getRiwayatByCustomer(accessToken: String, customerId: String): List<RiwayatTopUp> {
        val json = SupabaseConfig.get("topup?customer_id=eq.$customerId&select=*&order=id.desc", accessToken)
        return parseDaftarRiwayat(JSONArray(json))
    }

    suspend fun tambahRiwayat(accessToken: String, customerId: String, saldo: Int, metode: String): RiwayatTopUp {
        val bodyJson = JSONObject().apply {
            put("customer_id", customerId)
            put("nominal", saldo)
            put("metode", metode)
            put("status", "Menunggu Konfirmasi")
        }.toString()

        val hasil = JSONArray(SupabaseConfig.postRest("topup", bodyJson, accessToken))
        return parseDaftarRiwayat(hasil).first()
    }

    private fun parseDaftarRiwayat(array: JSONArray): List<RiwayatTopUp> {
        val hasil = mutableListOf<RiwayatTopUp>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val id = obj.getInt("id")
            hasil.add(
                RiwayatTopUp(
                    id = id,
                    customerId = obj.getString("customer_id"),
                    saldo = obj.getInt("nominal"),
                    status = obj.getString("status"),
                    tanggal = formatTanggal(obj.optString("tanggal")),
                    metode = obj.optString("metode"),
                    noInvoice = "INV-%05d".format(id)
                )
            )
        }
        return hasil
    }

    private fun formatTanggal(iso: String): String {
        return try {
            val bagianTanggal = iso.take(19)
            val parser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
            val formatter = java.text.SimpleDateFormat("dd MMMM yyyy, HH.mm", java.util.Locale("in", "ID"))
            formatter.format(parser.parse(bagianTanggal)!!)
        } catch (e: Exception) {
            iso
        }
    }
}
