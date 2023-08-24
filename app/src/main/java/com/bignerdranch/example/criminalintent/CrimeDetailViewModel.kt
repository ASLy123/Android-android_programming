package com.bignerdranch.example.criminalintent

import android.view.View
import androidx.lifecycle.*
import java.util.*

class CrimeDetailViewModel : ViewModel() {
    private val crimeRepository = CrimeRepository.get()
    private val crimeIdLiveData = MutableLiveData<UUID>()       //保存着CrimeFragment当前显示（或将要显示）的crime对象的ID


    var crimeLiveData: LiveData<Crime?> = Transformations.switchMap(crimeIdLiveData) { crimeId ->
        crimeRepository.getCrime(crimeId)       //crime ID一变就触发新的数据库查询
    }
    fun loadCrime(crimeId: UUID){       //让ViewModel知道该加载哪个crime对象
        crimeIdLiveData.value = crimeId
    }
    fun saveCrime(crime: Crime){        //crime数据写入数据库
        crimeRepository.updateCrime(crime)
    }
}