package com.shawnrain.sdash.data.dashcam

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.CaptureRequestOptions
import android.hardware.camera2.CaptureRequest
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.core.CameraEffect
import androidx.camera.effects.OverlayEffect
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.shawnrain.sdash.data.SettingsRepository
import com.shawnrain.sdash.service.DashcamForegroundService
import com.shawnrain.sdash.debug.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import kotlin.math.atan
import kotlin.math.sqrt

class DashcamManager private constructor(private val context: Context) {
    private val TAG = "DashcamManager"
    
    companion object {
        private const val DASHCAM_ULTRA_WIDE_ZOOM_RATIO = 0.6f
        private const val VIVO_WIDE_CAMERA_SESSION_ID = 3
        private const val VIVO_VIDEO_UI_MODULE = 1
        private val VIVO_OPTICAL_ZOOM_FACTOR_KEY: CaptureRequest.Key<Float> =
            CaptureRequest.Key("com.vivo.optical_zoom_factor", Float::class.javaObjectType)
        private val VIVO_EQUIVALENT_FOCAL_LENGTH_KEY: CaptureRequest.Key<Float> =
            CaptureRequest.Key("vivo.camera.sensor.focallen.35mm", Float::class.javaObjectType)
        private val VIVO_SAT_SESSION_ID_KEY: CaptureRequest.Key<Int> =
            CaptureRequest.Key("com.vivo.MultiCameraSATSelectSessionId", Int::class.javaObjectType)
        private val VIVO_CURRENT_UI_MODULE_KEY: CaptureRequest.Key<Int> =
            CaptureRequest.Key("com.vivo.current_ui_module", Int::class.javaObjectType)
        private val VIVO_CAMERA_TYPE_KEY: CaptureRequest.Key<ByteArray> =
            CaptureRequest.Key("com.vivo.chi.override.CameraType", ByteArray::class.java)
        private val VIVO_CAMERA_TYPE_VALUE = "Wide\u0000".toByteArray(Charsets.UTF_8)

        @Volatile
        private var INSTANCE: DashcamManager? = null
        
        fun getInstance(context: Context): DashcamManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DashcamManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val settingsRepository = SettingsRepository(context)
    val repository = DashcamRepository(context)
    val dashcamOverlayConfig = settingsRepository.dashcamOverlayConfig

    private val _state = MutableStateFlow(DashcamState.IDLE)
    val state: StateFlow<DashcamState> = _state.asStateFlow()

    private val _recordingDurationMs = MutableStateFlow(0L)
    val recordingDurationMs: StateFlow<Long> = _recordingDurationMs.asStateFlow()

    private val _forcedCameraId = MutableStateFlow<String?>(null)
    val forcedCameraId: StateFlow<String?> = _forcedCameraId.asStateFlow()

    fun setForcedCameraIdAndRestart(cameraId: String?) {
        AppLogger.i(TAG, "setForcedCameraIdAndRestart: $cameraId")
        _forcedCameraId.value = cameraId
        if (_state.value == DashcamState.PREVIEWING || _state.value == DashcamState.ERROR) {
            stopPreviewOnly()
            startPreviewOnly()
        }
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    private var durationTickerJob: Job? = null

    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null
    private var currentLifecycleOwner: SimpleLifecycleOwner? = null
    private var camera: androidx.camera.core.Camera? = null

    private var surfaceProvider: Preview.SurfaceProvider? = null
    private var wasPreviewingBeforeBackground = false

    private var isRecordingRequested = false
    private var pendingStartPreview = false
    private var currentSegmentId: String? = null
    private var currentSegmentStartedAt = 0L
    private var currentSegmentRideId: String? = null
    private val telemetrySamples = mutableListOf<DashcamTelemetrySample>()
    private var currentTelemetryProvider: (() -> DashcamTelemetrySample)? = null
    private var dummySurfaceTexture: android.graphics.SurfaceTexture? = null
    private var dummySurface: android.view.Surface? = null
    private var currentOverlayConfig = DashcamOverlayConfig()
    private var overlayEffect: androidx.camera.effects.OverlayEffect? = null
    private val handler = Handler(Looper.getMainLooper())
    private val segmentEndRunnable = Runnable { stopCurrentRecordingOnly() }

    init {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                AppLogger.i(TAG, "Camera provider initialized successfully")
                if (pendingStartPreview) {
                    AppLogger.i(TAG, "Executing pending preview start request after initialization")
                    startPreviewOnly()
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to initialize camera provider", e)
            }
        }, ContextCompat.getMainExecutor(context))

        coroutineScope.launch {
            settingsRepository.dashcamCameraId.collect { savedId ->
                val targetId = if (savedId == "auto") null else savedId
                if (_forcedCameraId.value != targetId) {
                    AppLogger.i(TAG, "dashcamCameraId setting changed to: $savedId")
                    _forcedCameraId.value = targetId
                    if (_state.value == DashcamState.PREVIEWING) {
                        stopPreviewOnly()
                        startPreviewOnly()
                    }
                }
            }
        }

        coroutineScope.launch {
            settingsRepository.dashcamOverlayConfig.collect { config ->
                currentOverlayConfig = config
            }
        }
    }

    private class SimpleLifecycleOwner : LifecycleOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)
        
        init {
            lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
        }
        
        override val lifecycle: Lifecycle = lifecycleRegistry
        
        fun start() {
            lifecycleRegistry.currentState = Lifecycle.State.STARTED
        }
        
        fun resume() {
            lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        }
        
        fun stop() {
            val current = lifecycleRegistry.currentState
            if (current == Lifecycle.State.DESTROYED) return
            runCatching {
                if (current == Lifecycle.State.INITIALIZED) {
                    lifecycleRegistry.currentState = Lifecycle.State.CREATED
                }
                lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
            }.onFailure { e ->
                AppLogger.e("SimpleLifecycleOwner", "Failed to transition lifecycle to DESTROYED", e)
            }
        }
    }

    private data class DashcamCameraTarget(
        val bindCameraId: String? = null,
        val physicalCameraId: String? = null,
        val zoomRatio: Float = DASHCAM_ULTRA_WIDE_ZOOM_RATIO,
        val zoomMin: Float? = null,
        val zoomMax: Float? = null,
        val useVendorUltraWideRequest: Boolean = false,
        val description: String
    )

    private data class BackCameraCandidate(
        val id: String,
        val focalLengthMm: Float?,
        val diagonalFovDegrees: Float?,
        val zoomMin: Float?,
        val zoomMax: Float?,
        val physicalIds: Set<String>,
        val isLogicalMultiCamera: Boolean
    )

    private fun getTargetRotation(): Int {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? android.hardware.display.DisplayManager
        val display = displayManager?.getDisplay(android.view.Display.DEFAULT_DISPLAY)
        val rotation = display?.rotation ?: run {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as? android.view.WindowManager
            @Suppress("DEPRECATION")
            windowManager?.defaultDisplay?.rotation ?: android.view.Surface.ROTATION_0
        }
        AppLogger.i(TAG, "Current device target rotation: $rotation")
        return rotation
    }

    private fun resolveDashcamCameraTarget(): DashcamCameraTarget {
        val forced = _forcedCameraId.value
        if (forced != null) {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? android.hardware.camera2.CameraManager
            var zoomMin: Float? = null
            var zoomMax: Float? = null
            var isBack = false
            var isPhysUW = false
            if (cameraManager != null) {
                runCatching {
                    val chars = cameraManager.getCameraCharacteristics(forced)
                    isBack = chars.get(CameraCharacteristics.LENS_FACING) == CameraMetadata.LENS_FACING_BACK
                    isPhysUW = isPhysicallyUltraWide(chars)
                    val zoomRange = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        chars.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE)
                    } else {
                        null
                    }
                    zoomMin = zoomRange?.lower
                    zoomMax = zoomRange?.upper
                }
            }
            val useVendor = isBack && !isPhysUW && (zoomMin == null || zoomMin > DASHCAM_ULTRA_WIDE_ZOOM_RATIO)
            val target = DashcamCameraTarget(
                bindCameraId = forced,
                zoomRatio = if (isPhysUW) 1.0f else DASHCAM_ULTRA_WIDE_ZOOM_RATIO,
                zoomMin = zoomMin,
                zoomMax = zoomMax,
                useVendorUltraWideRequest = useVendor,
                description = "forced camera ID $forced" + (if (isPhysUW) " (physical ultra-wide)" else "")
            )
            AppLogger.i(TAG, "Forced camera target: $target")
            return target
        }

        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? android.hardware.camera2.CameraManager
            ?: return DashcamCameraTarget(description = "default back camera")

        try {
            val discoveredIds = cameraManager.cameraIdList.toMutableList()
            for (i in 0..9) {
                val idStr = i.toString()
                if (!discoveredIds.contains(idStr)) {
                    runCatching {
                        cameraManager.getCameraCharacteristics(idStr)
                        discoveredIds.add(idStr)
                    }
                }
            }

            val candidates = discoveredIds.mapNotNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                if (chars.get(CameraCharacteristics.LENS_FACING) != CameraMetadata.LENS_FACING_BACK) {
                    return@mapNotNull null
                }

                val physicalIds = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    chars.physicalCameraIds
                } else {
                    emptySet()
                }
                val zoomRange = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                    chars.get(CameraCharacteristics.CONTROL_ZOOM_RATIO_RANGE)
                } else {
                    null
                }
                val capabilities = chars.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
                    ?.toSet()
                    .orEmpty()

                BackCameraCandidate(
                    id = id,
                    focalLengthMm = minFocalLength(chars),
                    diagonalFovDegrees = diagonalFovDegrees(chars),
                    zoomMin = zoomRange?.lower,
                    zoomMax = zoomRange?.upper,
                    physicalIds = physicalIds,
                    isLogicalMultiCamera = capabilities.contains(
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_LOGICAL_MULTI_CAMERA
                    )
                )
            }

            candidates.forEach { candidate ->
                AppLogger.i(
                    TAG,
                    "Back camera candidate id=${candidate.id} focal=${candidate.focalLengthMm} " +
                        "fov=${candidate.diagonalFovDegrees} zoom=${candidate.zoomMin}..${candidate.zoomMax} " +
                        "logical=${candidate.isLogicalMultiCamera} physical=${candidate.physicalIds}"
                )
            }

            // 优先选择 Standalone 物理超广角摄像头（焦距 < 2.5mm，FOV 最大）
            val physicalUltraWide = candidates
                .filter { !it.isLogicalMultiCamera && it.focalLengthMm != null && it.focalLengthMm < 3.2f }
                .maxByOrNull { it.diagonalFovDegrees ?: 0.0f }

            if (physicalUltraWide != null) {
                val target = DashcamCameraTarget(
                    bindCameraId = physicalUltraWide.id,
                    zoomRatio = 1.0f,
                    zoomMin = physicalUltraWide.zoomMin,
                    zoomMax = physicalUltraWide.zoomMax,
                    useVendorUltraWideRequest = false,
                    description = "physical standalone ultra-wide camera ${physicalUltraWide.id}"
                )
                AppLogger.i(TAG, "Selected standalone physical ultra-wide camera target: $target")
                return target
            }

            val logicalUltraWide = candidates
                .filter { candidate ->
                    candidate.isLogicalMultiCamera &&
                        candidate.zoomMin != null &&
                        candidate.zoomMin <= DASHCAM_ULTRA_WIDE_ZOOM_RATIO + 0.02f &&
                        candidate.zoomMax != null &&
                        candidate.zoomMax >= DASHCAM_ULTRA_WIDE_ZOOM_RATIO
                }
                .maxByOrNull { candidate ->
                    candidate.physicalIds
                        .mapNotNull { physicalId -> candidates.firstOrNull { it.id == physicalId }?.diagonalFovDegrees }
                        .maxOrNull()
                        ?: candidate.diagonalFovDegrees
                        ?: 0.0f
                }

            if (logicalUltraWide != null) {
                val widestPhysical = logicalUltraWide.physicalIds
                    .mapNotNull { physicalId -> candidates.firstOrNull { it.id == physicalId } }
                    .maxByOrNull { it.diagonalFovDegrees ?: 0.0f }
                val target = DashcamCameraTarget(
                    bindCameraId = logicalUltraWide.id,
                    physicalCameraId = widestPhysical?.id,
                    zoomRatio = DASHCAM_ULTRA_WIDE_ZOOM_RATIO,
                    zoomMin = logicalUltraWide.zoomMin,
                    zoomMax = logicalUltraWide.zoomMax,
                    description = "logical ultra-wide camera ${logicalUltraWide.id}, physical=${widestPhysical?.id}"
                )
                AppLogger.i(TAG, "Selected dashcam camera target: $target")
                return target
            }

            val standaloneUltraWide = candidates
                .filter { it.zoomMin == null || it.zoomMin <= 1.0f }
                .maxByOrNull { it.diagonalFovDegrees ?: 0.0f }

            if (standaloneUltraWide != null) {
                val useVendorUltraWideRequest = candidates.none {
                    it.isLogicalMultiCamera && it.zoomMin != null && it.zoomMin < 1.0f
                }
                val target = DashcamCameraTarget(
                    bindCameraId = standaloneUltraWide.id,
                    zoomRatio = if (useVendorUltraWideRequest) DASHCAM_ULTRA_WIDE_ZOOM_RATIO else 1.0f,
                    zoomMin = standaloneUltraWide.zoomMin,
                    zoomMax = standaloneUltraWide.zoomMax,
                    useVendorUltraWideRequest = useVendorUltraWideRequest,
                    description = if (useVendorUltraWideRequest) {
                        "public back camera ${standaloneUltraWide.id} with vendor ultra-wide request"
                    } else {
                        "standalone widest back camera ${standaloneUltraWide.id}"
                    }
                )
                AppLogger.i(TAG, "Selected dashcam camera target: $target")
                return target
            }
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to resolve dashcam camera target", e)
        }

        val fallback = DashcamCameraTarget(description = "default back camera fallback")
        AppLogger.i(TAG, "Selected dashcam camera target: $fallback")
        return fallback
    }

    private fun minFocalLength(chars: CameraCharacteristics): Float? {
        return chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
            ?.minOrNull()
    }

    private fun diagonalFovDegrees(chars: CameraCharacteristics): Float? {
        val focalLength = minFocalLength(chars) ?: return null
        val physicalSize = chars.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE) ?: return null
        if (focalLength <= 0f || physicalSize.width <= 0f || physicalSize.height <= 0f) return null
        val diagonal = sqrt(
            (physicalSize.width * physicalSize.width + physicalSize.height * physicalSize.height).toDouble()
        )
        return Math.toDegrees(2.0 * atan(diagonal / (2.0 * focalLength))).toFloat()
    }

    private fun isPhysicallyUltraWide(chars: CameraCharacteristics): Boolean {
        val focalLengths = chars.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
        val minFocal = focalLengths?.minOrNull() ?: return false
        return minFocal < 2.5f
    }

    private fun buildDashcamCameraSelector(target: DashcamCameraTarget): CameraSelector {
        val builder = CameraSelector.Builder()
        val bindCameraId = target.bindCameraId
        
        if (bindCameraId != null) {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? android.hardware.camera2.CameraManager
            val facing = runCatching {
                val chars = cameraManager?.getCameraCharacteristics(bindCameraId)
                chars?.get(CameraCharacteristics.LENS_FACING)
            }.getOrNull()
            
            if (facing == CameraMetadata.LENS_FACING_FRONT) {
                builder.requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            } else {
                builder.requireLensFacing(CameraSelector.LENS_FACING_BACK)
            }
            
            builder.addCameraFilter { cameraInfos: List<CameraInfo> ->
                val filtered = cameraInfos.filter { cameraInfo ->
                    runCatching { Camera2CameraInfo.from(cameraInfo).cameraId == bindCameraId }
                        .getOrDefault(false)
                }
                if (filtered.isNotEmpty()) {
                    ArrayList(filtered)
                } else {
                    AppLogger.e(TAG, "CameraX did not expose target cameraId=$bindCameraId; falling back to default back camera")
                    ArrayList(cameraInfos)
                }
            }
        } else {
            builder.requireLensFacing(CameraSelector.LENS_FACING_BACK)
        }
        return builder.build()
    }

    @androidx.annotation.OptIn(androidx.camera.camera2.interop.ExperimentalCamera2Interop::class)
    private fun applyCamera2TargetOptions(
        previewBuilder: Preview.Builder,
        videoCaptureBuilder: VideoCapture.Builder<Recorder>,
        target: DashcamCameraTarget
    ) {
        val physicalCameraId = target.physicalCameraId
        if (physicalCameraId != null) {
            AppLogger.i(TAG, "Forcing physical ultra-wide camera ID $physicalCameraId for ${target.description}")
            Camera2Interop.Extender<Preview>(previewBuilder).setPhysicalCameraId(physicalCameraId)
            Camera2Interop.Extender<VideoCapture<Recorder>>(videoCaptureBuilder).setPhysicalCameraId(physicalCameraId)
        }
        if (target.useVendorUltraWideRequest) {
            applyVendorUltraWideRequest(previewBuilder, videoCaptureBuilder, target)
        }
    }

    @androidx.annotation.OptIn(androidx.camera.camera2.interop.ExperimentalCamera2Interop::class)
    private fun applyVendorUltraWideRequest(
        previewBuilder: Preview.Builder,
        videoCaptureBuilder: VideoCapture.Builder<Recorder>,
        target: DashcamCameraTarget
    ) {
        runCatching {
            val previewExtender = Camera2Interop.Extender<Preview>(previewBuilder)
            val videoExtender = Camera2Interop.Extender<VideoCapture<Recorder>>(videoCaptureBuilder)
            applyVendorUltraWideOptions(previewExtender, target)
            applyVendorUltraWideOptions(videoExtender, target)
            AppLogger.i(TAG, "Applied vivo/Qualcomm vendor ultra-wide request options to Preview and VideoCapture")
        }.onFailure { e ->
            AppLogger.e(TAG, "Failed to apply vendor ultra-wide request options", e)
        }
    }

    @androidx.annotation.OptIn(androidx.camera.camera2.interop.ExperimentalCamera2Interop::class)
    private fun <T> applyVendorUltraWideOptions(extender: Camera2Interop.Extender<T>, target: DashcamCameraTarget) {
        val zoomMin = target.zoomMin ?: 1.0f
        val zoomMax = target.zoomMax ?: 1.0f
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R &&
            DASHCAM_ULTRA_WIDE_ZOOM_RATIO >= zoomMin && DASHCAM_ULTRA_WIDE_ZOOM_RATIO <= zoomMax
        ) {
            extender.setCaptureRequestOption(CaptureRequest.CONTROL_ZOOM_RATIO, DASHCAM_ULTRA_WIDE_ZOOM_RATIO)
        }
        extender.setCaptureRequestOption(VIVO_OPTICAL_ZOOM_FACTOR_KEY, DASHCAM_ULTRA_WIDE_ZOOM_RATIO)
        extender.setCaptureRequestOption(VIVO_EQUIVALENT_FOCAL_LENGTH_KEY, 15.0f)
        extender.setCaptureRequestOption(VIVO_SAT_SESSION_ID_KEY, VIVO_WIDE_CAMERA_SESSION_ID)
        extender.setCaptureRequestOption(VIVO_CURRENT_UI_MODULE_KEY, VIVO_VIDEO_UI_MODULE)
        extender.setCaptureRequestOption(VIVO_CAMERA_TYPE_KEY, VIVO_CAMERA_TYPE_VALUE)
    }
    private fun getDummySurfaceProvider(): Preview.SurfaceProvider {
        return Preview.SurfaceProvider { request ->
            val texture = android.graphics.SurfaceTexture(0).also {
                it.setDefaultBufferSize(request.resolution.width, request.resolution.height)
            }
            dummySurfaceTexture = texture
            val surface = android.view.Surface(texture)
            dummySurface = surface
            request.provideSurface(surface, ContextCompat.getMainExecutor(context)) {
                surface.release()
                texture.release()
            }
        }
    }

    private fun releaseDummySurface() {
        dummySurface?.release()
        dummySurface = null
        dummySurfaceTexture?.release()
        dummySurfaceTexture = null
    }

    private fun getOrCreateOverlayEffect(): androidx.camera.effects.OverlayEffect {
        overlayEffect?.let { return it }
        val effect = androidx.camera.effects.OverlayEffect(
            CameraEffect.VIDEO_CAPTURE,
            0,
            android.os.Handler(android.os.Looper.getMainLooper())
        ) { throwable ->
            AppLogger.e(TAG, "OverlayEffect error callback", throwable)
        }

        val textPaint = Paint().apply {
            color = Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        effect.setOnDrawListener { frame ->
            val canvas = frame.overlayCanvas
            canvas.drawColor(Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)

            val width = canvas.width
            val height = canvas.height
            val widthF = width.toFloat()
            val heightF = height.toFloat()

            // 获取画面旋转角度，动态建立逻辑画布坐标系，解耦物理旋转
            val rotation = frame.rotationDegrees
            val logicalWidth: Float
            val logicalHeight: Float

            canvas.save()
            when (rotation) {
                90 -> {
                    logicalWidth = heightF
                    logicalHeight = widthF
                    canvas.translate(0f, heightF)
                    canvas.rotate(-90f)
                }
                180 -> {
                    logicalWidth = widthF
                    logicalHeight = heightF
                    canvas.translate(widthF, heightF)
                    canvas.rotate(180f)
                }
                270 -> {
                    logicalWidth = heightF
                    logicalHeight = widthF
                    canvas.translate(widthF, 0f)
                    canvas.rotate(90f)
                }
                else -> {
                    logicalWidth = widthF
                    logicalHeight = heightF
                }
            }

            val scale = maxOf(width, height).toFloat() / 1920f
            textPaint.textSize = 40f * scale
            textPaint.setShadowLayer(4f * scale, 2f * scale, 2f * scale, Color.BLACK)

            val margin = 60f * scale
            val bottomY = logicalHeight - 60f * scale
            val topY = 90f * scale

            val sample = currentTelemetryProvider?.invoke()

            val isPortrait = logicalWidth < logicalHeight

            if (isPortrait) {
                // 竖屏优化排版：从底向上折叠为三行左对齐绘制，防止两端长文字重合
                var currentY = bottomY
                val lineSpacing = 55f * scale

                // 第一行 (最底下)：绝对时间戳
                if (currentOverlayConfig.showTime) {
                    val dateStr = timeFormat.format(Date())
                    canvas.drawText(dateStr, margin, currentY, textPaint)
                    currentY -= lineSpacing
                }

                // 第二行 (中间)：车速、功率、方向
                val sbRight = StringBuilder()
                if (currentOverlayConfig.showSpeed && sample?.speedKmH != null) {
                    sbRight.append(String.format("速度: %.1f km/h  ", sample.speedKmH))
                }
                if (currentOverlayConfig.showPower && sample?.powerKw != null) {
                    sbRight.append(String.format("功率: %.2f kW  ", sample.powerKw))
                }
                if (currentOverlayConfig.showDirection && !sample?.direction.isNullOrEmpty()) {
                    sbRight.append(String.format("方向: %s", sample.direction))
                }
                val rightText = sbRight.toString().trim()
                if (rightText.isNotEmpty()) {
                    canvas.drawText(rightText, margin, currentY, textPaint)
                    currentY -= lineSpacing
                }

                // 第三行 (最上面)：电量、电压、能耗
                val sbLeft = StringBuilder()
                if (currentOverlayConfig.showSoc && sample?.soc != null) {
                    sbLeft.append(String.format("电量: %.0f%%  ", sample.soc))
                }
                if (currentOverlayConfig.showVoltage && sample?.voltage != null) {
                    sbLeft.append(String.format("电压: %.1f V  ", sample.voltage))
                }
                if (currentOverlayConfig.showEfficiency && sample?.efficiency != null) {
                    sbLeft.append(String.format("能耗: %.1f Wh/km", sample.efficiency))
                }
                val leftText = sbLeft.toString().trim()
                if (leftText.isNotEmpty()) {
                    canvas.drawText(leftText, margin, currentY, textPaint)
                }
            } else {
                // 横屏排版，保持原有经典设计
                // 左下角：电量、电压、能耗
                val sbLeft = StringBuilder()
                if (currentOverlayConfig.showSoc && sample?.soc != null) {
                    sbLeft.append(String.format("电量: %.0f%%  ", sample.soc))
                }
                if (currentOverlayConfig.showVoltage && sample?.voltage != null) {
                    sbLeft.append(String.format("电压: %.1f V  ", sample.voltage))
                }
                if (currentOverlayConfig.showEfficiency && sample?.efficiency != null) {
                    sbLeft.append(String.format("能耗: %.1f Wh/km", sample.efficiency))
                }
                val leftText = sbLeft.toString().trim()
                if (leftText.isNotEmpty()) {
                    canvas.drawText(leftText, margin, bottomY, textPaint)
                }

                // 右下角：车速、功率、方向
                val sbRight = StringBuilder()
                if (currentOverlayConfig.showSpeed && sample?.speedKmH != null) {
                    sbRight.append(String.format("速度: %.1f km/h  ", sample.speedKmH))
                }
                if (currentOverlayConfig.showPower && sample?.powerKw != null) {
                    sbRight.append(String.format("功率: %.2f kW  ", sample.powerKw))
                }
                if (currentOverlayConfig.showDirection && !sample?.direction.isNullOrEmpty()) {
                    sbRight.append(String.format("方向: %s", sample.direction))
                }
                val rightText = sbRight.toString().trim()
                if (rightText.isNotEmpty()) {
                    canvas.drawText(rightText, logicalWidth - textPaint.measureText(rightText) - margin, bottomY, textPaint)
                }

                // 右上角（或左上角）：绘制绝对时间戳
                if (currentOverlayConfig.showTime) {
                    val dateStr = timeFormat.format(Date())
                    canvas.drawText(dateStr, logicalWidth - textPaint.measureText(dateStr) - margin, topY, textPaint)
                }
            }

            canvas.restore()
            true
        }
        overlayEffect = effect
        return effect
    }

    fun setPreviewSurfaceProvider(provider: Preview.SurfaceProvider?) {
        if (this.surfaceProvider === provider) return
        this.surfaceProvider = provider
        
        if (_state.value == DashcamState.RECORDING) {
            if (provider != null) {
                AppLogger.i(TAG, "setPreviewSurfaceProvider: Restoring real preview surface during active recording")
                preview?.setSurfaceProvider(provider)
                releaseDummySurface()
            } else {
                AppLogger.i(TAG, "setPreviewSurfaceProvider: Switching to dummy preview surface because real preview was detached during active recording")
                preview?.setSurfaceProvider(getDummySurfaceProvider())
            }
            return
        }
        
        preview?.setSurfaceProvider(provider)
        
        if (provider != null && _state.value == DashcamState.IDLE) {
            startPreviewOnly()
        } else if (provider == null && _state.value == DashcamState.PREVIEWING) {
            stopPreviewOnly()
        }
    }

    @androidx.annotation.OptIn(androidx.camera.camera2.interop.ExperimentalCamera2Interop::class)
    internal fun startPreviewOnly() {
        if (_state.value != DashcamState.IDLE) return
        val provider = cameraProvider
        if (provider == null) {
            AppLogger.i(TAG, "Camera provider not ready when startPreviewOnly() called. Queueing request.")
            pendingStartPreview = true
            return
        }
        pendingStartPreview = false
        
        try {
            provider.unbindAll()
            val lifecycleOwner = SimpleLifecycleOwner()
            currentLifecycleOwner = lifecycleOwner

            val cameraTarget = resolveDashcamCameraTarget()
            val cameraSelector = buildDashcamCameraSelector(cameraTarget)
            val targetRotation = getTargetRotation()

            val previewBuilder = Preview.Builder()
                .setTargetRotation(targetRotation)

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.FHD))
                .build()
                
            val videoCaptureBuilder = VideoCapture.Builder(recorder)
                .setTargetRotation(targetRotation)
            applyCamera2TargetOptions(previewBuilder, videoCaptureBuilder, cameraTarget)

            preview = previewBuilder.build().also {
                it.setSurfaceProvider(surfaceProvider)
            }
            videoCapture = videoCaptureBuilder.build()

            val useCaseGroupBuilder = UseCaseGroup.Builder()
                .addUseCase(preview!!)
                .addUseCase(videoCapture!!)
            val hasWatermark = currentOverlayConfig.showTime || currentOverlayConfig.showSpeed ||
                    currentOverlayConfig.showPower || currentOverlayConfig.showDirection ||
                    currentOverlayConfig.showVoltage || currentOverlayConfig.showSoc ||
                    currentOverlayConfig.showEfficiency
            if (hasWatermark) {
                useCaseGroupBuilder.addEffect(getOrCreateOverlayEffect())
            }
            val useCaseGroup = useCaseGroupBuilder.build()

            camera = provider.bindToLifecycle(lifecycleOwner, cameraSelector, useCaseGroup)
            applyUltraWideZoomIfNeeded(cameraTarget.zoomRatio)

            lifecycleOwner.start()
            lifecycleOwner.resume()

            _state.value = DashcamState.PREVIEWING
            AppLogger.i(TAG, "Camera preview and video capture bound successfully (Previewing)")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to start camera preview", e)
            runCatching { currentLifecycleOwner?.stop() }
            currentLifecycleOwner = null
            _state.value = DashcamState.ERROR
            if (_forcedCameraId.value != null) {
                val failedId = _forcedCameraId.value
                AppLogger.w(TAG, "Failed to bind to forced cameraId $failedId, resetting to auto fallback")
                _forcedCameraId.value = null
                coroutineScope.launch {
                    settingsRepository.saveDashcamCameraId("auto")
                }
                _state.value = DashcamState.IDLE
                handler.post {
                    startPreviewOnly()
                }
            }
        }
    }

    internal fun stopPreviewOnly() {
        if (_state.value != DashcamState.PREVIEWING && _state.value != DashcamState.ERROR) return
        try {
            cameraProvider?.unbindAll()
            runCatching { currentLifecycleOwner?.stop() }
            currentLifecycleOwner = null
            preview = null
            videoCapture = null
            camera = null
            _state.value = DashcamState.IDLE
            AppLogger.i(TAG, "Camera preview stopped and resources released")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to stop camera preview", e)
        }
    }

    fun onAppVisibilityChanged(isForeground: Boolean) {
        AppLogger.i(TAG, "onAppVisibilityChanged: isForeground=$isForeground, state=${_state.value}, hasSurfaceProvider=${surfaceProvider != null}, wasPreviewingBeforeBackground=$wasPreviewingBeforeBackground")
        if (isForeground) {
            if (_state.value == DashcamState.RECORDING) {
                if (surfaceProvider != null) {
                    AppLogger.i(TAG, "Restoring real preview surface during recording")
                    preview?.setSurfaceProvider(surfaceProvider)
                }
            } else if (wasPreviewingBeforeBackground) {
                wasPreviewingBeforeBackground = false
                AppLogger.i(TAG, "Restoring preview automatically after returning to foreground")
                startPreviewOnly()
            } else if (surfaceProvider != null && _state.value == DashcamState.IDLE) {
                startPreviewOnly()
            }
            releaseDummySurface()
        } else {
            if (_state.value == DashcamState.RECORDING) {
                AppLogger.i(TAG, "Switching to dummy preview surface for background recording")
                preview?.setSurfaceProvider(getDummySurfaceProvider())
            } else if (_state.value == DashcamState.PREVIEWING) {
                wasPreviewingBeforeBackground = true
                stopPreviewOnly()
            } else {
                wasPreviewingBeforeBackground = false
            }
        }
    }

    fun startRecording(rideId: String?, telemetryProvider: () -> DashcamTelemetrySample) {
        if (isRecordingRequested) return
        isRecordingRequested = true
        currentSegmentRideId = rideId
        currentTelemetryProvider = telemetryProvider
        _state.value = DashcamState.RECORDING

        DashcamForegroundService.startService(context)

        coroutineScope.launch {
            try {
                if (videoCapture == null) {
                    setupCameraForRecording()
                }
                startNextSegment()
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to setup camera recording", e)
                _state.value = DashcamState.ERROR
                isRecordingRequested = false
                DashcamForegroundService.stopService(context)
            }
        }
    }

    fun stopRecording() {
        if (!isRecordingRequested) return
        isRecordingRequested = false
        handler.removeCallbacks(segmentEndRunnable)
        activeRecording?.stop()
        activeRecording = null
        stopDurationTicker()
        currentTelemetryProvider = null
        releaseDummySurface()

        if (surfaceProvider != null) {
            _state.value = DashcamState.PREVIEWING
            AppLogger.i(TAG, "Dashcam recording stopped, reverting to previewing")
        } else {
            try {
                cameraProvider?.unbindAll()
                currentLifecycleOwner?.stop()
                currentLifecycleOwner = null
                preview = null
                videoCapture = null
                camera = null
            } catch (e: Exception) {
                AppLogger.e(TAG, "Error unbinding camera on stop", e)
            }
            _state.value = DashcamState.IDLE
            AppLogger.i(TAG, "Dashcam recording completely stopped and camera released")
        }
        
        DashcamForegroundService.stopService(context)
    }

    @androidx.annotation.OptIn(androidx.camera.camera2.interop.ExperimentalCamera2Interop::class)
    private fun setupCameraForRecording() {
        val provider = cameraProvider ?: throw IllegalStateException("Camera provider not initialized")
        provider.unbindAll()

        val lifecycleOwner = SimpleLifecycleOwner()
        currentLifecycleOwner = lifecycleOwner

        val cameraTarget = resolveDashcamCameraTarget()
        val cameraSelector = buildDashcamCameraSelector(cameraTarget)
        val targetRotation = getTargetRotation()

        val previewBuilder = Preview.Builder()
            .setTargetRotation(targetRotation)

        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.FHD))
            .build()

        val videoCaptureBuilder = VideoCapture.Builder(recorder)
            .setTargetRotation(targetRotation)
        applyCamera2TargetOptions(previewBuilder, videoCaptureBuilder, cameraTarget)

        preview = previewBuilder.build().also {
            it.setSurfaceProvider(surfaceProvider ?: getDummySurfaceProvider())
        }
        videoCapture = videoCaptureBuilder.build()

        val useCaseGroupBuilder = UseCaseGroup.Builder()
            .addUseCase(preview!!)
            .addUseCase(videoCapture!!)
        val hasWatermark = currentOverlayConfig.showTime || currentOverlayConfig.showSpeed ||
                currentOverlayConfig.showPower || currentOverlayConfig.showDirection ||
                currentOverlayConfig.showVoltage || currentOverlayConfig.showSoc ||
                currentOverlayConfig.showEfficiency
        if (hasWatermark) {
            useCaseGroupBuilder.addEffect(getOrCreateOverlayEffect())
        }
        val useCaseGroup = useCaseGroupBuilder.build()

        camera = provider.bindToLifecycle(lifecycleOwner, cameraSelector, useCaseGroup)
        applyUltraWideZoomIfNeeded(cameraTarget.zoomRatio)
        
        lifecycleOwner.start()
        lifecycleOwner.resume()
    }

    private fun applyUltraWideZoomIfNeeded(targetZoomRatio: Float) {
        val cam = camera ?: return
        handler.post {
            try {
                val liveData = cam.cameraInfo.zoomState
                liveData.observeForever(object : androidx.lifecycle.Observer<androidx.camera.core.ZoomState> {
                    override fun onChanged(value: androidx.camera.core.ZoomState) {
                        if (targetZoomRatio in value.minZoomRatio..value.maxZoomRatio) {
                            AppLogger.i(
                                TAG,
                                "Applying dashcam zoom ratio $targetZoomRatio " +
                                    "(supported ${value.minZoomRatio}..${value.maxZoomRatio})"
                            )
                            cam.cameraControl.setZoomRatio(targetZoomRatio)
                        } else {
                            try {
                                val camera2CameraControl = Camera2CameraControl.from(cam.cameraControl)
                                val builder = CaptureRequestOptions.Builder()
                                builder.setCaptureRequestOption(
                                    VIVO_OPTICAL_ZOOM_FACTOR_KEY,
                                    targetZoomRatio
                                )
                                builder.setCaptureRequestOption(
                                    VIVO_EQUIVALENT_FOCAL_LENGTH_KEY,
                                    15.0f
                                )
                                builder.setCaptureRequestOption(
                                    VIVO_SAT_SESSION_ID_KEY,
                                    VIVO_WIDE_CAMERA_SESSION_ID
                                )
                                builder.setCaptureRequestOption(
                                    VIVO_CURRENT_UI_MODULE_KEY,
                                    VIVO_VIDEO_UI_MODULE
                                )
                                builder.setCaptureRequestOption(
                                    VIVO_CAMERA_TYPE_KEY,
                                    VIVO_CAMERA_TYPE_VALUE
                                )
                                val requestOptions = builder.build()
                                camera2CameraControl.captureRequestOptions = requestOptions
                                AppLogger.i(TAG, "Injected Camera2/vendor zoom ratio $targetZoomRatio to request ultra-wide")
                            } catch (ex: Exception) {
                                AppLogger.e(TAG, "Failed to inject Camera2 Interop options", ex)
                            }
                        }
                        liveData.removeObserver(this)
                    }
                })
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to register zoomState observer", e)
            }
        }
    }

    @android.annotation.SuppressLint("MissingPermission")
    private fun startNextSegment() {
        if (!isRecordingRequested) return

        val capture = videoCapture ?: return
        val segmentId = UUID.randomUUID().toString()
        currentSegmentId = segmentId
        currentSegmentStartedAt = System.currentTimeMillis()
        telemetrySamples.clear()

        val videoFile = repository.getNewVideoFile(segmentId)
        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        coroutineScope.launch {
            try {
                val audioEnabled = settingsRepository.dashcamRecordAudio.first()
                val segmentDurationMin = settingsRepository.dashcamSegmentDurationMin.first()

                val pendingRecording = capture.output.prepareRecording(context, outputOptions)
                if (audioEnabled && ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    pendingRecording.withAudioEnabled()
                }

                activeRecording = pendingRecording.start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                    when (recordEvent) {
                        is VideoRecordEvent.Start -> {
                            AppLogger.i(TAG, "Segment started: $segmentId")
                            _state.value = DashcamState.RECORDING
                            startDurationTicker()

                            handler.postDelayed(segmentEndRunnable, segmentDurationMin.toLong() * 60 * 1000)
                        }
                        is VideoRecordEvent.Finalize -> {
                            val duration = System.currentTimeMillis() - currentSegmentStartedAt
                            stopDurationTicker()

                            if (recordEvent.hasError()) {
                                AppLogger.e(TAG, "Segment finalize error: ${recordEvent.error}")
                                if (recordEvent.error != VideoRecordEvent.Finalize.ERROR_NONE && 
                                    recordEvent.error != VideoRecordEvent.Finalize.ERROR_RECORDING_GARBAGE_COLLECTED &&
                                    isRecordingRequested) {
                                    _state.value = DashcamState.ERROR
                                    AppLogger.e(TAG, "Non-fatal finalize error ${recordEvent.error}. Retaining camera and retrying loop in 1s.")
                                }
                            }

                            val samplesCopy = synchronized(telemetrySamples) { telemetrySamples.toList() }
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    repository.saveMetadataSync(segmentId, currentSegmentStartedAt, duration, currentSegmentRideId)
                                    repository.saveSidecarSync(segmentId, samplesCopy)
                                    repository.loadSegmentsSync()
                                    
                                    val limitMb = settingsRepository.dashcamStorageLimitMb.first()
                                    repository.checkStorageLimitAndCleanup(limitMb)
                                } catch (e: Exception) {
                                    AppLogger.e(TAG, "Error saving segment sync files on IO dispatcher", e)
                                }
                            }

                            if (isRecordingRequested) {
                                _state.value = DashcamState.SEGMENT_GAP
                                handler.postDelayed({
                                    if (isRecordingRequested) {
                                        startNextSegment()
                                    }
                                }, 1000)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "Failed to prepare or start next recording segment", e)
                if (isRecordingRequested) {
                    handler.postDelayed({
                        if (isRecordingRequested) {
                            startNextSegment()
                        }
                    }, 1000)
                }
            }
        }
    }

    private fun stopCurrentRecordingOnly() {
        activeRecording?.stop()
        activeRecording = null
    }

    private fun startDurationTicker() {
        durationTickerJob?.cancel()
        _recordingDurationMs.value = 0L
        durationTickerJob = coroutineScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                val elapsed = System.currentTimeMillis() - currentSegmentStartedAt
                _recordingDurationMs.value = elapsed
                
                currentTelemetryProvider?.let { provider ->
                    val sample = provider().copy(offsetMs = elapsed)
                    synchronized(telemetrySamples) {
                        telemetrySamples.add(sample)
                    }
                }
            }
        }
    }

    private fun stopDurationTicker() {
        durationTickerJob?.cancel()
        durationTickerJob = null
    }
}
