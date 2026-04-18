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

        // 1. 优先使用用户定义的规则识别
        val (company, matchedRule) = detectCourierCompany(normalizedContent)

        // 有规则匹配，按规则解析
        if (company.isNotEmpty()) {
            return parseWithRule(normalizedContent, company, matchedRule, sender, smsDate)
        }

        // 2. 通用回退：用【】识别公司名，用取件码关键词提取
        return parseUniversal(normalizedContent, sender, smsDate)
    }

    /**
     * 使用用户规则解析
     */
    private fun parseWithRule(
        content: String,
        company: String,
        matchedRule: String,
        sender: String,
        smsDate: Long
    ): ParseResult {
        val parcelCode = extractParcelCode(content, company)
        if (parcelCode.isEmpty()) {
            return ParseResult(null, matchedRule)
        }
        val address = extractAddress(content, company)
        return ParseResult(
            parcel = Parcel(
                parcelCode = parcelCode,
                address = address.ifEmpty { "未知地址" },
                courierCompany = company,
                smsContent = content,
                smsDate = smsDate,
                sender = sender,
                matchedRule = matchedRule
            ),
            matchedRule = matchedRule
        )
    }

    /**
     * 通用解析：从【】提取公司名，从"取件码[为]...，"提取取件码
     */
    private fun parseUniversal(content: String, sender: String, smsDate: Long): ParseResult {
        // 从【XX】提取快递公司
        val companyMatch = Regex("【([^】]+)】").find(content)
        val company = companyMatch?.groupValues?.get(1)?.trim()
            ?: "未知快递"

        // 从"取件码[为]...，"或"取件码[为]...。"提取取件码
        val codePattern = Regex("取件码(?:为|：|:|\\s)*([\\u4e00-\\u9fa5a-zA-Z0-9\\-/]{2,15})[，。.!！\\s]")
        val codeMatch = codePattern.find(content)
        val parcelCode = codeMatch?.groupValues?.get(1)?.trim()

        if (parcelCode.isNullOrEmpty()) {
            return ParseResult(null, "")
        }

        // 尝试提取地址：从"地址"或"到达/菜鸟驿站/快递柜"等关键词
        val address = extractAddressUniversal(content)

        return ParseResult(
            parcel = Parcel(
                parcelCode = parcelCode,
                address = address.ifEmpty { "未知地址" },
                courierCompany = company,
                smsContent = content,
                smsDate = smsDate,
                sender = sender,
                matchedRule = "通用·$company·【】取件码"
            ),
            matchedRule = "通用·$company·【】取件码"
        )
    }

    /**
     * 通用地址提取
     */
    private fun extractAddressUniversal(content: String): String {
        // 尝试"地址：XXX"
        Regex("地址[：:]\\s*([^，,。.!！\n]+)").find(content)?.let {
            return it.groupValues[1].trim()
        }
        // 尝试"到达XXX"
        Regex("到达([^，,。.!！\n]{3,30})").find(content)?.let {
            return it.groupValues[1].trim()
        }
        return ""
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
