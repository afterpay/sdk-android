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
package com.afterpay.android.model

import java.math.BigDecimal
import java.net.URL

interface CheckoutV3Item {
    /** Product name. Limited to 255 characters. */
    val name: String

    /** The quantity of the item, stored as a signed 32-bit integer. */
    val quantity: UInt

    /** The unit price of the individual item. Must be a positive value. */
    val price: BigDecimal

    /** Product SKU. Limited to 128 characters. */
    val sku: String?

    /** The canonical URL for the item's Product Detail Page. Limited to 2048 characters. */
    val pageUrl: URL?

    /** A URL for a web-optimised photo of the item, suitable for use directly as the src attribute of an img tag.
     * Limited to 2048 characters.
     */
    val imageUrl: URL?

    /** An array of arrays to accommodate multiple categories that might apply to the item.
     * Each array contains comma separated strings with the left-most category being the top level category.
     */
    val categories: List<List<String>>?

    /** The estimated date when the order will be shipped. YYYY-MM or YYYY-MM-DD format. */
    val estimatedShipmentDate: String?
}
