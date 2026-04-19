package com.mashangqujian.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mashangqujian.data.dao.DeletedParcelHistoryDao
import com.mashangqujian.data.dao.ParcelDao
import com.mashangqujian.data.dao.RuleDao
import com.mashangqujian.data.model.DeletedParcelHistory
import com.mashangqujian.data.model.Parcel
import com.mashangqujian.data.model.ParsingRule

// 从版本2升级到版本4的迁移
val MIGRATION_2_4 = object : Migration(2, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
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

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE parsing_rules ADD COLUMN code_prefix TEXT")
        db.execSQL("ALTER TABLE parsing_rules ADD COLUMN code_suffix TEXT")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE parcels ADD COLUMN matched_rule TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS deleted_parcels (
                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                parcel_code TEXT NOT NULL,
                address TEXT NOT NULL,
                courier_company TEXT NOT NULL,
                sms_content TEXT NOT NULL,
                sms_date INTEGER NOT NULL,
                matched_rule TEXT NOT NULL DEFAULT '',
                deleted_at INTEGER NOT NULL DEFAULT 0
            )
        """)
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS deleted_parcels")
        db.execSQL("""
            CREATE TABLE deleted_parcels (
                id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                parcel_code TEXT NOT NULL,
                address TEXT NOT NULL,
                courier_company TEXT NOT NULL,
                sms_content TEXT NOT NULL,
                sms_date INTEGER NOT NULL,
                matched_rule TEXT NOT NULL DEFAULT '',
                deleted_at INTEGER NOT NULL DEFAULT 0
            )
        """)
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE parcels ADD COLUMN collected_at INTEGER")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE parsing_rules ADD COLUMN company_prefix TEXT")
        db.execSQL("ALTER TABLE parsing_rules ADD COLUMN company_suffix TEXT")
        db.execSQL("ALTER TABLE parsing_rules ADD COLUMN address_prefix TEXT")
        db.execSQL("ALTER TABLE parsing_rules ADD COLUMN address_suffix TEXT")
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE parsing_rules ADD COLUMN rule_name TEXT NOT NULL DEFAULT ''")
    }
}

@Database(
    entities = [Parcel::class, ParsingRule::class, DeletedParcelHistory::class],
    version = 11,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun parcelDao(): ParcelDao
    abstract fun ruleDao(): RuleDao
    abstract fun deletedParcelHistoryDao(): DeletedParcelHistoryDao

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
                .addMigrations(MIGRATION_2_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
                .fallbackToDestructiveMigration()
                .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
