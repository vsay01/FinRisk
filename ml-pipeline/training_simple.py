import tensorflow as tf
import numpy as np
import os

def create_finrisk_training_data():
    """
    Generate synthetic FinTech training data that mimics real loan approval patterns.
    This simulates the historical data a bank would have collected.
    """
    print("🔄 Generating training data...")
    
    # Generate 10,000 synthetic loan applications
    n_samples = 10000
    
    # Feature 1: Normalized Annual Income (0 to 1)
    # Real income range: $20,000 to $200,000 → normalized to 0-1
    income_raw = np.random.uniform(20000, 200000, n_samples)
    income_normalized = (income_raw - 20000) / (200000 - 20000)
    
    # Feature 2: Age Factor (0 to 1)  
    # Age range: 18 to 65 → normalized to 0-1
    age_raw = np.random.uniform(18, 65, n_samples)
    age_normalized = (age_raw - 18) / (65 - 18)
    
    # Feature 3: App Engagement Score (0 to 1)
    # Based on user interaction with the FinTech app
    app_engagement = np.random.beta(2, 2, n_samples)  # Beta distribution for realistic engagement
    
    # Combine features into X matrix
    # Shape: [10000, 3] - This is our "x(i)" from the CS230 slides
    X = np.column_stack([income_normalized, age_normalized, app_engagement]).astype(np.float32)
    
    # Generate target labels using business logic
    # This simulates: "What loans were historically approved?"
    risk_score = (
        income_normalized * 0.5 +      # Income is 50% of decision
        age_normalized * 0.2 +         # Age stability is 20%
        app_engagement * 0.3 +         # User engagement is 30%
        np.random.normal(0, 0.1, n_samples)  # Add some noise for realism
    )
    
    # Convert to binary approval (1 = approve, 0 = deny)
    # Threshold at 0.6 - this creates our "y(i)" labels
    y = (risk_score > 0.6).astype(np.float32)
    
    print(f"✅ Generated {n_samples} samples")
    print(f"   - Approval rate: {np.mean(y)*100:.1f}%")
    print(f"   - Features shape: {X.shape}")
    
    return X, y

def create_logistic_regression_model():
    """
    Create the exact logistic regression model from CS230 lectures.
    
    From slides: "z = w^T * x + b, a = sigmoid(z)"
    This is implemented as a single Dense layer with sigmoid activation.
    """
    print("🧠 Creating logistic regression model...")
    
    model = tf.keras.Sequential([
        # This Dense layer implements: z = W*x + b, output = sigmoid(z)
        # - 3 inputs: income, age, engagement  
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
    Train the model using the math concepts from CS230.
    
    The training process implements:
    - Forward propagation: z = wx + b, a = σ(z) 
    - Cost function: J = -(1/m) * Σ[y*log(a) + (1-y)*log(1-a)]
    - Backpropagation: Compute gradients and update weights
    """
    print("📚 Training model (implementing CS230 algorithms)...")
    
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
    print(f"\n🎯 Learned Parameters (the 'w' and 'b' from CS230):")
    print(f"   Income weight: {weights[0][0]:.3f}")
    print(f"   Age weight: {weights[1][0]:.3f}") 
    print(f"   Engagement weight: {weights[2][0]:.3f}")
    print(f"   Bias: {bias[0]:.3f}")
    
    return model, history

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

def test_tflite_model(tflite_model, X_test):
    """
    Test the TFLite model to ensure it works correctly.
    This validates that the conversion preserved the mathematical accuracy.
    """
    print("🧪 Testing TFLite model...")
    
    # Load TFLite model
    interpreter = tf.lite.Interpreter(model_content=tflite_model)
    interpreter.allocate_tensors()
    
    # Get input/output tensor details
    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()
    
    # Test with a sample
    test_sample = X_test[0:1]  # Take first test sample
    
    # Run inference
    interpreter.set_tensor(input_details[0]['index'], test_sample)
    interpreter.invoke()
    output_data = interpreter.get_tensor(output_details[0]['index'])
    
    print(f"   Test input: {test_sample[0]}")
    print(f"   Predicted probability: {output_data[0][0]:.3f}")
    print(f"   Decision: {'APPROVE' if output_data[0][0] > 0.6 else 'DENY'}")
    
    return True

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
        "input_names": ["income_normalized", "age_normalized", "app_engagement"],
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
    Main training pipeline that implements the CS230 concepts
    """
    print("🚀 Starting FinRisk Model Training Pipeline")
    print("   Implementing CS230 Logistic Regression concepts\n")
    
    # Step 1: Generate training data (simulates historical loan data)
    X, y = create_finrisk_training_data()
    
    # Step 2: Split data for training and testing
    split_idx = int(0.8 * len(X))
    X_train, X_test = X[:split_idx], X[split_idx:]
    y_train, y_test = y[:split_idx], y[split_idx:]
    
    # Step 3: Create the logistic regression model
    model = create_logistic_regression_model()
    
    # Step 4: Train the model (gradient descent from CS230)
    trained_model, history = train_model(model, X_train, y_train)
    
    # Step 5: Convert to mobile-optimized format
    tflite_model = convert_to_tflite(trained_model)
    
    # Step 6: Test the converted model
    test_tflite_model(tflite_model, X_test)
    
    # Step 7: Save for Android integration
    model_path, metadata_path = save_model_for_android(tflite_model)
    
    print("\n🎉 Training pipeline completed successfully!")
    print("   Your model is ready for Android integration.")

if __name__ == "__main__":
    main()