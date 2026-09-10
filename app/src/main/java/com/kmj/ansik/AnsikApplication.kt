package com.kmj.ansik

import android.app.Application
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import com.kmj.ansik.auth.AuthSessionStore
import com.navercorp.nid.NidOAuth
import com.navercorp.nid.core.data.datastore.NidOAuthInitializingCallback

class AnsikApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AuthSessionStore.initialize(this)

        if (BuildConfig.KAKAO_NATIVE_APP_KEY.isNotBlank()) {
            KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
        }

        if (BuildConfig.NAVER_LOGIN_CLIENT_ID.isNotBlank() &&
            BuildConfig.NAVER_LOGIN_CLIENT_SECRET.isNotBlank()
        ) {
            NidOAuth.initialize(
                this,
                BuildConfig.NAVER_LOGIN_CLIENT_ID,
                BuildConfig.NAVER_LOGIN_CLIENT_SECRET,
                getString(R.string.app_name),
                object : NidOAuthInitializingCallback {
                    override fun onSuccess() {
                        Log.i("ANSIK_AUTH", "Naver login SDK initialized")
                    }

                    override fun onFailure(e: Exception) {
                        Log.e("ANSIK_AUTH", "Naver login SDK initialization failed", e)
                    }
                }
            )
        }
    }
}
