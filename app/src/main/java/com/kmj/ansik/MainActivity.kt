package com.kmj.ansik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
// 만들어둔 AnsikApp 임포트 (패키지에 맞게 조정)
import com.kmj.ansik.ui.AnsikApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AnsikApp() // ✅ 내비게이션 컨트롤러가 포함된 앱 전체 실행
                }
            }
        }
    }
}