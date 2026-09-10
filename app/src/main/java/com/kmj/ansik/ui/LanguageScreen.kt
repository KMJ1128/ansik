package com.kmj.ansik.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
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
        color = AppColors.Background
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
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(bottom = 36.dp),
                textAlign = TextAlign.Center
            )

            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LanguageCard(
                        flag = "🇰🇷",
                        languageName = stringResource(id = R.string.lang_korean),
                        onClick = { onLanguageSelected("ko") }
                    )
                    LanguageCard(
                        flag = "🇺🇸",
                        languageName = stringResource(id = R.string.lang_english),
                        onClick = { onLanguageSelected("en") }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LanguageCard(
                        flag = "🇯🇵",
                        languageName = stringResource(id = R.string.lang_japanese),
                        onClick = { onLanguageSelected("ja") }
                    )
                    LanguageCard(
                        flag = "🇨🇳",
                        languageName = stringResource(id = R.string.lang_chinese_simplified),
                        onClick = { onLanguageSelected("zh-CN") }
                    )
                }
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
            .size(width = 142.dp, height = 150.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
        border = BorderStroke(3.dp, AppColors.Divider),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 국기가 메인이 되도록 폰트 크기를 매우 크게 설정
            Text(
                text = flag,
                fontSize = 58.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 해당 언어 텍스트는 작게 배치
            Text(
                text = languageName,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppColors.TextPrimary
            )
        }
    }
}
