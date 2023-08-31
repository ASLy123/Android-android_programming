package com.bignerdranch.example.criminalintent

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.bignerdranch.android.criminalintent.getScaledBitmap
import java.io.File
import java.text.DateFormat
import android.text.*
import android.text.format.DateFormat.format
import java.util.*

private const val TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_DATE = 0
private const val REQUEST_CONTACT = 1
private const val REQUEST_TIME = 2
private const val REQUEST_PHOTO = 3

private const val DATE_FORMAT = "EEE, MMM, dd"

class CrimeFragment : Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {
    private lateinit var crime: Crime
    private lateinit var photoFile: File
    private lateinit var photoUri: Uri

    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var reportButton: Button
    private lateinit var suspectButton: Button
//    private lateinit var callButton: Button
    private lateinit var photoButton: ImageButton
    private lateinit var photoView: ImageView

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

    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment {      //创建fragment实例及Bundle对象
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)       //调用Bundle限定类型的get函数
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
        val view = inflater.inflate(R.layout.fragment_crime, container, false)  //生成fragment的视图
        //视图的父视图, 是否立即将生成的视图添加给父视图
        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        timeButton = view.findViewById(R.id.btn_pickTime) as Button
        reportButton = view.findViewById(R.id.crime_report) as Button
        suspectButton = view.findViewById(R.id.crime_suspect) as Button
//        callButton = view.findViewById(R.id.crime_call) as Button
        photoButton = view.findViewById(R.id.crime_camera) as ImageButton
        photoView    = view.findViewById(R.id.crime_photo) as ImageView



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
                    photoFile = crimeDetailViewModel.getPhotoFile(crime)    //获取照片文件位置
                    photoUri = FileProvider.getUriForFile(      //getUriForFile())会把本地文件路径转换为相机能使用的Uri形式
                        requireActivity(),
                        "com.bignerdranch.android.criminalintent.fileprovider", //provider授权
                        photoFile)  //图片文件路径
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

            override fun afterTextChanged(sequence: Editable?) {
                // This one too
            }
        }
        titleField.addTextChangedListener(titleWatcher)
        solvedCheckBox.apply {                  //设置监听器，根据用户操作，更新solvedCheckBox状态
            setOnCheckedChangeListener { _, isChecked ->
                crime.isSolved = isChecked
            }
        }

        dateButton.setOnClickListener {
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }

        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_TIME) //目标fragment和请求代码
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_TIME)

            }
        }

        reportButton.setOnClickListener {
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"     //指定·数据类型为text/plain
                putExtra(Intent.EXTRA_TEXT, getCrimeReport())
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.crime_report_subject)
                )
            }.also { intent ->
                val chooserIntent = Intent.createChooser(intent, getString(R.string.send_report))   //创建每次都显示的activity选择器
                startActivity(chooserIntent)
            }
        }
        suspectButton.apply {
            val pickContactIntent =
                Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
            //要执行的操作, 待访问数据的位置
            setOnClickListener {
                startActivityForResult(pickContactIntent, REQUEST_CONTACT)
            }

            pickContactIntent.addCategory(Intent.CATEGORY_HOME)   //阻止任何联系人应用和你的intent匹配
            val packageManager: PackageManager = requireActivity().packageManager
            val resolvedActivity: ResolveInfo? =
                packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)    //resolveActivity()可以找到匹配给定Intent任务的activity
            //MATCH_DEFAULT_ONLY限定只搜索带CATEGORY_DEFAULT标志的activity
            if (resolvedActivity == null) {
                isEnabled = false
            }
        }

        photoButton.apply {
            val packageManager: PackageManager = requireActivity().packageManager
            val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val resolvedActivity: ResolveInfo? = packageManager.resolveActivity(captureImage, PackageManager.MATCH_DEFAULT_ONLY)
            //找PackageManager确认是否有响应相机隐式intent的activity
//            if (resolvedActivity == null) {
//                isEnabled = false
//            }
            setOnClickListener{
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                val cameraActivities: List<ResolveInfo> = packageManager.queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY)
                for (cameraActivity in cameraActivities){
                    requireActivity().grantUriPermission(
                            cameraActivity.activityInfo.packageName,
                            photoUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }
                startActivityForResult(captureImage, REQUEST_PHOTO)
            }
        }

    }





        override fun onStop() {
            super.onStop()
            crimeDetailViewModel.saveCrime(crime)       //保存数据
        }

    override fun onDetach() {       //　撤销URI权限
        super.onDetach()
        requireActivity().revokeUriPermission(photoUri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    }


    override fun onDateSelected(date: Date) {
            crime.date = date
            updateUI()
        }

        private fun updateUI() {
            titleField.setText(crime.title)
            dateButton.text = DateFormat.getDateInstance().format(crime.date)
//        solvedCheckBox.isChecked = crime.isSolved
            solvedCheckBox.apply {
                isChecked = crime.isSolved
                jumpDrawablesToCurrentState()       //跳过checkbox的勾选动画
            }
            if (crime.suspect.isNotEmpty()) {   //设置按钮文字
                suspectButton.text = crime.suspect
            }
            updatePhotoView()

        }


    private fun updatePhotoView(){  ////刷新photoView
        if(photoFile.exists()){
            val bitmap = getScaledBitmap(photoFile.path, requireActivity())
            photoView.setImageBitmap(bitmap)
            photoView.contentDescription = getString(R.string.crime_photo_image_description)
        } else {
            photoView.setImageDrawable(null)
            photoView.contentDescription = getString(R.string.crime_photo_no_image_description)
        }
    }

        override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
        ) {   //获取联系人姓名
            when {
                resultCode != Activity.RESULT_OK -> return
                requestCode == REQUEST_CONTACT && data != null -> {
                    val contactUri: Uri? = data.data
                    // Specify which fields you want your query to return values for
                    val queryFields = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
                    // Perform your query - the contactUri is like a "where" clause here
                    val cursor = contactUri?.let {
                        requireActivity().contentResolver
                            .query(
                                contactUri, queryFields, null,
                                null, null
                            )
                    }
                    cursor?.use {
                        // Verify cursor contains at least one result
                        if (it.count == 0) {
                            return
                        }
                        // Pull out the first column of the first row of data - that is your suspect's name
                        it.moveToFirst()
                        val suspect = it.getString(0)
                        crime.suspect = suspect
                        crimeDetailViewModel.saveCrime(crime)
                        suspectButton.text = suspect
                    }
                }
                requestCode == REQUEST_PHOTO -> {
                    updatePhotoView()
                }

            }
        }



        private fun getCrimeReport(): String {   //创建四段字符串信息，并返回拼接完整的消息
            val solvedString = if (crime.isSolved) {
                getString(R.string.crime_report_solved)
            } else {
                getString(R.string.crime_report_unsolved)
            }
            val dateString = android.text.format.DateFormat.format(DATE_FORMAT,crime.date).toString()
            var suspect = if (crime.suspect.isBlank()) {
                getString(R.string.crime_report_no_suspect)
            } else {
                getString(R.string.crime_report_suspect)
            }
            return getString(R.string.crime_report, crime.title, dateString, solvedString, suspect)
        }
    }

