package com.shawnrain.sdash.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

/**
 * Compatibility wrapper around Backdrop's native layer recorder.
 *
 * The previous custom recorder copied the graphics layer but ignored Backdrop's layerBlock,
 * preventing the complete effect pipeline from being applied consistently. Keeping this wrapper
 * preserves the existing call sites while routing every glass surface through the same supported
 * source implementation used by the library examples.
 */
internal typealias FreezableLayerBackdrop = LayerBackdrop

@Composable
internal fun rememberFreezableLayerBackdrop(): FreezableLayerBackdrop = rememberLayerBackdrop()

internal fun Modifier.freezableLayerBackdrop(
    backdrop: FreezableLayerBackdrop,
    frozen: Boolean
): Modifier =
    layerBackdrop(backdrop)
