package com.mashangqujian.sms

import com.mashangqujian.data.model.Parcel
import com.mashangqujian.data.model.ParsingRule

/**
 * 短信解析器 - 负责解析短信内容，提取取件码和地址信息
 */
class SMSParser {

    // 缓存启用的规则（由外部通过 updateRules 刷新）
    private var enabledRules: List<ParsingRule> = emptyList()

    companion object {
        const val UNIVERSAL_RULE_ID = "universal_rule"
    }

    /**
     * 更新缓存的规则（应在规则变化后调用）
     */
    fun updateRules(rules: List<ParsingRule>) {
        enabledRules = rules.filter { it.isEnabled }
    }

    data class ParseResult(
        val parcel: Parcel?,
        val matchedRule: String
    )

    fun parseSMS(smsContent: String, sender: String, smsDate: Long): ParseResult {
        val content = smsContent.trim()

        // 1. 优先匹配具体公司规则
        val companyRules = enabledRules.filter { it.id != UNIVERSAL_RULE_ID }
        for (rule in companyRules) {
            try {
                val pattern = Regex(rule.parcelCodePattern)
                if (pattern.containsMatchIn(content)) {
                    val parcelCode = extractFromPattern(content, rule.parcelCodePattern)
                    val address = extractAddress(content, rule)
                    return if (!parcelCode.isNullOrEmpty()) {
                        ParseResult(
                            parcel = Parcel(
                                parcelCode = parcelCode,
                                address = address.ifEmpty { "未知地址" },
                                courierCompany = rule.companyName,
                                smsContent = content,
                                smsDate = smsDate,
                                sender = sender,
                                matchedRule = buildRuleName(rule)
                            ),
                            matchedRule = buildRuleName(rule)
                        )
                    } else {
                        ParseResult(null, buildRuleName(rule))
                    }
                }
            } catch (e: Exception) {
                // 跳过无效正则
            }
        }

        // 2. 尝试通用规则（从【】提取公司名，从取件码关键词提取取件码）
        val universalRule = enabledRules.find { it.id == UNIVERSAL_RULE_ID }
        if (universalRule != null) {
            return parseUniversal(content, sender, smsDate, universalRule)
        }

        return ParseResult(null, "")
    }

    /**
     * 通用解析：从规则边界提取公司名，从规则正则提取取件码
     */
    private fun parseUniversal(content: String, sender: String, smsDate: Long, rule: ParsingRule): ParseResult {
        // 提取快递公司
        val company = extractCompanyName(content, rule)

        // 用通用规则的正则提取取件码
        val parcelCode = extractFromPattern(content, rule.parcelCodePattern)

        if (parcelCode.isNullOrEmpty()) {
            return ParseResult(null, "")
        }

        val address = extractAddress(content, rule)

        return ParseResult(
            parcel = Parcel(
                parcelCode = parcelCode,
                address = address.ifEmpty { "未知地址" },
                courierCompany = company,
                smsContent = content,
                smsDate = smsDate,
                sender = sender,
                matchedRule = buildRuleName(rule)
            ),
            matchedRule = buildRuleName(rule)
        )
    }

    /**
     * 提取快递公司名称
     */
    private fun extractCompanyName(content: String, rule: ParsingRule): String {
        val pattern = ParsingRule.generateBoundaryPattern(rule.companyPrefix, rule.companySuffix)
        if (pattern != null) {
            try {
                Regex(pattern).find(content)?.groups?.get(1)?.value?.let {
                    return it.trim()
                }
            } catch (e: Exception) {}
        }
        // 回退：从【】提取
        val companyMatch = Regex("【([^】]+)】").find(content)
        return companyMatch?.groupValues?.get(1)?.trim() ?: "未知快递"
    }

    /**
     * 从正则表达式中提取第一个捕获组
     */
    private fun extractFromPattern(content: String, pattern: String): String? {
        return try {
            val regex = Regex(pattern)
            regex.find(content)?.groups?.get(1)?.value?.trim()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 提取地址
     */
    private fun extractAddress(content: String, rule: ParsingRule): String {
        // 1. 优先尝试前缀/后缀模式
        ParsingRule.generateBoundaryPattern(rule.addressPrefix, rule.addressSuffix)?.let { addrPattern ->
            try {
                Regex(addrPattern).find(content)?.groups?.get(1)?.value?.let {
                    return it.trim()
                }
            } catch (e: Exception) {}
        }
        // 2. 回退到关键词模式
        rule.addressPattern?.let { addrPattern ->
            try {
                val pattern = Regex(addrPattern)
                pattern.find(content)?.groups?.get(1)?.value?.let {
                    return it.trim()
                }
            } catch (e: Exception) {
                // 跳过
            }
        }
        return ""
    }

    fun getSupportedCompanies(): List<String> {
        return enabledRules.map { it.companyName }.distinct().sorted()
    }

    private fun buildRuleName(rule: ParsingRule): String {
        val typeLabel = if (rule.isCustom) "自定义" else "预设"
        val codeDetail = if (!rule.codePrefix.isNullOrBlank() && !rule.codeSuffix.isNullOrBlank()) {
            "从[${rule.codePrefix}]到[${rule.codeSuffix}]"
        } else if (!rule.codePrefix.isNullOrBlank() && rule.codeSuffix.isNullOrBlank()) {
            "从[${rule.codePrefix}]到末尾"
        } else if (rule.codePrefix.isNullOrBlank() && !rule.codeSuffix.isNullOrBlank()) {
            "从开头到[${rule.codeSuffix}]"
        } else if (!rule.codeKeyword.isNullOrBlank()) {
            "关键词: ${rule.codeKeyword}"
        } else {
            ""
        }

        val companyDetail = if (!rule.companyPrefix.isNullOrBlank() && !rule.companySuffix.isNullOrBlank()) {
            "公司:从[${rule.companyPrefix}]到[${rule.companySuffix}]"
        } else if (rule.companyPrefix.isNullOrBlank() && !rule.companySuffix.isNullOrBlank()) {
            "公司:从开头到[${rule.companySuffix}]"
        } else if (!rule.companyPrefix.isNullOrBlank() && rule.companySuffix.isNullOrBlank()) {
            "公司:从[${rule.companyPrefix}]到末尾"
        } else {
            ""
        }

        val addressDetail = if (!rule.addressPrefix.isNullOrBlank() && !rule.addressSuffix.isNullOrBlank()) {
            "地址:从[${rule.addressPrefix}]到[${rule.addressSuffix}]"
        } else if (rule.addressPrefix.isNullOrBlank() && !rule.addressSuffix.isNullOrBlank()) {
            "地址:从开头到[${rule.addressSuffix}]"
        } else if (!rule.addressPrefix.isNullOrBlank() && rule.addressSuffix.isNullOrBlank()) {
            "地址:从[${rule.addressPrefix}]到末尾"
        } else if (!rule.addressKeyword.isNullOrBlank()) {
            "地址关键词: ${rule.addressKeyword}"
        } else {
            ""
        }

        val parts = listOfNotNull(typeLabel, rule.companyName, codeDetail, companyDetail, addressDetail)
        return parts.joinToString("·")
    }
}
