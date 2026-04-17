package com.mashangqujian.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mashangqujian.data.model.Parcel
import com.mashangqujian.ui.MainViewModel
import com.mashangqujian.ui.theme.MashangqujianTheme
import kotlin.math.abs
import kotlinx.coroutines.launch

// 导航项
sealed class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val activeIcon: ImageVector
) {
    object Home : NavItem("home", "首页", Icons.Outlined.Home, Icons.Default.LocalShipping)
    object History : NavItem("history", "历史", Icons.Outlined.History, Icons.Default.History)
    object Settings : NavItem("settings", "设置", Icons.Outlined.Settings, Icons.Default.Settings)
}

val navItems = listOf(NavItem.Home, NavItem.History, NavItem.Settings)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var currentRoute by remember { mutableStateOf(NavItem.Home.route) }

    val showManualInputDialog by remember { viewModel.showAddManuallyDialog }
    val showRuleManagement by remember { viewModel.showRuleManagement }
    val errorMessage = viewModel.errorMessage.value

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
                viewModel.clearError()
                if (message.contains("不包含有效的取件码") ||
                    message.contains("解析失败") ||
                    message.contains("添加失败") ||
                    message.contains("请输入短信内容")
                ) {
                    viewModel.keepDialogOpenOnFailure.value = true
                } else {
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
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "码上取件",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    // 扫描按钮
                    Button(
                        onClick = { viewModel.scanSMS() },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("扫描", fontSize = 14.sp)
                    }

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
                                onClick = { viewModel.scanLatestSMS(); showMenu = false },
                                leadingIcon = { Icon(Icons.Default.Search, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("手动输入取件码") },
                                onClick = { viewModel.openManualInputDialog(); showMenu = false },
                                leadingIcon = { Icon(Icons.Default.Add, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("规则管理") },
                                onClick = { viewModel.openRuleManagement(); showMenu = false },
                                leadingIcon = { Icon(Icons.Default.Rule, null) }
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            IOSBottomNavigationBar(
                items = navItems,
                currentRoute = currentRoute,
                onItemSelected = { route -> currentRoute = route }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentRoute) {
                NavItem.Home.route -> HomeScreen(viewModel)
                NavItem.History.route -> HistoryScreen(viewModel)
                NavItem.Settings.route -> SettingsPage(viewModel)
            }
        }
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

    // 粘贴板确认对话框
    if (viewModel.showClipboardDialog.value) {
        val parsedParcel = viewModel.clipboardParsedParcel.value
        if (parsedParcel != null) {
            ClipboardConfirmDialog(
                parcel = parsedParcel,
                clipboardText = viewModel.clipboardContent.value,
                onConfirm = { viewModel.confirmAddFromClipboard() },
                onDismiss = { viewModel.dismissClipboardDialog() }
            )
        }
    }
}

// ==================== iOS 风格底部导航栏 ====================

@Composable
fun IOSBottomNavigationBar(
    items: List<NavItem>,
    currentRoute: String,
    onItemSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                    .copy(alpha = 0.85f)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                IOSNavTab(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onItemSelected(item.route) }
                )
            }
        }

        // 顶部细线
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.5.dp)
                .align(Alignment.TopCenter)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        )
    }
}

@Composable
fun IOSNavTab(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (isSelected) item.activeIcon else item.icon,
            contentDescription = item.label,
            modifier = Modifier
                .size(24.dp)
                .scale(scale),
            tint = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = item.label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

// ==================== 首页（待取件） ====================

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val uncollectedParcels by remember(viewModel.parcels) {
        derivedStateOf { viewModel.parcels.filter { !it.isCollected } }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 待取件快速操作栏
        if (uncollectedParcels.isNotEmpty()) {
            UncollectedQuickAction(
                count = uncollectedParcels.size,
                onMarkAllCollected = {
                    uncollectedParcels.forEach { viewModel.markAsCollected(it) }
                }
            )
        }

        // 内容区域
        if (viewModel.isLoading.value) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("正在加载...")
                }
            }
        } else if (uncollectedParcels.isEmpty()) {
            EmptyState()
        } else {
            SwipeableParcelList(
                parcels = uncollectedParcels,
                viewModel = viewModel,
                showCollectedHeader = false
            )
        }
    }
}

// ==================== 取件历史 ====================

@Composable
fun HistoryScreen(viewModel: MainViewModel) {
    val collectedParcels by remember(viewModel.parcels) {
        derivedStateOf { viewModel.parcels.filter { it.isCollected }.sortedByDescending { it.smsDate } }
    }

    if (viewModel.isLoading.value) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("正在加载...")
            }
        }
    } else if (collectedParcels.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp),
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
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "暂无历史记录",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "已取件的包裹将显示在这里",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
            )
        }
    } else {
        SwipeableParcelList(
            parcels = collectedParcels,
            viewModel = viewModel,
            showCollectedHeader = true
        )
    }
}

// ==================== 待取件快速操作栏 ====================

@Composable
fun UncollectedQuickAction(count: Int, onMarkAllCollected: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：可点击的快速标记区域
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { showConfirm = true }
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$count",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "待取件包裹",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "点击一键标记为已取件",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                        )
                    }
                }
            }
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("确认标记") },
            text = { Text("确定要将 $count 个包裹全部标记为已取件吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        onMarkAllCollected()
                        showConfirm = false
                    }
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                Button(onClick = { showConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
}

// ==================== 可滑动的包裹列表 ====================

@Composable
fun SwipeableParcelList(
    parcels: List<Parcel>,
    viewModel: MainViewModel,
    showCollectedHeader: Boolean
) {
    var showSmsDialog by remember { mutableStateOf<Parcel?>(null) }
    val clipboard = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = rememberLazyListState(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (showCollectedHeader) {
            item {
                SectionHeader("已取件 (${parcels.size})")
            }
        }
        items(parcels, key = { it.id }) { parcel ->
            SwipeableParcelItem(
                parcel = parcel,
                onShowSms = { showSmsDialog = parcel },
                onMarkAsCollected = { viewModel.markAsCollected(parcel) },
                onUncollected = { viewModel.markAsUncollected(parcel) },
                onCopyCode = { clipboard.setText(AnnotatedString(parcel.parcelCode)) },
                onDelete = { viewModel.deleteParcel(parcel) }
            )
        }
    }

    // 短信详情对话框
    showSmsDialog?.let { parcel ->
        ParcelSmsDialog(
            parcel = parcel,
            onDismiss = { showSmsDialog = null },
            onDelete = { viewModel.deleteParcel(parcel) },
            onCopyCode = { clipboard.setText(AnnotatedString(parcel.parcelCode)) }
        )
    }
}

// ==================== 可滑动标记已取件的包裹项 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableParcelItem(
    parcel: Parcel,
    onShowSms: () -> Unit,
    onMarkAsCollected: () -> Unit,
    onUncollected: () -> Unit,
    onCopyCode: () -> Unit,
    onDelete: () -> Unit
) {
    val density = LocalDensity.current
    val swipeThreshold = with(density) { 100.dp.toPx() }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var isSwiping by remember { mutableStateOf(false) }
    val isCollected = parcel.isCollected

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 底部滑动揭示块 — 与卡片同高度
        if (offsetX < -30f) {
            val revealAlpha = ((-offsetX - 30f) / (swipeThreshold - 30f)).coerceIn(0f, 1f)
            val revealText = if (-offsetX >= swipeThreshold) "已取件" else ""
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Color(0xFF34C759).copy(alpha = revealAlpha * 0.9f)
                    ),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (revealText.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(end = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "已取件",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            revealText,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // 前景卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .offset { IntOffset(offsetX.toInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { isSwiping = true },
                        onDragEnd = {
                            isSwiping = false
                            if (offsetX < -swipeThreshold) {
                                onMarkAsCollected()
                            }
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            if (dragAmount < 0) {
                                offsetX = (offsetX + dragAmount).coerceIn(-swipeThreshold * 1.2f, 0f)
                            }
                        }
                    )
                },
            onClick = onShowSms,
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isCollected) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (!isCollected && !isSwiping) 0.5.dp else 0.dp
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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

                        Text(
                            text = parcel.parcelCode,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // 操作菜单
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.MoreVert, "更多")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            if (!isCollected) {
                                DropdownMenuItem(
                                    text = { Text("标记为已取件") },
                                    onClick = { onMarkAsCollected(); showMenu = false },
                                    leadingIcon = { Icon(Icons.Default.CheckCircle, null) }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("标记为未取件") },
                                    onClick = { onUncollected(); showMenu = false },
                                    leadingIcon = { Icon(Icons.Default.Close, null) }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("复制取件码") },
                                onClick = { onCopyCode(); showMenu = false },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("删除") },
                                onClick = { onDelete(); showMenu = false },
                                leadingIcon = { Icon(Icons.Default.Delete, null) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = parcel.address,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(6.dp))

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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
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
}

// ==================== 短信详情对话框 ====================

@Composable
fun ParcelSmsDialog(
    parcel: Parcel,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onCopyCode: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("原始短信") },
        text = {
            Column {
                // 快递公司信息
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
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
                    Text(
                        text = parcel.parcelCode,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 短信内容
                Text(
                    text = "短信内容：",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = parcel.smsContent,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 地址
                if (parcel.address.isNotEmpty() && parcel.address != "未知地址") {
                    Text(
                        text = "地址：${parcel.address}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // 时间
                Text(
                    text = "时间：${android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", parcel.smsDate)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )

                if (parcel.sender.isNotEmpty()) {
                    Text(
                        text = "发件人：${parcel.sender}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // 状态
                Text(
                    text = if (parcel.isCollected) "状态：已取件" else "状态：待取件",
                    fontSize = 12.sp,
                    color = if (parcel.isCollected)
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("关闭")
                }
                Button(
                    onClick = { onCopyCode(); onDismiss() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("复制")
                }
            }
        },
        dismissButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onDelete(); onDismiss() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("删除")
                }
            }
        }
    )
}

// ==================== 粘贴板确认对话框 ====================

@Composable
fun ClipboardConfirmDialog(
    parcel: Parcel,
    clipboardText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("检测到取件码") },
        text = {
            Column {
                Text(
                    text = "粘贴板内容：",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = clipboardText,
                    fontSize = 13.sp,
                    maxLines = 4,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalShipping, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("快递公司：", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text(parcel.courierCompany, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("取件码：", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Text(parcel.parcelCode, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                }
                if (parcel.address.isNotEmpty() && parcel.address != "未知地址") {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp).padding(top = 1.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("地址：", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Text(parcel.address, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("取消")
            }
        }
    )
}

// ==================== 原有组件（保持） ====================

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
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("需要短信读取权限", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
        modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp),
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
            fontWeight = FontWeight.SemiBold
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

// ====================== 预览函数 ======================

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun EmptyStatePreviewLight() {
    MashangqujianTheme(darkTheme = false) { EmptyState() }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun EmptyStatePreviewDark() {
    MashangqujianTheme(darkTheme = true) { EmptyState() }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun ParcelItemUncollectedPreview() {
    SwipeableParcelItem(
        parcel = Parcel(
            parcelCode = "123456",
            address = "北京市朝阳区某某小区快递柜1号柜",
            courierCompany = "顺丰",
            smsContent = "【顺丰速运】您的快件已到达某某小区快递柜，取件码：123456，请及时取件。",
            smsDate = System.currentTimeMillis(),
            isCollected = false
        ),
        onShowSms = {},
        onMarkAsCollected = {},
        onUncollected = {},
        onCopyCode = {},
        onDelete = {}
    )
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun ParcelItemCollectedPreview() {
    SwipeableParcelItem(
        parcel = Parcel(
            parcelCode = "789012",
            address = "上海市浦东新区某某京东快递站",
            courierCompany = "京东",
            smsContent = "【京东物流】您的包裹已到达某某京东快递站，取件码：789012，请凭码取件。",
            smsDate = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000,
            isCollected = true
        ),
        onShowSms = {},
        onMarkAsCollected = {},
        onUncollected = {},
        onCopyCode = {},
        onDelete = {}
    )
}

@Preview(showBackground = true)
@Composable
fun SectionHeaderPreview() {
    SectionHeader("待取件 (2)")
}
