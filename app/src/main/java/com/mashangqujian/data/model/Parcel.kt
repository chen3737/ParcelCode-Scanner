package com.mashangqujian.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

/**
 * 取件记录实体类
 * @property id 主键ID
 * @property parcelCode 取件码
 * @property address 取件地址
 * @property courierCompany 快递公司
 * @property smsContent 原始短信内容
 * @property smsDate 短信接收日期
 * @property createdAt 记录创建时间
 * @property isCollected 是否已取件
 * @property notes 备注信息
 * @property sender 发件人号码
 */
@Entity(tableName = "parcels")
data class Parcel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    val parcelCode: String,
    val address: String,
    val courierCompany: String,
    val smsContent: String,
    val smsDate: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val isCollected: Boolean = false,
    val notes: String = "",
    val sender: String = ""
) {
    fun toDisplayText(): String {
        return "$courierCompany - $parcelCode\n$address"
    }
    
    fun getDaysSinceReceived(): Long {
        val now = System.currentTimeMillis()
        val diff = now - smsDate
        return diff / (1000 * 60 * 60 * 24)
    }
    
    companion object {
        fun fromSms(smsContent: String, sender: String, smsDate: Long): Parcel? {
            // 这里会调用短信解析器来提取信息
            // 暂时返回空，后续会实现解析逻辑
            return null
        }
    }
}