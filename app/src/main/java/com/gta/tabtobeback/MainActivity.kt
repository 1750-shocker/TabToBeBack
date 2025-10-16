package com.gta.tabtobeback

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.gta.tabtobeback.service.FloatingButtonService
import com.gta.tabtobeback.ui.theme.TabToBeBackTheme
import com.gta.tabtobeback.utils.DebugUtils
import com.gta.tabtobeback.utils.PermissionUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    
    private var isServiceRunning by mutableStateOf(false)
    private var hasOverlayPermission by mutableStateOf(false)
    private var hasAccessibilityPermission by mutableStateOf(false)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            TabToBeBackTheme {
                MainScreen()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // 立即检查一次权限状态
        refreshPermissions()
        checkPermissions()
    }
    
    private fun refreshPermissions() {
        hasOverlayPermission = PermissionUtils.hasOverlayPermission(this)
        hasAccessibilityPermission = PermissionUtils.hasAccessibilityPermission(this)
        
        // 输出调试信息
        DebugUtils.logAccessibilityServiceStatus(this)
    }
    
    private fun checkPermissions() {
        lifecycleScope.launch {
            while (true) {
                val newOverlayPermission = PermissionUtils.hasOverlayPermission(this@MainActivity)
                val newAccessibilityPermission = PermissionUtils.hasAccessibilityPermission(this@MainActivity)
                
                // 只在权限状态发生变化时更新UI
                if (newOverlayPermission != hasOverlayPermission || 
                    newAccessibilityPermission != hasAccessibilityPermission) {
                    hasOverlayPermission = newOverlayPermission
                    hasAccessibilityPermission = newAccessibilityPermission
                }
                
                delay(2000) // 每2秒检查一次权限状态，减少频繁检查
            }
        }
    }
    
    private fun startFloatingService() {
        val intent = Intent(this, FloatingButtonService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        isServiceRunning = true
    }
    
    private fun stopFloatingService() {
        val intent = Intent(this, FloatingButtonService::class.java)
        stopService(intent)
        isServiceRunning = false
    }
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen() {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = "悬浮返回",
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 应用介绍
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "悬浮返回按钮",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "在屏幕上显示一个半透明的悬浮按钮，点击即可执行返回操作。可拖拽移动位置，支持在任何应用界面使用。",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            lineHeight = 20.sp
                        )
                    }
                }
                
                // 权限状态
                PermissionStatusCard(
                    title = "悬浮窗权限",
                    description = "允许应用在其他应用上方显示内容",
                    isGranted = hasOverlayPermission,
                    onRequestPermission = {
                        PermissionUtils.requestOverlayPermission(this@MainActivity)
                    }
                )
                
                PermissionStatusCard(
                    title = "无障碍服务",
                    description = "允许应用执行返回操作",
                    isGranted = hasAccessibilityPermission,
                    onRequestPermission = {
                        PermissionUtils.requestAccessibilityPermission(this@MainActivity)
                    }
                )
                
                // 手动刷新按钮
                OutlinedButton(
                    onClick = { refreshPermissions() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "刷新权限状态",
                        fontSize = 14.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 服务控制按钮
                val allPermissionsGranted = hasOverlayPermission && hasAccessibilityPermission
                
                Button(
                    onClick = {
                        if (isServiceRunning) {
                            stopFloatingService()
                        } else {
                            startFloatingService()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = allPermissionsGranted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isServiceRunning) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isServiceRunning) "停止服务" else "启动服务",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                if (!allPermissionsGranted) {
                    Text(
                        text = "请先授予所需权限后再启动服务",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // 服务状态
                Text(
                    text = if (isServiceRunning) "✅ 服务正在运行" else "⭕ 服务已停止",
                    color = if (isServiceRunning) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
    
    @Composable
    fun PermissionStatusCard(
        title: String,
        description: String,
        isGranted: Boolean,
        onRequestPermission: () -> Unit
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isGranted) {
                    Color(0xFF4CAF50).copy(alpha = 0.1f)
                } else {
                    Color(0xFFFF9800).copy(alpha = 0.1f)
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isGranted) "✅" else "❌",
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                if (!isGranted) {
                    Button(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "授权",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}