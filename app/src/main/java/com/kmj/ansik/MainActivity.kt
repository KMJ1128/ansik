package com.kmj.ansik

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity // ✅ ComponentActivity 대신 추가
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.kmj.ansik.ui.AnsikApp

// ✅ AppCompatActivity 상속으로 변경
class MainActivity : AppCompatActivity() {
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