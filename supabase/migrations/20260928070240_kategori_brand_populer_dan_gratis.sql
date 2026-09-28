-- 1. Kategori = brand majalah: 1 Food Review Indonesia, 2 Kulinologi Indonesia, 3 Food For Kids Indonesia
comment on column public.majalah.kategori_id is '1 = Food Review Indonesia (FRI), 2 = Kulinologi Indonesia, 3 = Food For Kids Indonesia';
-- Edisi unik per brand (Kulinologi Juli 2021 dan Food For Kids Juli 2021 boleh sama-sama edisi 7 tahun 2021)
alter table public.majalah drop constraint majalah_tahun_edisi_unik, add constraint majalah_kategori_tahun_edisi_unik unique (kategori_id, tahun, edisi);
alter table public.majalah alter column kategori_id set not null, alter column kategori_id set default 1;

-- 2. "Populer" = jumlah pembeli terbanyak. Disimpan sebagai counter di tabel majalah (dibaca publik, gak membuka data pembelian user lain)
alter table public.majalah add column jumlah_pembeli integer not null default 0;
update public.majalah m set jumlah_pembeli = (select count(*) from public.pembelian b where b.majalah_id = m.id);
create or replace function public.tambah_jumlah_pembeli() returns trigger language plpgsql security definer set search_path = '' as $$
begin
  update public.majalah set jumlah_pembeli = jumlah_pembeli + 1 where id = new.majalah_id;
  return new;
end $$;
create trigger on_pembelian_baru after insert on public.pembelian for each row execute function public.tambah_jumlah_pembeli();
revoke execute on function public.tambah_jumlah_pembeli() from public, anon, authenticated;

-- 3. Notifikasi "Edisi Terbaru" hanya untuk majalah berbayar FRI (majalah gratis brand lain gak memicu notifikasi massal)
create or replace function public.notif_edisi_baru() returns trigger language plpgsql security definer set search_path = '' as $$
begin
  if coalesce(new.url_pdf, '') = '' or new.harga = 0 then return new; end if;
  if exists (select 1 from public.majalah where id <> new.id and kategori_id = new.kategori_id and (tahun, edisi) >= (new.tahun, new.edisi)) then return new; end if;
  insert into public.notifikasi (customer_id, tipe, judul, pesan)
  select id, 'edisi_baru', 'Edisi Terbaru Telah Terbit!', new.judul || ' sudah bisa kamu baca sekarang.' from public.profiles;
  return new;
end $$;
