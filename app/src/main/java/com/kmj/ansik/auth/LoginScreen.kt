package com.kmj.ansik.auth

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kmj.ansik.R
import com.kmj.ansik.ui.AppColors
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onAuthenticated: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    val state by viewModel.uiState
    val context = LocalContext.current
    val activity = context.findActivity()
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) onAuthenticated()
    }

    Surface(color = Color(0xFFF7FAF7), modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.6f))
            Image(
                painter = painterResource(R.drawable.ansik_logo_final),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(122.dp)
            )
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = stringResource(R.string.login_welcome_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = AppColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.login_welcome_description),
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 21.sp
            )
            Spacer(modifier = Modifier.height(34.dp))

            SocialLoginButton(
                label = stringResource(R.string.login_with_kakao),
                badge = "K",
                background = Color(0xFFFEE500),
                foreground = Color(0xFF191919),
                enabled = !state.isLoading
            ) {
                if (activity == null) return@SocialLoginButton
                viewModel.clearError()
                SocialLoginManager.kakao(activity) { result ->
                    result.onSuccess { viewModel.exchangeProviderToken("KAKAO", it) }
                        .onFailure { viewModel.showProviderError(it.message) }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            SocialLoginButton(
                label = stringResource(R.string.login_with_naver),
                badge = "N",
                background = Color(0xFF03C75A),
                foreground = Color.White,
                enabled = !state.isLoading
            ) {
                if (activity == null) return@SocialLoginButton
                viewModel.clearError()
                SocialLoginManager.naver(activity) { result ->
                    result.onSuccess { viewModel.exchangeProviderToken("NAVER", it) }
                        .onFailure { viewModel.showProviderError(it.message) }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            SocialLoginButton(
                label = stringResource(R.string.login_with_google),
                badge = "G",
                background = Color.White,
                foreground = Color(0xFF202124),
                border = Color(0xFFDADCE0),
                enabled = !state.isLoading
            ) {
                if (activity == null) return@SocialLoginButton
                viewModel.clearError()
                scope.launch {
                    SocialLoginManager.google(activity)
                        .onSuccess { viewModel.exchangeProviderToken("GOOGLE", it) }
                        .onFailure { viewModel.showProviderError(it.message) }
                }
            }

            if (state.isLoading) {
                Row(
                    modifier = Modifier.padding(top = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.5.dp,
                        color = AppColors.Primary
                    )
                    Text(stringResource(R.string.login_in_progress), color = AppColors.TextSecondary)
                }
            } else if (!state.errorMessage.isNullOrBlank()) {
                Text(
                    text = state.errorMessage.orEmpty(),
                    modifier = Modifier.padding(top = 16.dp),
                    color = Color(0xFFB3261E),
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.continue_without_login),
                modifier = Modifier
                    .clickable(enabled = !state.isLoading, onClick = onContinueAsGuest)
                    .padding(14.dp),
                color = AppColors.TextSecondary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.login_terms_notice),
                color = Color(0xFF8A938C),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun SocialLoginButton(
    label: String,
    badge: String,
    background: Color,
    foreground: Color,
    enabled: Boolean,
    border: Color? = null,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .then(if (border != null) Modifier.border(1.dp, border, shape) else Modifier)
            .background(if (enabled) background else background.copy(alpha = 0.55f), shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(foreground.copy(alpha = 0.10f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(badge, color = foreground, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = foreground,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.size(28.dp))
    }
}

private tailrec fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
