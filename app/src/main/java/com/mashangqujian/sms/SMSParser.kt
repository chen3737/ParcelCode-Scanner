package com.mashangqujian.sms

import com.mashangqujian.data.model.Parcel
import com.mashangqujian.data.model.ParsingRule

/**
 * 短信解析器 - 负责解析短信内容，提取取件码和地址信息
 */
class SMSParser {

    // 缓存启用的规则（由外部通过 updateRules 刷新）
    private var enabledRules: List<ParsingRule> = emptyList()

    /**
     * 更新缓存的规则（应在规则变化后调用）
     */
    fun updateRules(rules: List<ParsingRule>) {
        enabledRules = rules.filter { it.isEnabled }
    }

    /**
     * 解析短信内容，提取取件信息
     * 只匹配缓存中已启用的规则，无通用回退
     */
    data class ParseResult(
        val parcel: Parcel?,
        val matchedRule: String
    )

    fun parseSMS(smsContent: String, sender: String, smsDate: Long): ParseResult {
        val normalizedContent = smsContent.trim()

        // 1. 识别快递公司，同时记录匹配规则
        val (company, matchedRule) = detectCourierCompany(normalizedContent)

        // 没有规则匹配，直接返回 null
        if (company.isEmpty()) {
            return ParseResult(null, "")
        }

        // 2. 提取取件码
        val parcelCode = extractParcelCode(normalizedContent, company)

        // 3. 提取地址
        val address = extractAddress(normalizedContent, company)

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
     * 检测快递公司
     * @return Pair(companyName, matchedRuleName)，无匹配时返回空
     */
    private fun detectCourierCompany(content: String): Pair<String, String> {
        for (rule in enabledRules) {
            try {
                val pattern = Regex(rule.parcelCodePattern)
                if (pattern.containsMatchIn(content)) {
                    val ruleName = buildRuleName(rule)
                    return rule.companyName to ruleName
                }
            } catch (e: Exception) {
                // 正则表达式可能有误，跳过此规则
            }
        }

        return "" to ""
    }

    /**
     * 提取取件码
     */
    private fun extractParcelCode(content: String, company: String): String {
        val companyRules = enabledRules.filter { it.companyName == company && it.isEnabled }

        for (rule in companyRules) {
            try {
                val pattern = Regex(rule.parcelCodePattern)
                val match = pattern.find(content)
                match?.groups?.get(1)?.value?.let {
                    return it
                }
            } catch (e: Exception) {
                // 跳过无效正则
            }
        }

        return ""
    }

    /**
     * 提取地址
     */
    private fun extractAddress(content: String, company: String): String {
        val companyRules = enabledRules.filter { it.companyName == company && it.isEnabled }

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
        return enabledRules.map { it.companyName }.distinct().sorted()
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
