package com.gta.tabtobeback.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import com.gta.tabtobeback.R

class FloatingButtonService : Service() {
    
    private var windowManager: WindowManager? = null
    private var floatingView: ImageView? = null
    private var params: WindowManager.LayoutParams? = null
    
    companion object {
        const val CHANNEL_ID = "FloatingButtonService"
        const val NOTIFICATION_ID = 1
        const val ACTION_BACK = "com.gta.tabtobeback.ACTION_BACK"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        createFloatingButton()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "悬浮返回按钮服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示悬浮返回按钮"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("悬浮返回")
            .setContentText("悬浮返回按钮正在运行")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
    
    private fun createFloatingButton() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        floatingView = ImageView(this).apply {
            // 创建半透明圆形背景
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0x80000000.toInt()) // 半透明黑色
                setStroke(3, 0xFFFFFFFF.toInt()) // 白色边框
            }
            background = drawable
            scaleType = ImageView.ScaleType.CENTER
            
            // 设置返回箭头图标
            setImageResource(R.drawable.ic_back_arrow)
            setPadding(20, 20, 20, 20)
            
            // 添加阴影效果
            elevation = 8f
        }
        
        // 设置窗口参数
        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
        
        params = WindowManager.LayoutParams(
            120, // 宽度
            120, // 高度
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }
        
        // 添加触摸监听
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false
        
        floatingView?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params?.x ?: 0
                    initialY = params?.y ?: 0
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialTouchX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()
                    
                    if (Math.abs(deltaX) > 10 || Math.abs(deltaY) > 10) {
                        isDragging = true
                        params?.x = initialX + deltaX
                        params?.y = initialY + deltaY
                        windowManager?.updateViewLayout(floatingView, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        // 点击事件 - 执行返回操作
                        performBackAction()
                    }
                    true
                }
                else -> false
            }
        }
        
        // 添加到窗口
        try {
            windowManager?.addView(floatingView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun performBackAction() {
        // 发送广播给无障碍服务执行返回操作
        val intent = Intent(ACTION_BACK).apply {
            setPackage(packageName) // 明确指定包名，确保安全
        }
        sendBroadcast(intent)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        floatingView?.let { view ->
            windowManager?.removeView(view)
        }
    }
}