package com.mashangqujian.ui.preview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mashangqujian.data.model.Parcel
import com.mashangqujian.ui.components.EmptyState
import com.mashangqujian.ui.components.ParcelItem
import com.mashangqujian.ui.components.ParcelList
import com.mashangqujian.ui.components.PermissionRequestCard
import com.mashangqujian.ui.components.SectionHeader
import com.mashangqujian.ui.theme.MashangqujianTheme

/**
 * 预览集合屏幕 - 包含所有UI组件的预览
 */
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun AllComponentsPreview() {
    MashangqujianTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("UI组件预览集合", style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("1. 空状态", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            EmptyState()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("2. 权限请求卡片", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            PermissionRequestCard(onRequestPermission = {})
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("3. 章节标题", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            SectionHeader("待取件 (3)")
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("4. 单个取件记录项", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
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
    }
}

/**
 * 取件列表完整预览
 */
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun FullParcelListPreview() {
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
    
    MashangqujianTheme(darkTheme = false) {
        ParcelList(
            parcels = parcels,
            onItemClick = {},
            onMarkAsCollected = {},
            onDelete = {}
        )
    }
}

/**
 * 浅色主题预览集合
 */
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun LightThemeComponentsPreview() {
    MashangqujianTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("浅色主题预览", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 各种快递公司示例
            Text("不同快递公司示例", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            
            ParcelItem(
                parcel = Parcel(
                    parcelCode = "SF123456",
                    address = "顺丰快递柜",
                    courierCompany = "顺丰",
                    smsContent = "",
                    smsDate = System.currentTimeMillis(),
                    isCollected = false
                ),
                onClick = {},
                onMarkAsCollected = {},
                onDelete = {}
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ParcelItem(
                parcel = Parcel(
                    parcelCode = "JD789012",
                    address = "京东快递站",
                    courierCompany = "京东",
                    smsContent = "",
                    smsDate = System.currentTimeMillis(),
                    isCollected = false
                ),
                onClick = {},
                onMarkAsCollected = {},
                onDelete = {}
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            ParcelItem(
                parcel = Parcel(
                    parcelCode = "YT345678",
                    address = "圆通快递点",
                    courierCompany = "圆通",
                    smsContent = "",
                    smsDate = System.currentTimeMillis(),
                    isCollected = true
                ),
                onClick = {},
                onMarkAsCollected = {},
                onDelete = {},
                isCollected = true
            )
        }
    }
}

/**
 * 深色主题预览集合
 */
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun DarkThemeComponentsPreview() {
    MashangqujianTheme(darkTheme = true) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("深色主题预览", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PermissionRequestCard(onRequestPermission = {})
            
            Spacer(modifier = Modifier.height(16.dp))
            
            ParcelItem(
                parcel = Parcel(
                    parcelCode = "CN654321",
                    address = "菜鸟驿站",
                    courierCompany = "菜鸟驿站",
                    smsContent = "",
                    smsDate = System.currentTimeMillis(),
                    isCollected = false
                ),
                onClick = {},
                onMarkAsCollected = {},
                onDelete = {}
            )
        }
    }
}

/**
 * 混合状态预览
 */
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun MixedStatesPreview() {
    MashangqujianTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("不同状态预览", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 今天收到的
            ParcelItem(
                parcel = Parcel(
                    parcelCode = "111111",
                    address = "今天收到的快递",
                    courierCompany = "顺丰",
                    smsContent = "",
                    smsDate = System.currentTimeMillis(),
                    isCollected = false
                ),
                onClick = {},
                onMarkAsCollected = {},
                onDelete = {}
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 昨天收到的
            ParcelItem(
                parcel = Parcel(
                    parcelCode = "222222",
                    address = "昨天收到的快递",
                    courierCompany = "京东",
                    smsContent = "",
                    smsDate = System.currentTimeMillis() - 24 * 60 * 60 * 1000,
                    isCollected = false
                ),
                onClick = {},
                onMarkAsCollected = {},
                onDelete = {}
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 3天前收到已取件
            ParcelItem(
                parcel = Parcel(
                    parcelCode = "333333",
                    address = "3天前已取件的快递",
                    courierCompany = "中通",
                    smsContent = "",
                    smsDate = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000,
                    isCollected = true
                ),
                onClick = {},
                onMarkAsCollected = {},
                onDelete = {},
                isCollected = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 7天前收到已取件
            ParcelItem(
                parcel = Parcel(
                    parcelCode = "444444",
                    address = "7天前已取件的快递",
                    courierCompany = "韵达",
                    smsContent = "",
                    smsDate = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000,
                    isCollected = true
                ),
                onClick = {},
                onMarkAsCollected = {},
                onDelete = {},
                isCollected = true
            )
        }
    }
}