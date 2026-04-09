package com.contacts.controller

import com.contacts.dto.auth.LoginRequest
import com.contacts.dto.auth.RegisterRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `register should create user and return tokens`() {
        val request = RegisterRequest(
            username = "testuser",
            email = "test@example.com",
            password = "password123",
            deviceId = "device-001",
            deviceName = "Test Phone",
            deviceType = "ANDROID",
            publicKey = "base64encodedpublickey=="
        )

        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.accessToken") { exists() }
            jsonPath("$.data.refreshToken") { exists() }
            jsonPath("$.data.username") { value("testuser") }
        }
    }

    @Test
    fun `login with valid credentials should return tokens`() {
        val registerRequest = RegisterRequest(
            username = "loginuser",
            email = "login@example.com",
            password = "password123",
            deviceId = "device-login-001",
            deviceName = "Login Phone",
            deviceType = "IOS",
            publicKey = "base64encodedpublickey=="
        )
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(registerRequest)
        }

        val loginRequest = LoginRequest(
            usernameOrEmail = "loginuser",
            password = "password123",
            deviceId = "device-login-002",
            deviceName = "Login Phone 2",
            deviceType = "PC",
            publicKey = "base64encodedpublickey2=="
        )

        mockMvc.post("/api/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(loginRequest)
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.accessToken") { exists() }
        }
    }

    @Test
    fun `register with duplicate username should return 409`() {
        val request = RegisterRequest(
            username = "dupuser",
            email = "dup@example.com",
            password = "password123",
            deviceId = "device-dup-001",
            deviceName = "Dup Phone",
            publicKey = "key=="
        )

        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }

        val request2 = request.copy(email = "dup2@example.com", deviceId = "device-dup-002")
        mockMvc.post("/api/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request2)
        }.andExpect {
            status { isConflict() }
        }
    }
}
