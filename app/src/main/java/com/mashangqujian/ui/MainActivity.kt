package com.mashangqujian.ui

import android.Manifest
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.mashangqujian.sms.SMSReceiver
import com.mashangqujian.ui.components.MainScreen
import com.mashangqujian.ui.theme.MashangqujianTheme

class MainActivity : ComponentActivity() {

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var smsReceiver: SMSReceiver? = null

    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化权限请求Launcher
        initializePermissionLauncher()
        
        try {
            // 先初始化所有组件
            viewModel.initializeAllComponents(this)
            
            setContent {
                MashangqujianTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainScreen(viewModel = viewModel)
                    }
                }
            }
            
            // 检查组件是否已正确初始化
            if (viewModel.areComponentsInitialized()) {
                // 检查权限（延迟到UI准备好后）
                viewModel.checkPermissions(this)
                
                // 处理从其他应用分享的文本
                handleSharedText()

                // 检查粘贴板内容
                viewModel.checkClipboard(this)

                // 注册短信接收器
                registerSMSReceiver()
            } else {
                Log.e("MainActivity", "组件初始化失败")
                // 显示错误消息或重试机制
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "启动失败: ${e.message}", e)
            // 这里可以添加错误处理，比如显示错误提示
            Toast.makeText(this, "应用启动失败: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            finish() // 如果启动失败，关闭应用
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 注销短信接收器
        smsReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                Log.w("MainActivity", "注销接收器失败: ${e.message}")
            }
            smsReceiver = null
        }
    }

    /**
     * 动态注册短信接收器（Android 12+ 需要运行时注册）
     */
    private fun registerSMSReceiver() {
        smsReceiver = SMSReceiver()
        val filter = IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION).apply {
            priority = 999
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(smsReceiver, filter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(smsReceiver, filter)
        }
        Log.d("MainActivity", "短信接收器已注册")
    }

    override fun onResume() {
        super.onResume()
        // 刷新数据
        viewModel.loadParcels()
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        viewModel.onPermissionResult(requestCode, grantResults)
    }
    
    private fun handleSharedText() {
        val intent = intent
        val action = intent.action
        val type = intent.type
        
        if (action == android.content.Intent.ACTION_SEND && type == "text/plain") {
            intent.getStringExtra(android.content.Intent.EXTRA_TEXT)?.let { sharedText ->
                // 解析分享的文本
                viewModel.parseAndAddText(sharedText)
            }
        }
    }
    
    private fun initializePermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val readGranted = permissions[Manifest.permission.READ_SMS] == true
            val receiveGranted = permissions[Manifest.permission.RECEIVE_SMS] == true

            if (readGranted) {
                Log.d("MainActivity", "短信读取权限已授予")
                viewModel.onPermissionGranted()

                if (receiveGranted) {
                    Log.d("MainActivity", "短信接收权限已授予，自动识别已启用")
                } else {
                    Log.w("MainActivity", "RECEIVE_SMS 权限被拒绝，自动识别可能不可用")
                    Toast.makeText(
                        this,
                        "短信接收权限未授予，自动识别新短信功能可能不可用",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Log.w("MainActivity", "短信读取权限被拒绝")
                viewModel.onPermissionDenied()
            }
        }
    }

    fun requestSMSPermission() {
        try {
            Log.d("MainActivity", "请求短信权限")
            // 同时请求 READ_SMS 和 RECEIVE_SMS
            val permissions = arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS
            )
            requestPermissionLauncher.launch(permissions)
        } catch (e: Exception) {
            Log.e("MainActivity", "权限请求失败: ${e.message}", e)
            Toast.makeText(
                this,
                "权限请求失败: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}