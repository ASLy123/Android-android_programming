package com.bignerdranch.example.criminalintent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.bignerdranch.example.criminalintent.database.CrimeDatabase
import com.bignerdranch.example.criminalintent.database.migration_1_2
import java.io.File
import java.util.*
import java.util.concurrent.Executors

private const val DATABASE_NAME = "crime-database"
class CrimeRepository private constructor(context: Context) {

    private val database : CrimeDatabase = Room.databaseBuilder( //databaseBuilder()实现CrimeDatabase抽象类
        context.applicationContext,
        CrimeDatabase::class.java,              //Room用来创建数据库的类
        DATABASE_NAME                           //Room将要创建的数据库文件的名字
    ).addMigrations(migration_1_2).build()  //调用addMigrations()创建数据库迁移

    private val crimeDao = database.crimeDao()
    private val executor = Executors.newSingleThreadExecutor()  //newSingleThreadExecutor()函数会返回一个指向新线程的executor实例
    private val filesDir = context.applicationContext.filesDir
    //    fun getCrimes(): List<Crime> = crimeDao.getCrimes()
    fun getCrimes(): LiveData<List<Crime>> = crimeDao.getCrimes()
    //    fun getCrime(id: UUID): Crime? = crimeDao.getCrime(id)
    fun getCrime(id: UUID): LiveData<Crime?> = crimeDao.getCrime(id)

    fun updateCrime(crime: Crime){
        executor.execute{
            crimeDao.updateCrime(crime)
        }
    }

    fun addCrime(crime: Crime){
        executor.execute{
            crimeDao.addCrime(crime)
        }
    }

    fun getPhotoFile(crime: Crime):File = File(filesDir, crime.photoFileName)   //返回指向某个具体位置的File对象

    companion object {
        private var INSTANCE: CrimeRepository? = null
        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = CrimeRepository(context)
            }
        }
        fun get(): CrimeRepository {
            return INSTANCE ?: throw IllegalStateException("CrimeRepository must be initialized")
        }
    }
}
