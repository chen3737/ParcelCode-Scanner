package com.mashangqujian.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mashangqujian.data.database.AppDatabase
import com.mashangqujian.data.model.Parcel
import com.mashangqujian.sms.SMSParser
import com.mashangqujian.sms.SMSReader
import kotlinx.coroutines.launch
import java.util.Date

class MainViewModel : ViewModel() {
    
    // 状态
    val parcels = mutableStateListOf<Parcel>()
    val uncollectedCount = mutableStateOf(0)
    val isLoading = mutableStateOf(false)
    val hasSMSPermission = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val selectedParcel = mutableStateOf<Parcel?>(null)
    
    // 依赖
    private lateinit var database: AppDatabase
    private lateinit var smsReader: SMSReader
    private lateinit var smsParser: SMSParser
    
    // 权限请求
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    
    init {
        initializeComponents()
    }
    
    private fun initializeComponents() {
        smsParser = SMSParser()
    }
    
    fun initializeDatabase(context: Context) {
        database = AppDatabase.getInstance(context)
    }
    
    fun initializeSMSReader(context: Context) {
        smsReader = SMSReader(context.contentResolver)
    }
    
    fun loadParcels() {
        viewModelScope.launch {
            try {
                isLoading.value = true
                errorMessage.value = null
                
                val allParcels = database.parcelDao().getAllParcels()
                parcels.clear()
                parcels.addAll(allParcels)
                
                // 更新未取件计数
                uncollectedCount.value = parcels.count { !it.isCollected }
            } catch (e: Exception) {
                errorMessage.value = "加载数据失败: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
    
    fun checkPermissions(context: Activity) {
        val permission = Manifest.permission.READ_SMS
        val hasPermission = ContextCompat.checkSelfPermission(context, permission) == 
                PackageManager.PERMISSION_GRANTED
        
        hasSMSPermission.value = hasPermission
    }
    
    fun requestSMSPermission(context: Activity) {
        requestPermissionLauncher.launch(Manifest.permission.READ_SMS)
    }
    
    fun onPermissionResult(requestCode: Int, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            hasSMSPermission.value = true
            // 权限获取成功后扫描短信
            scanSMS()
        } else {
            errorMessage.value = "短信读取权限被拒绝，部分功能将无法使用"
        }
    }
    
    fun scanSMS() {
        viewModelScope.launch {
            try {
                isLoading.value = true
                errorMessage.value = null
                
                // 读取最近30天的短信
                val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                val smsItems = smsReader.readSMS(startDate = thirtyDaysAgo)
                
                // 解析为取件记录
                val newParcels = smsReader.parseToParcels(smsItems, smsParser)
                
                // 保存到数据库
                database.parcelDao().insertAll(newParcels)
                
                // 重新加载数据
                loadParcels()
                
                // 显示结果
                if (newParcels.isNotEmpty()) {
                    errorMessage.value = "扫描完成，发现${newParcels.size}个取件码"
                } else {
                    errorMessage.value = "扫描完成，未发现取件码"
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
                    val parcel = smsParser.parseSMS(
                        latestSMS.content,
                        latestSMS.sender,
                        latestSMS.date
                    )
                    
                    if (parcel != null) {
                        database.parcelDao().insert(parcel)
                        loadParcels()
                        errorMessage.value = "成功添加取件码: ${parcel.parcelCode}"
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
                
                val parcel = smsParser.parseSMS(text, "手动输入", System.currentTimeMillis())
                
                if (parcel != null) {
                    database.parcelDao().insert(parcel)
                    loadParcels()
                    errorMessage.value = "成功添加取件码: ${parcel.parcelCode}"
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
                database.parcelDao().updateCollectionStatus(parcel.id, true)
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
                database.parcelDao().delete(parcel)
                loadParcels()
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
}