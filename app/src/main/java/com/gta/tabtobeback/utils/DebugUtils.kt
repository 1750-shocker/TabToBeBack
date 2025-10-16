package com.gta.tabtobeback.utils

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager

object DebugUtils {
    private const val TAG = "DebugUtils"
    
    fun logAccessibilityServiceStatus(context: Context) {
        try {
            Log.d(TAG, "=== 无障碍服务调试信息 ===")
            
            // 检查无障碍服务是否启用
            val accessibilityEnabled = try {
                Settings.Secure.getInt(
                    context.contentResolver,
                    Settings.Secure.ACCESSIBILITY_ENABLED
                )
            } catch (e: Settings.SettingNotFoundException) {
                0
            }
            Log.d(TAG, "无障碍服务总开关: ${if (accessibilityEnabled == 1) "已开启" else "已关闭"}")
            
            // 获取已启用的服务列表
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            Log.d(TAG, "已启用的服务列表: $enabledServices")
            
            // 使用AccessibilityManager检查
            val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            if (accessibilityManager != null) {
                val services = accessibilityManager.getEnabledAccessibilityServiceList(
                    AccessibilityServiceInfo.FEEDBACK_ALL_MASK
                )
                Log.d(TAG, "通过AccessibilityManager获取的服务数量: ${services.size}")
                
                services.forEach { service ->
                    Log.d(TAG, "服务ID: ${service.id}")
                    Log.d(TAG, "服务包名: ${service.resolveInfo.serviceInfo.packageName}")
                    Log.d(TAG, "服务类名: ${service.resolveInfo.serviceInfo.name}")
                }
                
                // 检查我们的服务
                val ourServiceFound = services.any { service ->
                    service.id.contains("BackButtonAccessibilityService")
                }
                Log.d(TAG, "是否找到我们的服务: $ourServiceFound")
            }
            
            Log.d(TAG, "=== 调试信息结束 ===")
        } catch (e: Exception) {
            Log.e(TAG, "调试信息获取失败", e)
        }
    }
}