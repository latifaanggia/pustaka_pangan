package com.example.pustakapangan

data class Customer(
    val id: Int,
    val namaDepan: String,   // ⬅️ firstname
    val namaBelakang: String,// ⬅️ lastname
    val email: String,
    val saldo: Int,          // ⬅️ current_balance
    val provider: String = "local" // ⬅️ "local" (daftar manual) atau "google" (SSO) — kolom ini custom, bukan dari tabel asli
) {
    fun inisial(): String {
        val depan = namaDepan.firstOrNull()?.uppercaseChar() ?: ' '
        val belakang = namaBelakang.firstOrNull()?.uppercaseChar()
        return if (belakang != null) "$depan$belakang" else namaDepan.take(2).uppercase()
    }
}

object CustomerRepository {
    private val daftarCustomer = mutableMapOf<String, Customer>() // key: email
    private var userAktif: Customer? = null

    fun daftarkanCustomerBaru(customer: Customer) {
        daftarCustomer[customer.email] = customer
        userAktif = customer
    }

    fun signIn(email: String, dataDummyFallback: Customer): Customer {
        val customer = daftarCustomer[email] ?: dataDummyFallback
        userAktif = customer
        return customer
    }

    fun setUserAktif(customer: Customer) {
        userAktif = customer
    }

    fun getUserAktif(): Customer? = userAktif

    fun logout() {
        userAktif = null
    }
}