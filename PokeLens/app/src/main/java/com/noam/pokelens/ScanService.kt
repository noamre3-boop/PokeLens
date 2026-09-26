package com.noam.pokelens

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.view.WindowManager
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

/**
 * קורא את המסך (בדיוק כמו הקלטת מסך), מזהה בעזרת OCR את שם הפוקימון
 * ומציג בועה עם מידע. לא מתחבר לשרתים של המשחק ולא לוחץ על כלום.
 */
class ScanService : Service() {
    companion object {
        const val CH = "pokelens_scan"
        const val EXTRA_CODE = "code"
        const val EXTRA_DATA = "data"
        const val ACTION_STOP = "com.noam.pokelens.STOP"
        @Volatile var running = false
        private const val INTERVAL_MS = 700L
        private const val SCALE = 2          // לוכדים בחצי רזולוציה = מהיר וחוסך סוללה
        private const val SCAN_TOP = 0.55    // סורקים רק את ה-55% העליונים (שם השם וה-CP)
    }

    private val main = Handler(Looper.getMainLooper())
    private val thread = HandlerThread("pokelens").apply { start() }
    private val bg = Handler(thread.looper)
    private val busy = AtomicBoolean(false)
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val cpRegex = Regex("""CP\s*([0-9]{2,5})""", RegexOption.IGNORE_CASE)

    private var projection: MediaProjection? = null
    private var display: VirtualDisplay? = null
    private var reader: ImageReader? = null
    private var overlay: Overlay? = null
    private var capW = 0; private var capH = 0; private var cropH = 0
    private var rowBuf: ByteArray? = null
    private var outBuf: ByteBuffer? = null
    private var bitmap: Bitmap? = null

    private var lastMon: Mon? = null
    private var sameCount = 0
    private var missCount = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) { stopSelf(); return START_NOT_STICKY }
        startAsForeground()
        if (projection != null || intent == null) return START_NOT_STICKY

        val code = intent.getIntExtra(EXTRA_CODE, Activity.RESULT_CANCELED)
        val data: Intent? = if (Build.VERSION.SDK_INT >= 33)
            intent.getParcelableExtra(EXTRA_DATA, Intent::class.java)
        else @Suppress("DEPRECATION") intent.getParcelableExtra(EXTRA_DATA)
        if (data == null) { stopSelf(); return START_NOT_STICKY }

        val mpm = getSystemService(MediaProjectionManager::class.java)
        val p = mpm.getMediaProjection(code, data) ?: run { stopSelf(); return START_NOT_STICKY }
        projection = p
        p.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() { stopSelf() }
        }, main)

        val (sw, sh, dpi) = screenSize()
        capW = sw / SCALE; capH = sh / SCALE; cropH = (capH * SCAN_TOP).toInt()
        val r = ImageReader.newInstance(capW, capH, PixelFormat.RGBA_8888, 2)
        reader = r
        display = p.createVirtualDisplay("pokelens", capW, capH, dpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, r.surface, null, bg)

        overlay = Overlay(this).also { it.show() }
        running = true
        bg.postDelayed(loop, 500)
        return START_NOT_STICKY
    }

    private fun startAsForeground() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CH, "סריקת מסך", NotificationManager.IMPORTANCE_LOW))
        val stopPi = PendingIntent.getService(this, 1,
            Intent(this, ScanService::class.java).setAction(ACTION_STOP), PendingIntent.FLAG_IMMUTABLE)
        val n = Notification.Builder(this, CH)
            .setContentTitle("PokéLens פעיל")
            .setContentText("מזהה פוקימונים על המסך")
            .setSmallIcon(android.R.drawable.ic_menu_search)
            .setOngoing(true)
            .addAction(Notification.Action.Builder(
                Icon.createWithResource(this, android.R.drawable.ic_menu_close_clear_cancel), "עצור", stopPi).build())
            .build()
        if (Build.VERSION.SDK_INT >= 29) startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        else startForeground(1, n)
    }

    private fun screenSize(): Triple<Int, Int, Int> {
        val wm = getSystemService(WindowManager::class.java)
        val dpi = resources.displayMetrics.densityDpi
        return if (Build.VERSION.SDK_INT >= 30) {
            val b = wm.currentWindowMetrics.bounds
            Triple(b.width(), b.height(), dpi)
        } else {
            val m = DisplayMetrics()
            @Suppress("DEPRECATION") wm.defaultDisplay.getRealMetrics(m)
            Triple(m.widthPixels, m.heightPixels, dpi)
        }
    }

    private val loop = object : Runnable {
        override fun run() {
            if (!running) return
            try { scanOnce() } catch (_: Exception) { busy.set(false) }
            bg.postDelayed(this, INTERVAL_MS)
        }
    }

    private fun scanOnce() {
        val r = reader ?: return
        if (busy.get()) return
        val img = r.acquireLatestImage() ?: return
        val bmp: Bitmap
        try {
            val plane = img.planes[0]
            val src = plane.buffer
            val rowStride = plane.rowStride
            val rowBytes = capW * 4
            val tmp = rowBuf ?: ByteArray(rowBytes).also { rowBuf = it }
            val out = outBuf ?: ByteBuffer.allocate(rowBytes * cropH).also { outBuf = it }
            out.clear()
            for (y in 0 until cropH) {
                src.position(y * rowStride)
                src.get(tmp, 0, rowBytes)
                out.put(tmp)
            }
            out.rewind()
            bmp = bitmap ?: Bitmap.createBitmap(capW, cropH, Bitmap.Config.ARGB_8888).also { bitmap = it }
            bmp.copyPixelsFromBuffer(out)
        } finally {
            img.close()
        }
        busy.set(true)
        recognizer.process(InputImage.fromBitmap(bmp, 0))
            .addOnSuccessListener { handle(it) }       // רץ על ה-main thread
            .addOnCompleteListener { busy.set(false) }
    }

    private fun handle(text: Text) {
        val ov = overlay ?: return
        val ex: Rect? = ov.screenRect()?.let {
            Rect(it.left / SCALE, it.top / SCALE, it.right / SCALE, it.bottom / SCALE)
        }
        var best: Mon? = null
        var bestH = 0
        var cp: Int? = null
        for (block in text.textBlocks) for (line in block.lines) {
            val box = line.boundingBox
            if (box != null && ex != null && Rect.intersects(box, ex)) continue
            cpRegex.find(line.text)?.let { m -> if (cp == null) cp = m.groupValues[1].toIntOrNull() }
            val mon = PokemonDb.match(line.text) ?: continue
            val h = box?.height() ?: 1
            if (h > bestH) { bestH = h; best = mon }   // הטקסט הכי גדול = שם הפוקימון
        }
        val found = best
        if (found != null) {
            missCount = 0
            sameCount = if (found == lastMon) sameCount + 1 else 1
            lastMon = found
            if (sameCount >= 2) ov.showMon(found, cp)   // מאשרים ב-2 פריימים רצופים = בלי קפיצות
        } else if (++missCount >= 4) {
            lastMon = null; sameCount = 0
            ov.showIdle()
        }
    }

    override fun onDestroy() {
        running = false
        bg.removeCallbacksAndMessages(null)
        try { display?.release() } catch (_: Exception) {}
        try { reader?.close() } catch (_: Exception) {}
        try { projection?.stop() } catch (_: Exception) {}
        overlay?.hide()
        recognizer.close()
        thread.quitSafely()
        super.onDestroy()
    }
}
