package com.mashangqujian.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import android.util.Log
import com.mashangqujian.data.database.AppDatabase
import com.mashangqujian.data.repository.RuleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * 短信广播接收器 — 收到新短信时自动解析取件码
 */
class SMSReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val smsBody = extractSmsBody(intent) ?: return
        val sender = extractSender(intent) ?: ""
        val timestamp = System.currentTimeMillis()

        Log.d("SMSReceiver", "收到新短信: $sender - ${smsBody.take(30)}...")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val ruleRepo = RuleRepository(context)
                val parser = SMSParser()
                parser.updateRules(ruleRepo.getAllRules().first())

                val result = parser.parseSMS(smsBody, sender, timestamp)

                if (result.parcel != null) {
                    val existingCodes = db.parcelDao().getAllParcelCodes()
                    if (result.parcel.parcelCode !in existingCodes) {
                        db.parcelDao().insert(result.parcel)
                        Log.d("SMSReceiver", "自动识别并添加: ${result.parcel.courierCompany} - ${result.parcel.parcelCode}")
                    } else {
                        Log.d("SMSReceiver", "取件码已存在，跳过: ${result.parcel.parcelCode}")
                    }
                } else {
                    Log.d("SMSReceiver", "未识别到取件码")
                }
            } catch (e: Exception) {
                Log.e("SMSReceiver", "自动解析短信失败: ${e.message}")
            }
        }
    }

    private fun extractSmsBody(intent: Intent): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            messages?.joinToString("") { it.messageBody }
        } else {
            null
        }
    }

    private fun extractSender(intent: Intent): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            messages?.firstOrNull()?.displayOriginatingAddress
        } else {
            null
        }
    }
}
