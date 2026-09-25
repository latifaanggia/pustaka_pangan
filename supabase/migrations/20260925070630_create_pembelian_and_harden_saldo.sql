-- Migration: create_pembelian_and_harden_saldo (diterapkan 25 Sep 2026 via Supabase)
-- 1. Tabel pembelian (meniru tb_buy_product di database asli)
create table public.pembelian (
  id serial primary key,
  customer_id uuid not null references public.profiles(id) on delete cascade,
  majalah_id integer not null references public.majalah(id),
  harga integer not null check (harga >= 0),
  tanggal timestamptz not null default now(),
  unique (customer_id, majalah_id)
);
alter table public.pembelian enable row level security;
create policy "User bisa lihat pembelian sendiri" on public.pembelian for select to authenticated using (auth.uid() = customer_id);
-- sengaja TIDAK ada policy insert/update/delete: pembelian hanya boleh lewat fungsi beli_majalah

-- 2. Fungsi beli: cek saldo -> potong saldo -> catat pembelian, dalam 1 transaksi
create or replace function public.beli_majalah(p_majalah_id integer)
returns json language plpgsql security definer set search_path = '' as $$
declare v_uid uuid := auth.uid(); v_harga integer; v_saldo integer; v_id integer;
begin
  if v_uid is null then raise exception 'Silakan login terlebih dahulu'; end if;
  select harga into v_harga from public.majalah where id = p_majalah_id;
  if not found then raise exception 'Majalah tidak ditemukan'; end if;
  if exists (select 1 from public.pembelian where customer_id = v_uid and majalah_id = p_majalah_id) then raise exception 'Majalah sudah kamu beli'; end if;
  update public.profiles set saldo = saldo - v_harga where id = v_uid and saldo >= v_harga returning saldo into v_saldo;
  if not found then raise exception 'Saldo tidak cukup, silakan top up terlebih dahulu'; end if;
  insert into public.pembelian (customer_id, majalah_id, harga) values (v_uid, p_majalah_id, v_harga) returning id into v_id;
  return json_build_object('pembelian_id', v_id, 'saldo_baru', v_saldo);
exception when unique_violation then raise exception 'Majalah sudah kamu beli';
end $$;
revoke execute on function public.beli_majalah(integer) from public, anon;
grant execute on function public.beli_majalah(integer) to authenticated;

-- 3. Tutup celah saldo: user hanya boleh ubah nama, bukan saldo
revoke update on public.profiles from anon, authenticated;
grant update (nama_depan, nama_belakang) on public.profiles to authenticated;
drop policy "User bisa bikin profil sendiri saat daftar" on public.profiles; -- profil dibuat trigger handle_new_user
revoke insert on public.profiles from anon, authenticated;

-- 4. Top up dari client wajib berstatus menunggu (yang boleh ubah jadi berhasil hanya admin)
drop policy "User bisa bikin top up sendiri" on public.topup;
create policy "User bisa bikin top up sendiri" on public.topup for insert to authenticated with check (auth.uid() = customer_id and status = 'Menunggu Konfirmasi');
