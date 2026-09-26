package com.noam.pokelens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private val reqCapture = 42
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dp = resources.displayMetrics.density
        val col = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((22 * dp).toInt(), (40 * dp).toInt(), (22 * dp).toInt(), (22 * dp).toInt())
            setBackgroundColor(Color.parseColor("#0B0F1F"))
        }
        fun label(s: String, size: Float, c: Int) = TextView(this).apply {
            text = s; textSize = size; setTextColor(c); setPadding(0, 0, 0, (14 * dp).toInt())
        }
        fun btn(s: String, bg: String, fg: Int, on: () -> Unit) = Button(this).apply {
            text = s; textSize = 17f; setTextColor(fg); isAllCaps = false
            background = GradientDrawable().apply { cornerRadius = 16 * dp; setColor(Color.parseColor(bg)) }
            layoutParams = LinearLayout.LayoutParams(-1, (58 * dp).toInt()).apply { bottomMargin = (12 * dp).toInt() }
            setOnClickListener { on() }
        }
        col.addView(label("✨ PokéLens", 30f, Color.parseColor("#FFD54A")).apply { gravity = Gravity.CENTER })
        col.addView(label(
            "מזהה את הפוקימון שמופיע על המסך ומציג בועה עם מספר, טיפוסים, חולשות ו-CP.\n\n" +
            "איך זה עובד: האפליקציה קוראת את המסך (כמו הקלטת מסך) ומזהה את השם עם OCR שרץ בטלפון, בלי אינטרנט. " +
            "היא לא מתחברת לשרתים של המשחק, לא משנה אותו ולא לוחצת בשבילך.\n\n" +
            "1. לחץ \"הפעל\" ואשר הרשאת ציור מעל אפליקציות\n2. אשר הקלטת מסך (אפשר לבחור רק את Pokémon GO)\n" +
            "3. פתח את המשחק ולחץ על פוקימון",
            15f, Color.parseColor("#EEF1FF")))
        col.addView(btn("▶ הפעל", "#FFD54A", Color.BLACK) { start() })
        col.addView(btn("■ עצור", "#1D2548", Color.WHITE) {
            stopService(Intent(this, ScanService::class.java)); refresh()
        })
        status = label("", 14f, Color.parseColor("#9AA3C7"))
        col.addView(status)
        col.addView(label("מזהה כרגע ${PokemonDb.all.size} פוקימונים (דור 1–2), שמות באנגלית.", 12f, Color.parseColor("#9AA3C7")))
        setContentView(ScrollView(this).apply { addView(col); setBackgroundColor(Color.parseColor("#0B0F1F")) })
    }

    override fun onResume() { super.onResume(); refresh() }

    private fun refresh() {
        status.text = if (ScanService.running) "🟢 פעיל, אפשר לעבור למשחק" else "⚪ כבוי"
    }

    private fun start() {
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 7)
        }
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "אפשר את PokéLens ואז חזור ולחץ שוב \"הפעל\"", Toast.LENGTH_LONG).show()
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            return
        }
        if (ScanService.running) { Toast.makeText(this, "כבר פעיל", Toast.LENGTH_SHORT).show(); return }
        val mpm = getSystemService(MediaProjectionManager::class.java)
        @Suppress("DEPRECATION")
        startActivityForResult(mpm.createScreenCaptureIntent(), reqCapture)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != reqCapture) return
        if (resultCode != RESULT_OK || data == null) {
            Toast.makeText(this, "צריך לאשר הקלטת מסך", Toast.LENGTH_SHORT).show(); return
        }
        startForegroundService(Intent(this, ScanService::class.java)
            .putExtra(ScanService.EXTRA_CODE, resultCode)
            .putExtra(ScanService.EXTRA_DATA, data))
        Toast.makeText(this, "פעיל! עבור ל-Pokémon GO", Toast.LENGTH_SHORT).show()
        moveTaskToBack(true)
    }
}
