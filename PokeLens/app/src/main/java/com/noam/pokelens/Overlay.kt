package com.noam.pokelens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

/** בועה צפה מעל המשחק. אפשר לגרור אותה. היא לא לוחצת על שום דבר במשחק. */
class Overlay(private val ctx: Context) {
    private val wm = ctx.getSystemService(WindowManager::class.java)
    private val dp = ctx.resources.displayMetrics.density
    private val root = LinearLayout(ctx)
    private val title = tv(19f, Color.parseColor("#FFD54A"), true)
    private val line1 = tv(14f, Color.WHITE, false)
    private val line2 = tv(13f, Color.parseColor("#FF9DB8"), false)
    private val line3 = tv(12f, Color.parseColor("#9AA3C7"), false)
    private var added = false

    private val params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        x = (12 * dp).toInt()
        y = (ctx.resources.displayMetrics.heightPixels * 0.66).toInt()
    }

    init {
        root.orientation = LinearLayout.VERTICAL
        val pad = (12 * dp).toInt()
        root.setPadding(pad, pad, pad, pad)
        root.background = GradientDrawable().apply {
            cornerRadius = 18 * dp
            setColor(Color.parseColor("#E60B0F1F"))
            setStroke((2 * dp).toInt(), Color.parseColor("#FFD54A"))
        }
        root.addView(title); root.addView(line1); root.addView(line2); root.addView(line3)
        root.maxWidth(280)
        enableDrag()
        showIdle()
    }

    private fun LinearLayout.maxWidth(dpW: Int) {
        for (i in 0 until childCount) (getChildAt(i) as TextView).maxWidth = (dpW * dp).toInt()
    }

    private fun tv(size: Float, color: Int, bold: Boolean) = TextView(ctx).apply {
        textSize = size; setTextColor(color)
        if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun enableDrag() {
        var sx = 0f; var sy = 0f; var px = 0; var py = 0
        root.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> { sx = e.rawX; sy = e.rawY; px = params.x; py = params.y; true }
                MotionEvent.ACTION_MOVE -> {
                    params.x = px + (e.rawX - sx).toInt()
                    params.y = py + (e.rawY - sy).toInt()
                    if (added) wm.updateViewLayout(root, params); true
                }
                else -> true
            }
        }
    }

    fun show() { if (!added) { wm.addView(root, params); added = true } }
    fun hide() { if (added) { try { wm.removeView(root) } catch (_: Exception) {}; added = false } }

    fun showIdle() {
        title.text = "🔍 PokéLens"
        line1.text = "פתח מפגש עם פוקימון במשחק"
        line2.visibility = View.GONE; line3.visibility = View.GONE
    }

    fun showMon(m: Mon, cp: Int?) {
        title.text = "#%03d %s".format(m.num, m.name) + (cp?.let { "  ·  CP $it" } ?: "")
        line1.text = m.types.joinToString("  ") { PokemonDb.HE[it] ?: it }
        val w = PokemonDb.weaknesses(m)
        line2.visibility = View.VISIBLE
        line2.text = "חלש מול: " + w.joinToString("  ") { (t, x) ->
            (PokemonDb.HE[t] ?: t) + if (x > 2.0) " ×2" else ""
        }
        line3.visibility = View.VISIBLE
        line3.text = "✨ שייני? חפש ניצוצות בכניסה למפגש · טבע ~1/512"
    }

    /** מיקום הבועה במסך, כדי שהסורק לא יקרא את הטקסט של עצמה */
    fun screenRect(): Rect? {
        if (!added || root.width == 0) return null
        val loc = IntArray(2); root.getLocationOnScreen(loc)
        return Rect(loc[0], loc[1], loc[0] + root.width, loc[1] + root.height)
    }
}
