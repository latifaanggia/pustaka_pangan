# Pustaka Pangan

Aplikasi Android native (Kotlin) untuk membeli dan membaca majalah digital FOODREVIEW Indonesia (FOODREVIEW Indonesia, Kulinologi Indonesia, Food For Kids Indonesia). Dikembangkan sebagai prototipe fungsional dalam program PKL (Praktik Kerja Lapangan).

> **Status: Prototipe fungsional v1.0 (29/09/2026)**, lolos regression testing (77 dari 78 skenario).
> Aplikasi sudah terhubung ke **backend sungguhan (Supabase)**: akun, saldo, top up, pembelian, notifikasi, serta file cover dan PDF majalah tersimpan di server.
> Database Supabase ini **terpisah dari database web pustakapangan.com**. Strukturnya meniru tabel web, tetapi belum berisi data pelanggan asli, jadi akun yang terdaftar di web belum bisa dipakai login. Rencana sinkronisasi ada di bagian [Rencana Pengembangan](#rencana-pengembangan).

## Fitur

| Fitur | Keterangan |
|---|---|
| **Autentikasi** | Register & Sign In via Supabase Auth (password di-*hash* di server), sesi diperpanjang otomatis dengan refresh token, Ubah Password (verifikasi password lama). Pengunjung (*guest*) hanya bisa melihat katalog, detail, dan pratinjau |
| **Katalog** | Beranda (Terbaru, Populer berdasarkan jumlah pembeli, Gratis), halaman Majalah per tahun 2020–2026 dengan chip tahun, Hero banner, Detail Majalah dengan Pratinjau Editorial 6 halaman |
| **Top Up Saldo** | BCA / QRIS, validasi nominal (minimal Rp10.000), konfirmasi via WhatsApp. Saldo bertambah **setelah admin mengonfirmasi**, lalu pengguna mendapat notifikasi |
| **Pembelian** | Saldo dipotong lewat fungsi database atomik (`beli_majalah`); tidak bisa beli dua kali; majalah gratis langsung masuk Koleksi |
| **Koleksi** | Majalah yang dibeli, dikelompokkan per tahun, kartu "Terakhir Dibaca" yang melanjutkan halaman terakhir |
| **Unduh & Baca Offline** | Unduh PDF ke penyimpanan HP (progres %, hapus unduhan, cek memori). Katalog dan Koleksi tetap bisa dibuka tanpa internet |
| **E-Reader** | `PdfRenderer`: pinch-to-zoom ke titik jari, geser halaman, layar penuh, lompat halaman, status Online / Offline / Offline · Belum Diunduh |
| **Notifikasi** | Dibuat otomatis oleh database (top up berhasil/ditolak, edisi baru, selamat datang). Tampil di halaman Notifikasi, badge lonceng, dan pop-up di dalam aplikasi |

## Tech Stack

- **Aplikasi**: Kotlin, XML Layout + Material Components, Coroutines, Glide · Min SDK 24 · Target SDK 37
- **Backend**: [Supabase](https://supabase.com) (PostgreSQL + Auth + Storage), diakses lewat REST API (`HttpURLConnection`, tanpa SDK tambahan)
- **Keamanan data**: Row Level Security (RLS), izin per kolom, fungsi `SECURITY DEFINER`, trigger, dan check constraint

## Arsitektur

```
Activity (UI)
   │
   ▼
Repository  ── satu pintu akses data per domain (Customer, Majalah, TopUp, Pembelian, Notifikasi)
   │
   ▼
SupabaseConfig  ── REST (/rest/v1), RPC (/rest/v1/rpc), Auth (/auth/v1)
   │
   ▼
Supabase: PostgreSQL (tabel + RLS + trigger) · Auth · Storage (cover, PDF, pratinjau)

Penyimpanan lokal di HP:
- SharedPreferences → sesi login, salinan katalog & daftar pembelian (mode offline), halaman terakhir dibaca
- filesDir  → PDF yang diunduh untuk baca offline (permanen)
- cacheDir  → PDF yang dibaca online (sementara, bisa dihapus Android)
```

Semua akses data lewat lapisan **Repository**. Kalau sumber data diganti (misalnya ke API web pustakapangan.com), yang diubah cukup Repository, tanpa menyentuh halaman/Activity.

| Repository | Tabel Supabase | Padanan di database web |
|---|---|---|
| `CustomerRepository` | `profiles` + Supabase Auth | `customer` |
| `MajalahRepository` | `majalah` | `product` / view `q_detail_product` |
| `TopUpRepository` | `topup` | `tb_topup` |
| `PembelianRepository` | `pembelian` (via RPC `beli_majalah`) | `tb_buy_product` |
| `NotifikasiRepository` | `notifikasi` | – (baru) |
| `LanggananRepository` | `langganan` | `langganan` *(belum dipakai, belum ada desain UI)* |

## Database (Supabase)

Seluruh skema tersimpan sebagai migrasi di [`supabase/migrations`](./supabase/migrations), dan data katalog (29 edisi) di [`supabase/seed.sql`](./supabase/seed.sql).

- **Tabel**: `profiles`, `majalah`, `topup`, `pembelian`, `notifikasi`, `langganan`
- **Fungsi & trigger**:
    - `beli_majalah`: cek saldo, potong saldo, dan catat pembelian dalam satu transaksi
    - `proses_konfirmasi_topup`: saat admin mengubah status top up, saldo bertambah dan notifikasi terkirim; status final tidak bisa diubah lagi
    - `handle_new_user`, `notif_selamat_datang`, `notif_edisi_baru`, `tambah_jumlah_pembeli`, `tandai_notifikasi_dibaca`
- **Aturan keamanan**: aplikasi tidak bisa mengubah saldo sendiri, tidak bisa membuat top up berstatus Berhasil, tidak bisa melihat data pengguna lain, dan nominal top up ≤ 0 ditolak database
- **Storage**: bucket `cover-majalah`, `pratinjau-majalah`, `pdf-majalah` (PDF dikompres ±50% dengan Ghostscript)

## Cara Menjalankan

**Aplikasi**
1. Clone repo, buka dengan Android Studio, lalu *Sync Project with Gradle Files*
2. Jalankan di HP/emulator (`Run 'app'`). Aplikasi langsung tersambung ke project Supabase yang ada di `SupabaseConfig.kt`
3. Daftar akun baru lewat halaman Register (akun web pustakapangan.com belum bisa dipakai)

**Memasang ulang backend di project Supabase baru**
1. Jalankan semua file di `supabase/migrations` secara berurutan, lalu `supabase/seed.sql`
2. Upload file cover, pratinjau, dan PDF ke bucket yang sesuai (sesuaikan URL di `seed.sql`)
3. Ganti `PROJECT_URL` dan `ANON_KEY` di `SupabaseConfig.kt`

> `ANON_KEY` memang aman berada di aplikasi (dibatasi RLS). **Jangan pernah** memasukkan `service_role` key ke aplikasi atau ke repo ini.

## Panduan Admin (Sementara, lewat Dashboard Supabase)

| Tugas | Cara |
|---|---|
| Konfirmasi top up | Table Editor → `topup` → ubah `status` jadi `Berhasil` atau `Ditolak`. Saldo dan notifikasi diproses otomatis |
| Tambah majalah baru | Upload cover & PDF ke Storage, lalu tambah baris di tabel `majalah`. Semua pengguna otomatis mendapat notifikasi "Edisi Terbaru" |
| Kompres PDF & buat pratinjau | `tools/compress_pdf.bat` dan `tools/buat_pratinjau.bat` (butuh Ghostscript) |

## Struktur Folder

```
app/src/main/java/com/example/pustakapangan/
├── *Activity.kt           → satu Activity per halaman
├── *Repository.kt         → akses data per domain
├── SupabaseConfig.kt      → koneksi REST/Auth/RPC ke Supabase
├── PdfDownloader.kt       → unduh PDF (cache & offline)
├── NotifikasiPopup.kt     → pop-up notifikasi di dalam aplikasi
├── RiwayatBaca.kt         → simpan halaman terakhir dibaca
├── SessionManager.kt      → status login
└── PustakaPanganApp.kt    → Application class
supabase/                  → migrasi skema + seed data
tools/                     → skrip kompresi PDF & pembuat pratinjau
docs/                      → SRS dan dokumen regression testing
```

## Dokumentasi

- [`docs/SRS_Pustaka_Pangan.docx`](./docs/SRS_Pustaka_Pangan.docx): Software Requirements Specification v1.3 beserta Traceability Matrix
- [`docs/testing/`](./docs/testing): Regression testing 21/09/2026 dan 29/09/2026
- [`KNOWN_LIMITATIONS.md`](./KNOWN_LIMITATIONS.md): batasan prototipe dan kebutuhan untuk produksi

## Rencana Pengembangan

1. **Sinkronisasi dengan database web pustakapangan.com (MySQL)** lewat API perantara (PHP): perubahan di Supabase dikirim lewat Database Webhook, perubahan di web dikirim balik ke Supabase, sehingga akun dan transaksi web dapat dipakai di aplikasi. Butuh akses server & database dari tim IT
2. **Bucket PDF privat**: saat ini file PDF masih bisa diakses lewat URL langsung. Rencana: bucket privat + policy berdasarkan data pembelian + unduhan dengan token login
3. SSO Google, push notification saat aplikasi ditutup (FCM), Lupa Password (email reset + deep link), pembatalan top up oleh pengguna, fitur pencarian, dan paket langganan