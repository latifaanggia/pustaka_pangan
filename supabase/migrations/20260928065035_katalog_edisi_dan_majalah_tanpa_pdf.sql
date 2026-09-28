-- 1. Kolom edisi (nomor terbitan 1-12, angka setelah "/" di judul). Urutan majalah pakai (tahun, edisi), BUKAN id.
--    id tetap nomor unik otomatis (serial) yang gak perlu sama dengan nomor edisi.
alter table public.majalah add column edisi integer;
update public.majalah set edisi = (regexp_match(judul, '/(\d+)'))[1]::integer;
alter table public.majalah alter column edisi set not null,
  add constraint majalah_edisi_check check (edisi between 1 and 12),
  add constraint majalah_tahun_edisi_unik unique (tahun, edisi); -- cegah edisi dobel saat input massal

-- 2. Notifikasi "Edisi Terbaru" hanya untuk edisi yang benar-benar TERBARU dan sudah ada PDF-nya
--    (supaya input katalog lama 2020-2024 gak mengirim puluhan notifikasi ke semua user)
create or replace function public.notif_edisi_baru() returns trigger language plpgsql security definer set search_path = '' as $$
begin
  if coalesce(new.url_pdf, '') = '' then return new; end if;
  if exists (select 1 from public.majalah where id <> new.id and (tahun, edisi) >= (new.tahun, new.edisi)) then return new; end if;
  insert into public.notifikasi (customer_id, tipe, judul, pesan)
  select id, 'edisi_baru', 'Edisi Terbaru Telah Terbit!', 'FoodReview ' || new.judul || ' sudah bisa kamu baca sekarang.' from public.profiles;
  return new;
end $$;

-- 3. Majalah yang baru ada cover-nya (url_pdf kosong) tidak bisa dibeli
create or replace function public.beli_majalah(p_majalah_id integer)
returns json language plpgsql security definer set search_path = '' as $$
declare v_uid uuid := auth.uid(); v_harga integer; v_pdf text; v_saldo integer; v_id integer;
begin
  if v_uid is null then raise exception 'Silakan login terlebih dahulu'; end if;
  select harga, url_pdf into v_harga, v_pdf from public.majalah where id = p_majalah_id;
  if not found then raise exception 'Majalah tidak ditemukan'; end if;
  if coalesce(v_pdf, '') = '' then raise exception 'Versi digital majalah ini belum tersedia'; end if;
  if exists (select 1 from public.pembelian where customer_id = v_uid and majalah_id = p_majalah_id) then raise exception 'Majalah sudah kamu beli'; end if;
  update public.profiles set saldo = saldo - v_harga where id = v_uid and saldo >= v_harga returning saldo into v_saldo;
  if not found then raise exception 'Saldo tidak cukup, silakan top up terlebih dahulu'; end if;
  insert into public.pembelian (customer_id, majalah_id, harga) values (v_uid, p_majalah_id, v_harga) returning id into v_id;
  return json_build_object('pembelian_id', v_id, 'saldo_baru', v_saldo);
exception when unique_violation then raise exception 'Majalah sudah kamu beli';
end $$;
revoke execute on function public.beli_majalah(integer) from public, anon;
grant execute on function public.beli_majalah(integer) to authenticated;
