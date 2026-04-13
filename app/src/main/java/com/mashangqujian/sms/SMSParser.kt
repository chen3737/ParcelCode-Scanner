package com.mashangqujian.sms

import com.mashangqujian.data.model.Parcel

/**
 * 短信解析器 - 负责解析短信内容，提取取件码和地址信息
 */
class SMSParser {
    
    companion object {
        // 预定义的快递公司识别规则
        private val companyPatterns = mapOf(
            "顺丰" to listOf(
                Regex("""顺丰.*?取件码.*?(\d{4,8})"""),
                Regex("""SF.*?(\d{4,8})""")
            ),
            "京东" to listOf(
                Regex("""京东.*?取件码.*?(\d{4,6})"""),
                Regex("""JD.*?(\d{4,6})""")
            ),
            "中通" to listOf(
                Regex("""中通.*?取件码.*?(\d{3,6})"""),
                Regex("""ZTO.*?(\d{3,6})""")
            ),
            "圆通" to listOf(
                Regex("""圆通.*?取件码.*?(\d{3,6})"""),
                Regex("""YTO.*?(\d{3,6})""")
            ),
            "韵达" to listOf(
                Regex("""韵达.*?取件码.*?(\d{3,6})"""),
                Regex("""YD.*?(\d{3,6})""")
            ),
            "菜鸟驿站" to listOf(
                Regex("""菜鸟.*?取件码.*?(\d{3}-\d{4})"""),
                Regex("""取件码.*?(\d{3}-\d{4})""")
            ),
            "邮政" to listOf(
                Regex("""邮政.*?取件码.*?(\d{6,12})"""),
                Regex("""EMS.*?(\d{6,12})""")
            )
        )
        
        // 地址提取规则
        private val addressPatterns = listOf(
            Regex("""到.*?((?:[\u4e00-\u9fa5]|\d)+.*?(?:驿站|快递|网点|小区|大厦|广场))"""),
            Regex("""请到.*?((?:[\u4e00-\u9fa5]|\d)+.*?(?:驿站|快递|网点|小区|大厦|广场))"""),
            Regex("""地址.*?((?:[\u4e00-\u9fa5]|\d)+.*?(?:驿站|快递|网点|小区|大厦|广场))"""),
            Regex("""在.*?((?:[\u4e00-\u9fa5]|\d)+.*?(?:驿站|快递|网点|小区|大厦|广场))"""),
            Regex("""于.*?((?:[\u4e00-\u9fa5]|\d)+.*?(?:驿站|快递|网点|小区|大厦|广场))""")
        )
        
        // 通用取件码规则
        private val generalParcelCodePatterns = listOf(
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
    }
    
    /**
     * 解析短信内容，提取取件信息
     * @param smsContent 短信内容
     * @param sender 发件人号码
     * @param smsDate 短信接收时间
     * @return 解析出的取件记录，如果无法解析则返回null
     */
    fun parseSMS(smsContent: String, sender: String, smsDate: Long): Parcel? {
        val normalizedContent = smsContent.trim()
        
        // 1. 识别快递公司
        val company = detectCourierCompany(normalizedContent)
        
        // 2. 提取取件码
        val parcelCode = extractParcelCode(normalizedContent, company)
        
        // 3. 提取地址
        val address = extractAddress(normalizedContent, company)
        
        // 如果取件码为空，说明无法解析
        if (parcelCode.isEmpty()) {
            return null
        }
        
        return Parcel(
            parcelCode = parcelCode,
            address = address.ifEmpty { "未知地址" },
            courierCompany = company.ifEmpty { "未知快递" },
            smsContent = normalizedContent,
            smsDate = smsDate,
            sender = sender
        )
    }
    
    /**
     * 检测快递公司
     */
    private fun detectCourierCompany(content: String): String {
        for ((company, patterns) in companyPatterns) {
            for (pattern in patterns) {
                if (pattern.containsMatchIn(content)) {
                    return company
                }
            }
        }
        return ""
    }
    
    /**
     * 提取取件码
     */
    private fun extractParcelCode(content: String, company: String): String {
        // 先尝试特定公司的规则
        companyPatterns[company]?.forEach { pattern ->
            val match = pattern.find(content)
            match?.groups?.get(1)?.value?.let { return it }
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
    private fun extractAddress(content: String, company: String): String {
        for (pattern in addressPatterns) {
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
        return companyPatterns.keys.toList()
    }
    
    /**
     * 添加自定义解析规则
     * @param company 快递公司名称
     * @param parcelCodePattern 取件码正则表达式
     * @param addressPattern 地址正则表达式
     */
    fun addCustomRule(company: String, parcelCodePattern: Regex, addressPattern: Regex? = null) {
        val patterns = companyPatterns[company]?.toMutableList() ?: mutableListOf()
        patterns.add(parcelCodePattern)
        companyPatterns[company] = patterns
        
        addressPattern?.let {
            addressPatterns.add(it)
        }
    }
}