@echo off
REM Buat gambar Pratinjau Editorial (halaman 1-6) dari setiap PDF di folder ini -> subfolder "pratinjau"
REM Hasil: 2026_vol_07_hal1.jpg ... 2026_vol_07_hal6.jpg (lebar +-830px, cukup tajam untuk kartu pratinjau)
REM Upload semua isi folder "pratinjau" ke bucket Supabase: pratinjau-majalah
if not exist pratinjau mkdir pratinjau
for %%f in (*.pdf) do (
  echo Membuat pratinjau %%f ...
  gswin64c -sDEVICE=jpeg -dJPEGQ=75 -r100 -dFirstPage=1 -dLastPage=6 -dNOPAUSE -dQUIET -dBATCH -sOutputFile="pratinjau\%%~nf_hal%%d.jpg" "%%f"
)
echo Selesai! Cek folder pratinjau.
pause
