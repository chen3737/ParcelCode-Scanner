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
     * 初始化预设规则（已取消，不再自动添加预设规则）
     */
    suspend fun initializeDefaultRules() {
        // 不再自动添加预设规则，用户需手动添加自定义规则
    }

}
