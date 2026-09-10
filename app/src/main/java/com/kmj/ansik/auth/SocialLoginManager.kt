package com.kmj.ansik.auth

import androidx.activity.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.kmj.ansik.BuildConfig
import com.navercorp.nid.NidOAuth
import com.navercorp.nid.oauth.util.NidOAuthCallback

object SocialLoginManager {

    suspend fun google(activity: ComponentActivity): Result<String> = runCatching {
        check(BuildConfig.GOOGLE_LOGIN_WEB_CLIENT_ID.isNotBlank()) { "Google Client ID가 없습니다." }
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_LOGIN_WEB_CLIENT_ID)
            .build()
        val result = CredentialManager.create(activity).getCredential(
            context = activity,
            request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        )
        val credential = result.credential
        check(credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) { "Google 계정 정보를 받지 못했습니다." }
        GoogleIdTokenCredential.createFrom(credential.data).idToken
    }

    fun kakao(activity: ComponentActivity, callback: (Result<String>) -> Unit) {
        val accountLogin: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            when {
                token != null -> callback(Result.success(token.accessToken))
                error != null -> callback(Result.failure(error))
                else -> callback(Result.failure(IllegalStateException("카카오 로그인 토큰이 없습니다.")))
            }
        }

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(activity)) {
            UserApiClient.instance.loginWithKakaoTalk(activity) { token, error ->
                if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                    callback(Result.failure(error))
                } else if (token == null && error != null) {
                    UserApiClient.instance.loginWithKakaoAccount(activity, callback = accountLogin)
                } else {
                    accountLogin(token, error)
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(activity, callback = accountLogin)
        }
    }

    fun naver(activity: ComponentActivity, callback: (Result<String>) -> Unit) {
        NidOAuth.requestLogin(activity, object : NidOAuthCallback {
            override fun onSuccess() {
                val token = NidOAuth.getAccessToken()
                if (token.isNullOrBlank()) {
                    callback(Result.failure(IllegalStateException("네이버 로그인 토큰이 없습니다.")))
                } else {
                    callback(Result.success(token))
                }
            }

            override fun onFailure(errorCode: String, errorDesc: String) {
                callback(Result.failure(IllegalStateException("$errorCode: $errorDesc")))
            }
        })
    }
}
