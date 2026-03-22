package com.ai.finrisk.logging

import android.util.Log
import timber.log.Timber

/**
 * A Timber Tree that routes error-level logs to crash reporting.
 *
 * In a production app, this would send to Firebase Crashlytics:
 *
 * ```kotlin
 * FirebaseCrashlytics.getInstance().apply {
 *     setCustomKey("model_version", BuildConfig.MODEL_VERSION)
 *     log(message)
 *     t?.let { recordException(it) }
 * }
 * ```
 *
 * This implementation logs to Android's Log.e() as a placeholder.
 * Replace the body with Crashlytics calls after adding Firebase
 * to your project (see Chapter 6 for setup instructions).
 */
open class CrashlyticsTree : Timber.Tree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Only forward errors and above to crash reporting
        if (priority < Log.ERROR) return

        // TODO: Replace with Firebase Crashlytics when Firebase is configured
        // FirebaseCrashlytics.getInstance().apply {
        //     setCustomKey("model_version", BuildConfig.MODEL_VERSION)
        //     log(message)
        //     t?.let { recordException(it) }
        // }
        Timber.tag(tag ?: "FinRisk").e(t, message)
    }
}
