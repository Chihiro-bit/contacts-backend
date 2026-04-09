package com.contacts.security

import com.contacts.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CustomUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserPrincipal {
        val user = userRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("User not found: $username") }
        return UserPrincipal(
            userId = user.id,
            deviceId = "",
            username = user.username,
            password = user.passwordHash
        )
    }

    fun loadUserByUserId(userId: UUID, deviceId: String): UserPrincipal {
        val user = userRepository.findById(userId)
            .orElseThrow { UsernameNotFoundException("User not found: $userId") }
        return UserPrincipal(
            userId = user.id,
            deviceId = deviceId,
            username = user.username,
            password = user.passwordHash
        )
    }
}
