-- Data contoh katalog majalah (8 edisi demo). File PDF & cover di-upload manual ke bucket pdf-majalah & cover-majalah.
insert into public.majalah (id, kategori_id, judul, tahun, harga, url_cover, url_pdf) values
  (4, 1, 'FRI VOL XXI/04 2026', 2026, 20000, 'https://jteteeubteryqfgrnjks.supabase.co/storage/v1/object/public/cover-majalah/img_2026_vol_04.png', '2026_vol_04.pdf'),
  (5, 1, 'FRI VOL XXI/05 2026', 2026, 20000, 'https://jteteeubteryqfgrnjks.supabase.co/storage/v1/object/public/cover-majalah/img_2026_vol_05.png', '2026_vol_05.pdf'),
  (6, 1, 'FRI VOL XXI/06 2026', 2026, 20000, 'https://jteteeubteryqfgrnjks.supabase.co/storage/v1/object/public/cover-majalah/img_2026_vol_06.png', '2026_vol_06.pdf'),
  (7, 1, 'FRI VOL XXI/07 2026', 2026, 20000, 'https://jteteeubteryqfgrnjks.supabase.co/storage/v1/object/public/cover-majalah/img_2026_vol_07.png', '2026_vol_07.pdf'),
  (8, 1, 'FRI VOL XXI/08 2026', 2026, 20000, 'https://jteteeubteryqfgrnjks.supabase.co/storage/v1/object/public/cover-majalah/img_2026_vol_08.png', '2026_vol_08.pdf'),
  (10, 1, 'FRI VOL XX/10 2025', 2025, 20000, 'https://jteteeubteryqfgrnjks.supabase.co/storage/v1/object/public/cover-majalah/img_2025_vol_10.png', '2025_vol_10.pdf'),
  (11, 1, 'FRI VOL XX/11 2025', 2025, 20000, 'https://jteteeubteryqfgrnjks.supabase.co/storage/v1/object/public/cover-majalah/img_2025_vol_11.png', '2025_vol_11.pdf'),
  (12, 1, 'FRI VOL XX/12 2025', 2025, 20000, 'https://jteteeubteryqfgrnjks.supabase.co/storage/v1/object/public/cover-majalah/img_2025_vol_12.png', '2025_vol_12.pdf');
select setval('public.majalah_id_seq', (select max(id) from public.majalah));
