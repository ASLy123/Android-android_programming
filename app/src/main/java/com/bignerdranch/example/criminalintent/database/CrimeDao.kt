package com.bignerdranch.example.criminalintent.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.bignerdranch.example.criminalintent.Crime
import java.util.*

@Dao                    //@Dao注解告诉Room，CrimeDao是一个数据访问对象
interface CrimeDao {
    @Query("SELECT * FROM crime")
//    fun getCrimes(): List<Crime>
    fun getCrimes(): LiveData<List<Crime>>

    @Query("SELECT * FROM crime WHERE id=(:id)")
//    fun getCrime(id: UUID): Crime?
    fun getCrime(id: UUID): LiveData<Crime?>

    @Transaction
    @Query("SELECT COUNT(*) FROM crime")
    fun isNotEmpty(): LiveData<Boolean>

    @Query("DELETE FROM crime")
    fun deleteAll()

    @Update
    fun updateCrime(crime: Crime)

    @Insert
    fun addCrime(crime: Crime)

}