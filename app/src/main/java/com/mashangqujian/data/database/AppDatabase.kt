package com.mashangqujian.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mashangqujian.data.dao.ParcelDao
import com.mashangqujian.data.dao.RuleDao
import com.mashangqujian.data.model.Parcel
import com.mashangqujian.data.model.ParsingRule

// 从版本2升级到版本4的迁移
val MIGRATION_2_4 = object : Migration(2, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 删除旧表并创建新表
        db.execSQL("DROP TABLE IF EXISTS parsing_rules")
        db.execSQL("""
            CREATE TABLE parsing_rules (
                id TEXT NOT NULL PRIMARY KEY,
                company_name TEXT NOT NULL,
                code_keyword TEXT,
                code_prefix TEXT,
                code_suffix TEXT,
                code_format TEXT NOT NULL DEFAULT 'DIGITS',
                code_min_digits INTEGER NOT NULL DEFAULT 3,
                code_max_digits INTEGER NOT NULL DEFAULT 8,
                code_seg1 INTEGER NOT NULL DEFAULT 1,
                code_seg2 INTEGER NOT NULL DEFAULT 2,
                code_seg3 INTEGER NOT NULL DEFAULT 4,
                letter_count INTEGER NOT NULL DEFAULT 2,
                address_keyword TEXT,
                sms_example TEXT NOT NULL DEFAULT '',
                parcel_code_pattern TEXT NOT NULL DEFAULT '',
                address_pattern TEXT,
                is_custom INTEGER NOT NULL DEFAULT 0,
                is_enabled INTEGER NOT NULL DEFAULT 1,
                description TEXT NOT NULL DEFAULT '',
                match_count INTEGER NOT NULL DEFAULT 0,
                created_at INTEGER NOT NULL DEFAULT 0,
                updated_at INTEGER NOT NULL DEFAULT 0
            )
        """)
    }
}

// 从版本4升级到版本5的迁移：新增 code_prefix 和 code_suffix 字段
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE parsing_rules ADD COLUMN code_prefix TEXT")
        db.execSQL("ALTER TABLE parsing_rules ADD COLUMN code_suffix TEXT")
    }
}

@Database(
    entities = [Parcel::class, ParsingRule::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun parcelDao(): ParcelDao
    abstract fun ruleDao(): RuleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mashangqujian_db"
                )
                .addMigrations(MIGRATION_2_4, MIGRATION_4_5)
                .fallbackToDestructiveMigration()
                .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
