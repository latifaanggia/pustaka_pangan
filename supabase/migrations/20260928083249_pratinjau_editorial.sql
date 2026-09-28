-- Pratinjau Editorial: gambar halaman 1-6 tiap PDF (JPG hasil render Ghostscript, tools/buat_pratinjau.bat),

-- nama file <nama_pdf_tanpa_.pdf>_hal<n>.jpg di bucket pratinjau-majalah
insert into storage.buckets (id, name, public) values ('pratinjau-majalah', 'pratinjau-majalah', true) on conflict (id) do nothing;

-- 0 = belum ada gambar pratinjau -> app menampilkan cover saja
alter table public.majalah add column jumlah_pratinjau integer not null default 0 check (jumlah_pratinjau between 0 and 6);
