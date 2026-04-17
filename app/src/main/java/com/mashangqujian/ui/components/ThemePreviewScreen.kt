package com.mashangqujian.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mashangqujian.ui.theme.MashangqujianBlueTheme
import com.mashangqujian.ui.theme.MashangqujianGreenTheme
import com.mashangqujian.ui.theme.IOSBluePrimary
import com.mashangqujian.ui.theme.IOSBlueSecondary
import com.mashangqujian.ui.theme.IOSGreen
import com.mashangqujian.ui.theme.IOSOrange
import com.mashangqujian.ui.theme.IOSRed
import com.mashangqujian.ui.theme.IOSPurple
import com.mashangqujian.ui.theme.BackgroundWhite
import com.mashangqujian.ui.theme.SurfaceWhite
import com.mashangqujian.ui.theme.TextPrimary
import com.mashangqujian.ui.theme.TextSecondary
import com.mashangqujian.ui.theme.TextTertiary

/**
 * 主题预览屏幕，展示两种配色方案
 */
@Composable
fun ThemePreviewScreen() {
    val useGreenTheme = remember { mutableStateOf(false) }
    val darkTheme = remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // 主题选择器
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "配色方案选择",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !useGreenTheme.value,
                        onClick = { useGreenTheme.value = false }
                    )
                    Text(
                        text = "简约蓝色方案 (iOS风格)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = useGreenTheme.value,
                        onClick = { useGreenTheme.value = true }
                    )
                    Text(
                        text = "清新绿色方案 (健康风格)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = darkTheme.value,
                        onCheckedChange = { darkTheme.value = it }
                    )
                    Text(
                        text = "深色模式",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 颜色预览
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "颜色预览",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 主要颜色
                Text(
                    text = "主要颜色",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ColorSample(
                        color = MaterialTheme.colorScheme.primary,
                        label = "主色"
                    )
                    ColorSample(
                        color = MaterialTheme.colorScheme.secondary,
                        label = "副色"
                    )
                    ColorSample(
                        color = MaterialTheme.colorScheme.tertiary,
                        label = "三色"
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 状态颜色
                Text(
                    text = "状态颜色",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ColorSample(
                        color = MaterialTheme.colorScheme.error,
                        label = "错误",
                        icon = Icons.Filled.Error
                    )
                    
                    ColorSample(
                        color = if (useGreenTheme.value) {
                            if (darkTheme.value) Color(0xFF34C759) else Color(0xFF34C759)
                        } else {
                            MaterialTheme.colorScheme.secondary
                        },
                        label = "成功",
                        icon = Icons.Filled.Check
                    )
                    
                    ColorSample(
                        color = if (useGreenTheme.value) {
                            if (darkTheme.value) Color(0xFFFF9500) else Color(0xFFFF9500)
                        } else {
                            MaterialTheme.colorScheme.tertiary
                        },
                        label = "警告",
                        icon = Icons.Filled.Warning
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // UI组件预览
                Text(
                    text = "UI组件",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { /* 处理点击 */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "主要按钮")
                    }
                    
                    Button(
                        onClick = { /* 处理点击 */ },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false
                    ) {
                        Text(text = "禁用按钮")
                    }
                    
                    Text(
                        text = "主要文字",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "次要文字",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "禁用文字",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun ColorSample(
    color: Color,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(color)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun BlueThemePreview() {
    MashangqujianBlueTheme {
        ThemePreviewScreen()
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun GreenThemePreview() {
    MashangqujianGreenTheme {
        ThemePreviewScreen()
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun BlueDarkThemePreview() {
    MashangqujianBlueTheme(darkTheme = true) {
        ThemePreviewScreen()
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun GreenDarkThemePreview() {
    MashangqujianGreenTheme(darkTheme = true) {
        ThemePreviewScreen()
    }
}