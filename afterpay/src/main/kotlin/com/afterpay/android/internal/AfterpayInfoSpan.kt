/*
 * Copyright (C) 2024 Afterpay
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afterpay.android.internal

import android.content.Intent
import android.text.TextPaint
import android.text.style.URLSpan
import android.view.View

internal class AfterpayInfoSpan(url: String) : URLSpan(url) {
  private var underlined: Boolean = true

  constructor(url: String, underlined: Boolean) : this(url) {
    this.underlined = underlined
  }

  override fun onClick(widget: View) {
    val context = widget.context
    val intent = Intent(context, AfterpayInfoActivity::class.java).putInfoUrlExtra(url)
    if (intent.resolveActivity(context.packageManager) != null) {
      context.startActivity(intent)
    } else {
      super.onClick(widget)
    }
  }

  override fun updateDrawState(ds: TextPaint) {
    super.updateDrawState(ds)
    ds.isUnderlineText = this.underlined
  }
}
