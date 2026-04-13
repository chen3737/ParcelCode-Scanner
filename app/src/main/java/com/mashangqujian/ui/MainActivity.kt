package com.mashangqujian.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.mashangqujian.ui.theme.MashangqujianTheme

class MainActivity : ComponentActivity() {
    
    private val viewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
        
        // 检查权限
        viewModel.checkPermissions(this)
        
        // 处理从其他应用分享的文本
        handleSharedText()
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
}