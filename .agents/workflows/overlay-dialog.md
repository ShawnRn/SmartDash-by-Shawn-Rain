# SmartDash Overlay / Dialog 工作流

适用范围：
- 设置页「关于」/「应用更新」入口
- 自定义 `Dialog`
- 自定义 overlay
- 内容型底部 sheet
- 全屏详情层

强约束：
1. 先参考 `SpeedtestScreen.kt` 中的速度 / 行程记录 / 行程详情 overlay，再开始写新容器。
2. 不要默认做成又高又满的通用 `BottomSheet`；优先判断是否应该做成“限宽、限高、居中”的 overlay。
3. 入口卡片若位于设置页，默认放在设置列表最底部。
4. App 图标或品牌图形必须做自适应：
   - 保持原始宽高比
   - 优先 `ContentScale.Fit`
   - 用受控内边距，而不是粗暴塞进固定圆角矩形
5. 文案必须先做密度控制：
   - 标题不超过 1-2 行
   - 辅助文案尽量 1-2 句
   - 行为按钮分组清晰，不要堆成长段说明

容器约定：
1. 复用 `ApplyDialogWindowBlurEffect`
2. 复用 `PopupBackdropBlurLayer`
3. 复用 `rememberPredictiveBackMotion` 或 `PredictiveBackPopupTransform`
4. 复用统一的 dismiss 通道：
   - 返回手势
   - 遮罩点击
   - 关闭按钮
   - 下拉/拖拽关闭

动效约定：
1. 进入优先 `FastOutSlowIn`
2. 退出优先 `FastOutLinearIn`
3. 进入/退出位移距离保持克制，避免浮夸弹跳
4. 预测返回过程中至少体现：
   - 缩放
   - 位移
   - alpha
   - corner / inset 变化

排版约定：
1. 内容宽度必须受控，避免手机窄屏时文字碎裂换行。
2. 行动项优先做成统一 row，不要每块都重新发明视觉结构。
3. Hero 区只保留最核心身份信息，不要把说明文案全塞进顶部。

联调步骤：
1. `./gradlew :app:compileDebugKotlin --console plain`
2. 真机检查：
   - 小屏宽度是否炸裂
   - 文字是否出现尴尬断行
   - 图标是否居中且比例正确
   - scrim / blur / 预测返回是否都生效
