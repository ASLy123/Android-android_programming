package com.bignerdranch.example.criminalintent


import android.app.DatePickerDialog
import android.app.Dialog

import android.os.Bundle
import android.text.format.DateFormat.is24HourFormat
import android.widget.DatePicker
import android.widget.TimePicker

import androidx.fragment.app.DialogFragment
import java.sql.Time
import java.text.DateFormat

import java.util.*
import android.app.TimePickerDialog

private const val ARG_TIME = "time"
class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    interface Callbacks{
        fun onDateSelected(date: Date)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val date = arguments?.getSerializable(ARG_TIME) as Date
        val c = Calendar.getInstance()
        c.time = date
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)



        val timeListener = android.app.TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            val currentTime = Calendar.getInstance() // 获取当前日期和时间的 Calendar 实例
            currentTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
            currentTime.set(Calendar.MINUTE, minute)

            val resultDate: Date = currentTime.time

            targetFragment?.let { fragment ->
                (fragment as Callbacks).onDateSelected(resultDate)
            }
        }


        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(
            requireContext(),
            timeListener,
            hour,
            minute,
            android.text.format.DateFormat.is24HourFormat(activity)
        )
    }




    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {

    }

    companion object{
        fun newInstance(date: Date) : TimePickerFragment{
            val args = Bundle().apply {
                putSerializable(ARG_TIME, date)
            }
            return TimePickerFragment().apply {
                arguments = args
            }
        }
    }


}