package com.afterpay.android.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ImageSpan
import android.text.style.URLSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.afterpay.android.R

class AfterpayPriceBreakdown(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    constructor(context: Context) : this(context, attrs = null)

    var colorScheme: AfterpayColorScheme = AfterpayColorScheme.DEFAULT
        set(value) {
            field = value
            updateText()
            invalidate()
            requestLayout()
        }

    private val textView: TextView = TextView(context).apply {
        setTextColor(context.resolveColorAttr(android.R.attr.textColorPrimary))
        setLinkTextColor(context.resolveColorAttr(android.R.attr.textColorSecondary))
        setLineSpacing(0f, 1.2f)
        textSize = 14f
        movementMethod = LinkMovementMethod.getInstance()
        gravity = Gravity.CENTER
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.CENTER
        }
    }

    init {
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        isFocusable = true

        addView(textView)

        context.theme
            .obtainStyledAttributes(attrs, R.styleable.Afterpay, 0, 0)
            .apply {
                try {
                    val value = getInteger(R.styleable.Afterpay_colorScheme, 0)
                    colorScheme = AfterpayColorScheme.values()[value]
                } finally {
                    recycle()
                }
            }

        updateText()
    }

    private fun updateText() {
        val drawable = resources.getDrawable(colorScheme.badgeDrawable, null).apply {
            val metrics = textView.paint.fontMetrics
            val fontHeight = metrics.descent - metrics.ascent
            val aspectRatio = intrinsicWidth / intrinsicHeight.toFloat()
            val drawableHeight = fontHeight * 2.5
            val drawableWidth = drawableHeight * aspectRatio
            setBounds(0, 0, drawableWidth.toInt(), drawableHeight.toInt())
        }

        textView.apply {
            text = SpannableStringBuilder()
                .append(resources.getString(R.string.price_breakdown_total_cost))
                .append(" ")
                .append(" ", CenteredImageSpan(drawable), Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                .append(" ")
                .append(
                    resources.getString(R.string.price_breakdown_info_link),
                    URLSpan("https://static-us.afterpay.com/javascript/modal/us_modal.html"),
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
            contentDescription = resources.getString(R.string.price_breakdown_content_description)
        }
    }
}

/**
 * A vertically centered image span.
 */
private class CenteredImageSpan(drawable: Drawable) : ImageSpan(drawable) {
    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fontMetricsInt: Paint.FontMetricsInt?
    ): Int {
        val drawable = drawable
        val bounds = drawable.bounds
        fontMetricsInt?.let {
            val paintFontMetrics = paint.fontMetricsInt
            val fontHeight = paintFontMetrics.descent - paintFontMetrics.ascent
            val drawableHeight = drawable.bounds.height()

            val centerY = paintFontMetrics.ascent + fontHeight / 2
            it.ascent = centerY - drawableHeight / 2
            it.top = it.ascent
            it.bottom = centerY + drawableHeight / 2
            it.descent = it.bottom
        }
        return bounds.right
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val drawable = drawable
        canvas.save()
        val fmPaint = paint.fontMetricsInt
        val fontHeight = fmPaint.descent - fmPaint.ascent
        val centerY = y + fmPaint.descent - fontHeight / 2
        val translationY = centerY - drawable.bounds.height() / 2
        canvas.translate(x, translationY.toFloat())
        drawable.draw(canvas)
        canvas.restore()
    }
}

@ColorInt
private fun Context.resolveColorAttr(@AttrRes colorAttr: Int): Int {
    val attribute = TypedValue().also {
        theme.resolveAttribute(colorAttr, it, true)
    }
    val colorRes = if (attribute.resourceId != 0) attribute.resourceId else attribute.data
    return ContextCompat.getColor(this, colorRes)
}
