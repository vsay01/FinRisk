# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# ProGuard rules for FinRisk ML app

# LiteRT / TensorFlow Lite — preserve JNI bridge classes
# R8 can't see native C++ calls through JNI, so it strips
# the Java wrappers thinking they're unused. This prevents
# ClassNotFoundException in release builds.
-keep class org.tensorflow.lite.** { *; }
-keep class com.google.ai.edge.litert.** { *; }

# Preserve native methods required for TFLite JNI inference
-keepclasseswithmembernames class * {
    native <methods>;
}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name. This is for meaningful crash stack traces
-renamesourcefileattribute SourceFile