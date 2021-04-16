package com.example.afterpay.data

import android.content.SharedPreferences
import androidx.core.content.edit
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

class AfterpayRepository(
    private val merchantApi: MerchantApi,
    private val preferences: SharedPreferences
) {
    data class Configuration(
        val minimumAmount: String?,
        val maximumAmount: String,
        val currency: String,
        val language: String,
        val country: String
    )

    suspend fun fetchConfiguration(forceRefresh: Boolean = false): Configuration {
        val cachedConfiguration = preferences.getConfiguration()

        return if (forceRefresh || shouldRefreshConfiguration() || cachedConfiguration == null) {
            merchantApi.configuration().let {
                Configuration(
                    minimumAmount = it.minimumAmount?.amount,
                    maximumAmount = it.maximumAmount.amount,
                    currency = it.maximumAmount.currency,
                    language = it.locale.language,
                    country = it.locale.country
                )
            }.also { configuration ->
                preferences.edit {
                    putConfiguration(configuration)
                    putLastFetchDate(LocalDateTime.now())
                }
            }
        } else {
            cachedConfiguration
        }
    }

    private fun shouldRefreshConfiguration(): Boolean {
        val lastFetchedDate = preferences.getLastFetchDate() ?: return true
        val daysSinceLastFetch = Duration.between(lastFetchedDate, LocalDateTime.now()).toDays()
        return daysSinceLastFetch > 1
    }
}

private object PreferenceKey {
    const val lastFetchDate = "lastFetchDate"
    const val minimumAmount = "minimumAmount"
    const val maximumAmount = "maximumAmount"
    const val currency = "currency"
    const val language = "language"
    const val country = "country"
}

private fun SharedPreferences.getLastFetchDate(): LocalDateTime? =
    getString(PreferenceKey.lastFetchDate, null)?.let {
        LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
    }

private fun SharedPreferences.getConfiguration(): AfterpayRepository.Configuration? {
    return AfterpayRepository.Configuration(
        minimumAmount = getString(PreferenceKey.minimumAmount, null),
        maximumAmount = getString(PreferenceKey.maximumAmount, null) ?: return null,
        currency = getString(PreferenceKey.currency, null) ?: return null,
        language = getString(PreferenceKey.language, null) ?: return null,
        country = getString(PreferenceKey.country, null) ?: return null
    )
}

private fun SharedPreferences.Editor.putLastFetchDate(date: LocalDateTime) {
    putString(PreferenceKey.lastFetchDate, date.format(DateTimeFormatter.ISO_DATE_TIME))
}

private fun SharedPreferences.Editor.putConfiguration(configuration: AfterpayRepository.Configuration) {
    putString(PreferenceKey.minimumAmount, configuration.minimumAmount)
    putString(PreferenceKey.maximumAmount, configuration.maximumAmount)
    putString(PreferenceKey.currency, configuration.currency)
    putString(PreferenceKey.language, configuration.language)
    putString(PreferenceKey.country, configuration.country)
}
