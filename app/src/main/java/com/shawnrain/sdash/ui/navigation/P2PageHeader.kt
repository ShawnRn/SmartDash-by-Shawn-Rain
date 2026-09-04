package com.shawnrain.sdash.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop

@Composable
fun P2PageHeader(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    glass: Boolean = true,
    backdrop: Backdrop? = null,
    includeStatusBar: Boolean = true
) {
    val content: @Composable () -> Unit = {
        PageHeaderContent(
            title = title,
            subtitle = subtitle,
            onBack = onBack,
            includeStatusBar = includeStatusBar
        )
    }
    if (!glass || backdrop == null) {
        Column(modifier = modifier.fillMaxWidth()) { content() }
        return
    }

    DynamicBlurTopBar(backdrop = backdrop, modifier = modifier.fillMaxWidth()) {
        content()
    }
}

@Composable
private fun PageHeaderContent(
    title: String,
    subtitle: String?,
    onBack: (() -> Unit)?,
    includeStatusBar: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (includeStatusBar) Modifier.statusBarsPadding() else Modifier)
            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black
            )
        }
        subtitle
            ?.takeIf { it.isNotBlank() }
            ?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    if (onBack != null) {
                        Spacer(modifier = Modifier.width(52.dp)) // IconButton 48.dp + Spacer 4.dp = 52.dp
                    }
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
    }
}
