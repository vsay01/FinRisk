import tensorflow as tf
import numpy as np
import os

def create_finrisk_training_data(num_samples=10000):
    """Generate synthetic credit risk data with realistic distributions."""
    np.random.seed(42)

    # Feature 1: Annual Income ($20K-$200K)
    # Right-skewed: most applicants earn $40K-$80K, fewer earn $150K+
    income_raw = np.random.lognormal(mean=10.9, sigma=0.5, size=num_samples)
    income_raw = np.clip(income_raw, 20000, 200000)
    income_normalized = (income_raw - 20000) / (200000 - 20000)

    # Feature 2: Debt-to-Income Ratio (0-100%)
    # Beta distribution: most applicants have 20-40% DTI
    debt_ratio = np.random.beta(3, 5, num_samples)  # Peaks around 0.3-0.4

    # Feature 3: Credit Score (300-850)
    # Normal distribution centered at 680 (US median)
    credit_raw = np.random.normal(680, 80, num_samples)
    credit_raw = np.clip(credit_raw, 300, 850)
    credit_normalized = (credit_raw - 300) / (850 - 300)

    # Combine features
    X = np.column_stack([income_normalized, debt_ratio, credit_normalized]).astype(np.float32)

    # Generate labels with realistic interactions
    # Base score from weighted features
    base_score = (
            income_normalized * 0.40 +
            (1 - debt_ratio) * 0.35 +    # INVERTED: lower debt ratio = better
            credit_normalized * 0.25
    )

    # Add interaction: high income + high debt = still risky
    high_income_high_debt = (income_normalized > 0.6) & (debt_ratio > 0.5)
    interaction_penalty = np.where(high_income_high_debt, -0.15, 0.0)

    # Add interaction: low credit + any income = risky
    low_credit = credit_normalized < 0.3  # Score below ~465
    credit_penalty = np.where(low_credit, -0.10, 0.0)

    # Final score with noise and interactions
    risk_score = base_score + interaction_penalty + credit_penalty
    risk_score += np.random.normal(0, 0.08, num_samples)

    # Binary labels
    labels = (risk_score > 0.5).astype(np.float32)

    return X, labels

def create_logistic_regression_model():
    """
    Create the exact logistic regression model
    
    From slides: "z = w^T * x + b, a = sigmoid(z)"
    This is implemented as a single Dense layer with sigmoid activation.
    """
    print("🧠 Creating logistic regression model...")
    
    model = tf.keras.Sequential([
        # This Dense layer implements: z = W*x + b, output = sigmoid(z)
        # - 3 inputs: income_normalized, debt_ratio, credit_history_normalized
        # - 1 output: approval probability
        # - sigmoid activation: converts z to probability (0 to 1)
        tf.keras.layers.Dense(
            units=1,                    # Single output neuron
            activation='sigmoid',       # σ(z) = 1/(1 + e^(-z))
            input_shape=(3,),          # 3 features input
            name='logistic_layer'
        )
    ])
    
    return model

def train_model(model, X_train, y_train):
    """
    Train the model using the math concepts.
    
    The training process implements:
    - Forward propagation: z = wx + b, a = σ(z) 
    - Cost function: J = -(1/m) * Σ[y*log(a) + (1-y)*log(1-a)]
    - Backpropagation: Compute gradients and update weights
    """
    print("📚 Training model (implementing algorithms)...")
    
    # Compile model with binary crossentropy loss
    # This implements the cost function J from the lectures
    model.compile(
        optimizer=tf.keras.optimizers.Adam(learning_rate=0.001),
        loss='binary_crossentropy',  # -[y*log(ŷ) + (1-y)*log(1-ŷ)]
        metrics=['accuracy', 'precision', 'recall']
    )
    
    # Display model architecture
    print("\n🏗️  Model Architecture:")
    model.summary()
    
    # Train the model - this runs the gradient descent algorithm
    history = model.fit(
        X_train, y_train,
        epochs=50,              # Number of complete passes through data
        batch_size=32,          # Process 32 samples at a time
        validation_split=0.2,   # Hold out 20% for validation
        verbose=1               # Show training progress
    )
    
    # Extract the learned parameters (weights and bias)
    weights, bias = model.layers[0].get_weights()
    print(f"\n🎯 Learned Parameters (the 'w' and 'b'):")
    print(f"   Income weight: {weights[0][0]:.3f}")
    print(f"   Debt ratio weight: {weights[1][0]:.3f}")
    print(f"   Credit history weight: {weights[2][0]:.3f}")
    print(f"   Bias: {bias[0]:.3f}")
    
    return model, history

def verify_model(model):
    """
    Verify the trained model learned correct patterns before conversion.

    Two checks:
    1. Weight direction - do the signs match domain knowledge?
    2. Boundary tests - does the model make sensible predictions on known cases?

    Returns True if all critical checks pass.
    """
    print("\n🔍 Verifying model learned correct patterns...")
    passed = True

    # Check 1: Weight directions
    weights, bias = model.layers[0].get_weights()
    income_w, debt_w, credit_w = weights[0][0], weights[1][0], weights[2][0]

    print("\n   Weight direction check:")
    checks = [
        (income_w > 0,  f"Income weight:  {income_w:+.3f}", "positive (higher income = safer)"),
        (debt_w < 0,    f"Debt ratio weight: {debt_w:+.3f}", "negative (higher debt = riskier)"),
        (credit_w > 0,  f"Credit weight:  {credit_w:+.3f}", "positive (higher score = safer)"),
    ]
    for ok, label, expected in checks:
        symbol = "✓" if ok else "✗ WRONG"
        print(f"   {symbol} {label}  (expected {expected})")
        if not ok:
            passed = False

    # Check 2: Boundary tests with known expected outcomes
    print("\n   Boundary test cases:")
    test_cases = [
        # [income, debt_ratio, credit_history], expected decision
        ([1.0, 0.0, 1.0], "APPROVE"),   # Best possible applicant
        ([0.0, 1.0, 0.0], "DENY"),      # Worst possible applicant
        ([0.5, 0.3, 0.5], "EDGE"),      # Average -- just checking it runs
        ([1.0, 0.8, 1.0], "DENY"),      # Trap: high income but drowning in debt
        ([0.2, 0.1, 0.9], "APPROVE"),   # Low income but no debt + excellent credit
    ]

    failures = 0
    for features, expected in test_cases:
        input_data = np.array([features], dtype=np.float32)
        prob = model.predict(input_data, verbose=0)[0][0]
        decision = "APPROVE" if prob > 0.6 else ("REVIEW" if prob > 0.4 else "DENY")

        if expected == "EDGE":
            symbol = "~"  # Edge case, no pass/fail
        elif decision == expected:
            symbol = "✓"
        else:
            symbol = "✗"
            failures += 1

        print(f"   {symbol} {features} → {prob:.3f} ({decision})  expected: {expected}")

    if failures > 0:
        print(f"\n   ⚠️  {failures} boundary test(s) failed.")
        print("   For logistic regression, interaction-based cases (like high income + high debt)")
        print("   may fail because linear models can't learn feature interactions.")
        print("   This becomes the motivation for upgrading to a neural network in Chapter 5.")
        # Interaction failures are expected for logistic regression -- don't block the pipeline

    if not passed:
        print("\n   ❌ CRITICAL: Weight directions are wrong. Do not convert this model.")
    else:
        print("\n   ✅ Model verification passed.")

    return passed


def convert_to_tflite(model):
    """
    Convert the trained Keras model to TensorFlow Lite format.
    This creates the optimized model file that Android will use.
    """
    print("📱 Converting to TensorFlow Lite (mobile optimization)...")
    
    # Create TFLite converter
    converter = tf.lite.TFLiteConverter.from_keras_model(model)
    
    # Enable optimizations for mobile deployment
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    
    # Set representative dataset for quantization calibration
    # This helps convert float32 weights to int8 for smaller model size
    def representative_data_gen():
        for _ in range(100):
            # Generate sample data in the same format as training
            sample = np.random.uniform(0, 1, (1, 3)).astype(np.float32)
            yield [sample]
    
    converter.representative_dataset = representative_data_gen
    converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]
    converter.inference_input_type = tf.float32
    converter.inference_output_type = tf.float32
    
    # Perform conversion
    tflite_model = converter.convert()
    
    print(f"✅ TFLite model created")
    print(f"   Original model size: ~{model.count_params() * 4 / 1024:.1f} KB")
    print(f"   TFLite model size: {len(tflite_model) / 1024:.1f} KB")
    print(f"   Compression ratio: {(model.count_params() * 4) / len(tflite_model):.1f}x smaller")
    
    return tflite_model

def test_tflite_model(tflite_model, keras_model):
    """
    Test the TFLite model against the Keras model.

    Compares predictions from both on the same inputs to verify
    that quantization/conversion preserved accuracy.
    Returns True if the maximum difference is within tolerance.
    """
    print("\n🧪 Testing TFLite conversion accuracy...")

    # Load TFLite interpreter
    interpreter = tf.lite.Interpreter(model_content=tflite_model)
    interpreter.allocate_tensors()
    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()

    # Test inputs: same boundary cases from verify_model + a midpoint
    test_inputs = [
        [1.0, 0.0, 1.0],   # Best applicant
        [0.0, 1.0, 0.0],   # Worst applicant
        [0.5, 0.35, 0.5],  # Midpoint (reference pair for Android integration test)
        [0.8, 0.2, 0.7],   # Good applicant
    ]

    max_diff = 0.0
    print("   Keras vs TFLite comparison:")
    for features in test_inputs:
        input_data = np.array([features], dtype=np.float32)

        # Keras prediction
        keras_prob = keras_model.predict(input_data, verbose=0)[0][0]

        # TFLite prediction
        interpreter.set_tensor(input_details[0]['index'], input_data)
        interpreter.invoke()
        tflite_prob = interpreter.get_tensor(output_details[0]['index'])[0][0]

        diff = abs(float(keras_prob) - float(tflite_prob))
        max_diff = max(max_diff, diff)
        symbol = "✓" if diff < 0.01 else "✗"
        print(f"   {symbol} {features} → Keras: {keras_prob:.4f}  TFLite: {tflite_prob:.4f}  diff: {diff:.6f}")

    # Save the midpoint reference pair for Android integration test (Task 2.4)
    ref_input = np.array([[0.5, 0.35, 0.5]], dtype=np.float32)
    interpreter.set_tensor(input_details[0]['index'], ref_input)
    interpreter.invoke()
    ref_output = interpreter.get_tensor(output_details[0]['index'])[0][0]
    print(f"\n   📌 Reference pair for Android integration test:")
    print(f"      Input:  [0.5, 0.35, 0.5]")
    print(f"      Output: {ref_output:.6f}")

    tolerance = 0.01
    if max_diff < tolerance:
        print(f"\n   ✅ Max difference: {max_diff:.6f} (within {tolerance} tolerance)")
        return True
    else:
        print(f"\n   ❌ Max difference: {max_diff:.6f} EXCEEDS {tolerance} tolerance")
        print("   Quantization damaged the model. Try removing int8 quantization.")
        return False

def save_model_for_android(tflite_model, output_dir="models"):
    """
    Save the TFLite model in the format needed for Android integration.
    """
    print("💾 Saving model for Android...")
    
    # Create output directory
    os.makedirs(output_dir, exist_ok=True)
    
    # Save TFLite model
    model_filename = "finrisk_classifier.tflite"
    model_path = os.path.join(output_dir, model_filename)
    
    with open(model_path, 'wb') as f:
        f.write(tflite_model)
    
    # Create metadata file for Android developers
    metadata = {
        "model_name": "FinRisk Credit Classifier",
        "model_version": "1.0.0",
        "input_shape": [1, 3],
        "input_names": ["income_normalized", "debt_ratio", "credit_history_normalized"],
        "output_shape": [1, 1],
        "output_name": "approval_probability",
        "decision_threshold": 0.6,
        "model_size_kb": len(tflite_model) / 1024
    }
    
    import json
    metadata_path = os.path.join(output_dir, "model_metadata.json")
    with open(metadata_path, 'w') as f:
        json.dump(metadata, f, indent=2)
    
    print(f"✅ Model saved to: {model_path}")
    print(f"✅ Metadata saved to: {metadata_path}")
    print(f"\n📋 Next steps:")
    print(f"   1. Copy {model_filename} to android/app/src/main/assets/")
    print(f"   2. Use model_metadata.json as reference for Android integration")
    
    return model_path, metadata_path

def main():
    """
    Main training pipeline that implements the concepts
    """
    print("🚀 Starting FinRisk Model Training Pipeline")
    print("   Implementing Logistic Regression concepts\n")
    
    # Step 1: Generate training data (simulates historical loan data)
    X, y = create_finrisk_training_data()
    
    # Step 2: Split data for training and testing
    split_idx = int(0.8 * len(X))
    X_train, X_test = X[:split_idx], X[split_idx:]
    y_train, y_test = y[:split_idx], y[split_idx:]
    
    # Step 3: Create the logistic regression model
    model = create_logistic_regression_model()

    # Step 4: Train the model (gradient descent)
    trained_model, history = train_model(model, X_train, y_train)

    # Step 5: Verify the model learned correct patterns
    if not verify_model(trained_model):
        print("\n❌ Pipeline stopped: model failed verification.")
        return

    # Step 6: Convert to mobile-optimized format
    tflite_model = convert_to_tflite(trained_model)

    # Step 7: Verify conversion preserved accuracy
    if not test_tflite_model(tflite_model, trained_model):
        print("\n❌ Pipeline stopped: TFLite conversion damaged accuracy.")
        return

    # Step 8: Save for Android integration
    save_model_for_android(tflite_model)

    print("\n🎉 Training pipeline completed successfully!")
    print("   Your model is ready for Android integration.")

if __name__ == "__main__":
    main()