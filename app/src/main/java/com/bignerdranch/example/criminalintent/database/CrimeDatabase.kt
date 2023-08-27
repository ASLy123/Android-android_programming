package com.bignerdranch.example.criminalintent.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bignerdranch.example.criminalintent.Crime

@Database(entities = [Crime::class], version = 2, exportSchema = false) //@Database注解告诉Room，CrimeDatabase类就是应用里的数据库
//第一个参数是实体类集合，告诉Room在创建和管理数据库表时该用哪个实体类

@TypeConverters(CrimeTypeConverter::class)
//告诉数据库，需要转换数据类型时，请使用CrimeTypeConverters类里的函数

abstract class CrimeDatabase : RoomDatabase(){
    abstract fun crimeDao(): CrimeDao
}

val migration_1_2 = object : Migration(1,2){        //告诉Room数据库版本有变化 创建一个 Migration对象更新数据库
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE Crime ADD COLUMN suspect TEXT NOT NULL DEFAULT''")
    }
}