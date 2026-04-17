package com.mashangqujian.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Warning
import com.mashangqujian.ui.components.SettingsDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mashangqujian.data.model.Parcel
import com.mashangqujian.ui.MainViewModel
import com.mashangqujian.ui.theme.MashangqujianTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // 设置对话框状态
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    // 使用ViewModel中的状态
    val showManualInputDialog by remember { viewModel.showAddManuallyDialog }
    val showRuleManagement by remember { viewModel.showRuleManagement }
    val errorMessage = viewModel.errorMessage.value
    
    // 监听错误信息
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.clearError()
                
                // 如果解析失败，保持对话框打开
                if (message.contains("不包含有效的取件码") || 
                    message.contains("解析失败") || 
                    message.contains("添加失败") ||
                    message.contains("请输入短信内容")) {
                    viewModel.keepDialogOpenOnFailure.value = true
                } else {
                    // 解析成功，关闭对话框
                    if (showManualInputDialog) {
                        viewModel.closeManualInputDialog()
                    }
                }
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("码上取件")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // 未取件数量徽章
                    val uncollectedCount = viewModel.uncollectedCount.value
                    if (uncollectedCount > 0) {
                        Box(
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.error)
                                    .align(Alignment.TopEnd),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (uncollectedCount > 99) "99+" else uncollectedCount.toString(),
                                    color = MaterialTheme.colorScheme.onError,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            IconButton(
                                onClick = { viewModel.scanSMS() }
                            ) {
                                Icon(Icons.Default.Refresh, "刷新")
                            }
                        }
                    } else {
                        IconButton(
                            onClick = { viewModel.scanSMS() }
                        ) {
                            Icon(Icons.Default.Refresh, "刷新")
                        }
                    }
                    
                    // 更多菜单
                    var showMenu by remember { mutableStateOf(false) }
                    
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "更多")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("扫描最新短信") },
                                onClick = {
                                    viewModel.scanLatestSMS()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("手动输入取件码") },
                                onClick = {
                                    viewModel.openManualInputDialog()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Add, null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("规则管理") },
                                onClick = {
                                    viewModel.openRuleManagement()
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Rule, null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("设置") },
                                onClick = {
                                    showSettingsDialog = true
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Settings, null)
                                }
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 权限提示
            if (!viewModel.hasSMSPermission.value) {
                PermissionRequestCard(
                    onRequestPermission = { viewModel.requestSMSPermission(context as android.app.Activity) }
                )
            }
            
            // 加载状态
            if (viewModel.isLoading.value) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("正在加载...")
                    }
                }
            } else {
                // 内容区域
                if (viewModel.parcels.isEmpty()) {
                    EmptyState()
                } else {
                    ParcelList(
                        parcels = viewModel.parcels,
                        onItemClick = { parcel ->
                            viewModel.selectParcel(parcel)
                        },
                        onMarkAsCollected = { parcel ->
                            viewModel.markAsCollected(parcel)
                        },
                        onDelete = { parcel ->
                            viewModel.deleteParcel(parcel)
                        }
                    )
                }
            }
        }
    }
    
    // 设置对话框
    if (showSettingsDialog) {
        SettingsDialog(
            viewModel = viewModel,
            onDismiss = { showSettingsDialog = false }
        )
    }
    
    // 手动输入对话框
    if (showManualInputDialog) {
        ManualInputDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeManualInputDialog() }
        )
    }
    
    // 规则管理界面
    if (showRuleManagement) {
        RuleManagementScreen(
            viewModel = viewModel,
            onBack = { viewModel.closeRuleManagement() }
        )
    }
}

// ====================== 预览函数 ======================

/**
 * 预览：空状态 - 浅色主题
 */
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun EmptyStatePreviewLight() {
    MashangqujianTheme(darkTheme = false) {
        EmptyState()
    }
}

/**
 * 预览：空状态 - 深色主题
 */
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun EmptyStatePreviewDark() {
    MashangqujianTheme(darkTheme = true) {
        EmptyState()
    }
}

/**
 * 预览：权限请求卡片
 */
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun PermissionRequestCardPreview() {
    PermissionRequestCard(onRequestPermission = {})
}

/**
 * 预览：单个取件记录项 - 未取件状态
 */
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun ParcelItemUncollectedPreview() {
    ParcelItem(
        parcel = Parcel(
            parcelCode = "123456",
            address = "北京市朝阳区某某小区快递柜1号柜",
            courierCompany = "顺丰",
            smsContent = "【顺丰速运】您的快件已到达某某小区快递柜，取件码：123456，请及时取件。",
            smsDate = System.currentTimeMillis(),
            isCollected = false
        ),
        onClick = {},
        onMarkAsCollected = {},
        onDelete = {}
    )
}

/**
 * 预览：单个取件记录项 - 已取件状态
 */
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun ParcelItemCollectedPreview() {
    ParcelItem(
        parcel = Parcel(
            parcelCode = "789012",
            address = "上海市浦东新区某某京东快递站",
            courierCompany = "京东",
            smsContent = "【京东物流】您的包裹已到达某某京东快递站，取件码：789012，请凭码取件。",
            smsDate = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000, // 2天前
            isCollected = true
        ),
        onClick = {},
        onMarkAsCollected = {},
        onDelete = {},
        isCollected = true
    )
}

/**
 * 预览：取件列表 - 包含多个记录
 */
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun ParcelListPreview() {
    val parcels = listOf(
        Parcel(
            parcelCode = "123456",
            address = "北京市朝阳区某某小区快递柜1号柜",
            courierCompany = "顺丰",
            smsContent = "【顺丰速运】您的快件已到达某某小区快递柜，取件码：123456，请及时取件。",
            smsDate = System.currentTimeMillis(),
            isCollected = false
        ),
        Parcel(
            parcelCode = "654321",
            address = "北京市海淀区某某菜鸟驿站",
            courierCompany = "菜鸟驿站",
            smsContent = "【菜鸟驿站】取件码：654321，请到某某菜鸟驿站取件。",
            smsDate = System.currentTimeMillis() - 24 * 60 * 60 * 1000, // 1天前
            isCollected = false
        ),
        Parcel(
            parcelCode = "789012",
            address = "上海市浦东新区某某京东快递站",
            courierCompany = "京东",
            smsContent = "【京东物流】您的包裹已到达某某京东快递站，取件码：789012，请凭码取件。",
            smsDate = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000, // 3天前
            isCollected = true
        ),
        Parcel(
            parcelCode = "345678",
            address = "广州市天河区某某邮政快递点",
            courierCompany = "邮政",
            smsContent = "【邮政快递】您的包裹已到达某某邮政快递点，取件码：345678，请及时取件。",
            smsDate = System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000, // 5天前
            isCollected = true
        )
    )
    
    ParcelList(
        parcels = parcels,
        onItemClick = {},
        onMarkAsCollected = {},
        onDelete = {}
    )
}

/**
 * 预览：章节标题
 */
@Preview(showBackground = true)
@Composable
fun SectionHeaderPreview() {
    SectionHeader("待取件 (2)")
}

// 注意：MainScreen需要ViewModel，因此需要创建一个模拟的ViewModel用于预览
// 这通常需要在专门的预览文件中实现，或者创建一个预览专用的ViewModel

@Composable
fun PermissionRequestCard(onRequestPermission: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "需要短信读取权限",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "为了自动识别取件码短信，需要授予短信读取权限。应用只会读取包含取件码的短信。",
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("授予权限")
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Sms,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "还没有取件记录",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "从顶部菜单扫描短信，或手动输入取件码",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
        )
    }
}

@Composable
fun ParcelList(
    parcels: List<Parcel>,
    onItemClick: (Parcel) -> Unit,
    onMarkAsCollected: (Parcel) -> Unit,
    onDelete: (Parcel) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf<Parcel?>(null) }

    val uncollectedParcels by remember(parcels) {
        derivedStateOf { parcels.filter { !it.isCollected } }
    }

    val collectedParcels by remember(parcels) {
        derivedStateOf { parcels.filter { it.isCollected } }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (uncollectedParcels.isNotEmpty()) {
            item {
                SectionHeader("待取件 (${uncollectedParcels.size})")
            }
            items(uncollectedParcels) { parcel ->
                ParcelItem(
                    parcel = parcel,
                    onClick = { onItemClick(parcel) },
                    onMarkAsCollected = { onMarkAsCollected(parcel) },
                    onDelete = { showDeleteDialog = parcel }
                )
            }
        }

        if (collectedParcels.isNotEmpty()) {
            item {
                SectionHeader("已取件 (${collectedParcels.size})")
            }
            items(collectedParcels) { parcel ->
                ParcelItem(
                    parcel = parcel,
                    onClick = { onItemClick(parcel) },
                    onMarkAsCollected = { onMarkAsCollected(parcel) },
                    onDelete = { showDeleteDialog = parcel },
                    isCollected = true
                )
            }
        }
    }
    
    // 删除确认对话框
    showDeleteDialog?.let { parcel ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("删除取件码") },
            text = { Text("确定要删除 ${parcel.parcelCode} 的取件记录吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(parcel)
                        showDeleteDialog = null
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteDialog = null }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParcelItem(
    parcel: Parcel,
    onClick: () -> Unit,
    onMarkAsCollected: () -> Unit,
    onDelete: () -> Unit,
    isCollected: Boolean = false
) {
    val clipboardManager = LocalClipboardManager.current
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCollected) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCollected) 0.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 快递公司标签 - iOS 风格圆角
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = parcel.courierCompany,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // 取件码
                    Text(
                        text = parcel.parcelCode,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 更多按钮
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, "更多")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (isCollected) "标记为未取件" else "标记为已取件") },
                            onClick = {
                                onMarkAsCollected()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    if (isCollected) Icons.Default.Clear else Icons.Default.CheckCircle,
                                    null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("复制取件码") },
                            onClick = {
                                clipboardManager.setText(AnnotatedString(parcel.parcelCode))
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.ContentCopy, null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("删除") },
                            onClick = {
                                onDelete()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, null)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 地址
            Text(
                text = parcel.address,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 底部信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = parcel.getDaysSinceReceived().let { days ->
                        when {
                            days == 0L -> "今天"
                            days == 1L -> "昨天"
                            else -> "${days}天前"
                        }
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                )

                if (isCollected) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "已取件",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}