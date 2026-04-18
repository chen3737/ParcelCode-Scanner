package com.mashangqujian.data.repository

import android.content.Context
import com.mashangqujian.data.database.AppDatabase
import com.mashangqujian.data.model.CodeFormatType
import com.mashangqujian.data.model.ParsingRule
import kotlinx.coroutines.flow.Flow

/**
 * 规则仓库 - 管理解析规则的增删改查
 */
class RuleRepository(context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val ruleDao = database.ruleDao()

    /**
     * 获取所有规则
     */
    fun getAllRules(): Flow<List<ParsingRule>> = ruleDao.getAllRules()

    /**
     * 获取所有启用的规则
     */
    fun getEnabledRules(): Flow<List<ParsingRule>> = ruleDao.getEnabledRules()

    /**
     * 获取指定公司的规则
     */
    fun getRulesByCompany(companyName: String): Flow<List<ParsingRule>> =
        ruleDao.getRulesByCompany(companyName)

    /**
     * 获取所有自定义规则
     */
    fun getCustomRules(): Flow<List<ParsingRule>> = ruleDao.getCustomRules()

    /**
     * 根据ID获取规则
     */
    suspend fun getRuleById(id: String): ParsingRule? = ruleDao.getRuleById(id)

    /**
     * 插入规则
     */
    suspend fun insert(rule: ParsingRule) {
        ruleDao.insert(rule)
    }

    /**
     * 添加自定义规则（使用用户友好参数，自动生成正则）
     */
    suspend fun addCustomRule(
        companyName: String,
        codeKeyword: String,
        codeFormat: CodeFormatType,
        codeMinDigits: Int = 3,
        codeMaxDigits: Int = 8,
        addressKeyword: String? = null,
        description: String = "",
        smsExample: String = ""
    ): ParsingRule {
        val rule = ParsingRule(
            id = ParsingRule.generateId(companyName),
            companyName = companyName,
            codeKeyword = codeKeyword,
            codeFormat = codeFormat.name,
            codeMinDigits = codeMinDigits,
            codeMaxDigits = codeMaxDigits,
            addressKeyword = addressKeyword,
            smsExample = smsExample,
            parcelCodePattern = ParsingRule.generateParcelCodePattern(
                formatType = codeFormat,
                codeKeyword = codeKeyword,
                minDigits = codeMinDigits,
                maxDigits = codeMaxDigits
            ),
            addressPattern = ParsingRule.generateAddressPattern(addressKeyword),
            isCustom = true,
            isEnabled = true,
            description = description
        )
        ruleDao.insert(rule)
        return rule
    }

    /**
     * 更新规则
     */
    suspend fun updateRule(rule: ParsingRule) {
        ruleDao.update(rule.copy(updatedAt = System.currentTimeMillis()))
    }

    /**
     * 删除规则
     */
    suspend fun deleteRule(rule: ParsingRule) = ruleDao.delete(rule)

    /**
     * 根据ID删除规则
     */
    suspend fun deleteRuleById(id: String) = ruleDao.deleteById(id)

    /**
     * 删除所有自定义规则
     */
    suspend fun deleteAllCustomRules() = ruleDao.deleteAllCustomRules()

    /**
     * 启用/禁用规则
     */
    suspend fun setRuleEnabled(id: String, enabled: Boolean) {
        ruleDao.setRuleEnabled(id, enabled)
    }

    /**
     * 批量启用/禁用规则
     */
    suspend fun setRulesEnabled(ids: List<String>, enabled: Boolean) {
        ruleDao.setRulesEnabled(ids, enabled)
    }

    /**
     * 增加规则匹配次数
     */
    suspend fun incrementMatchCount(id: String) {
        ruleDao.incrementMatchCount(id)
    }

    /**
     * 获取规则数量
     */
    suspend fun getRuleCount(): Int = ruleDao.getRuleCount()

    /**
     * 获取启用的规则数量
     */
    suspend fun getEnabledRuleCount(): Int = ruleDao.getEnabledRuleCount()

    /**
     * 初始化预设规则
     */
    suspend fun initializeDefaultRules() {
        // 检查通用规则是否已存在
        val existing = getRuleById("universal_rule")
        if (existing != null) return

        // 预设：通用规则（使用【】识别公司，取件码关键词识别取件码）
        insert(
            ParsingRule(
                id = "universal_rule",
                companyName = "通用",
                codePrefix = "取件码",
                codeSuffix = "，",
                addressKeyword = "到达",
                isCustom = true,
                isEnabled = true,
                parcelCodePattern = "取件码(?:为|：|:|\\s)*([\\u4e00-\\u9fa5a-zA-Z0-9\\-]{2,15})[，。.!！\\s]",
                addressPattern = ParsingRule.generateAddressPattern("到达"),
                smsExample = "【兔喜生活】您有包裹已到达佳和园东门水果店店，取件码为6-5-2502，地址:清江南路5号小区东门水果店",
                description = "通用识别规则：从【】提取公司名，从取件码关键词提取取件码",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

}
