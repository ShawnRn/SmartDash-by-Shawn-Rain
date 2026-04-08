package com.shawnrain.sdash.ble.protocols

/**
 * 智科参数写入状态机
 * 驱动完整的写入流程: handshake → write mode → write packets → ack → verify
 */
sealed interface ZhikeWriteState {
    data object Idle : ZhikeWriteState
    data object Handshaking : ZhikeWriteState
    data object EnteringWriteMode : ZhikeWriteState
    data object ReadyToWrite : ZhikeWriteState
    data object WritingPackets : ZhikeWriteState
    data object WaitingWriteAck : ZhikeWriteState
    data object Verifying : ZhikeWriteState
    data object Succeeded : ZhikeWriteState
    data class Failed(val reason: String, val phase: WriteFailurePhase) : ZhikeWriteState
}

/**
 * 写入失败阶段分类
 */
enum class WriteFailurePhase(val label: String, val userHint: String) {
    HANDSHAKE_FAILED(
        label = "握手失败",
        userHint = "控制器未响应握手指令，请检查连接后重试"
    ),
    ENTER_WRITE_MODE_TIMEOUT(
        label = "进入写入模式超时",
        userHint = "控制器未进入写入模式，请重新读取参数后再试"
    ),
    PACKET_SEND_FAILED(
        label = "数据包发送失败",
        userHint = "BLE 写入过程中断，请保持设备靠近后重试"
    ),
    ACK_TIMEOUT(
        label = "ACK 超时",
        userHint = "控制器未返回确认信号，参数可能未生效"
    ),
    CONTROLLER_REJECTED(
        label = "控制器拒绝",
        userHint = "控制器拒绝了本次写入，参数可能不合法或需要更高权限"
    ),
    VERIFY_MISMATCH(
        label = "回读不一致",
        userHint = "控制器返回成功，但部分参数未完全生效"
    ),
    UNKNOWN_ERROR(
        label = "未知错误",
        userHint = "写入过程中发生异常，请重新操作"
    )
}

/**
 * 智科协议写入事件
 */
sealed interface ZhikeProtocolEvent {
    data object WriteModeReady : ZhikeProtocolEvent
    data object WriteSuccess : ZhikeProtocolEvent
    data class WriteFailure(val reason: String) : ZhikeProtocolEvent
    data object FactoryResetAck : ZhikeProtocolEvent
    data class FactoryResetFailure(val reason: String) : ZhikeProtocolEvent
    data object PasswordChangeAck : ZhikeProtocolEvent
    data class PasswordChangeFailure(val reason: String) : ZhikeProtocolEvent
    data object ParameterReadComplete : ZhikeProtocolEvent
    data class ParameterReadFailure(val reason: String) : ZhikeProtocolEvent
}
