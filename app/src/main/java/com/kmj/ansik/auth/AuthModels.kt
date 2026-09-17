package com.kmj.ansik.auth

data class SocialLoginRequest(
    val provider: String,
    val providerToken: String,
    val deviceId: String,
    val language: String
)

data class RefreshRequest(
    val refreshToken: String,
    val deviceId: String
)

data class LogoutRequest(val refreshToken: String)

data class AuthUser(
    val id: Long,
    val nickname: String,
    val profileImageUrl: String = "",
    val preferredLanguage: String = "ko",
    val provider: String = ""
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresInSeconds: Long,
    val user: AuthUser
)

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val user: AuthUser? = null,
    val errorMessage: String? = null
)
