package com.shawnrain.sdash.ui.poster

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun PosterPreviewScreen(
    title: String,
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
    val bitmap = remember(spec, context) { PosterRendererV2(context).render(spec) }

    LaunchedEffect(settings) {
        onSettingsChange?.invoke(settings)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth()
        )
        Text("比例", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PosterAspectRatio.entries.forEach { ratio ->
                AssistChip(
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
        Text("模板", style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            PosterTemplates.all().forEach { template ->
                AssistChip(
                    onClick = { settings = settings.copy(defaultTemplate = template.id) },
                    label = { Text(template.id.replace('_', ' ')) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onShare(settings) }, modifier = Modifier.fillMaxWidth()) {
            Text("分享图片")
        }
        Button(onClick = { onSave(settings) }, modifier = Modifier.fillMaxWidth()) {
            Text("保存到相册")
        }
    }
}
