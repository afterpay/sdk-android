/*
 * Copyright (C) 2026 Afterpay
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
package com.afterpay.android

/**
 * How the SDK presents Afterpay checkout.
 */
enum class AfterpayCheckoutPresentation {
  /** Checkout floats over the app in a dialog. This is the default. */
  MODAL,

  /**
   * Checkout fills the screen edge to edge, keeping its content clear of the system bars, display
   * cutout, and keyboard.
   */
  FULL_SCREEN,
}
