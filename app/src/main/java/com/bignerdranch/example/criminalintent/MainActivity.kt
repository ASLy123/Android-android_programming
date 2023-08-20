package com.bignerdranch.example.criminalintent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //获取FragmentManager之后，再获取一个fragment交给它管理
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        //使用supportFragmentManager属性就能获取activity的fragment管理器

        if (currentFragment == null) {  //如果指定容器视图资源ID的fragment不存在 应该新建CrimeFragment，并启动一个新的fragment事务，将新建fragment添加到队列中
            val fragment = CrimeListFragment()
            supportFragmentManager
                .beginTransaction()                     //创建一个新的fragment事务
                .add(R.id.fragment_container, fragment) //执行一个fragment添加操作
                .commit()                               //然后提交该事务
        }
    }
}