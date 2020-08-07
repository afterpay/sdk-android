package com.afterpay.android.internal

import android.content.Intent
import android.text.style.URLSpan
import android.view.View

internal class AfterpayInfoSpan(url: String) : URLSpan(url) {
    override fun onClick(widget: View) {
        val context = widget.context
        val intent = Intent(context, AfterpayInfoActivity::class.java).putInfoUrlExtra(url)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            super.onClick(widget)
        }
    }
}
