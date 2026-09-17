package com.kmj.ansik.auth

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kmj.ansik.privacy.PrivacyConsentStore
import com.kmj.ansik.ui.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    var uiState = mutableStateOf(
        AuthUiState(
            isAuthenticated = AuthSessionStore.isSignedIn(),
            user = AuthSessionStore.currentUser()
        )
    )
        private set

    fun exchangeProviderToken(
        provider: String,
        providerToken: String,
        termsAccepted: Boolean,
        privacyCollectionAccepted: Boolean
    ) {
        if (providerToken.isBlank() || uiState.value.isLoading) return
        if (!termsAccepted || !privacyCollectionAccepted) {
            uiState.value = uiState.value.copy(
                errorMessage = "필수 약관과 개인정보 수집·이용에 동의해 주세요."
            )
            return
        }

        uiState.value = uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            runCatching {
                RetrofitClient.api.socialLogin(
                    SocialLoginRequest(
                        provider = provider,
                        providerToken = providerToken,
                        deviceId = AuthSessionStore.deviceId(),
                        language = currentLanguage()
                    )
                )
            }.onSuccess { response ->
                AuthSessionStore.save(response)
                uiState.value = AuthUiState(
                    isAuthenticated = true,
                    user = response.user
                )
            }.onFailure { error ->
                uiState.value = AuthUiState(
                    errorMessage = friendlyMessage(error)
                )
            }
        }
    }

    fun showProviderError(message: String?) {
        uiState.value = uiState.value.copy(
            isLoading = false,
            errorMessage = message?.takeIf { it.isNotBlank() } ?: "로그인을 완료하지 못했습니다."
        )
    }

    fun clearError() {
        uiState.value = uiState.value.copy(errorMessage = null)
    }

    fun logout(onComplete: () -> Unit) {
        val refreshToken = AuthSessionStore.refreshToken()
        AuthSessionStore.clear()
        uiState.value = AuthUiState()
        onComplete()
        if (!refreshToken.isNullOrBlank()) {
            viewModelScope.launch {
                runCatching { RetrofitClient.api.logout(LogoutRequest(refreshToken)) }
            }
        }
    }

    fun deleteAccount(onComplete: () -> Unit, onFailure: (String) -> Unit) {
        if (!uiState.value.isAuthenticated || uiState.value.isLoading) return
        uiState.value = uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            runCatching { RetrofitClient.api.deleteAccount() }
                .onSuccess {
                    AuthSessionStore.clear()
                    PrivacyConsentStore.revokeSensitiveInfoConsent()
                    uiState.value = AuthUiState()
                    onComplete()
                }
                .onFailure { error ->
                    uiState.value = uiState.value.copy(isLoading = false)
                    onFailure(
                        if (error is HttpException) {
                            "계정 삭제를 완료하지 못했습니다. (${error.code()})"
                        } else {
                            "서버에 연결할 수 없어 계정을 삭제하지 못했습니다."
                        }
                    )
                }
        }
    }

    private fun currentLanguage(): String {
        val tag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
            .substringBefore(',')
            .lowercase()
        return when {
            tag.startsWith("en") -> "en"
            tag.startsWith("ja") -> "ja"
            tag.startsWith("zh") -> "zh-cn"
            else -> "ko"
        }
    }

    private fun friendlyMessage(error: Throwable): String = when (error) {
        is HttpException -> when (error.code()) {
            400 -> "요청 정보를 확인해 주세요."
            401 -> "소셜 로그인 확인에 실패했습니다. 다시 로그인해 주세요."
            429 -> "로그인 요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."
            else -> "서버에서 로그인을 완료하지 못했습니다. (${error.code()})"
        }
        else -> "서버에 연결할 수 없습니다. 네트워크와 서버 주소를 확인해 주세요."
    }
}
