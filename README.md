# Pustaka Pangan

Aplikasi Android native (Kotlin) untuk membaca majalah digital FoodReview Indonesia — versi prototipe/slicing yang dikembangkan sebagai bagian dari PKL (Praktik Kerja Lapangan).

> Status: **Prototipe UI/UX dengan data dummy**. Belum terhubung ke backend/API sungguhan. Lihat [Known Limitations](#known-limitations) untuk detail lengkap apa yang masih simulasi.

## Tech Stack

- **Bahasa**: Kotlin
- **UI**: XML Layout + Material Design Components (`MaterialCardView`, `MaterialButton`, `TextInputLayout`)
- **Min SDK**: 24 · **Target SDK**: 37
- **Library utama**: RecyclerView, ViewPager2, Material Components

## Cara Menjalankan

1. Clone repo ini
2. Buka dengan Android Studio (Sync Project with Gradle Files)
3. Jalankan di emulator/device (`Run 'app'`)

Tidak ada API key atau konfigurasi tambahan yang dibutuhkan — semua data masih dummy/lokal di dalam kode (lihat [Arsitektur Data](#arsitektur-data)).

## Fitur yang Sudah Berfungsi

| Fitur | Keterangan |
|---|---|
| **Autentikasi** | Sign In, Register (validasi form dinamis), simulasi SSO Google, session guard (guest vs logged-in) |
| **Katalog Majalah** | List per tahun (Beranda, Majalah), Hero banner, Detail Majalah, Pratinjau Editorial |
| **Pembelian** | Bottom Sheet Konfirmasi Pembelian → Dialog Pembelian Berhasil → E-Reader |
| **E-Reader** | `PdfRenderer` custom: zoom, fullscreen toggle, lompat halaman, status offline/online |
| **Koleksi** | Baca & unduh majalah, status unduhan tersimpan permanen |
| **Top Up Saldo** | Pilih metode (BCA/QRIS), pilih nominal, submit → tercatat di Riwayat |
| **Riwayat Top Up** | List dinamis, konfirmasi via WhatsApp dengan template otomatis per transaksi |
| **Notifikasi** | Badge dot sinkron di semua halaman, status baca tersimpan |
| **Akun** | Profil dengan inisial avatar, saldo, logout |

## Arsitektur Data

Karena belum ada backend, semua data "sumber kebenaran" dipusatkan lewat pola **Repository** — supaya nanti kalau backend/Supabase sudah siap, cukup ganti isi Repository tanpa mengubah Activity yang memakainya.

```
Activity  →  Repository (sumber kebenaran)  →  saat ini: data dummy di kode
                                              →  nanti: API/Supabase
```

| Repository | Niru Tabel/View di Database Asli | Dipakai di |
|---|---|---|
| `MajalahRepository` | view `q_detail_product` | Home, Majalah, Detail Majalah, Koleksi |
| `CustomerRepository` | tabel `customer` | Sign In, Register, Akun, Top Up, Detail Majalah |
| `TopUpRepository` | tabel `tb_topup` | Top Up, Riwayat Top Up |
| `LanggananRepository` | tabel `langganan` | *(belum dipakai — lihat Known Limitations)* |

Session login (`SessionManager`) dan status baca notifikasi (`NotifikasiState`) disimpan permanen ke `SharedPreferences`, sedangkan data Customer & Majalah dari Repository disimpan di memori **dan** `SharedPreferences` (untuk `CustomerRepository`) supaya bertahan walau aplikasi ditutup paksa.

## Struktur Folder

Semua file Kotlin berada flat di satu package (`com.example.pustakapangan`), mengikuti konvensi project ini:

```
app/src/main/java/com/example/pustakapangan/
├── *Activity.kt          → satu Activity per halaman
├── *Repository.kt        → sumber data terpusat per domain
├── SessionManager.kt     → status login (SharedPreferences)
└── NotifikasiState.kt    → status badge notifikasi (SharedPreferences)
```

## Known Limitations

Lihat [`KNOWN_LIMITATIONS.md`](./KNOWN_LIMITATIONS.md) untuk daftar lengkap fitur yang masih simulasi/dummy dan alasannya.

## Testing

Regression testing dilakukan manual mengikuti checklist di [`REGRESSION_TESTING.md`](./REGRESSION_TESTING.md). Jalankan ulang checklist ini setiap ada perubahan besar (migrasi data, penambahan fitur) sebelum push.
