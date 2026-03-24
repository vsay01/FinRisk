package com.ai.finrisk.di

import android.content.Context
import com.ai.finrisk.BuildConfig
import com.ai.finrisk.data.ml.FallbackRiskClassifier
import com.ai.finrisk.data.ml.LiteRtRiskClassifier
import com.ai.finrisk.domain.classifier.RiskClassifier
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import timber.log.Timber
import javax.inject.Singleton

/**
 * Hilt module for ML-related dependencies.
 *
 * Provides the [RiskClassifier] implementation based on the ML_ENABLED
 * feature flag in BuildConfig. When ML is disabled (kill switch active),
 * a [FallbackRiskClassifier] is used instead — it returns REVIEW for
 * all inputs, keeping the app functional without ML.
 *
 * In production, replace BuildConfig.ML_ENABLED with Firebase Remote
 * Config for real-time control without app updates:
 *
 * ```kotlin
 * val mlEnabled = FirebaseRemoteConfig.getInstance()
 *     .getBoolean("ml_risk_assessment_enabled")
 * ```
 */
@Module
@InstallIn(SingletonComponent::class)
object MlModule {

    @Provides
    @Singleton
    fun provideRiskClassifier(
        @ApplicationContext context: Context
    ): RiskClassifier {
        if (!BuildConfig.ML_ENABLED) {
            Timber.w("ML is disabled via feature flag — using fallback classifier")
            return FallbackRiskClassifier()
        }

        return try {
            LiteRtRiskClassifier(context)
        } catch (e: Exception) {
            Timber.e(e, "ML model failed to load — falling back to safe default")
            FallbackRiskClassifier()
        }
    }
}
