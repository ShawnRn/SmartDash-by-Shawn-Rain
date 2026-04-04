# Predictive Back 实施约定

适用范围：
- 二级页面
- 自定义 `Dialog`
- 全屏 overlay
- 页面内自绘 sheet
- `AlertDialog`
- `ModalBottomSheet`

实施原则：
1. 优先复用 `app/src/main/java/com/shawnrain/habe/ui/navigation/PredictiveBackMotion.kt`
2. 返回手势过程中至少提供：
   - 跟手缩放
   - 轻微位移
   - 圆角 / inset 预览
3. `ModalBottomSheet` 等平台组件若系统默认预测性返回不满足设计要求，应在内容层补充应用内动画
4. 新增 overlay 时，同时检查：
   - 状态栏 / 导航栏透明与模糊
   - 返回取消时的回弹动画
   - 左右边缘返回方向是否正确
   - 与拖拽关闭手势是否冲突
5. 所有弹窗窗口默认复用 `DialogWindowEffects.kt`，确保背景模糊不是“某几个弹窗特例”
6. 行程详情卡片 -> 横屏全屏图表，必须满足：
   - 打开和退出都有非线性动画（推荐：进入 `FastOutSlowIn`，退出 `FastOutLinearIn`）
   - 所有退出入口（返回手势/遮罩点击/按钮/下拉）走统一的 dismiss 动画通道
   - 动线保持“从哪来回哪去”，避免一个入口多套退出效果造成割裂
7. 行程详情概览卡片编辑约定：
   - 长按卡片直接进入编辑并触发拖拽
   - 非编辑态不显示“编辑卡片”按钮
   - 仅编辑态显示“完成”和“添加卡片”

联调建议：
1. 先本地跑 `./gradlew :app:compileDebugKotlin --console plain`
2. 再执行 `.agents/scripts/install-dev-release.sh` 真机验证
3. 实测以下路径：
   - 左边缘返回
   - 右边缘返回
   - 手势取消回弹
   - overlay / sheet / 二级页三类容器
