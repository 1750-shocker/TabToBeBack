package com.gta.tabtobeback.utils

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityManager

object PermissionUtils {
    
    /**
     * 检查是否有悬浮窗权限
     */
    fun hasOverlayPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }
    
    /**
     * 请求悬浮窗权限
     */
    fun requestOverlayPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
    }
    
    /**
     * 检查是否有无障碍权限
     */
    fun hasAccessibilityPermission(context: Context): Boolean {
        try {
            val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
                ?: return false
            
            val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK
            )
            
            val targetServiceName = "com.gta.tabtobeback.service.BackButtonAccessibilityService"
            
            for (service in enabledServices) {
                val serviceId = service.id
                if (serviceId.contains(targetServiceName)) {
                    return true
                }
            }
            
            // 备用检查方法
            return checkAccessibilityPermissionFallback(context)
        } catch (e: Exception) {
            e.printStackTrace()
            return checkAccessibilityPermissionFallback(context)
        }
    }
    
    /**
     * 备用的无障碍权限检查方法
     */
    private fun checkAccessibilityPermissionFallback(context: Context): Boolean {
        val accessibilityEnabled = try {
            Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: Settings.SettingNotFoundException) {
            0
        }
        
        if (accessibilityEnabled == 1) {
            val services = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            
            val targetServiceNames = listOf(
                "${context.packageName}/.service.BackButtonAccessibilityService",
                "${context.packageName}/com.gta.tabtobeback.service.BackButtonAccessibilityService",
                "com.gta.tabtobeback/.service.BackButtonAccessibilityService",
                "com.gta.tabtobeback/com.gta.tabtobeback.service.BackButtonAccessibilityService"
            )
            
            return targetServiceNames.any { serviceName ->
                services.contains(serviceName)
            }
        }
        
        return false
    }
    
    /**
     * 请求无障碍权限
     */
    fun requestAccessibilityPermission(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
}