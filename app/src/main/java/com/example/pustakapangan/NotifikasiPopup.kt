package com.example.pustakapangan

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// Pop-up notifikasi IN-APP
object NotifikasiPopup {
    interface PenerimaNotifikasi {
        fun onNotifikasiBaru(daftar: List<Notifikasi>)
    }
    private const val INTERVAL_MS = 30_000L
    private const val TAG_BANNER = "banner_notifikasi"
    private val LAYAR_TANPA_POPUP = setOf(
        MainActivity::class.java,
        SignInActivity::class.java,
        RegisterActivity::class.java,
        EReaderActivity::class.java,
        NotifikasiActivity::class.java)

    fun pasang(app: Application) = app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(a: Activity, savedInstanceState: Bundle?) {
            val activity = a as? AppCompatActivity ?: return
            if (activity.javaClass in LAYAR_TANPA_POPUP) return

            activity.lifecycleScope.launch {
                activity.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    while (true) {
                        cek(activity);
                        delay(INTERVAL_MS)
                    }
                }
            }
        }
        override fun onActivityStarted(a: Activity) {}
        override fun onActivityResumed(a: Activity) {}
        override fun onActivityPaused(a: Activity) {}
        override fun onActivityStopped(a: Activity) {}
        override fun onActivitySaveInstanceState(a: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(a: Activity) {}
    })

    private val kunci = Mutex()

    suspend fun cekSekarang(activity: AppCompatActivity) = cek(activity)

    private suspend fun cek(activity: AppCompatActivity) = kunci.withLock { cekTanpaKunci(activity) }

    private suspend fun cekTanpaKunci(activity: AppCompatActivity) {
        if (!SessionManager.isLoggedIn(activity) || CustomerRepository.getUserAktif(activity) == null) return
        val baru = try {
            NotifikasiRepository.ambilUntukPopup(activity)
        }
        catch (e: CancellationException) { throw e }
        catch (e: Exception) { return } // offline: diam saja

        if (baru.isEmpty()) return
        val dot: View? = activity.findViewById(R.id.dotNotifBell);
        dot?.visibility = View.VISIBLE
        tampilkan(activity, baru.first(), baru.size - 1)
        (activity as? PenerimaNotifikasi)?.onNotifikasiBaru(baru)
        if (baru.any { it.tipe == "topup_berhasil" }
        )
            try {
                CustomerRepository.refreshSaldo(activity)
            }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) { }
    }

    private fun tampilkan(activity: AppCompatActivity, n: Notifikasi, lainnya: Int) {
        val root = activity.window.decorView as? ViewGroup ?: return
        root.findViewWithTag<View>(TAG_BANNER)?.let {
            root.removeView(it)
        }

        val banner = activity.layoutInflater.inflate(R.layout.view_popup_notifikasi, root, false).apply { tag = TAG_BANNER }

        banner.findViewById<ImageView>(R.id.imgIconPopup).setImageResource(NotifikasiRepository.ikon(n.tipe))
        banner.findViewById<TextView>(R.id.tvJudulPopup).text = n.judul
        banner.findViewById<TextView>(R.id.tvPesanPopup).text =
            if (lainnya > 0) "${n.pesan}\n+$lainnya notifikasi lainnya"
            else n.pesan

        val d = activity.resources.displayMetrics.density
        val atas = (ViewCompat.getRootWindowInsets(root)?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0) + (8 * d).toInt()

        banner.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP)
            .apply {
                setMargins((12 * d).toInt(), atas, (12 * d).toInt(), 0)
            }
        fun tutup() {
            if (banner.isAttachedToWindow)
                banner.animate().translationY(-(banner.height + atas).toFloat()).alpha(0f).setDuration(200).withEndAction {
                    root.removeView(banner)
                }.start()
        }
        banner.setOnClickListener {
            tutup();
            activity.startActivity(Intent(activity, NotifikasiActivity::class.java))
        }
        banner.findViewById<View>(R.id.btnTutupPopup).setOnClickListener { tutup() }
        root.addView(banner)
        banner.translationY = -200 * d; banner.alpha = 0f
        banner.animate().translationY(0f).alpha(1f).setDuration(250).start()
        banner.postDelayed({ tutup() }, 5_000)
    }
}
