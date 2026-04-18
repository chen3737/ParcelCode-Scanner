package com.mashangqujian.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.SharedPreferences
import com.mashangqujian.data.database.AppDatabase
import com.mashangqujian.data.model.DeletedParcelHistory
import com.mashangqujian.data.model.Parcel
import com.mashangqujian.data.model.ParsingRule
import com.mashangqujian.data.repository.RuleRepository
import com.mashangqujian.sms.SMSParser
import com.mashangqujian.sms.SMSReader
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    // 状态
    val parcels = mutableStateListOf<Parcel>()
    val uncollectedCount = mutableStateOf(0)
    val isLoading = mutableStateOf(false)
    val hasSMSPermission = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val selectedParcel = mutableStateOf<Parcel?>(null)

    // 手动输入相关状态
    val showAddManuallyDialog = mutableStateOf(false)
    val manualSMSText = mutableStateOf("")

    // 规则管理相关状态
    val showRuleManagement = mutableStateOf(false)
    val allRules = mutableStateListOf<ParsingRule>()

    // 粘贴板检测相关状态
    val showClipboardDialog = mutableStateOf(false)
    val clipboardContent = mutableStateOf("")
    val clipboardParsedParcel = mutableStateOf<Parcel?>(null)

    // 小米通知类短信权限引导
    val showXiaomiSMSPermissionGuide = mutableStateOf(false)

    // 删除历史相关状态
    val deletedHistory = mutableStateListOf<DeletedParcelHistory>()
    val showDeleteHistoryScreen = mutableStateOf(false)

    // 依赖
    private lateinit var database: AppDatabase
    private lateinit var smsReader: SMSReader
    private lateinit var smsParser: SMSParser
    private lateinit var ruleRepository: RuleRepository
    private var appContext: Context? = null

    // 权限回调引用（由Activity提供）
    private var permissionCallback: (() -> Unit)? = null

    /**
     * 检测是否为小米/Redmi设备
     */
    fun isXiaomiDevice(): Boolean {
        return try {
            val brand = android.os.Build.BRAND.lowercase()
            brand == "xiaomi" || brand == "redmi" || brand == "mi"
        } catch (e: Exception) {
            false
        }
    }
    
    init {
        initializeComponents()
    }
    
    private fun initializeComponents() {
        smsParser = SMSParser()
        // 数据库和SMSReader将在Activity中初始化
    }
    
    fun initializeDatabase(context: Context) {
        database = AppDatabase.getInstance(context)
        ruleRepository = RuleRepository(context)
        appContext = context.applicationContext
    }

    /**
     * 获取扫描天数（从SharedPreferences，默认7天）
     */
    private fun getScanDays(): Long {
        return try {
            val prefs = appContext?.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            prefs?.getInt("scan_days", 7)?.toLong() ?: 7L
        } catch (e: Exception) {
            7L
        }
    }
    
    fun initializeSMSReader(context: Context) {
        smsReader = SMSReader(context.contentResolver)
        // 初始化预设规则（如果数据库为空则自动添加）
        initDefaultRules()
    }

    private fun initDefaultRules() {
        viewModelScope.launch {
            try {
                ruleRepository.initializeDefaultRules()
            } catch (e: Exception) {
                // 忽略错误
            }
        }
    }
    
    /**
     * 初始化所有组件
     */
    fun initializeAllComponents(context: Context) {
        initializeDatabase(context)
        initializeSMSReader(context)
        loadAllRules()
    }
    
    /**
     * 检查组件是否已初始化
     */
    fun areComponentsInitialized(): Boolean {
        return ::database.isInitialized && ::smsReader.isInitialized && ::smsParser.isInitialized && ::ruleRepository.isInitialized
    }
    
    fun loadParcels() {
        viewModelScope.launch {
            try {
                isLoading.value = true
                errorMessage.value = null

                // 先获取一次初始数据，设置加载状态为false
                database.parcelDao().getAllParcels().collect { allParcels ->
                    parcels.clear()
                    parcels.addAll(allParcels)
                    uncollectedCount.value = parcels.count { !it.isCollected }
                    isLoading.value = false // 收到第一次数据后关闭loading
                }
            } catch (e: Exception) {
                errorMessage.value = "加载数据失败: ${e.message}"
                isLoading.value = false
            }
        }
    }
    
    /**
     * 加载所有规则
     */
    fun loadAllRules() {
        viewModelScope.launch {
            try {
                isLoading.value = true
                ruleRepository.getAllRules().collect { rules ->
                    allRules.clear()
                    allRules.addAll(rules.sortedBy { it.companyName })
                    smsParser.updateRules(rules)
                    isLoading.value = false
                }
            } catch (e: Exception) {
                errorMessage.value = "加载规则失败: ${e.message}"
                isLoading.value = false
            }
        }
    }
    
    /**
     * 添加规则
     */
    fun addRule(rule: ParsingRule) {
        viewModelScope.launch {
            try {
                ruleRepository.insert(rule)
                loadAllRules()
            } catch (e: Exception) {
                errorMessage.value = "添加规则失败: ${e.message}"
            }
        }
    }
    
    /**
     * 更新规则
     */
    fun updateRule(rule: ParsingRule) {
        viewModelScope.launch {
            try {
                ruleRepository.updateRule(rule)
                loadAllRules()
            } catch (e: Exception) {
                errorMessage.value = "更新规则失败: ${e.message}"
            }
        }
    }
    
    /**
     * 删除规则
     */
    fun deleteRule(rule: ParsingRule) {
        viewModelScope.launch {
            try {
                ruleRepository.deleteRule(rule)
                loadAllRules()
            } catch (e: Exception) {
                errorMessage.value = "删除规则失败: ${e.message}"
            }
        }
    }
    
    /**
     * 启用/禁用规则
     */
    fun setRuleEnabled(ruleId: String, enabled: Boolean) {
        viewModelScope.launch {
            try {
                ruleRepository.setRuleEnabled(ruleId, enabled)
                loadAllRules()
            } catch (e: Exception) {
                errorMessage.value = "更新规则状态失败: ${e.message}"
            }
        }
    }
    
    fun checkPermissions(context: Activity) {
        val permission = Manifest.permission.READ_SMS
        val hasPermission = ContextCompat.checkSelfPermission(context, permission) == 
                PackageManager.PERMISSION_GRANTED
        
        hasSMSPermission.value = hasPermission
    }
    
    fun requestSMSPermission(activity: android.app.Activity) {
        // 调用Activity中的权限请求方法
        if (activity is com.mashangqujian.ui.MainActivity) {
            activity.requestSMSPermission()
        } else {
            errorMessage.value = "权限请求失败：无法访问MainActivity"
        }
    }
    
    fun onPermissionGranted() {
        hasSMSPermission.value = true
        errorMessage.value = "短信权限已授予，现在可以手动扫描短信或手动输入短信内容"
    }
    
    fun onPermissionDenied() {
        hasSMSPermission.value = false
        errorMessage.value = "短信读取权限被拒绝，部分功能将无法使用"
    }
    
    fun onPermissionResult(@Suppress("UNUSED_PARAMETER") requestCode: Int, grantResults: IntArray) {
        // 保持向后兼容性，处理旧的权限回调方式
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            onPermissionGranted()
            // 不再自动扫描短信，改为用户手动触发
        } else {
            onPermissionDenied()
        }
    }
    
    fun scanSMS() {
        viewModelScope.launch {
            try {
                isLoading.value = true
                errorMessage.value = null

                // 读取用户设置的扫描天数（默认7天）
                val scanDays = getScanDays()
                val startDate = System.currentTimeMillis() - (scanDays * 24L * 60 * 60 * 1000)
                val smsItems = smsReader.readSMS(startDate = startDate)

                // 解析为取件记录
                val parsedParcels = smsReader.parseToParcels(smsItems, smsParser)

                // 去重：过滤掉已存在的取件码
                val existingCodes = parcels.map { it.parcelCode }.toSet()
                val newParcels = parsedParcels.filter { it.parcelCode !in existingCodes }

                // 保存到数据库
                if (newParcels.isNotEmpty()) {
                    database.parcelDao().insertAll(newParcels)
                }

                // 重新加载数据
                loadParcels()

                // 显示结果
                if (newParcels.isNotEmpty()) {
                    val skippedCount = parsedParcels.size - newParcels.size
                    val extraMsg = if (skippedCount > 0) "（已跳过${skippedCount}条重复）" else ""
                    errorMessage.value = "扫描完成，新增${newParcels.size}个取件码$extraMsg"
                } else {
                    // 小米设备读取不到短信时，引导用户开启通知类短信权限
                    if (isXiaomiDevice() && smsItems.isEmpty()) {
                        showXiaomiSMSPermissionGuide.value = true
                    } else {
                        errorMessage.value = "扫描完成，未发现新取件码"
                    }
                }
            } catch (e: Exception) {
                errorMessage.value = "扫描失败: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
    
    fun scanLatestSMS() {
        viewModelScope.launch {
            try {
                isLoading.value = true
                errorMessage.value = null
                
                // 读取最新的一条短信
                val latestSMS = smsReader.readLatestSMS()

                if (latestSMS != null) {
                    val result = smsParser.parseSMS(
                        latestSMS.content,
                        latestSMS.sender,
                        latestSMS.date
                    )

                    if (result.parcel != null) {
                        database.parcelDao().insert(result.parcel)
                        loadParcels()
                        errorMessage.value = "成功添加取件码: ${result.parcel.parcelCode}"
                    } else {
                        errorMessage.value = "最新短信不包含取件码"
                    }
                } else {
                    errorMessage.value = "没有找到短信"
                }
            } catch (e: Exception) {
                errorMessage.value = "扫描失败: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
    
    fun parseAndAddText(text: String) {
        viewModelScope.launch {
            try {
                isLoading.value = true
                errorMessage.value = null

                val result = smsParser.parseSMS(text, "手动输入", System.currentTimeMillis())

                if (result.parcel != null) {
                    database.parcelDao().insert(result.parcel)
                    loadParcels()
                    errorMessage.value = "成功添加取件码: ${result.parcel.parcelCode}"
                } else {
                    errorMessage.value = "文本不包含有效的取件码"
                }
            } catch (e: Exception) {
                errorMessage.value = "添加失败: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
    
    fun markAsCollected(parcel: Parcel) {
        viewModelScope.launch {
            try {
                database.parcelDao().updateCollectionStatusWithTime(parcel.id, true, System.currentTimeMillis())
                loadParcels()
                errorMessage.value = "已标记为已取件"
            } catch (e: Exception) {
                errorMessage.value = "操作失败: ${e.message}"
            }
        }
    }

    fun markAsUncollected(parcel: Parcel) {
        viewModelScope.launch {
            try {
                database.parcelDao().updateCollectionStatus(parcel.id, false)
                loadParcels()
                errorMessage.value = "已标记为未取件"
            } catch (e: Exception) {
                errorMessage.value = "操作失败: ${e.message}"
            }
        }
    }
    
    fun deleteParcel(parcel: Parcel) {
        viewModelScope.launch {
            try {
                // 先保存到删除历史
                val history = DeletedParcelHistory(
                    parcelCode = parcel.parcelCode,
                    address = parcel.address,
                    courierCompany = parcel.courierCompany,
                    smsContent = parcel.smsContent,
                    smsDate = parcel.smsDate,
                    matchedRule = parcel.matchedRule,
                    deletedAt = System.currentTimeMillis()
                )
                database.deletedParcelHistoryDao().insert(history)

                // 再删除原记录
                database.parcelDao().delete(parcel)
                loadParcels()
                loadDeleteHistory()
                errorMessage.value = "已删除取件码"
            } catch (e: Exception) {
                errorMessage.value = "删除失败: ${e.message}"
            }
        }
    }
    
    fun updateNotes(parcel: Parcel, notes: String) {
        viewModelScope.launch {
            try {
                database.parcelDao().updateNotes(parcel.id, notes)
                loadParcels()
                errorMessage.value = "备注已更新"
            } catch (e: Exception) {
                errorMessage.value = "更新失败: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        errorMessage.value = null
    }
    
    fun selectParcel(parcel: Parcel?) {
        selectedParcel.value = parcel
    }
    
    fun getCompanies(): List<String> {
        return smsParser.getSupportedCompanies()
    }
    
    /**
     * 清理所有数据
     */
    fun clearAllData() {
        viewModelScope.launch {
            try {
                database.parcelDao().deleteAllParcels()
                loadParcels()
                errorMessage.value = "所有数据已清理"
            } catch (e: Exception) {
                errorMessage.value = "清理失败: ${e.message}"
            }
        }
    }
    
    /**
     * 打开手动输入对话框
     */
    fun openManualInputDialog() {
        manualSMSText.value = ""
        showAddManuallyDialog.value = true
    }
    
    /**
     * 关闭手动输入对话框
     */
    fun closeManualInputDialog() {
        showAddManuallyDialog.value = false
    }
    
    /**
     * 手动添加文本并处理结果
     */
    fun addManually() {
        val text = manualSMSText.value.trim()
        if (text.isEmpty()) {
            errorMessage.value = "请输入短信内容"
            return
        }

        parseAndAddText(text)
    }
    
    /**
     * 打开规则管理界面
     */
    fun openRuleManagement() {
        showRuleManagement.value = true
        loadAllRules()
    }
    
    /**
     * 关闭规则管理界面
     */
    fun closeRuleManagement() {
        showRuleManagement.value = false
    }

    /**
     * 检查粘贴板内容并尝试解析取件码
     */
    fun checkClipboard(context: android.content.Context) {
        try {
            val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                    as android.content.ClipboardManager
            val clip = clipboardManager.primaryClip
            if (clip == null || clip.itemCount == 0) return

            val text = clip.getItemAt(0).coerceToText(context).toString().trim()
            if (text.isEmpty()) return

            // 尝试解析粘贴板内容
            val result = smsParser.parseSMS(text, "粘贴板", System.currentTimeMillis())
            if (result.parcel != null) {
                clipboardContent.value = text
                clipboardParsedParcel.value = result.parcel
                showClipboardDialog.value = true
            }
        } catch (e: Exception) {
            // 粘贴板读取失败，静默处理
        }
    }

    /**
     * 确认添加粘贴板解析的取件码
     */
    fun confirmAddFromClipboard() {
        val parcel = clipboardParsedParcel.value ?: return
        viewModelScope.launch {
            try {
                database.parcelDao().insert(parcel)
                loadParcels()
                errorMessage.value = "成功添加取件码: ${parcel.parcelCode}"
            } catch (e: Exception) {
                errorMessage.value = "添加失败: ${e.message}"
            }
        }
        showClipboardDialog.value = false
    }

    /**
     * 取消粘贴板对话框
     */
    fun dismissClipboardDialog() {
        showClipboardDialog.value = false
    }

    /**
     * 取消小米通知类短信权限引导对话框
     */
    fun dismissXiaomiSMSPermissionGuide() {
        showXiaomiSMSPermissionGuide.value = false
    }

    // ==================== 删除历史 ====================

    /**
     * 加载30天内的删除历史
     */
    fun loadDeleteHistory() {
        viewModelScope.launch {
            try {
                val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                database.deletedParcelHistoryDao().getRecent(thirtyDaysAgo).collect { histories ->
                    deletedHistory.clear()
                    deletedHistory.addAll(histories)
                }
            } catch (e: Exception) {
                // 忽略错误
            }
        }
    }

    /**
     * 打开删除历史界面
     */
    fun openDeleteHistory() {
        showDeleteHistoryScreen.value = true
        loadDeleteHistory()
    }

    /**
     * 关闭删除历史界面
     */
    fun closeDeleteHistory() {
        showDeleteHistoryScreen.value = false
    }

    /**
     * 单条删除删除历史
     */
    fun deleteHistoryItem(history: DeletedParcelHistory) {
        viewModelScope.launch {
            try {
                database.deletedParcelHistoryDao().deleteById(history.id)
                loadDeleteHistory()
                errorMessage.value = "已清除历史记录"
            } catch (e: Exception) {
                errorMessage.value = "删除失败: ${e.message}"
            }
        }
    }

    /**
     * 一键清除所有删除历史
     */
    fun clearDeleteHistory() {
        viewModelScope.launch {
            try {
                database.deletedParcelHistoryDao().deleteAll()
                loadDeleteHistory()
                errorMessage.value = "删除历史已清空"
            } catch (e: Exception) {
                errorMessage.value = "清理失败: ${e.message}"
            }
        }
    }
}