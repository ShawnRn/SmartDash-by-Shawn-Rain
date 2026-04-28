import Foundation

struct TelemetryStreamProcessor {
    private var lastTimestampMs: Int64?
    private var lastGoodSample: TelemetrySample?
    private var invalidFrameCount = 0
    private var invalidStartMs: Int64?

    mutating func process(_ metrics: VehicleMetrics, profile: VehicleProfile, nowMs: Int64 = Date.nowMs) -> TelemetrySample {
        let dtMs = lastTimestampMs.map { max(0, nowMs - $0) } ?? 0
        lastTimestampMs = nowMs

        guard allFinite(metrics.voltage, metrics.busCurrent, metrics.phaseCurrent, metrics.rpm, metrics.speedKmh, metrics.motorTemp, metrics.controllerTemp) else {
            return outlier(metrics, nowMs: nowMs, dtMs: dtMs)
        }

        let minPackVoltage = recommendedMinPackVoltageV(series: profile.batterySeries)
        let allZeroLike = metrics.voltage < 2 && abs(metrics.busCurrent) < 0.1 && abs(metrics.rpm) < 1 && metrics.speedKmh < 0.1
        let powerOffLike = metrics.voltage > 0 && metrics.voltage < minPackVoltage && abs(metrics.busCurrent) < 1 && abs(metrics.rpm) < 20

        if allZeroLike || powerOffLike {
            invalidFrameCount += 1
            invalidStartMs = invalidStartMs ?? nowMs
            if let lastGoodSample {
                return TelemetrySample(
                    timestampMs: nowMs,
                    sourceFrameId: metrics.sourceFrameId,
                    voltageV: lastGoodSample.voltageV,
                    busCurrentA: 0,
                    phaseCurrentA: 0,
                    rpm: 0,
                    controllerSpeedKmH: 0,
                    displaySpeedKmH: 0,
                    distanceSpeedKmH: 0,
                    motorTempC: lastGoodSample.motorTempC,
                    controllerTempC: lastGoodSample.controllerTempC,
                    braking: metrics.braking,
                    cruise: metrics.cruise,
                    quality: invalidFrameCount >= 10 ? .powerOff : .recovered,
                    dataMode: invalidFrameCount >= 10 ? .powerOff : .recovered,
                    dtMs: dtMs,
                    allowIntegration: false,
                    allowLearning: false
                )
            }
            return outlier(metrics, nowMs: nowMs, dtMs: dtMs)
        }

        invalidFrameCount = 0
        invalidStartMs = nil

        let isDuplicate: Bool
        if let lastGoodSample {
            isDuplicate = dtMs < 20
                && abs(lastGoodSample.voltageV - metrics.voltage) < 0.01
                && abs(lastGoodSample.busCurrentA - metrics.busCurrent) < 0.01
                && abs(lastGoodSample.rpm - metrics.rpm) < 0.5
        } else {
            isDuplicate = false
        }

        let quality: SampleQuality
        if isOutlier(metrics, minPackVoltage: minPackVoltage) {
            quality = .outlier
        } else if isDuplicate {
            quality = .duplicate
        } else if dtMs < 15, dtMs > 0 {
            quality = .tooDense
        } else if dtMs > 5000 {
            quality = .gapReset
        } else {
            quality = .good
        }

        let sample = TelemetrySample(
            timestampMs: nowMs,
            sourceFrameId: metrics.sourceFrameId,
            voltageV: metrics.voltage,
            busCurrentA: metrics.busCurrent,
            phaseCurrentA: metrics.phaseCurrent,
            rpm: metrics.rpm,
            controllerSpeedKmH: metrics.speedKmh,
            displaySpeedKmH: max(0, metrics.speedKmh),
            distanceSpeedKmH: max(0, metrics.speedKmh),
            motorTempC: metrics.motorTemp,
            controllerTempC: metrics.controllerTemp,
            braking: metrics.braking,
            cruise: metrics.cruise,
            quality: quality,
            dataMode: .raw,
            dtMs: dtMs,
            allowIntegration: quality == .good && dtMs >= 20,
            allowLearning: quality == .good && (50...2000).contains(dtMs)
        )

        if quality == .good {
            lastGoodSample = sample
        }
        return sample
    }

    static func recommendedMinPackVoltageV(series: Int) -> Double {
        max(15, Double(series) * 2.7)
    }

    private func recommendedMinPackVoltageV(series: Int) -> Double {
        Self.recommendedMinPackVoltageV(series: series)
    }

    private func allFinite(_ values: Double...) -> Bool {
        values.allSatisfy(\.isFinite)
    }

    private func isOutlier(_ metrics: VehicleMetrics, minPackVoltage: Double) -> Bool {
        metrics.voltage < minPackVoltage ||
            metrics.voltage > 120 ||
            abs(metrics.busCurrent) > 550 ||
            abs(metrics.rpm) > 20_000 ||
            metrics.speedKmh > 300 ||
            (abs(metrics.rpm) > 1000 && metrics.speedKmh < 1)
    }

    private func outlier(_ metrics: VehicleMetrics, nowMs: Int64, dtMs: Int64) -> TelemetrySample {
        TelemetrySample(
            timestampMs: nowMs,
            sourceFrameId: metrics.sourceFrameId,
            voltageV: 0,
            busCurrentA: 0,
            phaseCurrentA: 0,
            rpm: 0,
            controllerSpeedKmH: 0,
            displaySpeedKmH: 0,
            distanceSpeedKmH: 0,
            motorTempC: 0,
            controllerTempC: 0,
            braking: false,
            cruise: false,
            quality: .outlier,
            dataMode: .raw,
            dtMs: dtMs,
            allowIntegration: false,
            allowLearning: false
        )
    }
}

struct RideAccumulator {
    private(set) var state = RideAccumulatorState()

    mutating func accumulate(_ sample: TelemetrySample) {
        let isRecovered = sample.dataMode != .raw
        let isDuplicate = sample.quality == .duplicate
        let isTooDense = sample.quality == .tooDense
        let isOutlier = isRecovered || sample.quality == .outlier || sample.quality == .gapReset || sample.quality == .powerOff

        let distanceSpeedKmh = sample.distanceSpeedKmH.isFinite && sample.distanceSpeedKmH > 0 ? sample.distanceSpeedKmH : 0

        if !isDuplicate && !isTooDense {
            state.sampleCount += 1
            state.totalSpeedSum += max(0, sample.displaySpeedKmH)
        }

        if !isDuplicate && !isTooDense && !isOutlier {
            state.maxSpeedKmh = max(state.maxSpeedKmh, distanceSpeedKmh)
            state.maxControllerTempC = max(state.maxControllerTempC, sample.controllerTempC)
            state.maxMotorTempC = max(state.maxMotorTempC, sample.motorTempC)
        }

        guard sample.allowIntegration else { return }

        let safeDtMs = min(max(sample.dtMs, 50), 500)
        let powerW = sample.voltageV * sample.busCurrentA
        let dtHours = Double(safeDtMs) / 3_600_000.0

        state.netBatteryAh += sample.busCurrentA * dtHours
        if powerW >= 0 {
            state.tractionEnergyWh += powerW * dtHours
            state.tractionAh += sample.busCurrentA * dtHours
            state.peakDrivePowerKw = max(state.peakDrivePowerKw, powerW / 1000)
        } else {
            state.regenEnergyWh += abs(powerW) * dtHours
            state.peakRegenPowerKw = max(state.peakRegenPowerKw, abs(powerW) / 1000)
        }

        state.netBatteryEnergyWh = state.tractionEnergyWh - state.regenEnergyWh
        if distanceSpeedKmh > 0.8 {
            state.totalDistanceMeters += distanceSpeedKmh * dtHours * 1000
        }
        if distanceSpeedKmh > 1.0 {
            state.movingTimeMs += safeDtMs
        }
        state.integrationCount += 1
    }

    mutating func reset() {
        state = RideAccumulatorState()
    }
}

struct BatteryStateEstimator {
    private var filteredVoltage: Double = 0
    private var filteredCurrent: Double = 0
    private var learnedInternalResistanceOhm: Double = 0
    private var displaySoc: Double?

    mutating func estimate(sample: TelemetrySample, accumulator: RideAccumulatorState, profile: VehicleProfile) -> BatteryState {
        let nominalWh = max(1, profile.batteryCapacityAh * Double(profile.batterySeries) * 3.7 * profile.learnedUsableEnergyRatio.finiteOr(0.85))
        let usedAhPct = (accumulator.netBatteryAh / max(1, profile.batteryCapacityAh)) * 100
        let socByAh = (100 - usedAhPct).clamped(to: 0...100)
        let socByOcv = ocvSoc(voltage: sample.voltageV, series: profile.batterySeries)

        let alpha = sample.quality == .good ? 0.12 : 0.04
        filteredVoltage = filteredVoltage == 0 ? sample.voltageV : filteredVoltage * (1 - alpha) + sample.voltageV * alpha
        filteredCurrent = filteredCurrent * 0.85 + sample.busCurrentA * 0.15

        if sample.allowLearning, abs(sample.busCurrentA) > 5, abs(sample.busCurrentA) < 80 {
            learnedInternalResistanceOhm = profile.learnedInternalResistanceOhm.finiteOr(0)
        }

        let wAh = sample.quality == .recovered ? 0.85 : 0.65
        let fused = (socByAh * wAh + socByOcv * (1 - wAh)).clamped(to: 0...100)
        let previous = displaySoc ?? fused
        let maxStep = sample.dtMs > 0 ? max(0.05, Double(sample.dtMs) / 1000.0 * 0.35) : 0.2
        let limited = previous + (fused - previous).clamped(to: -maxStep...maxStep)
        displaySoc = limited

        let remainingWh = nominalWh * limited / 100.0
        let confidence = sample.quality == .good ? 0.75 : 0.45
        _ = remainingWh

        return BatteryState(
            socPercent: limited,
            socByAhPercent: socByAh,
            socByOcvPercent: socByOcv,
            filteredVoltage: filteredVoltage,
            filteredCurrent: filteredCurrent,
            learnedInternalResistanceOhm: learnedInternalResistanceOhm,
            isStationary: sample.displaySpeedKmH < 1.0,
            confidence: confidence,
            irContext: "default"
        )
    }

    private func ocvSoc(voltage: Double, series: Int) -> Double {
        guard series > 0 else { return 0 }
        let cell = voltage / Double(series)
        return ((cell - 3.15) / (4.18 - 3.15) * 100).clamped(to: 0...100)
    }
}

struct RangeEstimator {
    private var efficiencyWindow: [Double] = []
    private var qualityWindow: [SampleQuality] = []
    private var displayedRangeKm: Double = 0

    mutating func estimate(sample: TelemetrySample, batteryState: BatteryState, accumulator: RideAccumulatorState, profile: VehicleProfile) -> RangeEstimate {
        if sample.allowIntegration {
            let efficiency = accumulator.avgNetEfficiencyWhKm
            if efficiency.isFinite && efficiency > 1 {
                efficiencyWindow.append(efficiency)
                if efficiencyWindow.count > 30 { efficiencyWindow.removeFirst() }
            }
        }
        qualityWindow.append(sample.quality)
        if qualityWindow.count > 20 { qualityWindow.removeFirst() }

        let fallback = profile.learnedEfficiencyWhKm.finiteOr(35).clamped(to: 5...250)
        let windowEfficiency = efficiencyWindow.isEmpty ? fallback : efficiencyWindow.reduce(0, +) / Double(efficiencyWindow.count)
        let usableWh = profile.batteryCapacityAh * Double(profile.batterySeries) * 3.7 * profile.learnedUsableEnergyRatio.finiteOr(0.85)
        let remainingWh = usableWh * batteryState.socPercent / 100
        let rawRange = windowEfficiency > 1 ? remainingWh / windowEfficiency : 0

        if displayedRangeKm == 0 {
            displayedRangeKm = rawRange
        } else {
            let maxDelta = max(0.15, Double(max(sample.dtMs, 100)) / 1000.0 * 0.8)
            displayedRangeKm += (rawRange - displayedRangeKm).clamped(to: -maxDelta...maxDelta)
        }

        let good = qualityWindow.filter { $0 == .good }.count
        let confidence: RangeConfidence = good > 14 && efficiencyWindow.count > 12 ? .high : (good > 6 ? .medium : .low)
        return RangeEstimate(
            estimatedRangeKm: max(0, displayedRangeKm),
            remainingEnergyWh: max(0, remainingWh),
            averageWindowEfficiencyWhKm: windowEfficiency,
            isWindowFresh: !efficiencyWindow.isEmpty,
            confidence: confidence
        )
    }
}
