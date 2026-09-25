-- Migration: topup_confirmation_and_notifikasi (diterapkan 25 Sep 2026 via Supabase)
-- Alur: admin ubah status topup di Dashboard (Menunggu Konfirmasi -> Berhasil/Ditolak) -> trigger tambah saldo + kirim notifikasi

-- 1. Tabel notifikasi (per user)
create table public.notifikasi (
  id serial primary key,
  customer_id uuid not null references public.profiles(id) on delete cascade,
  tipe text not null check (tipe in ('edisi_baru','topup_berhasil','topup_ditolak','selamat_datang')),
  judul text not null,
  pesan text not null,
  sudah_dibaca boolean not null default false,
  created_at timestamptz not null default now()
);
create index on public.notifikasi (customer_id, created_at desc);
alter table public.notifikasi enable row level security;
create policy "User bisa lihat notifikasi sendiri" on public.notifikasi for select to authenticated using (auth.uid() = customer_id);
create policy "User bisa tandai notifikasi sendiri dibaca" on public.notifikasi for update to authenticated using (auth.uid() = customer_id);
revoke insert, update, delete on public.notifikasi from anon, authenticated;
grant update (sudah_dibaca) on public.notifikasi to authenticated; -- user cuma boleh ubah status dibaca

create or replace function public.format_rupiah(n integer) returns text language sql immutable set search_path = '' as
$$ select 'Rp ' || replace(to_char(n, 'FM999,999,999,999'), ',', '.') $$;

-- 2. Konfirmasi top up oleh admin: saldo + notifikasi otomatis
create or replace function public.proses_konfirmasi_topup() returns trigger language plpgsql security definer set search_path = '' as $$
declare v_metode text := case when new.metode = 'BCA' then 'BCA Transfer' else new.metode end;
begin
  if new.customer_id <> old.customer_id or new.nominal <> old.nominal then raise exception 'Customer dan nominal top up tidak boleh diubah'; end if;
  if new.status = old.status then return new; end if;
  if old.status <> 'Menunggu Konfirmasi' then raise exception 'Top up berstatus % sudah final dan tidak bisa diubah', old.status; end if;
  if new.status = 'Berhasil' then
    update public.profiles set saldo = saldo + new.nominal where id = new.customer_id;
    insert into public.notifikasi (customer_id, tipe, judul, pesan) values (new.customer_id, 'topup_berhasil', 'Top Up Saldo Berhasil',
      'Top up saldo sebesar ' || public.format_rupiah(new.nominal) || ' via ' || v_metode || ' telah dikonfirmasi.');
  elsif new.status = 'Ditolak' then
    insert into public.notifikasi (customer_id, tipe, judul, pesan) values (new.customer_id, 'topup_ditolak', 'Top Up Saldo Ditolak',
      'Top up saldo sebesar ' || public.format_rupiah(new.nominal) || ' via ' || v_metode || ' ditolak. Hubungi admin via WhatsApp jika kamu sudah melakukan transfer.');
  else raise exception 'Status tidak valid: % (pilih Berhasil atau Ditolak)', new.status;
  end if;
  return new;
end $$;
create trigger on_topup_dikonfirmasi before update on public.topup for each row execute function public.proses_konfirmasi_topup();

-- 3. Notifikasi selamat datang saat profil dibuat
create or replace function public.notif_selamat_datang() returns trigger language plpgsql security definer set search_path = '' as $$
begin
  insert into public.notifikasi (customer_id, tipe, judul, pesan) values (new.id, 'selamat_datang', 'Selamat Datang di FoodReview!',
    'Akun kamu berhasil didaftarkan. Nikmati kemudahan membaca majalah pangan.');
  return new;
end $$;
create trigger on_profile_dibuat after insert on public.profiles for each row execute function public.notif_selamat_datang();

-- 4. Notifikasi edisi baru ke semua user saat majalah ditambahkan
create or replace function public.notif_edisi_baru() returns trigger language plpgsql security definer set search_path = '' as $$
begin
  insert into public.notifikasi (customer_id, tipe, judul, pesan)
  select id, 'edisi_baru', 'Edisi Terbaru Telah Terbit!', 'FoodReview ' || new.judul || ' sudah bisa kamu baca sekarang.' from public.profiles;
  return new;
end $$;
create trigger on_majalah_terbit after insert on public.majalah for each row execute function public.notif_edisi_baru();

revoke execute on function public.proses_konfirmasi_topup(), public.notif_selamat_datang(), public.notif_edisi_baru() from public, anon, authenticated;

-- 5. Backfill: user lama dapat notif selamat datang (tanggal = tanggal daftar)
insert into public.notifikasi (customer_id, tipe, judul, pesan, created_at)
select id, 'selamat_datang', 'Selamat Datang di FoodReview!', 'Akun kamu berhasil didaftarkan. Nikmati kemudahan membaca majalah pangan.', created_at from public.profiles;
