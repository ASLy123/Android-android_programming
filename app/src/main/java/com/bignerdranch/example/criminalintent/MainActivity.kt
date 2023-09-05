package com.bignerdranch.example.criminalintent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import java.util.*

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity(), CrimeListFragment.Callbacks {
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

    override fun onCrimeSelected(crimeId: UUID) {
        //Log.d(TAG, "MainActivity.onCrimeSelected: $crimeId")
//        val fragment = CrimeFragment()

        //用户只要在CrimeListFragment界面点击某一条crime记录，就用CrimeFragment实例替换CrimeListFragment
        val fragment = CrimeFragment.newInstance(crimeId)   //创建CrimeFragment 传入.onCrimeSelected(UUID)获取的UUID参数值
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)     //替换了MainActivity托管的fragment
            .addToBackStack(null)                     //把替换事务添加到回退栈里
            .commit()
    }



}