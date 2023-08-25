package com.bignerdranch.example.criminalintent

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.*

private const val ARG_DATE = "date"

class DatePickerFragment : DialogFragment() {
    interface Callbacks{
        fun onDateSelected(date: Date)
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val date = arguments?.getSerializable(ARG_DATE) as Date
        val calendar = Calendar.getInstance()
        calendar.time = date
        val  initialYear = calendar.get(Calendar.YEAR)
        val  initialMonth = calendar.get(Calendar.MONDAY)
        val  initialDay = calendar.get(Calendar.DAY_OF_MONTH)
        val dateListener = DatePickerDialog.OnDateSetListener{
                _: DatePicker, year: Int, month: Int, day: Int ->
            val resultDate : Date = GregorianCalendar(year, month, day).time
            targetFragment?.let { fragment ->
                (fragment as Callbacks).onDateSelected(resultDate)
            }
        }


        return DatePickerDialog(
            requireContext(),   //获取视图相关必需资源的context对象
            dateListener,
            initialYear,       //年、月、日初始值
            initialMonth,
            initialDay
        )
    }
    companion object{
        fun newInstance(date: Date) : DatePickerFragment{
            val args = Bundle().apply {
                putSerializable(ARG_DATE, date)
            }
            return DatePickerFragment().apply {
                arguments = args
            }
        }

    }
}