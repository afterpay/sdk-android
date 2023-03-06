package com.example.afterpay.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CashData(val cashTag: String, val amount: String, val grantId: String) : Parcelable
