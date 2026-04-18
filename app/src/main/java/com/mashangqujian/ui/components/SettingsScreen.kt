package com.mashangqujian.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mashangqujian.ui.MainViewModel
import com.mashangqujian.ui.theme.MashangqujianTheme

private const val PREFS_NAME = "app_prefs"

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(viewModel: MainViewModel) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 顶部标题区
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                text = "设置",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // 扫描设置分组
        SettingsSection(title = "扫描设置") {
            // 自动扫描开关
            SettingsRow(
                icon = Icons.Default.Settings,
                title = "自动扫描新短信",
                subtitle = "收到新短信时自动扫描",
                trailing = {
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
            )

            // 扫描天数
            SettingsRow(
                icon = Icons.Default.Schedule,
                title = "扫描短信范围",
                subtitle = "最近 ${scanDays} 天的短信"
            )

            // 天数选项
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                scanDaysOptions.forEach { days ->
                    val isSelected = scanDays == days
                    androidx.compose.material3.FilterChip(
                        selected = isSelected,
                        onClick = {
                            scanDays = days
                            sharedPrefs.edit()
                                .putInt("scan_days", days)
                                .apply()
                        },
                        label = { Text("${days}天", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 快捷操作分组
        SettingsSection(title = "快捷操作") {
            // 规则管理
            SettingsActionRow(
                icon = Icons.Default.Rule,
                title = "规则管理",
                subtitle = "管理快递解析规则",
                onClick = { viewModel.openRuleManagement() }
            )

            // 删除历史
            SettingsActionRow(
                icon = Icons.Default.Delete,
                title = "删除历史",
                subtitle = "查看30天内删除的记录",
                onClick = { viewModel.openDeleteHistory() }
            )

            // 清理数据
            SettingsActionRow(
                icon = Icons.Default.Storage,
                title = "清理所有数据",
                subtitle = "删除所有取件记录",
                onClick = { showClearConfirmDialog = true },
                tint = MaterialTheme.colorScheme.error
            )
        }

        // 关于分组
        SettingsSection(title = "关于") {
            SettingsRow(
                icon = Icons.Default.Info,
                title = "版本",
                subtitle = "v0.2"
            )
            SettingsRow(
                icon = Icons.Default.History,
                title = "记录数",
                subtitle = "${viewModel.parcels.size} 条 (${viewModel.uncollectedCount.value} 未取)"
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }

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
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                Button(onClick = { showClearConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

// ==================== 辅助组件 ====================

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                content()
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 图标
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = tint
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // 文字
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
            }
        }

        // 尾部内容
        if (trailing != null) {
            trailing()
        }
    }
}

@Composable
fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = tint
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
            }
        }

        // 右侧箭头
        Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
    }
}

// ====================== 预览函数 ======================

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun SettingsPagePreview() {
    MashangqujianTheme(darkTheme = false) {
        SettingsPage(viewModel = MainViewModel())
    }
}
