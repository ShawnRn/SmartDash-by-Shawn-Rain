package com.shawnrain.sdash.ui.poster

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.shawnrain.sdash.ui.poster.model.PosterAspectRatio
import com.shawnrain.sdash.ui.poster.model.PosterSpec
import com.shawnrain.sdash.ui.theme.bezierPillShape
import com.shawnrain.sdash.ui.theme.bezierRoundedShape

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun PosterPreviewScreen(
    initialSettings: PosterSettings = PosterSettings(),
    buildSpec: (PosterSettings) -> PosterSpec,
    onSettingsChange: ((PosterSettings) -> Unit)? = null,
    onShare: (PosterSettings) -> Unit,
    onSave: (PosterSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var settings by remember { mutableStateOf(initialSettings) }
    val spec = remember(settings) { buildSpec(settings) }
    val renderer = remember(context) { PosterRendererV2(context) }
    val bitmap = remember(spec, renderer) { renderer.render(spec) }
    val previewAspectRatio = remember(spec.aspectRatio) {
        spec.aspectRatio.width.toFloat() / spec.aspectRatio.height.toFloat()
    }

    LaunchedEffect(settings) {
        onSettingsChange?.invoke(settings)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = bezierRoundedShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.24f)
            ),
            tonalElevation = 2.dp,
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "海报预览",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = bezierRoundedShape(26.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.18f)
                        )
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(previewAspectRatio)
                        )
                    }
                }
            }
        }
        PosterSection(title = "比例") {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PosterAspectRatio.entries.forEach { ratio ->
                    FilterChip(
                        selected = settings.defaultAspectRatio == ratio,
                        onClick = { settings = settings.copy(defaultAspectRatio = ratio) },
                        label = {
                            Text(
                                when (ratio) {
                                    PosterAspectRatio.STORY_9_16 -> "9:16"
                                    PosterAspectRatio.PORTRAIT_4_5 -> "4:5"
                                    PosterAspectRatio.SQUARE_1_1 -> "1:1"
                                }
                            )
                        }
                    )
                }
            }
        }
        PosterSection(title = "模板", subtitle = PosterTemplates.byId(settings.defaultTemplate).description) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PosterTemplates.all().forEach { template ->
                    FilterChip(
                        selected = settings.defaultTemplate == template.id,
                        onClick = { settings = settings.copy(defaultTemplate = template.id) },
                        label = { Text(template.displayName) }
                    )
                }
            }
        }
        PosterSection(title = "显示项") {
            PosterOptionRow(
                label = "显示轨迹",
                checked = settings.showTrack,
                onCheckedChange = { checked -> settings = settings.copy(showTrack = checked) }
            )
            PosterOptionRow(
                label = "显示水印",
                checked = settings.showWatermark,
                onCheckedChange = { checked -> settings = settings.copy(showWatermark = checked) }
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = bezierRoundedShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onShare(settings) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = bezierPillShape()
                ) {
                    Text("分享图片")
                }
                OutlinedButton(
                    onClick = { onSave(settings) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = bezierPillShape()
                ) {
                    Text("保存到相册")
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun PosterOptionRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PosterSection(
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = bezierRoundedShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f)
        ),
        tonalElevation = 1.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                subtitle
                    ?.takeIf { it.isNotBlank() }
                    ?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
            }
            content()
        }
    }
}
