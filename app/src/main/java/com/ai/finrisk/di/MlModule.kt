package com.ai.finrisk.di

import com.ai.finrisk.data.ml.LiteRtRiskClassifier
import com.ai.finrisk.domain.classifier.RiskClassifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for ML-related dependencies.
 *
 * Binds the [RiskClassifier] interface to its [LiteRtRiskClassifier] implementation.
 *
 * ## Why @Binds instead of @Provides?
 * - @Binds is more efficient (no wrapper method generated)
 * - Used when binding interface to implementation with no custom logic
 *
 * ## Scope
 * Singleton ensures one TFLite interpreter instance is shared app-wide,
 * avoiding repeated model loading overhead.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class MlModule {

    /**
     * Provides [RiskClassifier] implementation.
     *
     * Hilt will inject [LiteRtRiskClassifier] wherever [RiskClassifier] is requested.
     */
    @Binds
    @Singleton
    abstract fun bindRiskClassifier(
        liteRtRiskClassifier: LiteRtRiskClassifier
    ): RiskClassifier
}
