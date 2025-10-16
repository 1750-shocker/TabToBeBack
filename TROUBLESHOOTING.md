# 故障排除指南

## 车机系统解决方案 🚗

### 问题描述
在车机设备上安装应用后遇到以下问题：
1. 点击悬浮窗权限授权按钮直接闪退
2. 点击无障碍服务授权按钮提示"没有可处理此操作的应用"

### ADB命令解决方案

**前提条件：**
- 车机支持ADB调试
- 已通过USB连接车机和电脑
- 已启用车机的开发者选项和USB调试

**解决步骤：**

#### 1. 连接设备并确认连接
```bash
# 检查设备连接
adb devices
```

#### 2. 授予悬浮窗权限
```bash
# 打开悬浮窗权限
adb shell appops set com.gta.tabtobeback SYSTEM_ALERT_WINDOW allow
```

#### 3. 启用无障碍服务
```bash
# 首先启用无障碍功能总开关
adb shell settings put secure accessibility_enabled 1

# 然后启用我们的无障碍服务
adb shell settings put secure enabled_accessibility_services com.gta.tabtobeback/com.gta.tabtobeback.service.BackButtonAccessibilityService
```

#### 4. 验证权限状态
```bash
# 检查悬浮窗权限
adb shell appops get com.gta.tabtobeback SYSTEM_ALERT_WINDOW

# 检查无障碍总开关
adb shell settings get secure accessibility_enabled

# 检查已启用的无障碍服务
adb shell settings get secure enabled_accessibility_services
```

#### 5. 测试应用
1. 重启"悬浮返回"应用
2. 在应用中点击"刷新权限状态"按钮
3. 确认两个权限都显示为已授予
4. 点击"启动服务"按钮
5. 测试悬浮按钮功能

### 注意事项

⚠️ **重要提示：**
- 执行ADB命令的顺序很重要，必须先启用无障碍总开关，再启用具体服务
- 如果车机已有其他无障碍服务在运行，可能需要特殊处理（见下方高级用法）
- 某些车机系统可能需要重启后才能生效

### 高级用法

**如果车机已有无障碍服务在运行：**
```bash
# 先查看当前服务列表
adb shell settings get secure enabled_accessibility_services

# 假设输出为 "com.example.service1"，则需要追加我们的服务：
adb shell settings put secure enabled_accessibility_services "com.example.service1:com.gta.tabtobeback/com.gta.tabtobeback.service.BackButtonAccessibilityService"
```

**如果服务名称格式不对，尝试其他格式：**
```bash
# 格式1（推荐）
adb shell settings put secure enabled_accessibility_services com.gta.tabtobeback/com.gta.tabtobeback.service.BackButtonAccessibilityService

# 格式2
adb shell settings put secure enabled_accessibility_services com.gta.tabtobeback/.service.BackButtonAccessibilityService
```

**完全重置无障碍服务（谨慎使用）：**
```bash
adb shell settings delete secure enabled_accessibility_services
adb shell settings put secure accessibility_enabled 0
adb shell settings put secure accessibility_enabled 1
adb shell settings put secure enabled_accessibility_services com.gta.tabtobeback/com.gta.tabtobeback.service.BackButtonAccessibilityService
```

### 故障排除

**如果ADB命令执行失败：**
1. 确认设备已正确连接：`adb devices`
2. 确认应用已安装：`adb shell pm list packages | grep tabtobeback`
3. 检查车机是否支持这些设置项

**如果权限显示已授予但功能不工作：**
1. 重启应用
2. 重启车机系统
3. 检查车机是否有特殊的安全限制

## 无障碍服务权限问题

### 问题描述
开启应用的无障碍服务权限后，返回到应用依然显示未授权状态。

### 解决方案

#### 1. 使用应用内的刷新功能
- 在主界面点击"刷新权限状态"按钮
- 应用会重新检查权限状态

#### 2. 检查无障碍服务设置
1. 进入系统设置 → 无障碍 → 已下载应用
2. 找到"悬浮返回"应用
3. 确保服务已开启
4. 如果显示已开启但应用仍显示未授权，尝试：
   - 关闭服务，等待几秒后重新开启
   - 重启应用

#### 3. 查看调试日志
如果问题持续存在，可以通过以下方式查看调试信息：

```bash
# 连接设备后运行
adb logcat -s DebugUtils
```

调试信息会显示：
- 无障碍服务总开关状态
- 已启用的服务列表
- 是否找到我们的服务

#### 4. 常见问题和解决方法

**问题1：无障碍服务被系统自动关闭**
- 原因：某些系统会自动关闭未使用的无障碍服务
- 解决：将应用加入电池优化白名单，或在无障碍设置中重新开启

**问题2：权限检查延迟**
- 原因：系统权限状态更新可能有延迟
- 解决：等待几秒后点击"刷新权限状态"按钮

**问题3：服务名称匹配问题**
- 原因：不同Android版本的服务名称格式可能不同
- 解决：应用已包含多种格式的匹配逻辑

#### 5. 手动验证步骤

1. **确认无障碍服务已启用**：
   - 设置 → 无障碍 → 已下载应用 → 悬浮返回 → 开启

2. **确认悬浮窗权限已授予**：
   - 设置 → 应用管理 → 悬浮返回 → 权限 → 显示在其他应用上层 → 允许

3. **重启应用**：
   - 完全关闭应用后重新打开

4. **测试功能**：
   - 启动服务后，在其他应用中测试悬浮按钮是否能正确执行返回操作

### 技术细节

应用使用两种方法检查无障碍权限：

1. **AccessibilityManager方法**（主要）：
   ```kotlin
   val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
   val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
   ```

2. **Settings.Secure方法**（备用）：
   ```kotlin
   val services = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
   ```

如果两种方法都无法正确检测到权限状态，可能是系统兼容性问题。

### 联系支持

如果以上方法都无法解决问题，请提供以下信息：
- Android版本和设备型号
- 调试日志输出
- 具体的操作步骤和现象描述