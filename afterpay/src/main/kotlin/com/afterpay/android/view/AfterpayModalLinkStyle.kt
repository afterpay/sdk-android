package com.afterpay.android.view

import com.afterpay.android.Afterpay
import com.afterpay.android.R

sealed class AfterpayModalLinkStyle(internal val config: ModalLinkConfig) {
    object CircledInfoIcon : AfterpayModalLinkStyle(
        ModalLinkConfig(
            text = "\u24D8",
            underlined = false,
        ),
    )
    object MoreInfoText : AfterpayModalLinkStyle(
        ModalLinkConfig(
            text = Afterpay.strings.priceBreakdownLinkMoreInfo,
        ),
    )
    object LearnMoreText : AfterpayModalLinkStyle(
        ModalLinkConfig(
            text = Afterpay.strings.priceBreakdownLinkLearnMore,
        ),
    )
    object CircledQuestionIcon : AfterpayModalLinkStyle(
        ModalLinkConfig(
            image = R.drawable.icon_circled_question,
            imageRenderingMode = AfterpayImageRenderingMode.TEMPLATE,
        ),
    )
    object CircledLogo : AfterpayModalLinkStyle(
        ModalLinkConfig(
            image = R.drawable.afterpay_logo_small,
            imageRenderingMode = AfterpayImageRenderingMode.ORIGINAL,
        ),
    )
    object Custom : AfterpayModalLinkStyle(ModalLinkConfig()) {
        public fun setContent(content: CharSequence) {
            config.customContent = content
        }
    }
    object None : AfterpayModalLinkStyle(ModalLinkConfig())

    internal companion object {

        @JvmField
        val DEFAULT = CircledInfoIcon
    }
}

internal enum class AfterpayImageRenderingMode {
    ORIGINAL,
    TEMPLATE,
}

internal data class ModalLinkConfig(
    val text: String? = null,
    val image: Int? = null,
    val imageRenderingMode: AfterpayImageRenderingMode? = null,
    var customContent: CharSequence? = null,
    val underlined: Boolean = true,
)
