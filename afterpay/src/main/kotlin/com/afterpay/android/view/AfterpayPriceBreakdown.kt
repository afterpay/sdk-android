package com.afterpay.android.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.FrameLayout
import android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.res.use
import com.afterpay.android.Afterpay
import com.afterpay.android.R
import com.afterpay.android.internal.AfterpayInfoSpan
import com.afterpay.android.internal.AfterpayInstalment
import com.afterpay.android.internal.ConfigurationObservable
import com.afterpay.android.internal.coloredDrawable
import com.afterpay.android.internal.resolveColorAttr
import java.math.BigDecimal
import java.util.Observer

class AfterpayPriceBreakdown @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

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

    var introText: AfterpayIntroText = AfterpayIntroText.DEFAULT
        set(value) {
            field = value
            updateText()
        }

    var showWithText: Boolean = true
        set(value) {
            field = value
            updateText()
        }

    var showInterestFreeText: Boolean = true
        set(value) {
            field = value
            updateText()
        }

    var moreInfoOptions: AfterpayMoreInfoOptions = AfterpayMoreInfoOptions()
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
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
    }

    // The terms and conditions are tied to the configured locale on the configuration
    private val infoUrl: String
        get() {
            return "https://static.afterpay.com/modal/${moreInfoOptions.modalFile()}"
        }

    init {
        layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        isFocusable = true

        addView(textView)

        context.theme.obtainStyledAttributes(attrs, R.styleable.Afterpay, 0, 0).use { attributes ->
            colorScheme = AfterpayColorScheme.values()[
                attributes.getInteger(
                    R.styleable.Afterpay_afterpayColorScheme,
                    AfterpayColorScheme.DEFAULT.ordinal
                )
            ]
        }

        updateText()
    }

    private val configurationObserver = Observer { _, _ ->
        updateText()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        ConfigurationObservable.addObserver(configurationObserver)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        ConfigurationObservable.deleteObserver(configurationObserver)
    }

    private fun updateText() {
        val drawable = LayerDrawable(
            arrayOf(
                context.coloredDrawable(
                    drawableResId = R.drawable.afterpay_badge_bg,
                    colorResId = colorScheme.backgroundColorResId
                ),

                context.coloredDrawable(
                    drawableResId = Afterpay.brand.badgeForeground,
                    colorResId = colorScheme.foregroundColorResId
                )
            )
        )
            .apply {
                val aspectRatio = intrinsicWidth / intrinsicHeight.toFloat()
                val drawableHeight = textView.paint.fontMetrics.run { descent - ascent } * 2.5
                val drawableWidth = drawableHeight * aspectRatio
                setBounds(0, 0, drawableWidth.toInt(), drawableHeight.toInt())
            }

        val instalment = AfterpayInstalment.of(totalAmount, Afterpay.configuration, resources.configuration.locales[0])
        val content = generateContent(instalment)

        textView.apply {
            text = SpannableStringBuilder().apply {
                if (instalment is AfterpayInstalment.NotAvailable) {
                    append(
                        context.getString(Afterpay.brand.title),
                        CenteredImageSpan(drawable),
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                    append(" ")
                    append(content.text)
                } else {
                    append(content.text)
                    append(" ")
                    append(
                        context.getString(Afterpay.brand.title),
                        CenteredImageSpan(drawable),
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                }

                val linkStyle = moreInfoOptions.modalLinkStyle.config

                if (linkStyle.customContent != null) {
                    append(" ")
                    append(
                        linkStyle.customContent,
                        AfterpayInfoSpan(infoUrl, false),
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                } else if (linkStyle.text != null) {
                    append(" ")
                    append(
                        resources.getString(linkStyle.text),
                        AfterpayInfoSpan(infoUrl, linkStyle.underlined),
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                    )
                } else if (linkStyle.image != null && linkStyle.imageRenderingMode != null) {
                    append(" ")

                    val imageDrawable = if (linkStyle.imageRenderingMode == AfterpayImageRenderingMode.TEMPLATE) {
                        val typedValue = TypedValue()
                        context.theme.resolveAttribute(
                            android.R.attr.textColorSecondary,
                            typedValue,
                            true
                        )

                        context.coloredDrawable(
                            drawableResId = linkStyle.image,
                            colorResId = typedValue.resourceId
                        )
                    } else {
                        ResourcesCompat.getDrawable(resources, linkStyle.image, null)
                    }

                    if (imageDrawable != null) {
                        imageDrawable.apply {
                            val aspectRatio = intrinsicWidth / intrinsicHeight.toFloat()
                            val drawableHeight = textView.paint.fontMetrics.run { descent - ascent }
                            val drawableWidth = drawableHeight * aspectRatio
                            setBounds(0, 0, drawableWidth.toInt(), drawableHeight.toInt())
                        }

                        val accessibleLinkString =
                            context.getString(R.string.afterpay_price_breakdown_link_more_info_text)
                        append(
                            accessibleLinkString,
                            CenteredImageSpan(imageDrawable),
                            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                        )

                        setSpan(
                            AfterpayInfoSpan(infoUrl),
                            this.length - accessibleLinkString.length,
                            this.length,
                            Spannable.SPAN_INCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }
            contentDescription = content.description
        }
    }

    private fun generateContent(afterpay: AfterpayInstalment): Content = when (afterpay) {
        is AfterpayInstalment.Available -> {
            val withText: String = when {
                showWithText -> resources.getString(R.string.afterpay_price_breakdown_with)
                else -> ""
            }

            val interestFreeText: String = when {
                showInterestFreeText -> resources.getString(R.string.afterpay_price_breakdown_interest_free)
                else -> ""
            }

            Content(
                text = String.format(
                    resources.getString(R.string.afterpay_price_breakdown_available),
                    resources.getString(introText.resourceID),
                    interestFreeText,
                    afterpay.instalmentAmount,
                    withText
                ).trim(),
                description = String.format(
                    resources.getString(R.string.afterpay_price_breakdown_available_description),
                    resources.getString(introText.resourceID),
                    interestFreeText,
                    afterpay.instalmentAmount,
                    withText,
                    resources.getString(Afterpay.brand.description)
                ).trim()
            )
        }
        is AfterpayInstalment.NotAvailable ->
            if (afterpay.minimumAmount != null)
                Content(
                    text = String.format(
                        resources.getString(R.string.afterpay_price_breakdown_limit),
                        afterpay.minimumAmount,
                        afterpay.maximumAmount
                    ),
                    description = String.format(
                        resources.getString(R.string.afterpay_price_breakdown_limit_description),
                        resources.getString(Afterpay.brand.description),
                        afterpay.minimumAmount,
                        afterpay.maximumAmount
                    )
                )
            else
                Content(
                    text = String.format(
                        resources.getString(R.string.afterpay_price_breakdown_limit),
                        "1",
                        afterpay.maximumAmount
                    ),
                    description = String.format(
                        resources.getString(R.string.afterpay_price_breakdown_limit_description),
                        resources.getString(Afterpay.brand.description),
                        "1",
                        afterpay.maximumAmount
                    )
                )
        AfterpayInstalment.NoConfiguration ->
            Content(
                text = resources.getString(R.string.afterpay_price_breakdown_no_configuration),
                description = String.format(
                    resources.getString(R.string.afterpay_price_breakdown_no_configuration_description),
                    resources.getString(Afterpay.brand.description)
                )
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
