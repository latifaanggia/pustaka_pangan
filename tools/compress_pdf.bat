@echo off
REM Kompres semua PDF ke subfolder "compressed" (preset /ebook = 150 dpi)
REM LeaveColorUnchanged: warna asli (CMYK/ICC) dipertahankan, cegah error Blending colour space di Ghostscript 10.x
if not exist compressed mkdir compressed
for %%f in (*.pdf) do (
  echo Mengompres %%f ...
  gswin64c -sDEVICE=pdfwrite -dCompatibilityLevel=1.5 -dPDFSETTINGS=/ebook -sColorConversionStrategy=LeaveColorUnchanged -dNOPAUSE -dQUIET -dBATCH -sOutputFile="compressed\%%f" "%%f"
)
echo Selesai! Cek folder compressed.
pause