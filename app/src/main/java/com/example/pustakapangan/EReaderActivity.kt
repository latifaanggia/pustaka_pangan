package com.example.pustakapangan

import android.graphics.Bitmap
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

    // VIEW
    private lateinit var imgPage: ImageView
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
        imgPage = findViewById(R.id.imgPage)
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
                isScaling = true
                if (!isFullscreenReader) toggleReaderControls()
                return true
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                currentScale *= detector.scaleFactor
                currentScale = currentScale.coerceIn(1f, 4f)
                applyZoom()
                return true
            }

            override fun onScaleEnd(detector: ScaleGestureDetector) {
                isScaling = false
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
    private fun setupPdfTouch() {
        imgPage.setOnTouchListener { _, event ->
            scaleDetector.onTouchEvent(event)
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.x
                    downY = event.y
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val moveX = kotlin.math.abs(event.x - downX)
                    val moveY = kotlin.math.abs(event.y - downY)

                    if (!isScaling && moveX < 20 && moveY < 20) {
                        toggleReaderControls()
                    }
                    true
                }
                else -> true
            }
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
            isOfflineMode = modeOffline; currentPage = 0; currentScale = 1f
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

        currentScale = 1f
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
    private fun zoomIn() {
        currentScale += 0.25f
        currentScale = currentScale.coerceAtMost(4f)
        applyZoom()
    }

    private fun zoomOut() {
        currentScale -= 0.25f
        currentScale = currentScale.coerceAtLeast(1f)
        applyZoom()
    }

    private fun applyZoom() {
        imgPage.scaleX = currentScale
        imgPage.scaleY = currentScale
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
        } else {
            tvStatus.text = "Membaca Online"
            tvStatus.setTextColor(Color.parseColor("#00A859")) // Hijau
        }
    }

    private fun showOptionsMenu() {
        val popup = PopupMenu(this, btnOptions)
        popup.menu.add("📖 Daftar Isi")
        popup.menu.add("⬇ Unduh Offline")

        popup.setOnMenuItemClickListener { item ->
            when (item.title.toString()) {
                "📖 Daftar Isi" -> {
                    Toast.makeText(this, "Daftar Isi belum tersedia", Toast.LENGTH_SHORT).show()
                    true
                }
                "⬇ Unduh Offline" -> { unduhOffline(); true }
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