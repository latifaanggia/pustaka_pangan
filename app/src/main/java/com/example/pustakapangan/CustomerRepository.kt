package com.example.pustakapangan

import android.content.Context
import org.json.JSONObject

data class Customer(
    val id: String,
    val namaDepan: String,
    val namaBelakang: String,
    val email: String,
    val saldo: Int,
    val provider: String = "local",
    val accessToken: String = ""
) {
    fun inisial(): String {
        val depan = namaDepan.firstOrNull()?.uppercaseChar() ?: ' '
        val belakang = namaBelakang.firstOrNull()?.uppercaseChar()
        return if (belakang != null) "$depan$belakang" else namaDepan.take(2).uppercase()
    }
}

object CustomerRepository {
    private const val PREF_NAME = "customer_prefs"
    private var userAktif: Customer? = null

    suspend fun daftar(context: Context, namaDepan: String, namaBelakang: String, email: String, password: String): Customer {
        val bodyJson = JSONObject().apply {
            put("email", email)
            put("password", password)
            put("data", JSONObject().apply {
                put("nama_depan", namaDepan)
                put("nama_belakang", namaBelakang)
            })
        }.toString()

        val hasil = JSONObject(SupabaseConfig.postAuth("signup", bodyJson))
        val userId = hasil.getJSONObject("user").getString("id")
        val accessToken = hasil.getString("access_token")

        // ⬅️ saldo baru = 0 (nilai default kolom saldo di tabel profiles, sesuai skema)
        val customer = Customer(id = userId, namaDepan = namaDepan, namaBelakang = namaBelakang, email = email, saldo = 0, accessToken = accessToken)
        simpanSesi(context, customer)
        return customer
    }

    suspend fun signIn(context: Context, email: String, password: String): Customer {
        val bodyJson = JSONObject().apply {
            put("email", email)
            put("password", password)
        }.toString()

        val hasil = JSONObject(SupabaseConfig.postAuth("token?grant_type=password", bodyJson))
        val userId = hasil.getJSONObject("user").getString("id")
        val accessToken = hasil.getString("access_token")

        // ⬅️ Ambil data profil asli (nama, saldo) pakai token milik user ini (bukan anon key),
        // karena RLS di tabel profiles cuma izinin user baca baris miliknya sendiri
        val profilJson = SupabaseConfig.get("profiles?id=eq.$userId&select=*", accessToken)
        val profil = org.json.JSONArray(profilJson).getJSONObject(0)

        val customer = Customer(
            id = userId,
            namaDepan = profil.optString("nama_depan"),
            namaBelakang = profil.optString("nama_belakang"),
            email = email,
            saldo = profil.optInt("saldo"),
            accessToken = accessToken
        )
        simpanSesi(context, customer)
        return customer
    }

    fun getUserAktif(context: Context): Customer? {
        if (userAktif == null) {
            userAktif = muatSesi(context)
        }
        return userAktif
    }

    fun logout(context: Context) {
        userAktif = null
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }

    private fun simpanSesi(context: Context, customer: Customer) {
        userAktif = customer
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().apply {
            putString("id", customer.id)
            putString("namaDepan", customer.namaDepan)
            putString("namaBelakang", customer.namaBelakang)
            putString("email", customer.email)
            putInt("saldo", customer.saldo)
            putString("provider", customer.provider)
            putString("accessToken", customer.accessToken)
            apply()
        }
    }

    private fun muatSesi(context: Context): Customer? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val id = prefs.getString("id", null) ?: return null
        return Customer(
            id = id,
            namaDepan = prefs.getString("namaDepan", "") ?: "",
            namaBelakang = prefs.getString("namaBelakang", "") ?: "",
            email = prefs.getString("email", "") ?: "",
            saldo = prefs.getInt("saldo", 0),
            provider = prefs.getString("provider", "local") ?: "local",
            accessToken = prefs.getString("accessToken", "") ?: ""
        )
    }
}
