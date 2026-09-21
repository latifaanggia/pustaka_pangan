package com.example.pustakapangan

import android.content.Context

data class Customer(
    val id: Int,
    val namaDepan: String,   // ⬅️ firstname
    val namaBelakang: String,// ⬅️ lastname
    val email: String,
    val saldo: Int,          // ⬅️ current_balance
    val provider: String = "local" // ⬅️ "local"
) {
    fun inisial(): String {
        val depan = namaDepan.firstOrNull()?.uppercaseChar() ?: ' '
        val belakang = namaBelakang.firstOrNull()?.uppercaseChar()
        return if (belakang != null) "$depan$belakang" else namaDepan.take(2).uppercase()
    }
}

object CustomerRepository {
    private const val PREF_NAME = "customer_prefs"
    private val daftarCustomer = mutableMapOf<String, Customer>() // key: email
    private var userAktif: Customer? = null

    fun daftarkanCustomerBaru(context: Context, customer: Customer) {
        daftarCustomer[customer.email] = customer
        simpanSesi(context, customer)
    }

    fun signIn(context: Context, email: String, dataDummyFallback: Customer): Customer {
        val customer = daftarCustomer[email] ?: dataDummyFallback
        simpanSesi(context, customer)
        return customer
    }

    fun setUserAktif(context: Context, customer: Customer) {
        simpanSesi(context, customer)
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
            putInt("id", customer.id)
            putString("namaDepan", customer.namaDepan)
            putString("namaBelakang", customer.namaBelakang)
            putString("email", customer.email)
            putInt("saldo", customer.saldo)
            putString("provider", customer.provider)
            apply()
        }
    }

    private fun muatSesi(context: Context): Customer? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val email = prefs.getString("email", null) ?: return null
        return Customer(
            id = prefs.getInt("id", 1),
            namaDepan = prefs.getString("namaDepan", "") ?: "",
            namaBelakang = prefs.getString("namaBelakang", "") ?: "",
            email = email,
            saldo = prefs.getInt("saldo", 0),
            provider = prefs.getString("provider", "local") ?: "local"
        )
    }
}