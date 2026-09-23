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
| File PDF | Nama file PDF di `MajalahRepository` (`namaFilePdf`) itu string biasa, **bukan** link ke file yang benar-benar ada — belum ada mekanisme upload/unduh PDF sungguhan | Sama seperti cover: dipindah ke object storage, field berubah jadi URL |
| Menambah majalah baru | Harus edit kode (`MajalahRepository.kt`) dan build ulang APK | Idealnya cukup lewat panel admin/CMS di sisi backend, tanpa update aplikasi |

## Fitur Unduh (Baca Offline)

`KoleksiActivity` saat ini **tidak benar-benar mengunduh file apa pun**. Tombol "Unduh" cuma mengubah nilai boolean (`is_offline_vol06`) di `SharedPreferences` — status "sudah diunduh" itu murni tampilan, bukan file PDF yang tersimpan di penyimpanan HP.

Sesuai arahan mentor project ini, implementasi sungguhan nanti perlu:
- Menyimpan file PDF ke **local storage** (`context.filesDir`), bukan cache — supaya tidak terhapus otomatis oleh sistem saat penyimpanan HP penuh
- Kemungkinan kompresi ukuran file di **sisi server** saat admin upload, bukan di sisi Android

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
