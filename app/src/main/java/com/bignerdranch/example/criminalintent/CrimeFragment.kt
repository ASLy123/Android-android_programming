package com.bignerdranch.example.criminalintent

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import java.util.*
private const val TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_DATE = 0
private const val REQUEST_TIME = 1


class CrimeFragment : Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {
    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var solvedCheckBox: CheckBox


    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {        //关联CrimeFragment和CrimeDetailViewModel
        ViewModelProviders.of(this).get(CrimeDetailViewModel::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
//        Log.d(TAG, "args bundle crime ID: $crimeId")
        crimeDetailViewModel.loadCrime(crimeId)
    }
    companion object{
        fun newInstance(crimeId: UUID): CrimeFragment{      //创建fragment实例及Bundle对象
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID,crimeId)       //调用Bundle限定类型的get函数
            }
            return CrimeFragment().apply {
                arguments = args        //把新建argument附加给fragment实例
            }
        }

    }

    override fun onCreateView(  //会实例化fragment视图的布局，然后将实例化的View返回给托管activity
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime,container, false)  //生成fragment的视图
                                                          //视图的父视图, 是否立即将生成的视图添加给父视图
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        timeButton = view.findViewById(R.id.btn_pickTime) as Button


//        dateButton.apply {
//            text = crime.date.toString()    //显示crime的发生日期
//            isEnabled = false               //禁用Button按钮
//        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { crime ->
                crime?.let {
                    this.crime = crime
                    updateUI()
                }
            })
    }


    override fun onStart() {
        super.onStart()
        val titleWatcher = object : TextWatcher {
            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
                // This space intentionally left blank
            }
            override fun onTextChanged(
                sequence: CharSequence?,    //用户输入
                start: Int,
                before: Int,
                count: Int
            ) {
                crime.title = sequence.toString()

            }
            override fun afterTextChanged(sequence: Editable?)
            {
                // This one too
            }
        }
        titleField.addTextChangedListener(titleWatcher)
        solvedCheckBox.apply {                  //设置监听器，根据用户操作，更新solvedCheckBox状态
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }

    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)       //保存数据
    }

    override fun onDateSelected(date: Date){
        crime.date = date
        updateUI()
    }

    private fun updateUI(){
        titleField.setText(crime.title)
        dateButton.text = crime.date.toString()
//        solvedCheckBox.isChecked = crime.isSolved
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()       //跳过checkbox的勾选动画
        }

        dateButton.setOnClickListener{
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE) //目标fragment和请求代码
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE)

            }
        }

        timeButton.setOnClickListener{
            TimePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_TIME) //目标fragment和请求代码
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_TIME)

            }
        }
    }

}