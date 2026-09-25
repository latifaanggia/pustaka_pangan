# Known Limitations

Dokumen ini mencatat bagian-bagian dari Pustaka Pangan yang **masih simulasi/dummy**, kenapa dibuat begitu, dan apa yang dibutuhkan untuk membuatnya jadi implementasi sungguhan. Tujuannya supaya jelas mana yang "belum sempat dikerjakan" vs mana yang **memang sengaja** dibuat sederhana dulu untuk fase prototipe.

> Prinsip project ini: **functionality first, polish later** — banyak alur di bawah sudah benar secara *flow* dan *UI*, tapi datanya belum tersambung ke sistem sungguhan.

## Autentikasi & Sesi

| Bagian | Kondisi Saat Ini | Yang Dibutuhkan untuk Produksi |
|---|---|---|
| SSO Google | Simulasi — tombol langsung dianggap berhasil tanpa memanggil Google Sign-In SDK sama sekali | Integrasi Firebase Authentication / Google Identity Services, verifikasi token di server |
| Password | Tidak divalidasi ke mana pun, tidak di-*hash*, tidak disimpan permanen | Backend dengan hashing (bcrypt/argon2), endpoint login sungguhan |
| Data akun terdaftar | `CustomerRepository` menyimpan akun yang baru Register **hanya selama proses aplikasi masih hidup** — kalau aplikasi di-*force close*, daftar akun ini hilang. Data user yang **sedang login** tetap tersimpan (lewat `SharedPreferences`), tapi kalau logout lalu Sign In lagi pakai email yang sama setelah aplikasi sempat di-*kill*, sistem tidak akan mengenali itu sebagai akun lama | Database sungguhan (Supabase/API) untuk menyimpan seluruh akun secara permanen |
| Reset/Ubah Password | Halaman `UbahPasswordActivity` ada, tapi belum diverifikasi apakah sudah tersambung ke `CustomerRepository` atau masih statis | Perlu ditinjau ulang saat masuk fase ini |

## Data Majalah & Repository

| Bagian | Kondisi Saat Ini | Yang Dibutuhkan untuk Produksi |
|---|---|---|
| Sumber data majalah | `MajalahRepository` — daftar majalah hardcode di kode Kotlin (`listOf(...)`), bukan dari API/database | Ganti isi `MajalahRepository` untuk memanggil API/Supabase, tanpa perlu mengubah Activity pemanggilnya |
| Cover & Pratinjau Editorial | Gambar cover disimpan sebagai resource `drawable` di dalam APK. Cuma **FRI Vol 07** yang punya aset pratinjau multi-halaman asli (`_hal2` s/d `_hal6`); majalah lain fallback menampilkan cover-nya saja sebagai satu halaman pratinjau | Cover & halaman pratinjau di-upload ke object storage (Supabase Storage/S3), field data berubah dari resource ID (`Int`) ke URL (`String`) |
| File PDF | PDF sudah di Supabase Storage (bucket `pdf-majalah`, dikompres ±52% pakai Ghostscript `/ebook`). Tapi bucket masih **public** — siapa pun yang tahu URL-nya bisa mengunduh majalah berbayar tanpa membeli. Katalog baru 8 edisi demo (free plan: 1 GB storage, 5 GB egress/bulan) | Bucket private + RLS di `storage.objects` yang cek tabel pembelian + *signed URL* berumur pendek; katalog penuh 2018–2026 (±1,5 GB) butuh Pro plan atau `url_pdf` diarahkan ke server pustakapangan.com |
| Menambah majalah baru | Harus edit kode (`MajalahRepository.kt`) dan build ulang APK | Idealnya cukup lewat panel admin/CMS di sisi backend, tanpa update aplikasi |

## Fitur Unduh (Baca Offline)

Sudah **benar-benar mengunduh** lewat `PdfDownloader` (stream per 64 KB, file `.tmp` lalu rename atomic, cek memori kosong, pesan error untuk offline/timeout/404). Baca online disimpan di **cache** (`cacheDir`), unduhan offline di **local storage** (`filesDir`) sesuai arahan mentor; status "sudah diunduh" dicek dari keberadaan file, bukan flag `SharedPreferences`. Batasan yang tersisa:
- Unduhan terikat ke layar (`lifecycleScope`): keluar dari layar atau **rotasi layar** membatalkan unduhan dan harus diulang. Produksi: `WorkManager` (tetap jalan di background + notifikasi progres) dan resume unduhan dengan header HTTP `Range`
- Layout Koleksi masih hardcode 3 item (Vol 07/06/05); tombol unduh Vol 07 belum punya ID. Produksi: `RecyclerView` dari data pembelian user
- Belum ada fitur hapus unduhan / kelola penyimpanan
- Kompresi dilakukan manual sekali di sisi admin sebelum upload, belum otomatis di pipeline upload

## Top Up & Pembayaran

| Bagian | Kondisi Saat Ini | Yang Dibutuhkan untuk Produksi |
|---|---|---|
| Metode pembayaran | Tidak ada integrasi payment gateway sungguhan (BCA/QRIS). Alur konfirmasi sepenuhnya manual lewat WhatsApp ke admin | Integrasi payment gateway (Midtrans/Xendit dll) untuk verifikasi otomatis |
| Update saldo | Setelah top up "berhasil", saldo user **tidak otomatis bertambah** — belum ada mekanisme approval dari sisi admin yang mengubah `saldo` di `CustomerRepository`/backend | Endpoint approval di backend, yang men-trigger update saldo user |
| Riwayat Top Up | Tersimpan di `TopUpRepository` (dummy, di memori) — hilang saat aplikasi di-*force close* kecuali sedang login (behaviornya sama seperti keterbatasan Customer di atas) | Simpan riwayat ke database sungguhan |

## Notifikasi

- Konten notifikasi **statis/hardcode** — cuma ada 1 kartu contoh (Vol 07 terbaru), bukan digenerate otomatis dari kejadian nyata (majalah baru terbit, top up dikonfirmasi, dsb)
- **Tidak ada tabel `notifikasi` di skema database asli** (sudah dicek langsung ke SQL dump) — artinya fitur ini murni buatan sisi Android, belum punya "rumah" di backend
- Badge merah di ikon lonceng sudah sinkron dengan status baca (lewat `NotifikasiState` + `SharedPreferences`), tapi ini baru status "ada/tidak ada yang belum dibaca" secara global, bukan per-notifikasi individual
- Tidak ada push notification (Firebase Cloud Messaging) — notifikasi hanya muncul kalau user membuka halaman Notifikasi secara manual

## Langganan (Subscription)

`LanggananRepository` sudah dibuat (struktur datanya niru tabel `langganan` di database asli), tapi **belum disambungkan ke Activity mana pun**. Alasannya: belum ada desain UI untuk kondisi "user belum berlangganan" (empty state) di `KoleksiActivity`. Menyambungkan Repository ini tanpa desain yang jelas berisiko menghasilkan tampilan yang aneh atau menutupi bug lain.

## Arsitektur Kode

- Semua logic (network call di masa depan, validasi, state) masih langsung di dalam `Activity` — belum ada pemisahan `ViewModel`/`Repository` ala arsitektur MVVM yang sesungguhnya (yang ada sekarang baru lapisan Repository untuk data, belum `ViewModel`)
- Masih pakai `findViewById` manual di semua tempat, belum `ViewBinding`
- Belum ada unit test maupun UI test sama sekali
- SSO, validasi password, dan hal-hal sensitif lain belum melalui pertimbangan keamanan (enkripsi data di `SharedPreferences`, dsb) karena memang belum ada data sungguhan yang perlu diamankan di tahap ini
