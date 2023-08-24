package com.bignerdranch.example.criminalintent

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.bignerdranch.example.criminalintent.database.CrimeDatabase
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.Executors.newSingleThreadExecutor

private const val DATABASE_NAME = "crime-database"
class CrimeRepository private constructor(context: Context) {

    private val database : CrimeDatabase = Room.databaseBuilder( //databaseBuilder()使用三个参数具体实现了CrimeDatabase抽象类
        context.applicationContext,
        CrimeDatabase::class.java,              //Room用来创建数据库的类
        DATABASE_NAME                           //Room将要创建的数据库文件的名字
    ).build()

    private val crimeDao = database.crimeDao()
    private val executor = Executors.newSingleThreadExecutor()  //newSingleThreadExecutor()函数会返回一个指向新线程的executor实例

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
