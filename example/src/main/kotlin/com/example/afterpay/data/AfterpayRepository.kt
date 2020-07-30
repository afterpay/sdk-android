package com.example.afterpay.data

import android.content.SharedPreferences
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
        val currency: String
    )

    suspend fun fetchConfiguration(): Configuration {
        val cachedConfiguration = preferences.getConfiguration()

        return if (shouldRefreshConfiguration() || cachedConfiguration == null) {
            merchantApi.configuration().let {
                Configuration(
                    minimumAmount = it.minimumAmount?.amount,
                    maximumAmount = it.maximumAmount.amount,
                    currency = it.maximumAmount.currency
                )
            }.also { configuration ->
                with(preferences.edit()) {
                    putConfiguration(configuration)
                    putLastFetchDate(LocalDateTime.now())
                    commit()
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
}

private fun SharedPreferences.getLastFetchDate(): LocalDateTime? =
    getString(PreferenceKey.lastFetchDate, null)?.let {
        LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
    }

private fun SharedPreferences.getConfiguration(): AfterpayRepository.Configuration? {
    return AfterpayRepository.Configuration(
        minimumAmount = getString(PreferenceKey.minimumAmount, null),
        maximumAmount = getString(PreferenceKey.maximumAmount, null) ?: return null,
        currency = getString(PreferenceKey.currency, null) ?: return null
    )
}

private fun SharedPreferences.Editor.putLastFetchDate(date: LocalDateTime) {
    putString(PreferenceKey.lastFetchDate, date.format(DateTimeFormatter.ISO_DATE_TIME))
}

private fun SharedPreferences.Editor.putConfiguration(configuration: AfterpayRepository.Configuration) {
    putString(PreferenceKey.minimumAmount, configuration.minimumAmount)
    putString(PreferenceKey.maximumAmount, configuration.maximumAmount)
    putString(PreferenceKey.currency, configuration.currency)
}
