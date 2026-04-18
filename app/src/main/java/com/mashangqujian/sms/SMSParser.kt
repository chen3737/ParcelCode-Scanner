package com.mashangqujian.sms

import com.mashangqujian.data.model.Parcel
import com.mashangqujian.data.model.ParsingRule
import com.mashangqujian.data.repository.RuleRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * 短信解析器 - 负责解析短信内容，提取取件码和地址信息
 * 支持从持久化存储加载规则
 */
class SMSParser(private val ruleRepository: RuleRepository? = null) {

    /**
     * 解析短信内容，提取取件信息
     * 只匹配用户定义并开启的规则，无通用回退
     * @param smsContent 短信内容
     * @param sender 发件人号码
     * @param smsDate 短信接收时间
     * @return 解析结果，包含 Parcel 和匹配的规则名称
     */
    data class ParseResult(
        val parcel: Parcel?,
        val matchedRule: String
    )

    fun parseSMS(smsContent: String, sender: String, smsDate: Long): ParseResult {
        val normalizedContent = smsContent.trim()

        // 获取启用的规则
        val enabledRules = loadEnabledRules()

        // 1. 识别快递公司，同时记录匹配规则
        val (company, matchedRule) = detectCourierCompany(normalizedContent, enabledRules)

        // 没有规则匹配，直接返回 null
        if (company.isEmpty()) {
            return ParseResult(null, "")
        }

        // 2. 提取取件码
        val parcelCode = extractParcelCode(normalizedContent, company, enabledRules)

        // 3. 提取地址
        val address = extractAddress(normalizedContent, company, enabledRules)

        // 如果取件码为空，说明无法解析
        if (parcelCode.isEmpty()) {
            return ParseResult(null, matchedRule)
        }

        return ParseResult(
            parcel = Parcel(
                parcelCode = parcelCode,
                address = address.ifEmpty { "未知地址" },
                courierCompany = company,
                smsContent = normalizedContent,
                smsDate = smsDate,
                sender = sender,
                matchedRule = matchedRule
            ),
            matchedRule = matchedRule
        )
    }
    
    /**
     * 加载启用的规则
     */
    private fun loadEnabledRules(): List<ParsingRule> {
        if (ruleRepository == null) {
            return emptyList()
        }
        
        return runBlocking {
            try {
                ruleRepository.getEnabledRules().first()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
    
    /**
     * 检测快递公司
     * @return Pair(companyName, matchedRuleName)，无匹配时返回空
     */
    private fun detectCourierCompany(content: String, rules: List<ParsingRule>): Pair<String, String> {
        for (rule in rules) {
            if (rule.isEnabled) {
                try {
                    val pattern = Regex(rule.parcelCodePattern)
                    if (pattern.containsMatchIn(content)) {
                        runBlocking {
                            ruleRepository?.incrementMatchCount(rule.id)
                        }
                        val ruleName = buildRuleName(rule)
                        return rule.companyName to ruleName
                    }
                } catch (e: Exception) {
                    // 正则表达式可能有误，跳过此规则
                }
            }
        }

        return "" to ""
    }
    
    /**
     * 提取取件码 — 只使用用户规则
     */
    private fun extractParcelCode(content: String, company: String, rules: List<ParsingRule>): String {
        val companyRules = rules.filter { it.companyName == company && it.isEnabled }

        // 优先使用自定义规则
        val customRules = companyRules.filter { it.isCustom }
        for (rule in customRules) {
            try {
                val pattern = Regex(rule.parcelCodePattern)
                val match = pattern.find(content)
                match?.groups?.get(1)?.value?.let {
                    runBlocking {
                        ruleRepository?.incrementMatchCount(rule.id)
                    }
                    return it
                }
            } catch (e: Exception) {
                // 跳过无效正则
            }
        }

        return ""
    }
    
    /**
     * 提取取件地址 — 只使用规则中的地址关键词
     */
    private fun extractAddress(content: String, company: String, rules: List<ParsingRule>): String {
        val companyRules = rules.filter { it.companyName == company && it.isEnabled }

        for (rule in companyRules) {
            rule.addressPattern?.let { addressPattern ->
                try {
                    val pattern = Regex(addressPattern)
                    val match = pattern.find(content)
                    match?.groups?.get(1)?.value?.let {
                        return it.trim()
                    }
                } catch (e: Exception) {
                    // 跳过无效正则
                }
            }
        }

        return ""
    }
    
    /**
     * 获取所有已启用规则的快递公司名称列表
     */
    fun getSupportedCompanies(): List<String> {
        if (ruleRepository == null) {
            return emptyList()
        }

        return runBlocking {
            try {
                ruleRepository.getAllRules().first()
                    .filter { it.isEnabled }
                    .map { it.companyName }
                    .distinct()
                    .sorted()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
    
    /**
     * 添加自定义解析规则
     * @param company 快递公司名称
     * @param parcelCodePattern 取件码正则表达式
     * @param addressPattern 地址正则表达式
     */
    fun addCustomRule(company: String, parcelCodePattern: Regex, addressPattern: Regex? = null) {
        // 这个方法现在只是保持向后兼容
        // 实际应该通过RuleRepository添加规则
    }
    
    /**
     * 测试规则是否匹配文本
     * @param rule 要测试的规则
     * @param testText 测试文本
     * @return 匹配结果，包含匹配到的取件码和地址
     */
    data class RuleTestResult(
        val parcelCodeMatch: String?,
        val addressMatch: String?,
        val error: String?
    )
    
    fun testRule(rule: ParsingRule, testText: String): RuleTestResult {
        return try {
            val parcelCodePattern = Regex(rule.parcelCodePattern)
            val parcelCodeMatch = parcelCodePattern.find(testText)?.groups?.get(1)?.value
            
            val addressMatch = rule.addressPattern?.let { addressPattern ->
                try {
                    val pattern = Regex(addressPattern)
                    pattern.find(testText)?.groups?.get(1)?.value?.trim()
                } catch (e: Exception) {
                    null
                }
            }
            
            RuleTestResult(parcelCodeMatch, addressMatch, null)
        } catch (e: Exception) {
            RuleTestResult(null, null, "正则表达式错误: ${e.message}")
        }
    }

    private fun buildRuleName(rule: ParsingRule): String {
        val typeLabel = if (rule.isCustom) "自定义" else "预设"
        val detail = if (!rule.codePrefix.isNullOrBlank() && !rule.codeSuffix.isNullOrBlank()) {
            "从[${rule.codePrefix}]到[${rule.codeSuffix}]"
        } else if (!rule.codeKeyword.isNullOrBlank()) {
            "关键词: ${rule.codeKeyword}"
        } else {
            ""
        }
        return if (detail.isNotEmpty()) {
            "$typeLabel·${rule.companyName}·$detail"
        } else {
            "$typeLabel·${rule.companyName}"
        }
    }
}
