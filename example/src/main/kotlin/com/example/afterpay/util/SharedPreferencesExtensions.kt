package com.example.afterpay.util

import android.content.SharedPreferences

private object PreferenceKey {
    const val email = "email"
    const val useV1 = "useV1"
    const val express = "express"
    const val buyNow = "buyNow"
    const val pickup = "pickup"
    const val shippingOptionsRequired = "shippingOptionsRequired"
    const val hostname = "hostname"
    const val port = "port"
}

fun SharedPreferences.getEmail(): String = getString(PreferenceKey.email, null) ?: ""
fun SharedPreferences.Editor.putEmail(email: String) = putString(PreferenceKey.email, email)

fun SharedPreferences.getExpress(): Boolean = getBoolean(PreferenceKey.express, false)
fun SharedPreferences.Editor.putExpress(isExpress: Boolean) =
    putBoolean(PreferenceKey.express, isExpress)

fun SharedPreferences.getVersion(): Boolean = getBoolean(PreferenceKey.useV1, false)
fun SharedPreferences.Editor.putVersion(useV1: Boolean) =
    putBoolean(PreferenceKey.useV1, useV1)

fun SharedPreferences.getBuyNow(): Boolean = getBoolean(PreferenceKey.buyNow, false)
fun SharedPreferences.Editor.putBuyNow(isBuyNow: Boolean) =
    putBoolean(PreferenceKey.buyNow, isBuyNow)

fun SharedPreferences.getPickup(): Boolean = getBoolean(PreferenceKey.pickup, false)
fun SharedPreferences.Editor.putPickup(isPickup: Boolean) =
    putBoolean(PreferenceKey.pickup, isPickup)

fun SharedPreferences.getShippingOptionsRequired(): Boolean =
    getBoolean(PreferenceKey.shippingOptionsRequired, false)

fun SharedPreferences.Editor.putShippingOptionsRequired(isShippingOptionsRequired: Boolean) =
    putBoolean(PreferenceKey.shippingOptionsRequired, isShippingOptionsRequired)

fun SharedPreferences.getHostname(): String = getString(PreferenceKey.hostname, "https://10.0.2.2") ?: "https://10.0.2.2"
fun SharedPreferences.Editor.putHostname(hostname: String) = putString(PreferenceKey.hostname, hostname)

fun SharedPreferences.getPort(): String = getString(PreferenceKey.port, "3001") ?: ""
fun SharedPreferences.Editor.putPort(port: String) = putString(PreferenceKey.port, port)
