/*
 * Copyright (C) 2025 Afterpay
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
package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class EntryMainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.entry_activity)

    findViewById<Button>(R.id.button_afterpay_v2).setOnClickListener {
      startActivity(Intent(this, AfterpayV2SampleActivity::class.java))
    }

    findViewById<Button>(R.id.button_afterpay_v3).setOnClickListener {
      startActivity(Intent(this, AfterpayV3SampleActivity::class.java))
    }

    findViewById<Button>(R.id.button_cashapp_v3).setOnClickListener {
      startActivity(Intent(this, CashAppV3SampleActivity::class.java))
    }

    findViewById<Button>(R.id.button_ui_widgets).setOnClickListener {
      startActivity(Intent(this, AfterpayUiGalleryActivity::class.java))
    }
  }
}
