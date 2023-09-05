    package com.bignerdranch.example.criminalintent

    import android.Manifest
    import android.app.Activity
    import android.content.ActivityNotFoundException
    import android.content.Intent
    import android.content.pm.PackageManager
    import android.content.pm.ResolveInfo
    import android.net.Uri
    import android.os.Bundle
    import android.provider.ContactsContract
    import android.provider.ContactsContract.Contacts
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
    import android.view.ViewTreeObserver
    import android.view.ViewTreeObserver.*
    import androidx.core.app.ActivityCompat
    import com.bumptech.glide.Glide
    import java.util.*

//    private const val TAG = "CrimeFragment"
    private const val ARG_CRIME_ID = "crime_id"
    private const val DIALOG_DATE = "DialogDate"
    private const val DIALOG_TIME = "DialogTime"
    private const val REQUEST_DATE = 0
    private const val REQUEST_CONTACT = 1
    private const val REQUEST_TIME = 2
    private const val REQUEST_PHOTO = 3
//    private const val REQUEST_PHONENUMBLE = 4


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
        private lateinit var callPhone: Button

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

            // 请求联系人权限
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    REQUEST_CONTACT
                )
            }

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
            photoView = view.findViewById(R.id.crime_photo) as ImageView
            callPhone = view.findViewById(R.id.crime_call_phone) as Button
            photoView = view.findViewById(R.id.crime_photo) as ImageView

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
                        photoUri =
                            FileProvider.getUriForFile(      //getUriForFile())会把本地文件路径转换为相机能使用的Uri形式
                                requireActivity(),
                                "com.bignerdranch.android.criminalintent.fileprovider", //provider授权
                                photoFile
                            )  //图片文件路径
                        updateUI()
                    }
                })

            fun loadThumbnailImage(width: Int, height: Int) {
                // 使用 Glide 或其他图像加载库加载缩略图，传递图像视图的宽度和高度
                Glide.with(this)
                    .load(crimeDetailViewModel.getPhotoFile(crime).path)
                    .override(width, height) // 设置缩略图的尺寸
                    .centerCrop() // 可根据需要使用其他缩放策略
                    .into(photoView)
            }

            // 添加视图树监听器以确保视图准备好后加载缩略图
            photoView.viewTreeObserver.addOnGlobalLayoutListener(
                object : OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        // 在视图树准备好后，加载缩略图
                        loadThumbnailImage(photoView.width, photoView.height)

                        // 移除监听器，以确保只加载一次缩略图
                        photoView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                }
            )
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
                    val chooserIntent = Intent.createChooser(
                        intent,
                        getString(R.string.send_report)
                    )   //创建每次都显示的activity选择器
                    startActivity(chooserIntent)
                }
            }
            suspectButton.apply {
                val pickContactIntent =
                    Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI)
                                //要执行的操作, 待访问数据的位置
                setOnClickListener {
                    startActivityForResult(pickContactIntent, REQUEST_CONTACT)
                }

                //   pickContactIntent.addCategory(Intent.CATEGORY_HOME)   //阻止任何联系人应用和你的intent匹配
//                   val packageManager: PackageManager = requireActivity().packageManager
//                   val resolvedActivity: ResolveInfo? =
//                   packageManager.resolveActivity(pickContactIntent, PackageManager.MATCH_DEFAULT_ONLY)    //resolveActivity()可以找到匹配给定Intent任务的activity
//                   MATCH_DEFAULT_ONLY限定只搜索带CATEGORY_DEFAULT标志的activity
//                   if (resolvedActivity == null) {
//                       isEnabled = false
//                   }
            }

            callPhone.apply {
                // 拨号

                setOnClickListener {
                    try {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${crimeDetailViewModel.phoneNumber}")
                        }
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, "Unable to dial number", Toast.LENGTH_SHORT).show()
                    }
                }

            }


            photoButton.apply {
                val packageManager: PackageManager = requireActivity().packageManager
                val captureImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                val resolvedActivity: ResolveInfo? =
                    packageManager.resolveActivity(captureImage, PackageManager.MATCH_DEFAULT_ONLY)
                //找PackageManager确认是否有响应相机隐式intent的activity
                //            if (resolvedActivity == null) {
                //                isEnabled = false
                //            }
                setOnClickListener {
                    captureImage.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    val cameraActivities: List<ResolveInfo> = packageManager.queryIntentActivities(
                        captureImage,
                        PackageManager.MATCH_DEFAULT_ONLY
                    )
                    for (cameraActivity in cameraActivities) {
                        requireActivity().grantUriPermission(
                            cameraActivity.activityInfo.packageName,
                            photoUri,
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    }
                    startActivityForResult(captureImage, REQUEST_PHOTO)
                }
            }

            photoView.setOnClickListener {
                val photoUrl = photoFile.path
                val fragment = photoDialogFragment.newInstance(photoUrl)
                fragment.show(this@CrimeFragment.requireFragmentManager(), "photo_dialog")
            }





        }

        override fun onStop() {
            super.onStop()
            crimeDetailViewModel.saveCrime(crime)       //保存数据
        }

        override fun onDetach() {       //　撤销URI权限
            super.onDetach()
            requireActivity().revokeUriPermission(
                photoUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
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


        private fun updatePhotoView() {  ////刷新photoView
            if (photoFile.exists()) {
                val bitmap = getScaledBitmap(photoFile.path, requireActivity())
                photoView.setImageBitmap(bitmap)
                photoView.contentDescription = getString(R.string.crime_photo_image_description)
            } else {
                photoView.setImageDrawable(null)
                photoView.contentDescription = getString(R.string.crime_photo_no_image_description)
            }
        }

        override fun onActivityResult(  //onActivityResult(...)是在onViewCreated(...)函数之前被调用
            requestCode: Int,
            resultCode: Int,
            data: Intent?
        ) {   //获取联系人姓名
            when {
                resultCode != Activity.RESULT_OK -> return
                requestCode == REQUEST_CONTACT && data != null -> {

                val contactUri: Uri? = data.data    //从返回的数据中获取联系人的Uri（唯一标识符）

                val resolver = requireActivity().contentResolver
                val queryFields = arrayOf(Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts._ID)
                // 定义一个字符串数组，包含您想要查询的联系人字段，这里只查询显示名字

                val cursor = contactUri?.let {
                    resolver.query(it, queryFields, null, null, null)
                }
                //查询联系人数据库，获取指定Uri的联系人信息
                cursor?.use {
                    // Verify cursor contains at least one result
                   if (it.count == 0) {
                        return
                    }
                    // Pull out the first column of the first row of data - that is your suspect's name
                    it.moveToFirst()    //将游标移到查询结果的第一行。
                    val suspect = it.getString(0)
                    val contactID = it.getString(1)
                     crime.suspect = suspect
                     crimeDetailViewModel.saveCrime(crime)
                     suspectButton.text = suspect
                     val phone = activity?.contentResolver?.query(
                          ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                         null,
                         ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactID,
                         null,
                         null)
                     phone?.apply {
                        moveToNext()
                        val columnIndex = getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        if (columnIndex >= 0){
                             crimeDetailViewModel.phoneNumber = getString(columnIndex)
                        }
                    }



                    }
                }

                requestCode == REQUEST_PHOTO -> {
                    updatePhotoView()
                }

            }

        }




        private fun getCrimeReport(): String {
            val solvedString = if (crime.isSolved) {
                getString(R.string.crime_report_solved)
            } else {
                getString(R.string.crime_report_unsolved)
            }
            val dateString = DateFormat.getDateInstance().format(crime.date)
            val suspect = if (crime.suspect.isBlank()) {
                getString(R.string.crime_report_no_suspect)
            } else {
                getString(R.string.crime_report_suspect, crime.suspect)
            }

            // 使用 String.format 将值插入到报告字符串中
            return String.format(
                getString(R.string.crime_report),
                crime.title,
                dateString,
                solvedString,
                suspect
            )
        }



    }

