# Known Limitations

Dokumen ini mencatat bagian-bagian dari Pustaka Pangan yang **masih simulasi/dummy**, kenapa dibuat begitu, dan apa yang dibutuhkan untuk membuatnya jadi implementasi sungguhan. Tujuannya supaya jelas mana yang "belum sempat dikerjakan" vs mana yang **memang sengaja** dibuat sederhana dulu untuk fase prototipe.

> Prinsip project ini: **functionality first, polish later** — banyak alur di bawah sudah benar secara *flow* dan *UI*, tapi datanya belum tersambung ke sistem sungguhan.

## Autentikasi & Sesi

| Bagian | Kondisi Saat Ini | Yang Dibutuhkan untuk Produksi |
|---|---|---|
| Register & Sign In email | **Sudah sungguhan** lewat Supabase Auth (password di-*hash* oleh Supabase). Access token (berlaku 1 jam) diperpanjang otomatis pakai refresh token (`CustomerRepository.getTokenValid`) | Enkripsi token di HP (`EncryptedSharedPreferences`), verifikasi email |
| SSO Google | Belum tersedia — tombol menampilkan pesan "SSO Google belum tersedia di prototipe ini" | Google Identity Services + provider Google di Supabase Auth |
| Ubah Password | **Sudah berfungsi** — password lama diverifikasi, password baru disimpan lewat `auth/v1/user` | — |

## Data Majalah & Repository

| Bagian | Kondisi Saat Ini | Yang Dibutuhkan untuk Produksi |
|---|---|---|
| Sumber data majalah | Dari tabel `majalah` di Supabase, cover dari Storage (bucket `cover-majalah`). Salinan terakhir disimpan di HP, jadi app tetap bisa dibuka **offline** | Pagination kalau katalog sudah ratusan edisi |
| Pratinjau Editorial | Halaman 1–6 dibuat dari PDF (`tools/buat_pratinjau.bat`) dan di-upload ke bucket `pratinjau-majalah`; majalah tanpa PDF menampilkan cover | Otomatis dibuat server saat PDF baru di-upload |
| File PDF | PDF di Supabase Storage (bucket `pdf-majalah`, dikompres ±52% pakai Ghostscript `/ebook`). Tapi bucket masih **public** — siapa pun yang tahu URL-nya bisa mengunduh majalah berbayar tanpa membeli. Katalog 29 edisi (2020–2026), 14 di antaranya sudah punya PDF (free plan: 1 GB storage, 5 GB egress/bulan). Ditunda setelah demo (keputusan 29/09) | Bucket private + RLS di `storage.objects` yang cek tabel `pembelian` + *signed URL* berumur pendek; katalog penuh 2018–2026 (±1,5 GB) butuh Pro plan atau `url_pdf` diarahkan ke server pustakapangan.com |
| Menambah majalah baru | Lewat Supabase Table Editor (tanpa build ulang APK); semua user otomatis dapat notifikasi "Edisi Terbaru" | Panel admin/CMS dengan form upload cover + PDF |

## Koleksi & Fitur Unduh (Baca Offline)

Koleksi menampilkan majalah yang **benar-benar dibeli** user (tabel `pembelian`), dikelompokkan per tahun, plus kartu "Terakhir Dibaca" yang membuka E-Reader di halaman terakhir. Unduhan lewat `PdfDownloader` (stream per 64 KB, file `.tmp` lalu rename atomic, cek memori kosong). Baca online disimpan di **cache** (`cacheDir`), unduhan offline di **local storage** (`filesDir`) sesuai arahan mentor. Daftar pembelian ikut disimpan di HP, jadi Koleksi tetap bisa dibuka offline. Batasan yang tersisa:
- Unduhan terikat ke layar (`lifecycleScope`): keluar dari Koleksi atau **rotasi layar** membatalkan unduhan. Produksi: `WorkManager` (jalan di background + notifikasi progres) dan resume unduhan dengan header HTTP `Range`
- File unduhan tetap tersimpan di HP setelah logout (tidak terlihat oleh akun lain karena Koleksi difilter per pembelian). Produksi: hapus unduhan saat logout atau enkripsi file per akun
- Posisi halaman terakhir disimpan lokal di HP, belum tersinkron antar perangkat
- Kompresi dilakukan manual sekali di sisi admin sebelum upload, belum otomatis di pipeline upload

## Top Up, Pembelian & Pembayaran

| Bagian | Kondisi Saat Ini | Yang Dibutuhkan untuk Produksi |
|---|---|---|
| Metode pembayaran | Transfer BCA/QRIS manual, bukti dikirim lewat WhatsApp ke admin | Payment gateway (Midtrans/Xendit): Virtual Account/QRIS dinamis + *webhook* supaya saldo masuk otomatis dalam hitungan detik |
| Konfirmasi top up | Admin mengubah `status` di Supabase Dashboard (`Berhasil`/`Ditolak`); trigger database otomatis menambah saldo + mengirim notifikasi. Status final tidak bisa diubah lagi, nominal tidak bisa diedit | Akses Dashboard = "kunci master" database, tidak cocok untuk banyak staf. Produksi: panel admin khusus dengan role (tombol Terima/Tolak saja) + kode unik nominal transfer |
| Keamanan saldo | Client **tidak bisa** mengubah saldo (RLS + grant per kolom). Top up dari app wajib berstatus "Menunggu Konfirmasi" | — |
| Pembatalan top up | User belum bisa membatalkan sendiri top up yang salah pencet; solusinya admin mengubah status jadi `Ditolak`. Nominal 0/di bawah Rp10.000 sudah ditolak di app, dan nominal ≤ 0 juga ditolak database (`topup_nominal_positif`) | Status baru `Dibatalkan` + fungsi RPC `batalkan_topup` (hanya pemilik & hanya saat "Menunggu Konfirmasi") + tombol Batalkan di Riwayat Top Up |
| Pembelian majalah | Lewat fungsi database `beli_majalah` (cek saldo, potong saldo, catat pembelian dalam **1 transaksi atomik**); majalah yang sama tidak bisa dibeli dua kali | Fitur refund/pembatalan, riwayat pembelian untuk user |

## Notifikasi

Notifikasi disimpan di tabel `notifikasi` dan dibuat **otomatis oleh trigger database** (top up dikonfirmasi/ditolak, edisi baru ditambahkan, akun baru terdaftar). Badge lonceng dihitung dari notifikasi belum dibaca. Selama app terbuka, notifikasi baru muncul sebagai **banner pop-up in-app** (dicek tiap 30 detik, berhenti otomatis saat app di background). Batasan yang tersisa:
- **Belum ada push notification** saat app ditutup (Firebase Cloud Messaging). Sudah didiskusikan dengan mentor: ditunda karena butuh setup Firebase + Edge Function + izin notifikasi Android 13, dan waktu PKL tersisa 2–3 hari. Fondasinya (tabel + trigger) sudah siap: tinggal kirim tiap baris baru ke FCM lewat Database Webhook → Edge Function
- Banner pop-up memakai *polling* (cek berkala), bukan real-time. Produksi: Supabase Realtime atau FCM
- Notifikasi "Edisi Terbaru" ditulis 1 baris per user (fan-out) — cukup untuk prototype, tapi untuk ribuan user sebaiknya tabel broadcast + status baca terpisah
- Hanya 50 notifikasi terbaru yang dimuat (belum ada pagination)

## Akun & Autentikasi

- **Ubah Password** sudah berfungsi: password lama diverifikasi dulu, lalu password baru disimpan ke Supabase Auth
- **Lupa Password belum tersedia** (tombol hanya menampilkan info). Produksi: `auth/v1/recover` mengirim email reset + *deep link* kembali ke app untuk mengisi password baru
- **SSO Google belum tersedia** (butuh setup Google Cloud Console + provider Google di Supabase)

## Langganan (Subscription)

`LanggananRepository` sudah dibuat (struktur datanya niru tabel `langganan` di database asli), tapi **belum disambungkan ke Activity mana pun**. Alasannya: belum ada desain UI untuk kondisi "user belum berlangganan" (empty state) di `KoleksiActivity`. Menyambungkan Repository ini tanpa desain yang jelas berisiko menghasilkan tampilan yang aneh atau menutupi bug lain.

## Arsitektur Kode

- Semua logic (network call di masa depan, validasi, state) masih langsung di dalam `Activity` — belum ada pemisahan `ViewModel`/`Repository` ala arsitektur MVVM yang sesungguhnya (yang ada sekarang baru lapisan Repository untuk data, belum `ViewModel`)
- Masih pakai `findViewById` manual di semua tempat, belum `ViewBinding`
- Belum ada unit test maupun UI test sama sekali
- SSO, validasi password, dan hal-hal sensitif lain belum melalui pertimbangan keamanan (enkripsi data di `SharedPreferences`, dsb) karena memang belum ada data sungguhan yang perlu diamankan di tahap ini

## Data & Integrasi dengan Web pustakapangan.com

Database Supabase ini **terpisah** dari database web (MySQL). Struktur tabelnya meniru tabel web (`customer`, `tb_topup`, `tb_buy_product`, `product`), tetapi belum berisi data pelanggan asli, sehingga akun web belum bisa dipakai login di aplikasi.

Rencana dari mentor (30/09): **API perantara (PHP)** di server kantor untuk sinkronisasi dua arah — Supabase mengirim perubahan lewat Database Webhook, perubahan di MySQL dikirim balik ke Supabase. Hal yang perlu diputuskan saat implementasi:
- MySQL tidak bisa memanggil API langsung dari trigger → arah web ke Supabase memakai cron job atau dipanggil dari kode web
- Penanda asal data supaya tidak terjadi loop sinkronisasi bolak-balik
- Yang disinkronkan adalah transaksi (top up, pembelian), bukan angka saldo, supaya saldo tidak bentrok
- Password akun web memakai format hash lama yang tidak didukung Supabase Auth → login akun lama diverifikasi lewat API perantara
- Pemetaan ID: `customer_id` (angka) di web ↔ UUID di Supabase
- Butuh akses server & database dari tim IT
