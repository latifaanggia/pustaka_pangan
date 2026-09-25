create table public.profiles (
  id uuid primary key references auth.users(id) on delete cascade,
  nama_depan text,
  nama_belakang text,
  saldo integer not null default 0,
  created_at timestamptz not null default now()
);

create table public.majalah (
  id serial primary key,
  kategori_id integer,
  judul text not null,
  tahun integer,
  harga integer not null,
  url_cover text,
  url_pdf text,
  daftar_isi text
);

create table public.topup (
  id serial primary key,
  customer_id uuid references public.profiles(id),
  nominal integer not null,
  metode text,
  status text not null default 'Menunggu Konfirmasi',
  bukti_transfer_url text,
  tanggal timestamptz not null default now()
);

create table public.langganan (
  id serial primary key,
  topup_id integer references public.topup(id),
  customer_id uuid references public.profiles(id),
  nama_paket text,
  tgl_awal date,
  tgl_akhir date,
  status text default 'Aktif'
);
