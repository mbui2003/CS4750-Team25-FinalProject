package com.bignerdranch.android.todolist.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bignerdranch.android.todolist.Task

@Database(entities = [ Task::class ], version=3)
@TypeConverters(TaskTypeConverters::class)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDAO(): TaskDao
}

val migration_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE Task ADD COLUMN priority TEXT NOT NULL DEFAULT ''"
        )
        database.execSQL(
            "ALTER TABLE Task ADD COLUMN category TEXT NOT NULL DEFAULT ''"
        )
    }
}

val migration_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create a new table with the desired schema
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS `Task_new` (" +
                    "`date` INTEGER NOT NULL, " +
                    "`selectedPriority` INTEGER NOT NULL, " +
                    "`selectedCategory` INTEGER NOT NULL, " +
                    "`id` BLOB NOT NULL, " +
                    "`title` TEXT NOT NULL, " +
                    "`priority` TEXT NOT NULL, " +
                    "`category` TEXT NOT NULL, " +
                    "`isComplete` INTEGER NOT NULL, " +
                    "PRIMARY KEY(`id`))"
        )

        // Copy the data from the old table to the new one
        database.execSQL("INSERT INTO `Task_new` (`date`, `selectedPriority`, `selectedCategory`, `id`, `title`, `priority`, `category`, `isComplete`) SELECT `date`, 0 as `selectedPriority`, 0 as `selectedCategory`, `id`, `title`, `priority`, `category`, `isComplete` FROM `Task`")

        // Remove the old table
        database.execSQL("DROP TABLE `Task`")

        // Change the new table name to the original name
        database.execSQL("ALTER TABLE `Task_new` RENAME TO `Task`")
    }
}
