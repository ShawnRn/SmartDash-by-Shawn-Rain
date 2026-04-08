package com.shawnrain.habe.ble.protocols

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
    data class Failed(val reason: String, val phase: String) : ZhikeWriteState
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
