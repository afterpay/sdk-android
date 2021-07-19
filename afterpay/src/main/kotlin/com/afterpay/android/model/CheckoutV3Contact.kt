package com.afterpay.android.model

interface CheckoutV3Contact {
    /** Full name of contact. Limited to 255 characters */
    var name: String
    /** First line of the address. Limited to 128 characters */
    var line1: String
    /** Second line of the address. Limited to 128 characters. */
    var line2: String?
    /** Australian suburb, U.S. city, New Zealand town or city, U.K. Postal town.
     * Maximum length is 128 characters.
     */
    var area1: String?
    /** New Zealand suburb, U.K. village or local area. Maximum length is 128 characters. */
    var area2: String?
    /** U.S. state, Australian state, U.K. county, New Zealand region. Maximum length is 128 characters. */
    var region: String?
    /** The zip code or equivalent. Maximum length is 128 characters. */
    var postcode: String?
    /** The two-character ISO 3166-1 country code. */
    var countryCode: String
    /** The phone number, in E.123 format. Maximum length is 32 characters. */
    var phoneNumber: String?
}
