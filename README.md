# 悬浮返回按钮 (TabToBeBack)

一个Android应用，可以在设备任何界面显示一个半透明的悬浮返回按钮，点击后执行返回操作。

## 功能特性

- ✅ **全局悬浮按钮**：在任何应用界面都能显示半透明圆形按钮
- ✅ **拖拽移动**：可以拖拽按钮到屏幕任意位置
- ✅ **返回操作**：点击按钮执行系统返回操作
- ✅ **开机自启**：支持开机自动启动服务
- ✅ **现代UI**：使用Compose构建的现代化用户界面
- ✅ **权限管理**：智能权限申请和状态检查

## 技术实现

### 核心组件

1. **FloatingButtonService**：前台服务，负责显示悬浮按钮
2. **BackButtonAccessibilityService**：无障碍服务，负责执行返回操作
3. **BootReceiver**：开机广播接收器，实现开机自启
4. **PermissionUtils**：权限管理工具类

### 权限要求

- **悬浮窗权限** (`SYSTEM_ALERT_WINDOW`)：允许应用在其他应用上方显示内容
- **无障碍服务**：允许应用执行返回等系统操作
- **前台服务权限**：保持悬浮按钮服务持续运行
- **开机自启权限**：支持开机自动启动

## 使用说明

### 普通Android设备安装和设置

1. 安装APK文件到Android设备
2. 打开应用，按照界面提示授予权限：
   - 点击"授权"按钮申请悬浮窗权限
   - 点击"授权"按钮开启无障碍服务
3. 所有权限授予后，点击"启动服务"

### 车机系统安装和设置 🚗

**如果在车机上遇到权限授权问题（点击授权按钮闪退或提示无法处理），可以使用ADB命令解决：**

#### 前提条件
- 车机支持ADB调试
- 通过USB连接车机和电脑
- 已启用车机的开发者选项和USB调试

#### ADB授权步骤

1. **连接设备**
```bash
# 检查设备连接
adb devices
```

2. **授予悬浮窗权限**
```bash
# 打开悬浮窗权限
adb shell appops set com.gta.tabtobeback SYSTEM_ALERT_WINDOW allow
```

3. **启用无障碍服务**
```bash
# 首先启用无障碍功能总开关
adb shell settings put secure accessibility_enabled 1

# 然后启用我们的无障碍服务
adb shell settings put secure enabled_accessibility_services com.gta.tabtobeback/com.gta.tabtobeback.service.BackButtonAccessibilityService
```

4. **验证权限**
```bash
# 检查悬浮窗权限
adb shell appops get com.gta.tabtobeback SYSTEM_ALERT_WINDOW

# 检查无障碍服务
adb shell settings get secure enabled_accessibility_services
```

5. **测试应用**
- 重启"悬浮返回"应用
- 点击"刷新权限状态"确认权限已授予
- 点击"启动服务"开始使用

**注意：** 车机系统通常是定制化Android，部分标准权限界面可能被移除或修改，使用ADB是最可靠的授权方式。

### 使用方法

1. **启动服务**：在应用中点击"启动服务"按钮
2. **使用悬浮按钮**：
   - 屏幕上会出现一个半透明的圆形按钮，带有白色返回箭头图标
   - 点击按钮执行返回操作
   - 长按并拖拽可以移动按钮位置
3. **停止服务**：返回应用点击"停止服务"

### 界面说明

- **权限状态卡片**：实时显示权限授予状态
- **服务控制按钮**：启动/停止悬浮按钮服务
- **状态指示**：显示当前服务运行状态

## 开发环境

- **Android Studio**：最新版本
- **Kotlin**：2.0.21
- **Android Gradle Plugin**：8.11.2
- **Compose BOM**：2024.09.00
- **最低SDK版本**：29 (Android 10)
- **目标SDK版本**：36

## 目录结构

```
app/src/main/java/com/gta/tabtobeback/
├── MainActivity.kt                    # 主界面Activity
├── service/
│   ├── FloatingButtonService.kt       # 悬浮按钮服务
│   └── BackButtonAccessibilityService.kt  # 无障碍服务
├── receiver/
│   └── BootReceiver.kt               # 开机自启接收器
├── utils/
│   └── PermissionUtils.kt            # 权限管理工具
└── ui/theme/                         # UI主题文件
```

## 构建说明

```bash
# 清理项目
.\gradlew.bat clean

# 构建Debug版本（跳过Lint检查加快构建）
.\gradlew.bat assembleDebug -x lint

# 构建Release版本
.\gradlew.bat assembleRelease
```

## 注意事项

1. **权限要求**：应用需要敏感权限，首次使用需要手动授权
2. **系统兼容性**：适用于Android 10及以上版本
3. **电池优化**：建议将应用加入电池优化白名单，避免被系统杀死
4. **无障碍服务**：无障碍服务可能会被系统自动关闭，需要定期检查
5. **车机系统**：车机等定制系统可能需要使用ADB命令授权，详见上方车机安装说明

## 设备兼容性

### ✅ 已测试兼容
- 标准Android设备（Android 10+）
- 车机系统（通过ADB授权）

### ⚠️ 可能需要特殊处理
- 深度定制的Android系统
- 某些品牌的定制ROM
- 企业版或教育版Android设备

如遇到权限问题，请参考 [TROUBLESHOOTING.md](TROUBLESHOOTING.md) 获取详细解决方案。

## 安全说明

- 应用仅在本地执行操作，不收集或上传任何用户数据
- 无障碍服务权限仅用于执行返回操作
- 悬浮窗权限仅用于显示返回按钮

## 许可证

本项目采用MIT许可证，详见LICENSE文件。