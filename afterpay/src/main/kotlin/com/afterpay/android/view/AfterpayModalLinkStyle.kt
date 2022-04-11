package com.afterpay.android.view

import com.afterpay.android.R

sealed class AfterpayModalLinkStyle(val config: ModalLinkConfig) {
    constructor(string: String, config: ModalLinkConfig) : this(config)

    object CircledInfoIcon : AfterpayModalLinkStyle(ModalLinkConfig(text = R.string.afterpay_price_breakdown_link_circled_info_icon, underlined = false))
    object MoreInfoText : AfterpayModalLinkStyle(ModalLinkConfig(text = R.string.afterpay_price_breakdown_link_more_info_text))
    object LearnMoreText : AfterpayModalLinkStyle(ModalLinkConfig(text = R.string.afterpay_price_breakdown_link_learn_more_text))
    object CircledQuestionIcon : AfterpayModalLinkStyle(
        ModalLinkConfig(
            image = R.drawable.icon_circled_question,
            imageRenderingMode = AfterpayImageRenderingMode.TEMPLATE
        )
    )
    object CircledLogo : AfterpayModalLinkStyle(
        ModalLinkConfig(
            image = R.drawable.afterpay_logo_small,
            imageRenderingMode = AfterpayImageRenderingMode.ORIGINAL
        )
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
    TEMPLATE
}

internal data class ModalLinkConfig(
    val text: Int? = null,
    val image: Int? = null,
    val imageRenderingMode: AfterpayImageRenderingMode? = null,
    var customContent: CharSequence? = null,
    val underlined: Boolean = true
)
