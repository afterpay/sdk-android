// Copyright Block, Inc.
package com.example.api

/**
 * Required format to work with https://github.com/afterpay/sdk-example-server
 */
data class GetTokenResponse(
    val url: String,
    val token: String,
)
