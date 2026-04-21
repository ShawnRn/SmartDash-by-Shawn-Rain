package com.shawnrain.sdash.data.telemetry

enum class SampleQuality {
    /**
     * 合法样本，可用于显示和积分
     */
    GOOD,

    /**
     * 短时毛刺被前值保持掩蔽后的恢复样本，可用于展示但不能参与学习/积分
     */
    RECOVERED,

    /**
     * 重复样本（与上一帧完全一致或 dtMs < 50ms），不参与积分
     */
    DUPLICATE,

    /**
     * 采样过密，不参与积分
     */
    TOO_DENSE,

    /**
     * 采样过旧，不参与积分
     */
    STALE,

    /**
     * 异常值（物理量超出合理范围），不参与积分
     */
    OUTLIER,

    /**
     * 持续异常已经被接受为真实掉电/休眠
     */
    POWER_OFF,

    /**
     * 断流重置（dtMs > 3000ms），标志着一个新的积分段开始
     */
    GAP_RESET
}
