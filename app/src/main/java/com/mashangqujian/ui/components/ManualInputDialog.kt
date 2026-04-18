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
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mashangqujian.ui.MainViewModel
import com.mashangqujian.ui.theme.MashangqujianTheme
import kotlinx.coroutines.delay

@Composable
fun ManualInputDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val manualSMSText by remember { viewModel.manualSMSText }
    val clipboardManager = LocalClipboardManager.current

    var textValue by remember { mutableStateOf(TextFieldValue(manualSMSText)) }
    var isParsing by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var showErrorMessage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // 自动粘贴剪贴板内容（仅当对话框首次打开时）
    LaunchedEffect(Unit) {
        clipboardManager.getText()?.let { annotatedString ->
            annotatedString.text.let { text ->
                if (text.isNotBlank() && text.contains("取件码")) {
                    // 延迟一点时间，等对话框显示后再粘贴
                    delay(100)
                    textValue = TextFieldValue(text)
                    viewModel.manualSMSText.value = text
                }
            }
        }
    }
    
    // 成功消息显示3秒后自动关闭
    if (showSuccessMessage) {
        LaunchedEffect(showSuccessMessage) {
            delay(3000)
            onDismiss()
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("手动输入短信")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 说明文字
                Text(
                    text = "粘贴或输入包含取件码的短信内容：",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                // 粘贴按钮
                Button(
                    onClick = {
                        clipboardManager.getText()?.let { annotatedString ->
                            textValue = TextFieldValue(annotatedString.text)
                            viewModel.manualSMSText.value = annotatedString.text
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContentPaste, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("粘贴剪贴板内容")
                }

                // 输入框
                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it
                        viewModel.manualSMSText.value = it.text
                    },
                    label = { Text("短信内容") },
                    placeholder = { Text("例如：【顺丰速运】您的快件已到达某某小区快递柜，取件码：123456，请及时取件。") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 6,
                    maxLines = 8,
                    enabled = !isParsing,
                    isError = showErrorMessage
                )

                // 错误信息
                if (showErrorMessage) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("取消")
                }
                Button(
                    onClick = {
                        val text = textValue.text.trim()
                        if (text.isEmpty()) {
                            errorMessage = "请输入短信内容"
                            showErrorMessage = true
                            showSuccessMessage = false
                            return@Button
                        }

                        isParsing = true
                        showErrorMessage = false

                        // 调用ViewModel的addManually方法，它会处理解析和更新状态
                        viewModel.addManually()

                        // 检查是否解析成功（通过ViewModel的errorMessage状态）
                        // 这里不立即关闭对话框，让MainScreen根据解析结果决定是否关闭
                        isParsing = false
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isParsing && !showSuccessMessage
                ) {
                    if (isParsing) {
                        Text("解析中...")
                    } else if (showSuccessMessage) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sms, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("添加成功")
                        }
                    } else {
                        Text("识别")
                    }
                }
            }
        }
    )
}

// 预览函数
@Composable
fun ManualInputDialogPreview() {
    MashangqujianTheme {
        ManualInputDialog(
            viewModel = MainViewModel(),
            onDismiss = {}
        )
    }
}