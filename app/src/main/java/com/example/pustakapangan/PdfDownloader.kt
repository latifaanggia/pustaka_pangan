package com.example.pustakapangan

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException

// Error yang pesannya sudah ramah user (Bahasa Indonesia), jadi diteruskan apa adanya
class PesanUnduhException(pesan: String) : IOException(pesan)

object PdfDownloader {
    private const val BUCKET = "pdf-majalah"
    private const val CADANGAN_RUANG = 10L * 1024 * 1024

    fun urlPdf(namaFile: String) = "${SupabaseConfig.PROJECT_URL}/storage/v1/object/public/$BUCKET/$namaFile"
    fun fileOffline(ctx: Context, namaFile: String) = File(File(ctx.filesDir, "majalah").apply { mkdirs() }, namaFile)
    fun fileCache(ctx: Context, namaFile: String) = File(File(ctx.cacheDir, "majalah").apply { mkdirs() }, namaFile)
    fun sudahDiunduh(ctx: Context, namaFile: String) = fileOffline(ctx, namaFile).let { it.exists() && it.length() > 0 }
    fun adaDiCache(ctx: Context, namaFile: String) = fileCache(ctx, namaFile).let { it.exists() && it.length() > 0 }

    suspend fun unduh(namaFile: String, tujuan: File, onProgress: (Int) -> Unit): File = withContext(Dispatchers.IO) {
        val tmp = File(tujuan.parentFile, "${tujuan.name}.tmp")
        var koneksi: HttpURLConnection? = null
        try {
            koneksi = (URL(urlPdf(namaFile)).openConnection() as HttpURLConnection).apply { connectTimeout = 15_000; readTimeout = 30_000 }
            when (koneksi.responseCode) {
                in 200..299 -> Unit
                400, 404 -> throw PesanUnduhException("File majalah belum tersedia di server")
                else -> throw PesanUnduhException("Server error (${koneksi.responseCode}), coba lagi nanti")
            }
            val total = koneksi.contentLengthLong
            if (total > 0 && tujuan.parentFile!!.usableSpace < total + CADANGAN_RUANG)
                throw PesanUnduhException("Memori HP penuh, kosongkan minimal ${(total / 1_048_576) + 10} MB")

            var terunduh = 0L;
            var persenTerakhir = -1
            koneksi.inputStream.use { input -> tmp.outputStream().use { output ->
                val buffer = ByteArray(64 * 1024)
                while (true) {
                    ensureActive()

                    val n = input.read(buffer); if (n == -1) break
                    output.write(buffer, 0, n); terunduh += n
                    if (total > 0) {
                        val persen = (terunduh * 100 / total).toInt();
                        if (persen != persenTerakhir) {
                            persenTerakhir = persen;
                            withContext(Dispatchers.Main) {
                                onProgress(persen)
                            }
                        }
                    }
                }
            } }
            if (total > 0 && terunduh != total) throw PesanUnduhException("Unduhan tidak lengkap, coba lagi")
            if (tujuan.exists()) tujuan.delete()
            if (!tmp.renameTo(tujuan)) throw PesanUnduhException("Gagal menyimpan file")
            tujuan
        } catch (e: PesanUnduhException) { tmp.delete(); throw e
        } catch (e: UnknownHostException) { tmp.delete(); throw PesanUnduhException("Tidak ada koneksi internet")
        } catch (e: SocketTimeoutException) { tmp.delete(); throw PesanUnduhException("Koneksi terlalu lambat atau terputus")
        } catch (e: IOException) { tmp.delete(); throw PesanUnduhException("Koneksi terputus, silakan coba lagi") // mis. "Software caused connection abort"
        } catch (e: Exception) { tmp.delete(); throw e
        } finally { koneksi?.disconnect() }
    }

    suspend fun simpanDariCache(ctx: Context, namaFile: String): File = withContext(Dispatchers.IO) {
        val asal = fileCache(ctx, namaFile); val tujuan = fileOffline(ctx, namaFile); val tmp = File(tujuan.parentFile, "${tujuan.name}.tmp")
        try { asal.copyTo(tmp, overwrite = true); if (!tmp.renameTo(tujuan)) throw IOException("Gagal menyimpan file"); asal.delete(); tujuan }
        catch (e: Exception) { tmp.delete(); throw e }
    }

    fun hapusFile(file: File) = file.delete()
}