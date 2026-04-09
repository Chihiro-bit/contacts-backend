package com.contacts.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val jwt: JwtProperties = JwtProperties(),
    val contact: ContactProperties = ContactProperties()
) {
    data class JwtProperties(
        var secret: String = "",
        var accessTokenExpiration: Long = 900000,
        var refreshTokenExpiration: Long = 2592000000
    )

    data class ContactProperties(
        var pendingDeleteWindowSeconds: Long = 5
    )
}
