package com.bignerdranch.example.criminalintent

import android.app.Application

class CriminalIntentApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CrimeRepository.initialize(this)        //初始化CrimeRepository
    }
}
