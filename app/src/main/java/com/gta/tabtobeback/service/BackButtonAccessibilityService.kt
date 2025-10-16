package com.gta.tabtobeback.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent

class BackButtonAccessibilityService : AccessibilityService() {
    
    private val backActionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == FloatingButtonService.ACTION_BACK) {
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
        }
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        
        // 注册广播接收器
        val filter = IntentFilter(FloatingButtonService.ACTION_BACK)
        registerReceiver(backActionReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // 这里可以处理其他无障碍事件，目前我们主要用于执行返回操作
    }
    
    override fun onInterrupt() {
        // 服务被中断时的处理
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(backActionReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}