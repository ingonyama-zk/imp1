#
# ProGuard rules for the imp1 library
#

# =======================================================================================
# == JNI RULES                                                                         ==
# =======================================================================================
#
# The JNI layer (`imp1.cpp`) finds the `NativeBridge` class and registers native methods
# by name using strings in `JNI_OnLoad`.
# If R8 renames the class or its methods, the `FindClass` or `RegisterNatives` calls
# will fail at runtime, leading to a `java.lang.UnsatisfiedLinkError`.
#
# We use `-keep` to preserve the class name and all its members. This ensures that:
#   1. `env->FindClass("com/ingonyama/imp1/NativeBridge")` succeeds.
#   2. The method names "proveNative" and "verifyNative" are not changed.
#   3. The public methods "prove" and "verify" that developers call are preserved.
#
-keep public class com.ingonyama.imp1.NativeBridge { *; }


# =======================================================================================
# == PUBLIC API RULES                                                                  ==
# =======================================================================================
#
# These classes are part of the library's public API. Consuming apps may need to
# catch the specific exception or interact with the enums by name. Keeping them
# prevents them from being renamed or removed.

# Keep the custom exception so it can be caught by the app.
-keep public class com.ingonyama.imp1.ProverException { *; }

# Keep the public enums. This preserves the enum class, its constants (e.g., Cpu, ProverSuccess),
# and its methods (e.g., fromInt, values, valueOf).
-keep public enum com.ingonyama.imp1.DeviceType { *; }
-keep public enum com.ingonyama.imp1.ProverResult { *; }
-keep public enum com.ingonyama.imp1.VerifierResult { *; }
