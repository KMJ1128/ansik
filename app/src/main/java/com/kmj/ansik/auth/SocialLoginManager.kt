package com.kmj.ansik.auth

import androidx.activity.ComponentActivity
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NidOAuth
import com.navercorp.nid.oauth.util.NidOAuthCallback

object SocialLoginManager {

    fun kakao(
        activity: ComponentActivity,
        callback: (Result<String>) -> Unit
    ) {

        val accountLogin:
                    (OAuthToken?, Throwable?) -> Unit =
            { token, error ->

                when {

                    token != null -> {
                        callback(
                            Result.success(
                                token.accessToken
                            )
                        )
                    }

                    error != null -> {
                        callback(
                            Result.failure(error)
                        )
                    }

                    else -> {
                        callback(
                            Result.failure(
                                IllegalStateException(
                                    "카카오 로그인 토큰이 없습니다."
                                )
                            )
                        )
                    }
                }
            }

        if (
            UserApiClient.instance
                .isKakaoTalkLoginAvailable(activity)
        ) {

            UserApiClient.instance
                .loginWithKakaoTalk(activity) {
                        token,
                        error ->

                    if (
                        error is ClientError &&
                        error.reason ==
                        ClientErrorCause.Cancelled
                    ) {

                        callback(
                            Result.failure(error)
                        )

                    } else if (
                        token == null &&
                        error != null
                    ) {

                        UserApiClient.instance
                            .loginWithKakaoAccount(
                                activity,
                                callback = accountLogin
                            )

                    } else {

                        accountLogin(
                            token,
                            error
                        )
                    }
                }

        } else {

            UserApiClient.instance
                .loginWithKakaoAccount(
                    activity,
                    callback = accountLogin
                )
        }
    }

    /*
     * 현재 UI에서는 호출하지 않음.
     * 네이버 검수 완료 후 버튼을 다시 활성화하면 그대로 사용 가능.
     */
    fun naver(
        activity: ComponentActivity,
        callback: (Result<String>) -> Unit
    ) {

        NidOAuth.requestLogin(
            activity,
            object : NidOAuthCallback {

                override fun onSuccess() {

                    val token =
                        NidOAuth.getAccessToken()

                    if (token.isNullOrBlank()) {

                        callback(
                            Result.failure(
                                IllegalStateException(
                                    "네이버 로그인 토큰이 없습니다."
                                )
                            )
                        )

                    } else {

                        callback(
                            Result.success(token)
                        )
                    }
                }

                override fun onFailure(
                    errorCode: String,
                    errorDesc: String
                ) {

                    callback(
                        Result.failure(
                            IllegalStateException(
                                "$errorCode: $errorDesc"
                            )
                        )
                    )
                }
            }
        )
    }
}