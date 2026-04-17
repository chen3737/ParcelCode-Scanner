package com.mashangqujian.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mashangqujian.ui.MainViewModel
import com.mashangqujian.ui.theme.MashangqujianTheme

private const val PREFS_NAME = "app_prefs"

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var autoScanEnabled by remember { mutableStateOf(
        sharedPrefs.getBoolean("auto_scan_enabled", true)
    )}
    var scanDays by remember { mutableStateOf(
        sharedPrefs.getInt("scan_days", 7)
    )}
    val scanDaysOptions = listOf(1, 3, 7, 14, 30)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("设置")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 自动扫描开关
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("自动扫描新短信", fontWeight = FontWeight.Medium)
                        Text("收到新短信时自动扫描", fontSize = 12.sp)
                    }
                    Switch(
                        checked = autoScanEnabled,
                        onCheckedChange = { enabled ->
                            autoScanEnabled = enabled
                            sharedPrefs.edit()
                                .putBoolean("auto_scan_enabled", enabled)
                                .apply()
                        }
                    )
                }

                // 扫描天数
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("扫描短信范围", fontWeight = FontWeight.Medium)
                    }
                    Text("最近 ${scanDays} 天的短信", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        scanDaysOptions.forEach { days ->
                            androidx.compose.material3.FilterChip(
                                selected = scanDays == days,
                                onClick = {
                                    scanDays = days
                                    sharedPrefs.edit()
                                        .putInt("scan_days", days)
                                        .apply()
                                },
                                label = { Text("${days}天") }
                            )
                        }
                    }
                }

                // 清理数据按钮
                Button(
                    onClick = { showClearConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Storage, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("清理所有数据")
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )

    // 清理数据确认对话框
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("确认清理数据") },
            text = { Text("确定要删除所有取件记录吗？此操作不可撤销。") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirmDialog = false
                        viewModel.clearAllData()
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showClearConfirmDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

// 预览函数
@Composable
fun SettingsDialogPreview() {
    MashangqujianTheme {
        SettingsDialog(
            viewModel = MainViewModel(),
            onDismiss = {}
        )
    }
}
