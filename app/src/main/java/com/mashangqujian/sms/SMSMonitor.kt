package com.mashangqujian.sms

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.util.Log
import com.mashangqujian.data.database.AppDatabase
import com.mashangqujian.data.repository.RuleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 短信数据库监听器 — 通过 ContentObserver 监听短信变化，兼容所有定制 ROM（含鸿蒙）
 */
class SMSMonitor(
    private val context: Context,
    private val onNewParcelAdded: () -> Unit = {}
) : ContentObserver(Handler(Looper.getMainLooper())) {

    private var lastSeenTimestamp: Long = 0
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        super.onChange(selfChange, uri)
        Log.d("SMSMonitor", "短信数据库变化: $uri")

        scope.launch {
            try {
                val db = AppDatabase.getInstance(context)
                val ruleRepo = RuleRepository(context)
                val parser = SMSParser()
                parser.updateRules(ruleRepo.getAllRules().first())

                // 读取最新的短信
                val contentResolver = context.contentResolver
                val projection = arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE, Telephony.Sms.TYPE)
                val cursor = contentResolver.query(
                    Telephony.Sms.CONTENT_URI,
                    projection,
                    null,
                    null,
                    "${Telephony.Sms.DATE} DESC LIMIT 1"
                )

                cursor?.use { c ->
                    if (c.moveToFirst()) {
                        val address = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)) ?: ""
                        val body = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY)) ?: ""
                        val date = c.getLong(c.getColumnIndexOrThrow(Telephony.Sms.DATE))
                        val type = c.getInt(c.getColumnIndexOrThrow(Telephony.Sms.TYPE))

                        // 处理收到的短信（TYPE_INBOX = 1）和通知类短信（TYPE_ALL = 0）
                        // 小米/鸿蒙等系统可能将通知类短信存储为不同类型
                        if ((type == Telephony.Sms.MESSAGE_TYPE_INBOX || type == Telephony.Sms.MESSAGE_TYPE_ALL) && date > lastSeenTimestamp) {
                            lastSeenTimestamp = date
                            Log.d("SMSMonitor", "新短信: $address - ${body.take(30)}...")

                            val result = parser.parseSMS(body, address, date)
                            if (result.parcel != null) {
                                val existingCodes = db.parcelDao().getAllParcelCodes()
                                if (result.parcel.parcelCode !in existingCodes) {
                                    db.parcelDao().insert(result.parcel)
                                    Log.d("SMSMonitor", "自动识别: ${result.parcel.courierCompany} - ${result.parcel.parcelCode}")

                                    // 通知 UI 刷新
                                    Handler(Looper.getMainLooper()).post { onNewParcelAdded() }
                                } else {
                                    Log.d("SMSMonitor", "取件码已存在，跳过: ${result.parcel.parcelCode}")
                                }
                            }
                        }
                    }
                }
                cursor?.close()
            } catch (e: Exception) {
                Log.e("SMSMonitor", "解析新短信失败: ${e.message}")
            }
        }
    }
}
