/*
 * Copyright (C) 2024 Afterpay
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afterpay.android.internal

import com.afterpay.android.BuildConfig
import timber.log.Timber

/**
 * Internal logging facade for the Afterpay SDK.
 *
 * This facade ensures that:
 * 1. Logging only occurs in DEBUG builds (checked via BuildConfig.DEBUG)
 * 2. Timber is lazily initialized with a DebugTree only when needed
 * 3. Works correctly whether or not consuming apps use Timber
 * 4. Gets completely stripped from release builds via ProGuard rules
 *
 * ## Timber Initialization
 *
 * If your app already uses Timber, plant your trees **before** calling any Afterpay SDK methods.
 * The SDK will detect existing trees and use them. If no trees exist, the SDK will plant a
 * default DebugTree for DEBUG builds only.
 *
 * ## Thread Safety
 *
 * All logging methods are thread-safe and can be called from any thread. Initialization uses
 * double-checked locking with volatile fields to ensure proper memory visibility.
 *
 * ## Release Builds
 *
 * All logging calls are automatically removed in release builds by ProGuard rules defined in
 * consumer-rules.pro. This ensures zero runtime overhead in production.
 */
internal object AfterpayLog {

  @Volatile private var initialized = false

  @Volatile private var loggingDisabled = false

  /**
   * Lazily initializes Timber if in DEBUG mode and no trees are planted yet.
   * Thread-safe with double-checked locking.
   */
  private fun ensureInitialized() {
    if (!BuildConfig.DEBUG || loggingDisabled || initialized) return

    synchronized(this) {
      if (!initialized && !loggingDisabled) {
        try {
          if (Timber.treeCount == 0) {
            Timber.plant(Timber.DebugTree())
          }
          initialized = true
        } catch (e: LinkageError) {
          // android.util.Log not available (unit tests without mocking)
          loggingDisabled = true
        } catch (e: ExceptionInInitializerError) {
          // Timber initialization failed
          loggingDisabled = true
        }
      }
    }
  }

  // ============================================================================
  // String-based logging (eager evaluation)
  // ============================================================================

  /**
   * Log a debug message.
   */
  fun d(message: String) {
    if (!BuildConfig.DEBUG || loggingDisabled) return
    try {
      ensureInitialized()
      if (!loggingDisabled) Timber.d(message)
    } catch (e: RuntimeException) {
      loggingDisabled = true
    }
  }

  /**
   * Log a debug message with arguments.
   */
  fun d(message: String, vararg args: Any?) {
    if (!BuildConfig.DEBUG || loggingDisabled) return
    try {
      ensureInitialized()
      if (!loggingDisabled) Timber.d(message, *args)
    } catch (e: RuntimeException) {
      loggingDisabled = true
    }
  }

  /**
   * Log an info message.
   */
  fun i(message: String) {
    if (!BuildConfig.DEBUG || loggingDisabled) return
    try {
      ensureInitialized()
      if (!loggingDisabled) Timber.i(message)
    } catch (e: RuntimeException) {
      loggingDisabled = true
    }
  }

  /**
   * Log an info message with arguments.
   */
  fun i(message: String, vararg args: Any?) {
    if (!BuildConfig.DEBUG || loggingDisabled) return
    try {
      ensureInitialized()
      if (!loggingDisabled) Timber.i(message, *args)
    } catch (e: RuntimeException) {
      loggingDisabled = true
    }
  }

  /**
   * Log a warning message.
   */
  fun w(message: String) {
    if (!BuildConfig.DEBUG || loggingDisabled) return
    try {
      ensureInitialized()
      if (!loggingDisabled) Timber.w(message)
    } catch (e: RuntimeException) {
      loggingDisabled = true
    }
  }

  /**
   * Log a warning message with arguments.
   */
  fun w(message: String, vararg args: Any?) {
    if (!BuildConfig.DEBUG || loggingDisabled) return
    try {
      ensureInitialized()
      if (!loggingDisabled) Timber.w(message, *args)
    } catch (e: RuntimeException) {
      loggingDisabled = true
    }
  }

  /**
   * Log a warning with a throwable.
   */
  fun w(throwable: Throwable, message: String) {
    if (!BuildConfig.DEBUG || loggingDisabled) return
    try {
      ensureInitialized()
      if (!loggingDisabled) Timber.w(throwable, message)
    } catch (e: RuntimeException) {
      loggingDisabled = true
    }
  }

  /**
   * Log an error message.
   */
  fun e(message: String) {
    if (!BuildConfig.DEBUG || loggingDisabled) return
    try {
      ensureInitialized()
      if (!loggingDisabled) Timber.e(message)
    } catch (e: RuntimeException) {
      loggingDisabled = true
    }
  }

  /**
   * Log an error message with arguments.
   */
  fun e(message: String, vararg args: Any?) {
    if (!BuildConfig.DEBUG || loggingDisabled) return
    try {
      ensureInitialized()
      if (!loggingDisabled) Timber.e(message, *args)
    } catch (e: RuntimeException) {
      loggingDisabled = true
    }
  }

  /**
   * Log an error with a throwable.
   */
  fun e(throwable: Throwable, message: String) {
    if (!BuildConfig.DEBUG || loggingDisabled) return
    try {
      ensureInitialized()
      if (!loggingDisabled) Timber.e(throwable, message)
    } catch (e: RuntimeException) {
      loggingDisabled = true
    }
  }

  // ============================================================================
  // Lambda-based logging (lazy evaluation for performance)
  // ============================================================================

  /**
   * Log a debug message using lazy evaluation.
   * The lambda is only executed if logging is enabled, avoiding string allocation overhead.
   *
   * Example: `AfterpayLog.d { "API request: ${method.name} ${url}" }`
   */
  inline fun d(messageProvider: () -> String) {
    if (!BuildConfig.DEBUG || loggingDisabled) return
    try {
      ensureInitialized()
      if (!loggingDisabled) Timber.d(messageProvider())
    } catch (e: RuntimeException) {
      loggingDisabled = true
    }
  }

  /**
   * Log an info message using lazy evaluation.
   */
  inline fun i(messageProvider: () -> String) {
    if (!BuildConfig.DEBUG || loggingDisabled) return
    try {
      ensureInitialized()
      if (!loggingDisabled) Timber.i(messageProvider())
    } catch (e: RuntimeException) {
      loggingDisabled = true
    }
  }

  /**
   * Log a warning message using lazy evaluation.
   */
  inline fun w(messageProvider: () -> String) {
    if (!BuildConfig.DEBUG || loggingDisabled) return
    try {
      ensureInitialized()
      if (!loggingDisabled) Timber.w(messageProvider())
    } catch (e: RuntimeException) {
      loggingDisabled = true
    }
  }

  /**
   * Log an error message using lazy evaluation.
   */
  inline fun e(messageProvider: () -> String) {
    if (!BuildConfig.DEBUG || loggingDisabled) return
    try {
      ensureInitialized()
      if (!loggingDisabled) Timber.e(messageProvider())
    } catch (e: RuntimeException) {
      loggingDisabled = true
    }
  }

  /**
   * Log an error with throwable using lazy evaluation.
   */
  inline fun e(throwable: Throwable, messageProvider: () -> String) {
    if (!BuildConfig.DEBUG || loggingDisabled) return
    try {
      ensureInitialized()
      if (!loggingDisabled) Timber.e(throwable, messageProvider())
    } catch (e: RuntimeException) {
      loggingDisabled = true
    }
  }

  // ============================================================================
  // Structured logging helpers
  // ============================================================================

  /**
   * Log an API request with structured data.
   */
  fun apiRequest(method: String, url: String) {
    d { "API request: $method ${sanitizeUrl(url)}" }
  }

  /**
   * Log a successful API response with structured data.
   */
  fun apiSuccess(method: String, statusCode: Int) {
    d { "API success: $method (HTTP $statusCode)" }
  }

  /**
   * Log an API error with structured data.
   */
  fun apiError(message: String, statusCode: Int, errorCode: String) {
    e { "API error: $message (HTTP $statusCode, code: $errorCode)" }
  }

  /**
   * Log a checkout event with structured data.
   */
  fun checkoutEvent(version: String, event: String, details: String = "") {
    i {
      if (details.isEmpty()) {
        "Checkout $version: $event"
      } else {
        "Checkout $version: $event - $details"
      }
    }
  }

  // ============================================================================
  // Sanitization utilities
  // ============================================================================

  /**
   * Sanitizes a token for safe logging by showing only the first 8 and last 4 characters.
   *
   * Examples:
   * - Long token: "tok_abcdefgh...wxyz" (first 8 + last 4)
   * - Short token: "****" (completely masked)
   *
   * @param token The token to sanitize
   * @return Sanitized token safe for logging
   */
  fun sanitizeToken(token: String?): String {
    if (token == null) return "null"
    return when {
      token.length <= 12 -> "****"
      else -> "${token.take(8)}...${token.takeLast(4)}"
    }
  }

  /**
   * Sanitizes a URL for safe logging by removing query parameters that might contain tokens.
   *
   * Examples:
   * - "https://api.afterpay.com/checkout?token=abc" -> "https://api.afterpay.com/checkout"
   * - "https://api.afterpay.com/checkout" -> "https://api.afterpay.com/checkout"
   *
   * @param url The URL to sanitize
   * @return Sanitized URL safe for logging
   */
  fun sanitizeUrl(url: String?): String {
    if (url == null) return "null"
    return url.substringBefore('?')
  }
}
