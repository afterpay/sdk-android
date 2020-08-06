package com.afterpay.android.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.afterpay.android.R
import com.afterpay.android.internal.AfterpayInfoSpan
import com.afterpay.android.internal.AfterpayInstalment
import com.afterpay.android.internal.resolveColorAttr
import java.math.BigDecimal

class AfterpayPriceBreakdown(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    constructor(context: Context) : this(context, attrs = null)

    private data class Content(
        val text: String,
        val description: String
    )

    var totalAmount: BigDecimal = BigDecimal.ZERO
        set(value) {
            field = value
            updateText()
        }

    var colorScheme: AfterpayColorScheme = AfterpayColorScheme.DEFAULT
        set(value) {
            field = value
            updateText()
        }

    private val textView: TextView = TextView(context).apply {
        setTextColor(context.resolveColorAttr(android.R.attr.textColorPrimary))
        setLinkTextColor(context.resolveColorAttr(android.R.attr.textColorSecondary))
        setLineSpacing(0f, 1.2f)
        textSize = 14f
        movementMethod = LinkMovementMethod.getInstance()
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
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

        val instalment = AfterpayInstalment.of(totalAmount)
        val content = generateContent(instalment)

        textView.apply {
            text = SpannableStringBuilder().apply {
                if (instalment is AfterpayInstalment.NotAvailable) {
                    append(" ", CenteredImageSpan(drawable), Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                    append(" ")
                    append(content.text)
                } else {
                    append(content.text)
                    append(" ")
                    append(" ", CenteredImageSpan(drawable), Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                }
                append(" ")
                append(
                    resources.getString(R.string.price_breakdown_info_link),
                    AfterpayInfoSpan("https://static-us.afterpay.com/javascript/modal/us_modal.html"),
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                )
            }
            contentDescription = content.description
        }
    }

    private fun generateContent(afterpay: AfterpayInstalment): Content = when (afterpay) {
        is AfterpayInstalment.Available ->
            Content(
                text = String.format(
                    resources.getString(R.string.price_breakdown_total_cost),
                    afterpay.instalmentAmount
                ),
                description = String.format(
                    resources.getString(R.string.price_breakdown_total_cost_description),
                    afterpay.instalmentAmount
                )
            )
        is AfterpayInstalment.NotAvailable ->
            if (afterpay.minimumAmount != null)
                Content(
                    text = String.format(
                        resources.getString(R.string.price_breakdown_limit),
                        afterpay.minimumAmount,
                        afterpay.maximumAmount
                    ),
                    description = String.format(
                        resources.getString(R.string.price_breakdown_limit_description),
                        afterpay.minimumAmount,
                        afterpay.maximumAmount
                    )
                )
            else
                Content(
                    text = String.format(
                        resources.getString(R.string.price_breakdown_upper_limit),
                        afterpay.maximumAmount
                    ),
                    description = String.format(
                        resources.getString(R.string.price_breakdown_upper_limit_description),
                        afterpay.maximumAmount
                    )
                )
        AfterpayInstalment.NoConfiguration ->
            Content(
                text = resources.getString(R.string.price_breakdown_no_configuration),
                description = resources.getString(R.string.price_breakdown_no_configuration_description)
            )
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
