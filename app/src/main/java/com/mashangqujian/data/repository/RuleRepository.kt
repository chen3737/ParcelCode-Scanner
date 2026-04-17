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
        // 检查顺丰是否有规则（代表预设规则已初始化）
        val existingCount = ruleDao.getRuleCount()
        if (existingCount > 0) {
            return
        }

        val defaultRules = createDefaultRules()
        ruleDao.insertAll(defaultRules)
    }

    /**
     * 创建预设规则（每个快递公司仅一条）
     */
    private fun createDefaultRules(): List<ParsingRule> {
        return listOf(
            ParsingRule(
                id = "顺丰",
                companyName = "顺丰",
                codeKeyword = "取件码",
                codeFormat = CodeFormatType.DIGITS.name,
                codeMinDigits = 4,
                codeMaxDigits = 8,
                addressKeyword = "到达",
                isCustom = false,
                isEnabled = true,
                description = "顺丰快递取件码规则",
                smsExample = "【顺丰】您的包裹已到达XX小区丰巢快递柜，取件码：123456"
            ),
            ParsingRule(
                id = "京东",
                companyName = "京东",
                codeKeyword = "取件码",
                codeFormat = CodeFormatType.DIGITS.name,
                codeMinDigits = 4,
                codeMaxDigits = 6,
                addressKeyword = "到达",
                isCustom = false,
                isEnabled = true,
                description = "京东快递取件码规则",
                smsExample = "【京东】您的包裹已到达XX驿站，取件码：123456"
            ),
            ParsingRule(
                id = "中通",
                companyName = "中通",
                codeKeyword = "取件码",
                codeFormat = CodeFormatType.DIGITS.name,
                codeMinDigits = 3,
                codeMaxDigits = 6,
                addressKeyword = "到达",
                isCustom = false,
                isEnabled = true,
                description = "中通快递取件码规则",
                smsExample = "【中通】您的包裹已到达XX快递点，取件码：123456"
            ),
            ParsingRule(
                id = "圆通",
                companyName = "圆通",
                codeKeyword = "取件码",
                codeFormat = CodeFormatType.DIGITS.name,
                codeMinDigits = 3,
                codeMaxDigits = 6,
                addressKeyword = "到达",
                isCustom = false,
                isEnabled = true,
                description = "圆通快递取件码规则",
                smsExample = "【圆通】您的包裹已到达XX快递点，取件码：123456"
            ),
            ParsingRule(
                id = "韵达",
                companyName = "韵达",
                codeKeyword = "取件码",
                codeFormat = CodeFormatType.DIGITS.name,
                codeMinDigits = 3,
                codeMaxDigits = 6,
                addressKeyword = "到达",
                isCustom = false,
                isEnabled = true,
                description = "韵达快递取件码规则",
                smsExample = "【韵达】您的包裹已到达XX快递点，取件码：123456"
            ),
            ParsingRule(
                id = "菜鸟驿站",
                companyName = "菜鸟驿站",
                codeKeyword = "取件码",
                codeFormat = CodeFormatType.DIGITS.name,
                codeMinDigits = 3,
                codeMaxDigits = 7,
                addressKeyword = "到达",
                isCustom = false,
                isEnabled = true,
                description = "菜鸟驿站取件码规则",
                smsExample = "【菜鸟驿站】您的包裹已到达XX驿站，取件码：123-4567"
            ),
            ParsingRule(
                id = "邮政",
                companyName = "邮政",
                codeKeyword = "取件码",
                codeFormat = CodeFormatType.DIGITS.name,
                codeMinDigits = 6,
                codeMaxDigits = 12,
                addressKeyword = "到达",
                isCustom = false,
                isEnabled = true,
                description = "邮政快递取件码规则",
                smsExample = "【邮政】您的包裹已到达XX邮局，取件码：12345678"
            ),
            ParsingRule(
                id = "EMS",
                companyName = "EMS",
                codeKeyword = "取件码",
                codeFormat = CodeFormatType.DIGITS.name,
                codeMinDigits = 4,
                codeMaxDigits = 8,
                addressKeyword = "到达",
                isCustom = false,
                isEnabled = true,
                description = "EMS快递取件码规则",
                smsExample = "【EMS】您的包裹已到达XX网点，取件码：123456"
            )
        ).map { rule ->
            rule.copy(
                parcelCodePattern = ParsingRule.generateParcelCodePattern(
                    formatType = rule.formatType,
                    codeKeyword = rule.codeKeyword,
                    minDigits = rule.codeMinDigits,
                    maxDigits = rule.codeMaxDigits
                ),
                addressPattern = ParsingRule.generateAddressPattern(rule.addressKeyword)
            )
        }
    }

    /**
     * 复制预设规则为自定义规则
     */
    suspend fun copyRuleAsCustom(rule: ParsingRule): ParsingRule {
        val customRule = rule.copy(
            id = ParsingRule.generateId(rule.companyName),
            isCustom = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        ruleDao.insert(customRule)
        return customRule
    }
}
