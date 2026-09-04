package com.shawnrain.sdash.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import com.kyant.backdrop.Backdrop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondaryScreenTopBar(
    title: String,
    onBack: () -> Unit,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val topBar: @Composable () -> Unit = {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    subtitle
                        ?.takeIf { it.isNotBlank() }
                        ?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                scrolledContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }

    if (backdrop != null) {
        DynamicBlurTopBar(
            backdrop = backdrop,
            modifier = modifier.fillMaxWidth()
        ) {
            topBar()
        }
    } else {
        Column(modifier = modifier.fillMaxWidth()) {
            topBar()
        }
    }
}
