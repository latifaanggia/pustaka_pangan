-- Data katalog majalah (29 edisi) — diekspor dari Supabase 28 Sep 2026.
-- kategori_id: 1 = Food Review Indonesia (FRI), 2 = Kulinologi Indonesia (KI), 3 = Food For Kids Indonesia (FFK)
-- harga 0 = majalah gratis; url_pdf null = baru cover ("Segera Hadir", tidak bisa dibeli).
-- File cover & PDF di-upload manual ke bucket cover-majalah & pdf-majalah. Kalau pindah project, ganti URL di bawah.

insert into public.majalah (id, kategori_id, judul, tahun, harga, url_cover, url_pdf, edisi)
select id, kategori_id, judul, tahun, harga, 'https://jteteeubteryqfgrnjks.supabase.co/storage/v1/object/public/cover-majalah/' || cover, pdf, edisi
from (values
  (8, 1, 'FRI VOL XXI/08 2026', 2026, 20000, 'img_2026_vol_08.png', '2026_vol_08.pdf', 8),
  (7, 1, 'FRI VOL XXI/07 2026', 2026, 20000, 'img_2026_vol_07.png', '2026_vol_07.pdf', 7),
  (6, 1, 'FRI VOL XXI/06 2026', 2026, 20000, 'img_2026_vol_06.png', '2026_vol_06.pdf', 6),
  (5, 1, 'FRI VOL XXI/05 2026', 2026, 20000, 'img_2026_vol_05.png', '2026_vol_05.pdf', 5),
  (4, 1, 'FRI VOL XXI/04 2026', 2026, 20000, 'img_2026_vol_04.png', '2026_vol_04.pdf', 4),
  (12, 1, 'FRI VOL XX/12 2025', 2025, 20000, 'img_2025_vol_12.png', '2025_vol_12.pdf', 12),
  (11, 1, 'FRI VOL XX/11 2025', 2025, 20000, 'img_2025_vol_11.png', '2025_vol_11.pdf', 11),
  (10, 1, 'FRI VOL XX/10 2025', 2025, 20000, 'img_2025_vol_10.png', '2025_vol_10.pdf', 10),
  (13, 1, 'FRI VOL XIX/12 2024', 2024, 20000, 'img_2024_vol_12.png', null, 12),
  (33, 1, 'FRI VOL XIX/11 2024', 2024, 20000, 'img_2024_vol_11.png', null, 11),
  (32, 1, 'FRI VOL XIX/10 2024', 2024, 20000, 'img_2024_vol_10.png', null, 10),
  (31, 1, 'FRI VOL XVIII/12 2023', 2023, 20000, 'img_2023_vol_12.png', null, 12),
  (27, 1, 'FRI VOL XVIII/11 2023', 2023, 20000, 'img_2023_vol_11.png', null, 11),
  (23, 1, 'FRI VOL XVIII/10 2023', 2023, 20000, 'img_2023_vol_10.png', null, 10),
  (30, 1, 'FRI VOL XVII/12 2022', 2022, 20000, 'img_2022_vol_12.png', null, 12),
  (26, 1, 'FRI VOL XVII/11 2022', 2022, 20000, 'img_2022_vol_11.png', null, 11),
  (22, 1, 'FRI VOL XVII/10 2022', 2022, 20000, 'img_2022_vol_10.png', null, 10),
  (29, 1, 'FRI VOL XVI/12 2021', 2021, 20000, 'img_2021_vol_12.png', null, 12),
  (25, 1, 'FRI VOL XVI/11 2021', 2021, 20000, 'img_2021_vol_11.png', null, 11),
  (21, 1, 'FRI VOL XVI/10 2021', 2021, 20000, 'img_2021_vol_10.png', null, 10),
  (28, 1, 'FRI VOL XV/12 2020', 2020, 20000, 'img_2020_vol_12.png', null, 12),
  (24, 1, 'FRI VOL XV/11 2020', 2020, 20000, 'img_2020_vol_11.png', null, 11),
  (20, 1, 'FRI VOL XV/10 2020', 2020, 20000, 'img_2020_vol_10.png', null, 10),
  (16, 2, 'KI VOL XII/08 2021', 2021, 0, 'img_2021_ki_vol_08.png', '2021_ki_vol_08.pdf', 8),
  (15, 2, 'KI VOL XII/07 2021', 2021, 0, 'img_2021_ki_vol_07.png', '2021_ki_vol_07.pdf', 7),
  (14, 2, 'KI VOL XII/06 2021', 2021, 0, 'img_2021_ki_vol_06.png', '2021_ki_vol_06.pdf', 6),
  (19, 3, 'FFK VOL 02/02 2022', 2022, 0, 'img_2022_ffk_vol_02.png', '2022_ffk_vol_02.pdf', 2),
  (18, 3, 'FFK VOL 09/06 2021', 2021, 0, 'img_2021_ffk_vol_06.png', '2021_ffk_vol_06.pdf', 6),
  (17, 3, 'FFK VOL 09/05 2021', 2021, 0, 'img_2021_ffk_vol_05.png', '2021_ffk_vol_05.pdf', 5)
) as v(id, kategori_id, judul, tahun, harga, cover, pdf, edisi);
select setval('public.majalah_id_seq', (select max(id) from public.majalah));
