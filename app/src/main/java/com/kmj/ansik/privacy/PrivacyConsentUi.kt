package com.kmj.ansik.privacy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kmj.ansik.R

@Composable
fun RequiredConsentRow(
    checked: Boolean,
    label: String,
    onCheckedChange: (Boolean) -> Unit,
    onDetails: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(
            text = label,
            modifier = Modifier
                .weight(1f)
                .clickable { onCheckedChange(!checked) },
            fontWeight = FontWeight.SemiBold
        )
        TextButton(onClick = onDetails) {
            Text(stringResource(R.string.consent_view_details))
        }
    }
}

@Composable
fun ConsentDetailDialog(
    title: String,
    body: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = body,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.confirm))
            }
        }
    )
}
