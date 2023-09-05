package com.bignerdranch.example.criminalintent

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide


class photoDialogFragment: DialogFragment() {
    companion object {
        private const val ARG_PHOTO_URL = "photo_url"

        // 创建一个静态的工厂方法来传递照片 URL 参数
        fun newInstance(photoUrl: String): photoDialogFragment {
            val fragment = photoDialogFragment()
            val args = Bundle()
            args.putString(ARG_PHOTO_URL, photoUrl)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 从参数中获取照片 URL
        val photoUrl = arguments?.getString(ARG_PHOTO_URL)
        val imageView = view.findViewById(R.id.iv) as ImageView

        // 使用图像加载库加载图像，这里使用Glide作为示例
         Glide.with(this).load(photoUrl).into(imageView)

        // 设置点击对话框外部时关闭对话框
        isCancelable = true

        // 关闭按钮点击事件
        imageView.setOnClickListener {
            dismiss()
        }
    }
}


