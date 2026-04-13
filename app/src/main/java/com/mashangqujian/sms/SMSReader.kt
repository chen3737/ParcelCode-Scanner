package com.mashangqujian.sms

import android.content.ContentResolver
import android.database.Cursor
import android.provider.Telephony
import androidx.core.database.getStringOrNull
import com.mashangqujian.data.model.Parcel

/**
 * 短信读取器 - 负责从系统读取短信
 */
class SMSReader(private val contentResolver: ContentResolver) {
    
    /**
     * 读取指定日期之后的短信
     * @param startDate 开始日期的时间戳（毫秒），如果为0则读取所有短信
     * @param limit 最多读取的短信数量，如果为0则不限制
     * @return 短信列表，每个元素包含内容、发件人和日期
     */
    fun readSMS(startDate: Long = 0, limit: Int = 0): List<SMSItem> {
        val smsList = mutableListOf<SMSItem>()
        
        val projection = arrayOf(
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.TYPE
        )
        
        val selection = if (startDate > 0) {
            "${Telephony.Sms.DATE} >= ?"
        } else {
            null
        }
        
        val selectionArgs = if (startDate > 0) {
            arrayOf(startDate.toString())
        } else {
            null
        }
        
        val sortOrder = "${Telephony.Sms.DATE} DESC"
        
        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )
            
            cursor?.use {
                var count = 0
                while (it.moveToNext() && (limit == 0 || count < limit)) {
                    val address = it.getStringOrNull(it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)) ?: ""
                    val body = it.getStringOrNull(it.getColumnIndexOrThrow(Telephony.Sms.BODY)) ?: ""
                    val date = it.getLong(it.getColumnIndexOrThrow(Telephony.Sms.DATE))
                    val type = it.getInt(it.getColumnIndexOrThrow(Telephony.Sms.TYPE))
                    
                    // 只处理收到的短信（TYPE = 1）
                    if (type == Telephony.Sms.MESSAGE_TYPE_INBOX) {
                        smsList.add(SMSItem(
                            sender = address,
                            content = body,
                            date = date
                        ))
                        count++
                    }
                }
            }
        } catch (e: SecurityException) {
            // 权限不足
            throw PermissionDeniedException("读取短信权限被拒绝")
        } catch (e: Exception) {
            // 其他异常
            throw SMSReadException("读取短信失败: ${e.message}")
        } finally {
            cursor?.close()
        }
        
        return smsList
    }
    
    /**
     * 读取最新的一条短信
     */
    fun readLatestSMS(): SMSItem? {
        return readSMS(limit = 1).firstOrNull()
    }
    
    /**
     * 批量解析短信为取件记录
     * @param smsItems 短信列表
     * @param parser 短信解析器
     * @return 解析出的取件记录列表
     */
    fun parseToParcels(smsItems: List<SMSItem>, parser: SMSParser): List<Parcel> {
        return smsItems.mapNotNull { sms ->
            parser.parseSMS(sms.content, sms.sender, sms.date)
        }
    }
    
    /**
     * 检查是否有短信读取权限
     */
    fun checkPermission(): Boolean {
        return try {
            // 尝试读取一条短信来检查权限
            val count = readSMS(limit = 1).size
            true
        } catch (e: PermissionDeniedException) {
            false
        } catch (e: Exception) {
            // 其他异常，可能是没有短信或其他问题
            true
        }
    }
    
    /**
     * 获取短信总数
     */
    fun getTotalSMSCount(): Int {
        return try {
            val cursor = contentResolver.query(
                Telephony.Sms.CONTENT_URI,
                null,
                "${Telephony.Sms.TYPE} = ?",
                arrayOf(Telephony.Sms.MESSAGE_TYPE_INBOX.toString()),
                null
            )
            
            val count = cursor?.count ?: 0
            cursor?.close()
            count
        } catch (e: Exception) {
            0
        }
    }
}

/**
 * 短信数据类
 */
data class SMSItem(
    val sender: String,
    val content: String,
    val date: Long
)

/**
 * 权限被拒绝异常
 */
class PermissionDeniedException(message: String) : Exception(message)

/**
 * 短信读取异常
 */
class SMSReadException(message: String) : Exception(message)