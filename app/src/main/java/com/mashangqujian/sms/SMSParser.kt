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
    
    // 通用取件码规则（作为后备）
    // 支持格式：3-5-1234 (数字-数字-4位) / A3-1234 (字母数字-4位) / AB1234 / 纯数字等
    private val generalParcelCodePatterns = listOf(
        // 格式1: 数字-数字-4位数字 (如 3-5-1234, 12-34-5678)
        Regex("""取件码.*?(\d{1,2}-\d{1,2}-\d{4})"""),
        Regex("""(\d{1,2}-\d{1,2}-\d{4})"""),
        // 格式2: 1-2位字母数字 + 连字符 + 4位数字 (如 A3-1234, AB-1234)
        Regex("""取件码.*?([A-Za-z0-9]{1,2}-\d{4})"""),
        Regex("""([A-Za-z]{1,2}\d{0,2}-\d{4})"""),
        // 格式3: 1-2位字母 + 4位数字 (如 A1234, AB1234)
        Regex("""([A-Za-z]{1,2}\d{4})"""),
        // 格式4: 传统取件码格式
        Regex("""取件码.*?(\d{3,8})"""),
        Regex("""验证码.*?(\d{4,6})"""),
        Regex("""提取码.*?(\d{4,6})"""),
        Regex("""密码.*?(\d{4,6})"""),
        Regex("""(\d{3}-\d{4})"""),
        Regex("""(\d{4}-\d{3})"""),
        Regex("""(\d{6})"""),
        Regex("""(\d{5})"""),
        Regex("""(\d{4})""")
    )
    
    // 通用地址提取规则
    private val generalAddressPatterns = listOf(
        Regex("""到.*?((?:[\u4e00-\u9fa5]|\d)+.*?(?:驿站|快递|网点|小区|大厦|广场))"""),
        Regex("""请到.*?((?:[\u4e00-\u9fa5]|\d)+.*?(?:驿站|快递|网点|小区|大厦|广场))"""),
        Regex("""地址.*?((?:[\u4e00-\u9fa5]|\d)+.*?(?:驿站|快递|网点|小区|大厦|广场))"""),
        Regex("""在.*?((?:[\u4e00-\u9fa5]|\d)+.*?(?:驿站|快递|网点|小区|大厦|广场))"""),
        Regex("""于.*?((?:[\u4e00-\u9fa5]|\d)+.*?(?:驿站|快递|网点|小区|大厦|广场))""")
    )
    
    /**
     * 解析短信内容，提取取件信息
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
                courierCompany = company.ifEmpty { "未知快递" },
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
     * @return Pair(companyName, matchedRuleName)
     */
    private fun detectCourierCompany(content: String, rules: List<ParsingRule>): Pair<String, String> {
        // 先尝试从持久化规则中匹配
        for (rule in rules) {
            if (rule.isEnabled) {
                try {
                    val pattern = Regex(rule.parcelCodePattern)
                    if (pattern.containsMatchIn(content)) {
                        // 更新匹配次数
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

        return "" to "通用规则"
    }
    
    /**
     * 提取取件码
     */
    private fun extractParcelCode(content: String, company: String, rules: List<ParsingRule>): String {
        // 先尝试特定公司的规则（自定义规则优先）
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
        
        // 然后尝试预设规则
        val presetRules = companyRules.filter { !it.isCustom }
        for (rule in presetRules) {
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
        
        // 如果没找到，尝试通用规则
        for (pattern in generalParcelCodePatterns) {
            val match = pattern.find(content)
            match?.groups?.get(1)?.value?.let { return it }
        }
        
        return ""
    }
    
    /**
     * 提取取件地址
     */
    private fun extractAddress(content: String, company: String, rules: List<ParsingRule>): String {
        // 先尝试特定公司的地址规则
        val companyRules = rules.filter { it.companyName == company && it.isEnabled }
        
        for (rule in companyRules) {
            rule.addressPattern?.let { addressPattern ->
                try {
                    val pattern = Regex(addressPattern)
                    val match = pattern.find(content)
                    match?.groups?.get(1)?.value?.let { 
                        runBlocking {
                            ruleRepository?.incrementMatchCount(rule.id)
                        }
                        return it.trim() 
                    }
                } catch (e: Exception) {
                    // 跳过无效正则
                }
            }
        }
        
        // 尝试通用地址规则
        for (pattern in generalAddressPatterns) {
            val match = pattern.find(content)
            match?.groups?.get(1)?.value?.let { return it.trim() }
        }
        
        // 如果没找到，尝试从短信中提取可能的地名
        val locationKeywords = listOf(
            "驿站", "快递", "网点", "超市", "便利店", "小区", "大厦", "广场",
            "门口", "前台", "收发室", "保安室", "物业", "菜鸟", "丰巢"
        )
        
        for (keyword in locationKeywords) {
            val index = content.indexOf(keyword)
            if (index != -1) {
                val start = maxOf(0, index - 30)
                val end = minOf(content.length, index + keyword.length + 20)
                val address = content.substring(start, end).trim()
                if (address.length >= 5) {
                    return address
                }
            }
        }
        
        return ""
    }
    
    /**
     * 获取所有支持的快递公司列表
     */
    fun getSupportedCompanies(): List<String> {
        if (ruleRepository == null) {
            return listOf("顺丰", "京东", "中通", "圆通", "韵达", "菜鸟驿站", "邮政")
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
                listOf("顺丰", "京东", "中通", "圆通", "韵达", "菜鸟驿站", "邮政")
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
