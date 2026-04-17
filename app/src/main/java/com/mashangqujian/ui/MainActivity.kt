package com.mashangqujian.ui

import android.Manifest
import android.os.Bundle
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
import com.mashangqujian.ui.components.MainScreen
import com.mashangqujian.ui.theme.MashangqujianTheme

class MainActivity : ComponentActivity() {
    
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    
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
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                Log.d("MainActivity", "短信权限已授予")
                viewModel.onPermissionGranted()
                // 不再自动扫描短信，改为用户手动触发
                Toast.makeText(
                    this,
                    "短信权限已授予，现在可以手动扫描短信或手动输入短信内容",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Log.w("MainActivity", "短信权限被拒绝")
                viewModel.onPermissionDenied()
                // 可以显示一个Toast或Snackbar提示用户
                Toast.makeText(
                    this,
                    "短信读取权限被拒绝，部分功能将无法使用",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    fun requestSMSPermission() {
        try {
            Log.d("MainActivity", "请求短信读取权限")
            requestPermissionLauncher.launch(Manifest.permission.READ_SMS)
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