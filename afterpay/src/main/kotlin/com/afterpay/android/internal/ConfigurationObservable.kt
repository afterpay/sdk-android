package com.afterpay.android.internal

import java.util.Observable

internal object ConfigurationObservable : Observable() {
    fun configurationChanged(configuration: Configuration?) {
        setChanged()
        notifyObservers(configuration)
        clearChanged()
    }
}
