package com.example.pustakapangan

import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.text.InputType
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.io.File

class EReaderActivity : AppCompatActivity() {

    // PDF
    private lateinit var pdfRenderer: PdfRenderer
    private lateinit var parcelFileDescriptor: ParcelFileDescriptor
    private var currentPage = 0
    private var namaFilePdf = "2026_vol_07.pdf"
    private var isOfflineMode = false
    private var sudahInfoUnduh = false
    private companion object { const val MENU_DAFTAR_ISI = 1; const val MENU_UNDUH = 2 }

    // VIEW
    private lateinit var imgPage: ImageView
    private lateinit var pdfContainer: View
    private lateinit var tvTitle: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvPageNumber: TextView
    private lateinit var btnBack: ImageView
    private lateinit var btnFullscreen: ImageView
    private lateinit var btnOptions: ImageView
    private lateinit var btnPrevious: ImageView
    private lateinit var btnNext: ImageView
    private lateinit var btnZoomOut: ImageView
    private lateinit var btnZoomIn: ImageView

    // ZOOM & INTERACTION
    private lateinit var scaleDetector: ScaleGestureDetector
    private var currentScale = 1f
    private var isFullscreenReader = false
    private var downX = 0f
    private var downY = 0f
    private var isScaling = false
    private var geserX = 0f; private var geserY = 0f
    private var lastX = 0f;
    private var lastY = 0f;
    private var lastFocusX = 0f;
    private var lastFocusY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ereader)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = getColor(android.R.color.white)
        window.navigationBarColor = getColor(android.R.color.white)

        val rootView = findViewById<View>(R.id.readerRoot)
        val headerReader = findViewById<View>(R.id.headerReader)
        val bottomControls = findViewById<View>(R.id.bottomControls)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            headerReader.setPadding(headerReader.paddingLeft, systemBars.top, headerReader.paddingRight, headerReader.paddingBottom)
            bottomControls.setPadding(bottomControls.paddingLeft, bottomControls.paddingTop, bottomControls.paddingRight, systemBars.bottom)
            insets
        }

        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }

        initViews()
        setupScaleDetector()
        setupButtons()
        setupPdfTouch()

        namaFilePdf = intent.getStringExtra("NAMA_FILE_PDF") ?: "2026_vol_07.pdf"
        val judulMajalah = intent.getStringExtra("JUDUL_MAJALAH") ?: "FRI VOL XXI/07 2026"
        tvTitle.text = judulMajalah

        siapkanPdf()
    }

    private fun initViews() {
        imgPage = findViewById(R.id.imgPage); pdfContainer = findViewById(R.id.pdfContainer)
        tvTitle = findViewById(R.id.tvTitle)
        tvStatus = findViewById(R.id.tvStatus)
        btnBack = findViewById(R.id.btnBack)
        btnFullscreen = findViewById(R.id.btnFullscreen)
        btnOptions = findViewById(R.id.btnOptions)
        btnPrevious = findViewById(R.id.btnPrevious)
        btnNext = findViewById(R.id.btnNext)
        btnZoomOut = findViewById(R.id.btnZoomOut)
        btnZoomIn = findViewById(R.id.btnZoomIn)

        tvPageNumber = findViewById(R.id.tvPageNumber)
        tvPageNumber.paintFlags = tvPageNumber.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    // SCALE DETECTOR (ZOOM)
    private fun setupScaleDetector() {
        scaleDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                isScaling = true; lastFocusX = detector.focusX; lastFocusY = detector.focusY
                if (!isFullscreenReader) toggleReaderControls()
                return true
            }

            // Zoom ke titik di antara 2 jari (bukan selalu ke tengah) + ikut geser kalau 2 jari bergerak bersama
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                geserX += detector.focusX - lastFocusX; geserY += detector.focusY - lastFocusY
                lastFocusX = detector.focusX; lastFocusY = detector.focusY
                zoomKe(currentScale * detector.scaleFactor, detector.focusX, detector.focusY)
                return true
            }
        })
    }

    // BUTTONS & INTERACTIONS
    private fun setupButtons() {
        btnBack.setOnClickListener { finish() }
        btnFullscreen.setOnClickListener { toggleReaderControls() }
        btnOptions.setOnClickListener { showOptionsMenu() }
        btnPrevious.setOnClickListener { previousPage() }
        btnNext.setOnClickListener { nextPage() }
        btnZoomOut.setOnClickListener { zoomOut() }
        btnZoomIn.setOnClickListener { zoomIn() }

        tvPageNumber.setOnClickListener { showPageJumpDialog() }
    }

    // PDF TOUCH (TAP TO TOGGLE)
    // PENTING: listener dipasang di pdfContainer, BUKAN di imgPage. Koordinat sentuhan di imgPage ikut ter-scale
    // oleh zoom-nya sendiri -> hasil hitungan zoom berubah tiap frame -> tampilan bergetar. Container gak pernah di-scale.
    private fun setupPdfTouch() {
        pdfContainer.setOnTouchListener { _, event ->
            scaleDetector.onTouchEvent(event)
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> { downX = event.x; downY = event.y; lastX = event.x; lastY = event.y; isScaling = false }
                // 1 jari digeser saat zoom = pan (lihat bagian lain halaman)
                MotionEvent.ACTION_MOVE -> if (!scaleDetector.isInProgress && event.pointerCount == 1) {
                    if (currentScale > 1f) { geserX += event.x - lastX; geserY += event.y - lastY; applyZoom() }
                    lastX = event.x; lastY = event.y
                }
                // Salah satu jari diangkat: lanjutkan dari jari yang tersisa, supaya halaman gak "loncat"
                MotionEvent.ACTION_POINTER_UP -> { val sisa = if (event.actionIndex == 0) 1 else 0; lastX = event.getX(sisa); lastY = event.getY(sisa) }
                MotionEvent.ACTION_UP -> if (!isScaling && kotlin.math.abs(event.x - downX) < 20 && kotlin.math.abs(event.y - downY) < 20) toggleReaderControls()
            }
            true
        }
    }

    // LOMPAT HALAMAN (DIALOG)
    private fun showPageJumpDialog() {
        if (!::pdfRenderer.isInitialized) return

        val editText = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            hint = "1 - ${pdfRenderer.pageCount}"
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(this)
            .setTitle("Lompat ke Halaman")
            .setView(editText)
            .setPositiveButton("Buka") { _, _ ->
                val inputText = editText.text.toString()
                if (inputText.isNotEmpty()) {
                    val targetPage = inputText.toIntOrNull()
                    if (targetPage != null && targetPage in 1..pdfRenderer.pageCount) {
                        currentPage = targetPage - 1
                        renderPage()
                    } else {
                        Toast.makeText(this, "Halaman tidak valid", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    // LOCAL STORAGE / CACHE / DOWNLOAD
    private fun siapkanPdf() {
        val modeOffline = intent.getBooleanExtra("IS_OFFLINE", false) || PdfDownloader.sudahDiunduh(this, namaFilePdf)
        val target = if (modeOffline) PdfDownloader.fileOffline(this, namaFilePdf) else PdfDownloader.fileCache(this, namaFilePdf)
        if (target.exists() && target.length() > 0) { openPdf(target, modeOffline); return }
        tvStatus.text = "Menyiapkan majalah..."; tvStatus.setTextColor(Color.parseColor("#6B7280")); tvPageNumber.text = "- / -"
        lifecycleScope.launch {
            try { openPdf(PdfDownloader.unduh(namaFilePdf, target) { tvStatus.text = "Memuat majalah $it%" }, modeOffline) }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                tvStatus.text = "Gagal memuat majalah"; tvStatus.setTextColor(Color.parseColor("#DC2626"))
                Toast.makeText(this@EReaderActivity, e.message ?: "Gagal mengunduh majalah", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openPdf(pdfFile: File, modeOffline: Boolean) {
        try {
            parcelFileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(parcelFileDescriptor)
            // Lanjutkan dari halaman terakhir yang dibaca (coerceIn: jaga-jaga kalau PDF di server diganti dan jumlah halamannya berubah)
            isOfflineMode = modeOffline; currentScale = 1f
            currentPage = RiwayatBaca.halaman(this, namaFilePdf).coerceIn(0, pdfRenderer.pageCount - 1)
            if (currentPage > 0) Toast.makeText(this, "Melanjutkan dari halaman ${currentPage + 1}", Toast.LENGTH_SHORT).show()
            RiwayatBaca.simpan(this, namaFilePdf, currentPage)
            updatePageNumber(); updateNavigationButton(); updateOnlineStatus(); renderPage()
        } catch (e: Exception) {
            if (::parcelFileDescriptor.isInitialized) parcelFileDescriptor.close()
            PdfDownloader.hapusFile(pdfFile)
            tvStatus.text = "File majalah rusak"; tvStatus.setTextColor(Color.parseColor("#DC2626"))
            Toast.makeText(this, "File rusak, silakan buka ulang majalah untuk mengunduh kembali", Toast.LENGTH_LONG).show()
        }
    }

    // RENDER PAGE
    private fun renderPage() {
        if (!::pdfRenderer.isInitialized) return
        if (currentPage < 0 || currentPage >= pdfRenderer.pageCount) return

        val page = pdfRenderer.openPage(currentPage)
        try {
            val density = resources.displayMetrics.density
            val renderScale = 1.5f * density
            val width = (page.width * renderScale).toInt()
            val height = (page.height * renderScale).toInt()

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            imgPage.setImageBitmap(bitmap)
        } finally {
            page.close() //
        }

        currentScale = 1f; geserX = 0f; geserY = 0f
        applyZoom()
        updatePageNumber()
        updateNavigationButton()
    }

    // NEXT & PREVIOUS PAGE
    private fun nextPage() {
        if (!::pdfRenderer.isInitialized) return
        if (currentPage < pdfRenderer.pageCount - 1) {
            currentPage++
            renderPage()
        }
    }

    private fun previousPage() {
        if (!::pdfRenderer.isInitialized) return
        if (currentPage > 0) {
            currentPage--
            renderPage()
        }
    }

    private fun updatePageNumber() {
        if (!::pdfRenderer.isInitialized) return
        tvPageNumber.text = "${currentPage + 1} / ${pdfRenderer.pageCount}"
    }

    private fun updateNavigationButton() {
        if (!::pdfRenderer.isInitialized) return
        btnPrevious.alpha = if (currentPage == 0) 0.4f else 1f
        btnNext.alpha = if (currentPage == pdfRenderer.pageCount - 1) 0.4f else 1f
    }

    // ZOOM IN / OUT
    private fun zoomIn() = zoomKe(currentScale + 0.25f, pdfContainer.width / 2f, pdfContainer.height / 2f)
    private fun zoomOut() = zoomKe(currentScale - 0.25f, pdfContainer.width / 2f, pdfContainer.height / 2f)

    // Zoom dengan titik fokus (fx, fy) tetap diam di bawah jari
    private fun zoomKe(target: Float, fx: Float, fy: Float) {
        val baru = target.coerceIn(1f, 4f); val rasio = baru / currentScale
        val cx = pdfContainer.width / 2f; val cy = pdfContainer.height / 2f // pivot scale default = tengah view
        geserX = (fx - cx) - (fx - cx - geserX) * rasio; geserY = (fy - cy) - (fy - cy - geserY) * rasio
        currentScale = baru; applyZoom()
    }

    // Terapkan zoom + geser
    private fun applyZoom() {
        imgPage.scaleX = currentScale; imgPage.scaleY = currentScale
        val d = imgPage.drawable; val w = imgPage.width.toFloat(); val h = imgPage.height.toFloat()
        if (d == null || w == 0f || d.intrinsicWidth <= 0) {
            geserX = 0f; geserY = 0f
        }
        else {
            val fit = minOf(w / d.intrinsicWidth, h / d.intrinsicHeight) // ukuran halaman setelah fitCenter
            val maxX = maxOf(0f, (d.intrinsicWidth * fit * currentScale - w) / 2);
            val maxY = maxOf(0f, (d.intrinsicHeight * fit * currentScale - h) / 2)
            geserX = geserX.coerceIn(-maxX, maxX); geserY = geserY.coerceIn(-maxY, maxY)
        }
        imgPage.translationX = geserX; imgPage.translationY = geserY
    }

    // FULLSCREEN READER
    private fun toggleReaderControls() {
        isFullscreenReader = !isFullscreenReader
        val header = findViewById<View>(R.id.headerReader)
        val footer = findViewById<View>(R.id.bottomControls)
        val controller = WindowCompat.getInsetsController(window, window.decorView)

        if (isFullscreenReader) {
            header.visibility = View.GONE
            footer.visibility = View.GONE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            header.visibility = View.VISIBLE
            footer.visibility = View.VISIBLE
            controller.show(WindowInsetsCompat.Type.systemBars())
            ViewCompat.requestApplyInsets(findViewById(R.id.readerRoot))
        }
    }

    // ONLINE STATUS & MENU
    private fun updateOnlineStatus() {
        if (isOfflineMode) {
            tvStatus.text = "Membaca Offline"
            tvStatus.setTextColor(Color.parseColor("#E67E22")) // Oranye
        } else if (!adaInternet()) {
            // Belum diunduh tapi masih tersimpan sementara (pernah dibuka online). Pakai bahasa user, bukan istilah "cache"
            tvStatus.text = "Offline · Belum Diunduh"
            tvStatus.setTextColor(Color.parseColor("#E67E22")) // Oranye
            if (!sudahInfoUnduh) { sudahInfoUnduh = true; Toast.makeText(this, "Majalah ini belum diunduh. Unduh lewat menu ⋮ agar selalu bisa dibaca tanpa internet.", Toast.LENGTH_LONG).show() }
        } else {
            tvStatus.text = "Membaca Online"
            tvStatus.setTextColor(Color.parseColor("#00A859")) // Hijau
        }
    }

    private fun adaInternet(): Boolean = try {
        val cm = getSystemService(ConnectivityManager::class.java)
        cm?.getNetworkCapabilities(cm.activeNetwork)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    } catch (e: Exception) { true }

    private fun showOptionsMenu() {
        val popup = PopupMenu(this, btnOptions)

        popup.menu.add(0, MENU_DAFTAR_ISI, 0, "Daftar Isi"); popup.menu.add(0, MENU_UNDUH, 1, "Unduh Offline")
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                MENU_DAFTAR_ISI -> { Toast.makeText(this, "Daftar Isi belum tersedia", Toast.LENGTH_SHORT).show(); true }
                MENU_UNDUH -> { unduhOffline(); true }
                else -> false
            }
        }
        popup.show()
    }

    private fun unduhOffline() {
        when {
            !::pdfRenderer.isInitialized -> Toast.makeText(this, "Tunggu majalah selesai dimuat", Toast.LENGTH_SHORT).show()
            isOfflineMode -> Toast.makeText(this, "Majalah sudah tersedia offline", Toast.LENGTH_SHORT).show()
            else -> lifecycleScope.launch {
                try { PdfDownloader.simpanDariCache(this@EReaderActivity, namaFilePdf); isOfflineMode = true; updateOnlineStatus(); Toast.makeText(this@EReaderActivity, "Majalah tersimpan, bisa dibaca tanpa internet", Toast.LENGTH_SHORT).show() }
                catch (e: CancellationException) { throw e }
                catch (e: Exception) { Toast.makeText(this@EReaderActivity, "Gagal menyimpan: ${e.message}", Toast.LENGTH_LONG).show() }
            }
        }
    }

    // Simpan posisi baca tiap kali layar ditinggalkan (back, pindah app, layar mati)
    override fun onPause() {
        super.onPause()
        if (::pdfRenderer.isInitialized) RiwayatBaca.simpan(this, namaFilePdf, currentPage)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::pdfRenderer.isInitialized) {
            pdfRenderer.close()
        }
        if (::parcelFileDescriptor.isInitialized) {
            parcelFileDescriptor.close()
        }
    }
}