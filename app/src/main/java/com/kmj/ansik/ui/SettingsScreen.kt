package com.kmj.ansik.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kmj.ansik.R
import com.kmj.ansik.auth.AuthUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    authUser: AuthUser?,
    onNavigateBack: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(id = R.string.settings), fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.go_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Background
                )
            )
        },
        containerColor = AppColors.Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = AppColors.PrimarySoft,
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(3.dp, AppColors.Primary.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = authUser?.nickname ?: stringResource(R.string.guest_user),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppColors.TextPrimary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (authUser == null) {
                            stringResource(R.string.guest_account_description)
                        } else {
                            stringResource(R.string.signed_in_with, authUser.provider)
                        },
                        color = AppColors.TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = if (authUser == null) onLogin else onLogout,
                        colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
                    ) {
                        Text(
                            text = stringResource(
                                if (authUser == null) R.string.sign_in else R.string.sign_out
                            ),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToLanguage() },
                color = AppColors.Surface,
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(3.dp, AppColors.Divider)
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            stringResource(id = R.string.language_settings),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AppColors.TextPrimary
                        )
                    },
                    colors = ListItemDefaults.colors(containerColor = AppColors.Surface)
                )
            }
        }
    }
}
