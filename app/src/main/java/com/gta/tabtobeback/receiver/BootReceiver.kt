package com.gta.tabtobeback.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.gta.tabtobeback.service.FloatingButtonService
import com.gta.tabtobeback.utils.PermissionUtils

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                // 检查权限后自动启动服务
                if (PermissionUtils.hasOverlayPermission(context) && 
                    PermissionUtils.hasAccessibilityPermission(context)) {
                    
                    val serviceIntent = Intent(context, FloatingButtonService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(serviceIntent)
                    } else {
                        context.startService(serviceIntent)
                    }
                }
            }
        }
    }
}