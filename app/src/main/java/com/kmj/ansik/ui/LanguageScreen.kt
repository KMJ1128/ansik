package com.kmj.ansik.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kmj.ansik.R

@Composable
fun LanguageScreen(
    onLanguageSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF9F9F9)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.select_language_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20),
                modifier = Modifier.padding(bottom = 48.dp),
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 한국어 카드
                LanguageCard(
                    flag = "🇰🇷",
                    languageName = stringResource(id = R.string.lang_korean),
                    onClick = { onLanguageSelected("ko") }
                )

                // 영어 카드
                LanguageCard(
                    flag = "🇺🇸",
                    languageName = stringResource(id = R.string.lang_english),
                    onClick = { onLanguageSelected("en") }
                )
            }
        }
    }
}

@Composable
fun LanguageCard(
    flag: String,
    languageName: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(130.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 국기가 메인이 되도록 폰트 크기를 매우 크게 설정
            Text(
                text = flag,
                fontSize = 64.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 해당 언어 텍스트는 작게 배치
            Text(
                text = languageName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF424242)
            )
        }
    }
}