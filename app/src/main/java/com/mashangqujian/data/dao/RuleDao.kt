package com.mashangqujian.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mashangqujian.data.model.ParsingRule
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleDao {
    
    /**
     * 插入或更新规则
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: ParsingRule)
    
    /**
     * 批量插入或更新规则
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(rules: List<ParsingRule>)
    
    /**
     * 更新规则
     */
    @Update
    suspend fun update(rule: ParsingRule)
    
    /**
     * 删除规则
     */
    @Delete
    suspend fun delete(rule: ParsingRule)
    
    /**
     * 根据ID删除规则
     */
    @Query("DELETE FROM parsing_rules WHERE id = :id")
    suspend fun deleteById(id: String)
    
    /**
     * 删除所有自定义规则
     */
    @Query("DELETE FROM parsing_rules WHERE is_custom = 1")
    suspend fun deleteAllCustomRules()
    
    /**
     * 获取所有规则
     */
    @Query("SELECT * FROM parsing_rules ORDER BY company_name ASC, is_custom ASC, match_count DESC")
    fun getAllRules(): Flow<List<ParsingRule>>
    
    /**
     * 获取所有启用的规则
     */
    @Query("SELECT * FROM parsing_rules WHERE is_enabled = 1 ORDER BY company_name ASC, is_custom ASC, match_count DESC")
    fun getEnabledRules(): Flow<List<ParsingRule>>
    
    /**
     * 获取指定公司的规则
     */
    @Query("SELECT * FROM parsing_rules WHERE company_name = :companyName ORDER BY is_custom ASC, match_count DESC")
    fun getRulesByCompany(companyName: String): Flow<List<ParsingRule>>
    
    /**
     * 获取所有自定义规则
     */
    @Query("SELECT * FROM parsing_rules WHERE is_custom = 1 ORDER BY company_name ASC, updated_at DESC")
    fun getCustomRules(): Flow<List<ParsingRule>>
    
    /**
     * 根据ID获取规则
     */
    @Query("SELECT * FROM parsing_rules WHERE id = :id")
    suspend fun getRuleById(id: String): ParsingRule?
    
    /**
     * 更新规则匹配次数
     */
    @Query("UPDATE parsing_rules SET match_count = match_count + 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun incrementMatchCount(id: String, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * 启用/禁用规则
     */
    @Query("UPDATE parsing_rules SET is_enabled = :enabled, updated_at = :updatedAt WHERE id = :id")
    suspend fun setRuleEnabled(id: String, enabled: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * 批量启用/禁用规则
     */
    @Query("UPDATE parsing_rules SET is_enabled = :enabled, updated_at = :updatedAt WHERE id IN (:ids)")
    suspend fun setRulesEnabled(ids: List<String>, enabled: Boolean, updatedAt: Long = System.currentTimeMillis())
    
    /**
     * 获取规则数量
     */
    @Query("SELECT COUNT(*) FROM parsing_rules")
    suspend fun getRuleCount(): Int
    
    /**
     * 获取启用的规则数量
     */
    @Query("SELECT COUNT(*) FROM parsing_rules WHERE is_enabled = 1")
    suspend fun getEnabledRuleCount(): Int
}