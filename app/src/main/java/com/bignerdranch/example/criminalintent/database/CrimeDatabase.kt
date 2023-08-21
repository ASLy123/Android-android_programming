package com.bignerdranch.example.criminalintent.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bignerdranch.example.criminalintent.Crime

@Database(entities = [Crime::class], version = 1, exportSchema = false) //@Database注解告诉Room，CrimeDatabase类就是应用里的数据库
//第一个参数是实体类集合，告诉Room在创建和管理数据库表时该用哪个实体类

@TypeConverters(CrimeTypeConverter::class)
//告诉数据库，需要转换数据类型时，请使用CrimeTypeConverters类里的函数

abstract class CrimeDatabase : RoomDatabase(){
    abstract fun crimeDao(): CrimeDao
}