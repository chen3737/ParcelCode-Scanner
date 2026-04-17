package com.mashangqujian.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Switch
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mashangqujian.data.model.CodeFormatType
import com.mashangqujian.data.model.ParsingRule
import com.mashangqujian.ui.MainViewModel
import kotlinx.coroutines.launch

// 预设选项
val PRESET_COMPANIES = listOf("顺丰", "京东", "中通", "圆通", "韵达", "菜鸟驿站", "邮政", "EMS")
val PRESET_CODE_KEYWORDS = listOf("取件码", "验证码", "提取码", "密码", "code")
val PRESET_ADDRESS_KEYWORDS = listOf("到达", "请到", "地址", "在", "于")
val PRESET_CODE_FORMATS = listOf(
    CodeFormatType.DIGITS to "纯数字（如 123456）",
    CodeFormatType.DIGIT_SEGMENTS to "数字段（如 3-5-1234）",
    CodeFormatType.LETTER_DIGITS to "字母+数字（如 A3-1234）",
)
val PRESET_DIGIT_RANGES = listOf(
    "3-6位" to (3 to 6),
    "4-6位" to (4 to 6),
    "4-8位" to (4 to 8),
    "3-8位" to (3 to 8),
    "6-12位" to (6 to 12),
)

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
                        onClick = { viewModel.loadAllRules() },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
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
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("添加规则")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val groupedRules = rules.groupBy { it.companyName }

                    groupedRules.forEach { (companyName, companyRules) ->
                        item {
                            Text(
                                text = companyName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

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
                    scope.launch {
                        viewModel.addRule(rule)
                        snackbarHostState.showSnackbar("规则已添加")
                        showAddDialog = false
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
                        scope.launch {
                            viewModel.updateRule(updatedRule)
                            snackbarHostState.showSnackbar("规则已更新")
                            showEditDialog = false
                            selectedRuleForEdit = null
                        }
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
 * 通用规则表单对话框 — 傻瓜式输入，自动生成正则
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
    // 表单状态 — 从现有规则预填或初始化
    var companyName by remember {
        mutableStateOf(initialRule?.companyName ?: "")
    }
    var companyExpanded by remember { mutableStateOf(false) }

    var selectedFormatType by remember {
        mutableStateOf(
            initialRule?.formatType ?: CodeFormatType.DIGITS
        )
    }
    var formatExpanded by remember { mutableStateOf(false) }

    var codeKeyword by remember {
        mutableStateOf(initialRule?.codeKeyword ?: "")
    }
    var keywordExpanded by remember { mutableStateOf(false) }

    var selectedDigitRange by remember {
        mutableStateOf(
            if (initialRule != null) {
                "${initialRule.codeMinDigits}-${initialRule.codeMaxDigits}位"
            } else {
                "4-6位"
            }
        )
    }
    var digitRangeExpanded by remember { mutableStateOf(false) }

    var addressKeyword by remember {
        mutableStateOf(initialRule?.addressKeyword ?: "")
    }
    var addressExpanded by remember { mutableStateOf(false) }

    var smsExample by remember {
        mutableStateOf(initialRule?.smsExample ?: "")
    }

    var description by remember {
        mutableStateOf(initialRule?.description ?: "")
    }

    // 如果是编辑预设规则，尝试从正则反向解析
    if (initialRule != null && isEditingPreset && initialRule.codeKeyword == null) {
        LaunchedEffect(Unit) {
            val parsed = ParsingRule.parseCodePattern(initialRule.parcelCodePattern)
            if (parsed.keyword != null) codeKeyword = parsed.keyword
            if (parsed.formatType != null) selectedFormatType = parsed.formatType
            if (parsed.minDigits != null && parsed.maxDigits != null) {
                selectedDigitRange = "${parsed.minDigits}-${parsed.maxDigits}位"
            }
            addressKeyword = ParsingRule.parseAddressKeyword(initialRule.addressPattern) ?: ""
        }
    }

    // 计算生成的正则表达式（用于预览）
    val generatedCodePattern = remember(codeKeyword, selectedFormatType, selectedDigitRange) {
        val pattern = ParsingRule.generateParcelCodePattern(
            formatType = selectedFormatType,
            codeKeyword = codeKeyword.takeIf { it.isNotBlank() },
            minDigits = 3,
            maxDigits = 8
        )
        pattern
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
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 取件码格式
                    item {
                        Text("取件码格式", style = MaterialTheme.typography.labelLarge)
                        ExposedDropdownMenuBox(
                            expanded = formatExpanded,
                            onExpandedChange = { formatExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedFormatType.label,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formatExpanded) }
                            )
                            ExposedDropdownMenu(
                                expanded = formatExpanded,
                                onDismissRequest = { formatExpanded = false }
                            ) {
                                PRESET_CODE_FORMATS.forEach { (type, desc) ->
                                    DropdownMenuItem(
                                        text = { Text("$type.label · $desc") },
                                        onClick = {
                                            selectedFormatType = type
                                            formatExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 取件码关键词
                    item {
                        Text("取件码关键词", style = MaterialTheme.typography.labelLarge)
                        ExposedDropdownMenuBox(
                            expanded = keywordExpanded,
                            onExpandedChange = { keywordExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = codeKeyword,
                                onValueChange = { codeKeyword = it },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                placeholder = { Text("例如：取件码、验证码") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = keywordExpanded) },
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = keywordExpanded,
                                onDismissRequest = { keywordExpanded = false }
                            ) {
                                PRESET_CODE_KEYWORDS.forEach { keyword ->
                                    DropdownMenuItem(
                                        text = { Text(keyword) },
                                        onClick = {
                                            codeKeyword = keyword
                                            keywordExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 数字位数（仅纯数字格式需要）
                    if (selectedFormatType == CodeFormatType.DIGITS) {
                        item {
                            Text("数字位数", style = MaterialTheme.typography.labelLarge)
                            ExposedDropdownMenuBox(
                                expanded = digitRangeExpanded,
                                onExpandedChange = { digitRangeExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = selectedDigitRange,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    placeholder = { Text("选择数字位数范围") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = digitRangeExpanded) }
                                )
                                ExposedDropdownMenu(
                                    expanded = digitRangeExpanded,
                                    onDismissRequest = { digitRangeExpanded = false }
                                ) {
                                    PRESET_DIGIT_RANGES.forEach { (label, _) ->
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = {
                                                selectedDigitRange = label
                                                digitRangeExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    // 地址关键词（可选）
                    item {
                        Text("地址关键词（可选）", style = MaterialTheme.typography.labelLarge)
                        ExposedDropdownMenuBox(
                            expanded = addressExpanded,
                            onExpandedChange = { addressExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = addressKeyword,
                                onValueChange = { addressKeyword = it },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                placeholder = { Text("例如：到达、请到") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = addressExpanded) },
                                singleLine = true
                            )
                            ExposedDropdownMenu(
                                expanded = addressExpanded,
                                onDismissRequest = { addressExpanded = false }
                            ) {
                                PRESET_ADDRESS_KEYWORDS.forEach { keyword ->
                                    DropdownMenuItem(
                                        text = { Text(keyword) },
                                        onClick = {
                                            addressKeyword = keyword
                                            addressExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
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
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 描述（可选）
                    item {
                        Text("描述（可选）", style = MaterialTheme.typography.labelLarge)
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("规则描述") },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 生成的正则预览
                    item {
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
                            if (companyName.isNotBlank()) {
                                val (min, max) = parseDigitRange(selectedDigitRange)
                                val rule = ParsingRule(
                                    id = initialRule?.id
                                        ?: ParsingRule.generateId(companyName),
                                    companyName = companyName,
                                    codeKeyword = codeKeyword.takeIf { it.isNotBlank() },
                                    codeFormat = selectedFormatType.name,
                                    codeMinDigits = min,
                                    codeMaxDigits = max,
                                    addressKeyword = addressKeyword.takeIf { it.isNotBlank() },
                                    smsExample = smsExample,
                                    parcelCodePattern = generatedCodePattern,
                                    addressPattern = generatedAddressPattern,
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
                        enabled = companyName.isNotBlank()
                    ) {
                        Text(saveButtonText)
                    }
                }
            }
        }
    }
}

/**
 * 解析 "3-6位" 格式的字符串为 (min, max) 元组
 */
private fun parseDigitRange(range: String): Pair<Int, Int> {
    val regex = Regex("""(\d+)-(\d+)位""")
    val match = regex.find(range)
    return if (match != null) {
        match.groupValues[1].toInt() to match.groupValues[2].toInt()
    } else {
        3 to 8
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
            containerColor = if (!rule.isEnabled) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
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
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 显示用户友好的配置信息
                    if (rule.codeKeyword != null) {
                        Text(
                            text = "${rule.codeKeyword} · ${rule.codeMinDigits}-${rule.codeMaxDigits}位数字",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        rule.addressKeyword?.let {
                            Text(
                                text = "地址关键词: $it",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    } else {
                        Text(
                            text = rule.description.ifEmpty { "取件码规则" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // 规则信息
                    Row {
                        Text(
                            text = if (rule.isCustom) "自定义规则" else "预设规则",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "匹配次数: ${rule.matchCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Switch(
                        checked = rule.isEnabled,
                        onCheckedChange = onToggle
                    )

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
