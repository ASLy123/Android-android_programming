package com.bignerdranch.example.criminalintent.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
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
}