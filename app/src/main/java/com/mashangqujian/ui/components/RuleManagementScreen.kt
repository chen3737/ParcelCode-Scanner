package com.mashangqujian.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mashangqujian.data.model.ParsingRule
import com.mashangqujian.ui.MainViewModel
import kotlinx.coroutines.launch

// 预设公司名称（用于输入提示）
val PRESET_COMPANIES = listOf("顺丰", "京东", "中通", "圆通", "韵达", "菜鸟驿站", "邮政", "EMS")

/**
 * 规则管理屏幕 - 管理取件码识别规则
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleManagementScreen(
    viewModel: MainViewModel = viewModel(),
    onBack: () -> Unit
) {
    val rules = viewModel.allRules
    val isLoading = viewModel.isLoading.value

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedRuleForEdit by remember { mutableStateOf<ParsingRule?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var selectedRuleForDelete by remember { mutableStateOf<ParsingRule?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 加载规则
    LaunchedEffect(Unit) {
        viewModel.loadAllRules()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("取件码识别规则管理") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddDialog = true }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "添加规则")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && rules.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("正在加载规则...")
                }
            } else if (rules.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("暂无规则", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("点击右上角 + 按钮添加规则", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val groupedRules = rules.groupBy { it.companyName }

                    groupedRules.forEach { (companyName, companyRules) ->
                        items(companyRules) { rule ->
                            RuleItem(
                                rule = rule,
                                onToggle = { enabled ->
                                    scope.launch {
                                        viewModel.setRuleEnabled(rule.id, enabled)
                                        val message = if (enabled) "规则已启用" else "规则已禁用"
                                        snackbarHostState.showSnackbar(message)
                                    }
                                },
                                onEdit = {
                                    selectedRuleForEdit = rule
                                    showEditDialog = true
                                },
                                onDelete = {
                                    selectedRuleForDelete = rule
                                    showDeleteConfirm = true
                                }
                            )
                        }

                        item {
                            Divider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                        }
                    }
                }
            }
        }

        // 添加规则对话框
        if (showAddDialog) {
            RuleFormDialog(
                title = "添加自定义规则",
                saveButtonText = "添加",
                onDismiss = { showAddDialog = false },
                onSave = { rule ->
                    showAddDialog = false
                    scope.launch {
                        viewModel.addRule(rule)
                        snackbarHostState.showSnackbar("规则已添加")
                    }
                }
            )
        }

        // 编辑规则对话框
        selectedRuleForEdit?.let { rule ->
            if (showEditDialog) {
                RuleFormDialog(
                    title = if (rule.isCustom) "编辑规则" else "复制为自定义规则",
                    saveButtonText = if (rule.isCustom) "保存" else "创建副本",
                    initialRule = rule,
                    isEditingPreset = !rule.isCustom,
                    onDismiss = {
                        showEditDialog = false
                        selectedRuleForEdit = null
                    },
                    onSave = { updatedRule ->
                        showEditDialog = false
                        val ruleToSave = updatedRule
                        scope.launch {
                            viewModel.updateRule(ruleToSave)
                            snackbarHostState.showSnackbar("规则已更新")
                        }
                        selectedRuleForEdit = null
                    }
                )
            }
        }

        // 删除确认对话框
        selectedRuleForDelete?.let { rule ->
            if (showDeleteConfirm) {
                DeleteConfirmDialog(
                    rule = rule,
                    onDismiss = {
                        showDeleteConfirm = false
                        selectedRuleForDelete = null
                    },
                    onConfirm = {
                        scope.launch {
                            viewModel.deleteRule(rule)
                            snackbarHostState.showSnackbar("规则已删除")
                            showDeleteConfirm = false
                            selectedRuleForDelete = null
                        }
                    }
                )
            }
        }
    }
}

/**
 * 通用规则表单对话框 — 傻瓜式输入，从[前缀]到[后缀]
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleFormDialog(
    title: String,
    saveButtonText: String,
    initialRule: ParsingRule? = null,
    isEditingPreset: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (ParsingRule) -> Unit
) {
    var companyName by remember {
        mutableStateOf(initialRule?.companyName ?: "")
    }
    var companyExpanded by remember { mutableStateOf(false) }

    var codePrefix by remember {
        mutableStateOf(initialRule?.codePrefix ?: "")
    }
    var codeSuffix by remember {
        mutableStateOf(initialRule?.codeSuffix ?: "")
    }

    var addressKeyword by remember {
        mutableStateOf(initialRule?.addressKeyword ?: "")
    }

    var smsExample by remember {
        mutableStateOf(initialRule?.smsExample ?: "")
    }

    var description by remember {
        mutableStateOf(initialRule?.description ?: "")
    }

    // 计算生成的正则表达式（用于预览）
    val generatedCodePattern = remember(codePrefix, codeSuffix) {
        if (codePrefix.isNotBlank() && codeSuffix.isNotBlank()) {
            ParsingRule.generatePatternFromPrefixSuffix(codePrefix, codeSuffix)
        } else {
            ""
        }
    }
    val generatedAddressPattern = remember(addressKeyword) {
        if (addressKeyword.isNotBlank()) {
            ParsingRule.generateAddressPattern(addressKeyword)
        } else null
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // 快递公司
                    item {
                        Text("快递公司", style = MaterialTheme.typography.labelLarge)
                        ExposedDropdownMenuBox(
                            expanded = companyExpanded,
                            onExpandedChange = { companyExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = companyName,
                                onValueChange = { companyName = it },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                placeholder = { Text("选择或输入公司名称") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = companyExpanded) },
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = companyExpanded,
                                onDismissRequest = { companyExpanded = false }
                            ) {
                                PRESET_COMPANIES.forEach { company ->
                                    DropdownMenuItem(
                                        text = { Text(company) },
                                        onClick = {
                                            companyName = company
                                            companyExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // 从 [前缀] 到 [后缀]
                    item {
                        Text("取件码识别范围", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("从", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.width(4.dp))
                            OutlinedTextField(
                                value = codePrefix,
                                onValueChange = { codePrefix = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("取件码前的文字") },
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("到", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.width(4.dp))
                            OutlinedTextField(
                                value = codeSuffix,
                                onValueChange = { codeSuffix = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("取件码后的文字") },
                                singleLine = true
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // 地址关键词（可选）
                    item {
                        Text("地址关键词（可选）", style = MaterialTheme.typography.labelLarge)
                        OutlinedTextField(
                            value = addressKeyword,
                            onValueChange = { addressKeyword = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("例如：到达、请到") },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // 短信示例（可选）
                    item {
                        Text("短信示例（可选）", style = MaterialTheme.typography.labelLarge)
                        OutlinedTextField(
                            value = smsExample,
                            onValueChange = { smsExample = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("粘贴一条真实短信用于预览") },
                            minLines = 2,
                            maxLines = 4
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // 生成的正则预览
                    item {
                        if (generatedCodePattern.isNotEmpty()) {
                            Text("生成的规则（自动）", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = "取件码: $generatedCodePattern",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            generatedAddressPattern?.let { addr ->
                                Text(
                                    text = "地址: $addr",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (companyName.isNotBlank() && codePrefix.isNotBlank() && codeSuffix.isNotBlank()) {
                                val pattern = ParsingRule.generatePatternFromPrefixSuffix(codePrefix, codeSuffix)
                                val addrPattern = if (addressKeyword.isNotBlank()) {
                                    ParsingRule.generateAddressPattern(addressKeyword)
                                } else null
                                val rule = ParsingRule(
                                    id = initialRule?.id
                                        ?: ParsingRule.generateId(companyName),
                                    companyName = companyName,
                                    codePrefix = codePrefix,
                                    codeSuffix = codeSuffix,
                                    addressKeyword = addressKeyword.takeIf { it.isNotBlank() },
                                    smsExample = smsExample,
                                    parcelCodePattern = pattern,
                                    addressPattern = addrPattern,
                                    isCustom = true,
                                    isEnabled = initialRule?.isEnabled ?: true,
                                    description = description,
                                    matchCount = initialRule?.matchCount ?: 0,
                                    createdAt = initialRule?.createdAt ?: System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis()
                                )
                                onSave(rule)
                            }
                        },
                        enabled = companyName.isNotBlank() && codePrefix.isNotBlank() && codeSuffix.isNotBlank()
                    ) {
                        Text(saveButtonText)
                    }
                }
            }
        }
    }
}

/**
 * 规则项组件
 */
@Composable
fun RuleItem(
    rule: ParsingRule,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (rule.isCustom) {
                            Text(
                                text = "✏️ ",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text(
                            text = rule.companyName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (rule.isEnabled)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 显示用户友好的配置信息
                    val textColor = if (rule.isEnabled)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)

                    if (rule.codePrefix != null) {
                        Text(
                            text = "从[${rule.codePrefix}]到[${rule.codeSuffix}]",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                        rule.addressKeyword?.let {
                            Text(
                                text = "地址关键词: $it",
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor
                            )
                        }
                    } else {
                        Text(
                            text = rule.description.ifEmpty { "取件码规则" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 规则信息
                    Row {
                        Text(
                            text = if (rule.isCustom) "自定义规则" else "预设规则",
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "匹配次数: ${rule.matchCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    // 状态徽章（可点击切换）
                    var badgeText = if (rule.isEnabled) "已启用" else "已禁用"
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onToggle(!rule.isEnabled) }
                            .background(
                                if (rule.isEnabled)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (rule.isEnabled) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = badgeText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (rule.isEnabled)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row {
                        if (rule.isCustom) {
                            IconButton(onClick = onEdit) {
                                Icon(Icons.Default.Edit, contentDescription = "编辑")
                            }
                            IconButton(onClick = onDelete) {
                                Icon(Icons.Default.Delete, contentDescription = "删除")
                            }
                        } else {
                            IconButton(onClick = onEdit) {
                                Icon(Icons.Default.Edit, contentDescription = "复制为自定义")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 删除确认对话框
 */
@Composable
fun DeleteConfirmDialog(
    rule: ParsingRule,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("删除规则") },
        text = {
            Column {
                Text("确定要删除此规则吗？")
                Spacer(modifier = Modifier.height(8.dp))
                Text("公司: ${rule.companyName}")
                if (rule.codeKeyword != null) {
                    Text("关键词: ${rule.codeKeyword}")
                }
                if (!rule.isCustom) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "注意：预设规则不能被删除，但可以禁用。",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = rule.isCustom
            ) {
                Text("删除")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
