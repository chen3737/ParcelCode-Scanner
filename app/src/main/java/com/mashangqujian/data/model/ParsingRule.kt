package com.mashangqujian.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 取件码格式类型
 */
enum class CodeFormatType(val label: String) {
    /** 纯数字: 取件码 + N-M位数字 */
    DIGITS("纯数字"),
    /** 数字段: 如 3-5-1234 */
    DIGIT_SEGMENTS("数字段"),
    /** 字母+数字: 如 A3-1234 / AB1234 */
    LETTER_DIGITS("字母+数字")
}

/**
 * 取件码解析规则
 */
@Entity(tableName = "parsing_rules")
data class ParsingRule(
    @PrimaryKey
    val id: String,

    @ColumnInfo(name = "company_name")
    val companyName: String,

    // 用户友好字段 — 用于傻瓜式配置
    @ColumnInfo(name = "code_keyword")
    val codeKeyword: String? = null,

    @ColumnInfo(name = "code_format")
    val codeFormat: String = CodeFormatType.DIGITS.name,

    @ColumnInfo(name = "code_min_digits")
    val codeMinDigits: Int = 3,

    @ColumnInfo(name = "code_max_digits")
    val codeMaxDigits: Int = 8,

    @ColumnInfo(name = "code_seg1")
    val codeSeg1: Int = 1,

    @ColumnInfo(name = "code_seg2")
    val codeSeg2: Int = 2,

    @ColumnInfo(name = "code_seg3")
    val codeSeg3: Int = 4,

    @ColumnInfo(name = "letter_count")
    val letterCount: Int = 2,

    @ColumnInfo(name = "address_keyword")
    val addressKeyword: String? = null,

    @ColumnInfo(name = "sms_example")
    val smsExample: String = "",

    // 自动生成的正则表达式（SMSParser 直接使用这些字段）
    @ColumnInfo(name = "parcel_code_pattern")
    val parcelCodePattern: String = "",

    @ColumnInfo(name = "address_pattern")
    val addressPattern: String? = null,

    @ColumnInfo(name = "is_custom")
    val isCustom: Boolean = false,

    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = true,

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "match_count")
    val matchCount: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    val formatType: CodeFormatType
        get() = CodeFormatType.valueOf(codeFormat)

    companion object {
        /**
         * 生成ID（使用公司名+时间戳）
         */
        fun generateId(companyName: String): String {
            return "${companyName}_${System.currentTimeMillis()}"
        }

        /**
         * 根据用户选择自动生成取件码正则
         */
        fun generateParcelCodePattern(
            formatType: CodeFormatType,
            codeKeyword: String? = null,
            minDigits: Int = 3,
            maxDigits: Int = 8,
            seg1: Int = 1,
            seg2: Int = 2,
            seg3: Int = 4,
            letterCount: Int = 2
        ): String {
            val base = when (formatType) {
                CodeFormatType.DIGITS -> "(\\d{$minDigits,$maxDigits})"
                CodeFormatType.DIGIT_SEGMENTS -> "(\\d{1,$seg1}-\\d{1,$seg2}-\\d{$seg3})"
                CodeFormatType.LETTER_DIGITS -> "([A-Za-z]{1,$letterCount}\\d{0,2}-\\d{4})"
            }
            return if (codeKeyword.isNullOrBlank()) {
                base
            } else {
                "${Regex.escape(codeKeyword)}.*?$base"
            }
        }

        /**
         * 根据用户选择自动生成地址正则
         */
        fun generateAddressPattern(addressKeyword: String?): String? {
            if (addressKeyword.isNullOrBlank()) return null
            return "${Regex.escape(addressKeyword)}.*?((?:[\\u4e00-\\u9fa5]|\\d)+.*?(?:驿站|快递|网点|小区|大厦|广场|柜))"
        }

        /**
         * 从现有正则反向解析
         */
        data class ParsedCodePattern(
            val keyword: String?,
            val formatType: CodeFormatType?,
            val minDigits: Int?,
            val maxDigits: Int?
        )

        fun parseCodePattern(pattern: String?): ParsedCodePattern {
            if (pattern == null || pattern.isBlank()) return ParsedCodePattern(null, null, null, null)

            // 检测格式类型
            return when {
                pattern.contains("\\d{1,") && pattern.contains("-\\\\d{1,") && pattern.contains("-\\\\d{") -> {
                    ParsedCodePattern(extractKeyword(pattern), CodeFormatType.DIGIT_SEGMENTS, null, null)
                }
                pattern.contains("[A-Za-z]") -> {
                    ParsedCodePattern(extractKeyword(pattern), CodeFormatType.LETTER_DIGITS, null, null)
                }
                else -> {
                    val regex = Regex("""^([^\(\)\\.\*\[]+).*?\\d\{(\d+),?(\d*)\}""")
                    val match = regex.find(pattern)
                    if (match != null) {
                        val keyword = match.groupValues[1].trim().takeIf { it.isNotBlank() }
                        val min = match.groupValues[2].toIntOrNull()
                        val max = match.groupValues[3].toIntOrNull() ?: min
                        ParsedCodePattern(keyword, CodeFormatType.DIGITS, min, max)
                    } else {
                        ParsedCodePattern(null, null, null, null)
                    }
                }
            }
        }

        private fun extractKeyword(pattern: String): String? {
            val regex = Regex("""^([^\(\)\\.\*\[\]]+)""")
            val match = regex.find(pattern)
            return match?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }
        }

        fun parseAddressKeyword(pattern: String?): String? {
            if (pattern == null || pattern.isBlank()) return null
            val regex = Regex("""^([^\(\)\\.\*\[]+)""")
            val match = regex.find(pattern)
            return match?.groupValues?.get(1)?.trim()?.takeIf { it.isNotBlank() }
        }
    }
}
