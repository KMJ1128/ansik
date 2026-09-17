package com.kmj.ansik.auth

import android.content.Context
import android.content.ContextWrapper
import android.util.Log
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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.kakao.sdk.common.util.Utility
import com.kmj.ansik.R
import com.kmj.ansik.privacy.ConsentDetailDialog
import com.kmj.ansik.privacy.RequiredConsentRow
import com.kmj.ansik.ui.AppColors

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onAuthenticated: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    val state by viewModel.uiState
    val context = LocalContext.current
    val activity = context.findActivity()

    var termsAccepted by remember { mutableStateOf(false) }
    var privacyAccepted by remember { mutableStateOf(false) }
    var showTerms by remember { mutableStateOf(false) }
    var showPrivacyCollection by remember { mutableStateOf(false) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }

    val allRequiredAccepted = termsAccepted && privacyAccepted

    LaunchedEffect(Unit) {
        val keyHash = Utility.getKeyHash(context)
        Log.d("KAKAO_KEY_HASH", "KEY HASH = $keyHash")
    }

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) onAuthenticated()
    }

    if (showTerms) {
        ConsentDetailDialog(
            title = stringResource(R.string.terms_title),
            body = stringResource(R.string.terms_body),
            onDismiss = { showTerms = false }
        )
    }
    if (showPrivacyCollection) {
        ConsentDetailDialog(
            title = stringResource(R.string.privacy_collection_title),
            body = stringResource(R.string.privacy_collection_body_corrected),
            onDismiss = { showPrivacyCollection = false }
        )
    }
    if (showPrivacyPolicy) {
        ConsentDetailDialog(
            title = stringResource(R.string.privacy_policy_title),
            body = stringResource(R.string.privacy_policy_body),
            onDismiss = { showPrivacyPolicy = false }
        )
    }

    Surface(
        color = AppColors.Background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.35f))

            Image(
                painter = painterResource(R.drawable.ansik_logo_final),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(108.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.login_welcome_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = AppColors.TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.login_welcome_description),
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 21.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = AppColors.Surface
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val next = !allRequiredAccepted
                                termsAccepted = next
                                privacyAccepted = next
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = allRequiredAccepted,
                            onCheckedChange = { checked ->
                                termsAccepted = checked
                                privacyAccepted = checked
                            }
                        )
                        Text(
                            text = stringResource(R.string.consent_all_required),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    RequiredConsentRow(
                        checked = termsAccepted,
                        label = stringResource(R.string.consent_terms_required),
                        onCheckedChange = { termsAccepted = it },
                        onDetails = { showTerms = true }
                    )
                    RequiredConsentRow(
                        checked = privacyAccepted,
                        label = stringResource(R.string.consent_privacy_required),
                        onCheckedChange = { privacyAccepted = it },
                        onDetails = { showPrivacyCollection = true }
                    )
                }
            }

            if (!allRequiredAccepted) {
                Text(
                    text = stringResource(R.string.consent_required_notice),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    color = AppColors.TextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            SocialLoginButton(
                label = stringResource(R.string.login_with_kakao),
                badge = "K",
                background = Color(0xFFFEE500),
                foreground = Color(0xFF191919),
                enabled = !state.isLoading && allRequiredAccepted
            ) {
                if (activity == null || !allRequiredAccepted) return@SocialLoginButton
                viewModel.clearError()
                SocialLoginManager.kakao(activity) { result ->
                    result
                        .onSuccess { accessToken ->
                            viewModel.exchangeProviderToken(
                                provider = "KAKAO",
                                providerToken = accessToken,
                                termsAccepted = termsAccepted,
                                privacyCollectionAccepted = privacyAccepted
                            )
                        }
                        .onFailure { error ->
                            viewModel.showProviderError(error.message)
                        }
                }
            }

            if (state.isLoading) {
                Row(
                    modifier = Modifier.padding(top = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.5.dp,
                        color = AppColors.Primary
                    )
                    Text(
                        text = stringResource(R.string.login_in_progress),
                        color = AppColors.TextSecondary
                    )
                }
            } else if (!state.errorMessage.isNullOrBlank()) {
                Text(
                    text = state.errorMessage.orEmpty(),
                    modifier = Modifier.padding(top = 12.dp),
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
                    .padding(10.dp),
                color = AppColors.TextSecondary,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = { showPrivacyPolicy = true }) {
                Text(
                    text = stringResource(R.string.privacy_policy_title),
                    color = AppColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
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
            .then(
                if (border != null) {
                    Modifier.border(width = 1.dp, color = border, shape = shape)
                } else {
                    Modifier
                }
            )
            .background(
                color = if (enabled) background else background.copy(alpha = 0.45f),
                shape = shape
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(color = foreground.copy(alpha = 0.10f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = badge,
                color = foreground,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
        }

        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = foreground.copy(alpha = if (enabled) 1f else 0.55f),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.size(28.dp))
    }
}

private tailrec fun Context.findActivity(): ComponentActivity? =
    when (this) {
        is ComponentActivity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
