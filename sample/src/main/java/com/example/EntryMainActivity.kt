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
  }
}
