package com.mashangqujian.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 已删除取件记录（保留在删除历史中）
 */
@Entity(tableName = "deleted_parcels")
data class DeletedParcelHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "parcel_code")
    val parcelCode: String,

    @ColumnInfo(name = "address")
    val address: String,

    @ColumnInfo(name = "courier_company")
    val courierCompany: String,

    @ColumnInfo(name = "sms_content")
    val smsContent: String,

    @ColumnInfo(name = "sms_date")
    val smsDate: Long,

    @ColumnInfo(name = "matched_rule")
    val matchedRule: String = "",

    // 删除时间
    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long = System.currentTimeMillis()
)
