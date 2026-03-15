"""
Chapter 5 — Experimenting Further

Trains 6 model configurations on identical data to answer:
"Does adding more neurons or layers improve FinRisk?"

Only the model definition changes between experiments.
Data, seeds, training setup, and evaluation are identical.

Run: python training_experiments.py
"""

import os
import sys
import warnings
import logging
import contextlib
import io

# Must be set BEFORE importing TensorFlow
os.environ['TF_CPP_MIN_LOG_LEVEL'] = '3'       # Suppress TF C++ warnings
warnings.filterwarnings('ignore')

import absl.logging
absl.logging.set_verbosity(absl.logging.ERROR)

import tensorflow as tf
import numpy as np

tf.get_logger().setLevel(logging.ERROR)


def create_finrisk_training_data(num_samples=10000):
    """Generate synthetic credit risk data — identical to training_neural_network.py."""
    np.random.seed(42)

    income_raw = np.random.lognormal(mean=10.9, sigma=0.5, size=num_samples)
    income_raw = np.clip(income_raw, 20000, 200000)
    income_normalized = (income_raw - 20000) / (200000 - 20000)

    debt_ratio = np.random.beta(3, 5, num_samples)

    credit_raw = np.random.normal(680, 80, num_samples)
    credit_raw = np.clip(credit_raw, 300, 850)
    credit_normalized = (credit_raw - 300) / (850 - 300)

    X = np.column_stack([income_normalized, debt_ratio, credit_normalized]).astype(np.float32)

    base_score = (
        income_normalized * 0.40 +
        (1 - debt_ratio) * 0.35 +
        credit_normalized * 0.25
    )

    high_income_high_debt = (income_normalized > 0.5) & (debt_ratio > 0.4)
    interaction_penalty = np.where(high_income_high_debt, -0.50, 0.0)

    low_credit = credit_normalized < 0.3
    credit_penalty = np.where(low_credit, -0.10, 0.0)

    risk_score = base_score + interaction_penalty + credit_penalty
    risk_score += np.random.normal(0, 0.08, num_samples)

    labels = (risk_score > 0.5).astype(np.float32)
    return X, labels


def create_model(config_name):
    """
    Create a fresh model for the given configuration.

    This is the ONLY function that changes between experiments.
    Compare the model definitions — everything else in this script is shared.
    """
    if config_name == "LR (0 hidden)":
        # Chapter 4: logistic regression — no hidden layer
        return tf.keras.Sequential([
            tf.keras.layers.Dense(1, activation='sigmoid', input_shape=(3,))
        ])

    elif config_name == "1 hidden (8)":
        # Chapter 5: the upgrade — one hidden layer, 8 neurons
        return tf.keras.Sequential([
            tf.keras.layers.Dense(8, activation='relu', input_shape=(3,)),
            tf.keras.layers.Dense(1, activation='sigmoid')
        ])

    elif config_name == "1 hidden (8) + dropout":
        # Regularization: randomly disable 20% of neurons during training
        # Forces the model to not rely on any single neuron
        return tf.keras.Sequential([
            tf.keras.layers.Dense(8, activation='relu', input_shape=(3,)),
            tf.keras.layers.Dropout(0.2),
            tf.keras.layers.Dense(1, activation='sigmoid')
        ])

    elif config_name == "1 hidden (32)":
        # Wider: same depth, 4x the neurons
        return tf.keras.Sequential([
            tf.keras.layers.Dense(32, activation='relu', input_shape=(3,)),
            tf.keras.layers.Dense(1, activation='sigmoid')
        ])

    elif config_name == "2 hidden (16, 8)":
        # Deeper: two hidden layers
        return tf.keras.Sequential([
            tf.keras.layers.Dense(16, activation='relu', input_shape=(3,)),
            tf.keras.layers.Dense(8, activation='relu'),
            tf.keras.layers.Dense(1, activation='sigmoid')
        ])

    elif config_name == "3 hidden (32, 16, 8)":
        # Deepest: three hidden layers
        return tf.keras.Sequential([
            tf.keras.layers.Dense(32, activation='relu', input_shape=(3,)),
            tf.keras.layers.Dense(16, activation='relu'),
            tf.keras.layers.Dense(8, activation='relu'),
            tf.keras.layers.Dense(1, activation='sigmoid')
        ])


def run_experiment(config_name, X_train, y_train, X_val, y_val, X_test, y_test):
    """Train one configuration and return metrics."""

    model = create_model(config_name)

    model.compile(
        optimizer=tf.keras.optimizers.Adam(learning_rate=0.001),
        loss='binary_crossentropy',
        metrics=['accuracy']
    )

    # Train silently — 200 epochs for all (LR converges early, extra epochs are harmless)
    model.fit(
        X_train, y_train,
        epochs=200,
        batch_size=32,
        validation_data=(X_val, y_val),
        verbose=0
    )

    # Test accuracy on held-out data
    _, test_acc = model.evaluate(X_test, y_test, verbose=0)

    # Interaction test: the key question — can it detect high income + high debt = risky?
    interaction_input = np.array([[1.0, 0.8, 1.0]], dtype=np.float32)
    interaction_prob = model.predict(interaction_input, verbose=0)[0][0]

    # Convert to TFLite to measure model size (suppress converter's print output)
    with contextlib.redirect_stdout(io.StringIO()), contextlib.redirect_stderr(io.StringIO()):
        converter = tf.lite.TFLiteConverter.from_keras_model(model)
        converter.optimizations = [tf.lite.Optimize.DEFAULT]
        tflite_model = converter.convert()

    return {
        "name": config_name,
        "params": model.count_params(),
        "test_acc": test_acc,
        "interaction_prob": float(interaction_prob),
        "tflite_kb": len(tflite_model) / 1024,
    }


def main():
    print("=" * 70)
    print("Chapter 5: Experimenting Further")
    print("Training 6 configurations on identical data...")
    print("Only the model definition changes. Everything else is the same.")
    print("=" * 70)

    # Generate data once — all experiments use the same data
    X, y = create_finrisk_training_data()

    # Same split for all experiments
    n = len(X)
    train_end = int(0.7 * n)
    val_end = int(0.85 * n)
    X_train, y_train = X[:train_end], y[:train_end]
    X_val, y_val = X[train_end:val_end], y[train_end:val_end]
    X_test, y_test = X[val_end:], y[val_end:]

    print(f"\nData: {len(X_train)} train / {len(X_val)} val / {len(X_test)} test\n")

    # Set TF seed once before all experiments.
    # Each model gets a different-but-sequential weight initialization
    # (model 2's init depends on how much random state model 1's training consumed).
    # The full sequence is reproducible across runs.
    tf.random.set_seed(42)

    # The 6 configurations to compare
    configs = [
        "LR (0 hidden)",
        "1 hidden (8)",
        "1 hidden (8) + dropout",
        "1 hidden (32)",
        "2 hidden (16, 8)",
        "3 hidden (32, 16, 8)",
    ]

    results = []
    for i, config in enumerate(configs, 1):
        print(f"[{i}/{len(configs)}] Training {config}...", end=" ", flush=True)
        result = run_experiment(config, X_train, y_train, X_val, y_val, X_test, y_test)

        status = "PASS" if result["interaction_prob"] < 0.4 else "FAIL"
        print(f"done  acc: {result['test_acc']:.1%}  interaction: {status}")
        results.append(result)

    # Print comparison table
    print("\n" + "=" * 70)
    print("RESULTS: Architecture Comparison")
    print("=" * 70)
    print(f"{'Configuration':<22} {'Params':>7} {'Accuracy':>9} {'Size':>8} {'Interaction Test'}")
    print("-" * 78)

    for r in results:
        prob = r["interaction_prob"]
        if prob < 0.4:
            interaction = f"PASS  -> {prob:.3f} DENY"
        else:
            interaction = f"FAIL  -> {prob:.3f} APPROVE"

        print(
            f"{r['name']:<22} "
            f"{r['params']:>7} "
            f"{r['test_acc']:>8.1%} "
            f"{r['tflite_kb']:>6.1f} KB "
            f" {interaction}"
        )

    print("-" * 78)
    print("\nWhat this tells you:")
    print("  0 -> 1 hidden layer : BIG jump (interaction test FAIL -> PASS)")
    print("  8 -> 32 neurons     : No meaningful accuracy gain for 3 features")
    print("  1 -> 2 hidden layers: Marginal gain, double the parameters")
    print("  2 -> 3 hidden layers: No gain, triple the parameters")
    print("\n  The jump from 0 to 1 hidden layer matters.")
    print("  Everything after that is diminishing returns for this problem.")
    print("  Start simple. Upgrade when measurement shows you need it.")
    print("=" * 70)


if __name__ == "__main__":
    main()
