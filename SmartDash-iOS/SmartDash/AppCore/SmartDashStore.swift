import Combine
import Foundation

@MainActor
final class SmartDashStore: ObservableObject {
    @Published var persisted: PersistedSmartDashState
    @Published var latestMetrics = VehicleMetrics()
    @Published var latestSample: TelemetrySample?
    @Published var accumulatorState = RideAccumulatorState()
    @Published var batteryState = BatteryState(socPercent: 85)
    @Published var rangeEstimate = RangeEstimate()
    @Published var rideDirectionLabel = "--"
    @Published var stableDirectionDegrees: Double?
    @Published var isRideActive = false
    @Published var syncStatus: CloudSyncStatus = .signedOut
    @Published var remoteDriveSummary: DriveRemoteSummary?

    let ble = BLEManager()
    let persistence = SmartDashPersistence.shared
    let googleAccount = GoogleDriveAccountService()
    let log = AppLog.shared

    private var telemetryProcessor = TelemetryStreamProcessor()
    private var accumulator = RideAccumulator()
    private var batteryEstimator = BatteryStateEstimator()
    private var rangeEstimator = RangeEstimator()
    private var directionStabilizer = DirectionStabilizer()
    private var directionFormatter = DirectionLabelFormatter()
    private var cancellables: Set<AnyCancellable> = []

    init() {
        var state = persistence.load()
        state.normalize()
        persisted = state
        bind()
        Task {
            await persistence.refreshICloudStatus()
            googleAccount.restore()
        }
    }

    var activeVehicle: VehicleProfile {
        persisted.vehicleProfiles.first(where: { $0.id == persisted.activeVehicleProfileId }) ?? persisted.vehicleProfiles.first ?? VehicleProfile()
    }

    func ensureDashboardMetricDefaults() {
        let all = DashboardMetric.allCases.map(\.id)
        var current = persisted.settings.dashboardItems.filter { all.contains($0) }
        for id in all where !current.contains(id) {
            current.append(id)
        }
        if current != persisted.settings.dashboardItems {
            persisted.settings.dashboardItems = current
            save()
        }
    }

    func startRide() {
        isRideActive = true
        accumulator.reset()
        accumulatorState = accumulator.state
        log.info("Ride started")
    }

    func stopRide() {
        isRideActive = false
        let now = Date.nowMs
        let record = RideHistoryRecord(
            vehicleProfileId: activeVehicle.id,
            title: "骑行 \(Date().formatted(date: .numeric, time: .shortened))",
            startedAtMs: max(0, now - accumulatorState.movingTimeMs),
            endedAtMs: now,
            durationMs: accumulatorState.movingTimeMs,
            distanceMeters: accumulatorState.totalDistanceMeters,
            maxSpeedKmh: accumulatorState.maxSpeedKmh,
            avgSpeedKmh: accumulatorState.averageSpeedKmh,
            peakPowerKw: accumulatorState.peakDrivePowerKw,
            totalEnergyWh: accumulatorState.netBatteryEnergyWh,
            tractionEnergyWh: accumulatorState.tractionEnergyWh,
            regenEnergyWh: accumulatorState.regenEnergyWh,
            avgEfficiencyWhKm: accumulatorState.avgNetEfficiencyWhKm,
            avgNetEfficiencyWhKm: accumulatorState.avgNetEfficiencyWhKm,
            avgTractionEfficiencyWhKm: accumulatorState.avgTractionEfficiencyWhKm,
            maxControllerTemp: accumulatorState.maxControllerTempC
        )
        if record.distanceMeters > 1 || record.totalEnergyWh > 0.1 {
            persisted.rides.insert(record, at: 0)
        }
        save()
        log.info("Ride stopped")
    }

    func save() {
        persisted.localStateVersion += 1
        persistence.save(persisted)
    }

    func simulateFrame() {
        let speed = Double.random(in: 18...42)
        let metrics = VehicleMetrics(
            voltage: Double.random(in: 66...82),
            busCurrent: Double.random(in: 8...65),
            phaseCurrent: Double.random(in: 20...130),
            rpm: speed / 1.8 * 50 * 1000 / 60,
            speedKmh: speed,
            motorTemp: Double.random(in: 35...78),
            controllerTemp: Double.random(in: 38...82),
            protocolId: "sim",
            timestampMs: Date.nowMs
        )
        ingest(metrics)
    }

    func syncNow() {
        Task {
            guard let email = googleAccount.signedInEmail else {
                syncStatus = .configurationRequired("请先接入 GoogleSignIn-iOS 并登录")
                return
            }
            do {
                syncStatus = .syncing("合并 Google Drive")
                let service = GoogleDriveSyncService(tokenProvider: googleAccount)
                let local = persisted.toDriveState()
                let merged: DriveCurrentState
                if let remote = try await service.downloadCurrentState(passwordEmail: email) {
                    merged = DriveStateMerger().merge(local: local, remote: remote, deviceId: persisted.deviceId, deviceName: persisted.deviceName)
                } else {
                    merged = local
                }
                try await service.uploadCurrentState(merged, passwordEmail: email)
                persisted.applyDriveState(merged)
                save()
                syncStatus = .success("同步完成")
            } catch {
                syncStatus = .failed(error.localizedDescription)
            }
        }
    }

    func signInGoogleDrive() {
        Task {
            await googleAccount.signIn()
        }
    }

    func signOutGoogleDrive() {
        googleAccount.signOut()
        remoteDriveSummary = nil
    }

    func checkGoogleDriveUpdate() {
        Task {
            guard let email = googleAccount.signedInEmail else {
                syncStatus = .configurationRequired("请先登录 Google")
                return
            }
            do {
                syncStatus = .syncing("检测 Drive 更新")
                let service = GoogleDriveSyncService(tokenProvider: googleAccount)
                remoteDriveSummary = try await service.remoteSummary(passwordEmail: email)
                syncStatus = remoteDriveSummary == nil ? .success("云端暂无状态") : .success("发现云端状态")
            } catch {
                syncStatus = .failed(error.localizedDescription)
            }
        }
    }

    func uploadGoogleDriveState() {
        Task {
            guard let email = googleAccount.signedInEmail else {
                syncStatus = .configurationRequired("请先登录 Google")
                return
            }
            do {
                syncStatus = .syncing("上传本机状态")
                let service = GoogleDriveSyncService(tokenProvider: googleAccount)
                try await service.uploadCurrentState(persisted.toDriveState(), passwordEmail: email)
                remoteDriveSummary = try await service.remoteSummary(passwordEmail: email)
                syncStatus = .success("上传完成")
            } catch {
                syncStatus = .failed(error.localizedDescription)
            }
        }
    }

    func handleOpenURL(_ url: URL) -> Bool {
        googleAccount.handle(url: url)
    }

    private func bind() {
        ble.$latestMetrics
            .receive(on: RunLoop.main)
            .sink { [weak self] metrics in self?.ingest(metrics) }
            .store(in: &cancellables)

        googleAccount.$status
            .receive(on: RunLoop.main)
            .assign(to: &$syncStatus)
    }

    private func ingest(_ metrics: VehicleMetrics) {
        latestMetrics = metrics
        let profile = activeVehicle
        let sample = telemetryProcessor.process(metrics, profile: profile, nowMs: metrics.timestampMs)
        latestSample = sample
        if isRideActive {
            accumulator.accumulate(sample)
            accumulatorState = accumulator.state
        }
        batteryState = batteryEstimator.estimate(sample: sample, accumulator: accumulatorState, profile: profile)
        rangeEstimate = rangeEstimator.estimate(sample: sample, batteryState: batteryState, accumulator: accumulatorState, profile: profile)

        let direction = directionStabilizer.update(
            DirectionInput(
                gpsBearingDeg: sample.displaySpeedKmH > 3 ? normalized(sample.rpm / 10) : nil,
                gpsAgeMs: 200,
                gpsAccuracyM: 10,
                gpsStepDistanceM: sample.displaySpeedKmH > 5 ? 4 : 1,
                sensorHeadingDeg: nil,
                speedKmH: sample.displaySpeedKmH,
                timestampMs: sample.timestampMs
            )
        )
        stableDirectionDegrees = direction
        rideDirectionLabel = directionFormatter.label(for: direction)
    }
}
