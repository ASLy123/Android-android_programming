package com.bignerdranch.example.criminalintent

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

private const val TAG = "CrimeListFragment"
class CrimeListFragment : Fragment(){
    interface Callbacks {       //代理任务给托管activity 接口里定义的就是被托管的fragment要求它的托管activity做的工作
        fun onCrimeSelected(crimeId: UUID)
    }

    private var callbacks: Callbacks? = null        //用来保存实现Callbacks接口的对象

    private lateinit var crimeRecyclerView: RecyclerView
//    private var adapter: CrimeAdapter? = null
    private var adapter: CrimeAdapter? = CrimeAdapter(emptyList())

    private val crimeListViewModel: CrimeListViewModel by lazy {

        ViewModelProviders.of(this).get(CrimeListViewModel::class.java)
    }
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        Log.d(TAG, "Total crimes:${crimeListViewModel.crimes.size}")
//    }

    override fun onAttach(context: Context) {   //context是托管它的activity实例。
        super.onAttach(context)
        callbacks = context as Callbacks?   //把托管activity转成了Callbacks. 这样托管activity就必须要实现callbacks接口
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)     //让FragmentManager知道CrimeListFragment需接收选项菜单函数回调
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime_list, container, false)
        crimeRecyclerView = view.findViewById(R.id.crime_recycler_view) as RecyclerView
        crimeRecyclerView.layoutManager = LinearLayoutManager(context) //LayoutManager不仅要安排列表项出现的位置，还负责定义如何滚屏
//        updateUI()
        crimeRecyclerView.adapter = adapter
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState:
    Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeListViewModel.crimeListLiveData.observe(       //用来给LiveData实例登记观察者，让观察者和类似activity或fragment这样的其他组件同呼吸共命运
            viewLifecycleOwner,
            Observer { crimes ->            //负责响应LiveData的新数据通知
                crimes?.let {
                    Log.i(TAG, "Got crimes ${crimes.size}")
                    updateUI(crimes)
                }
            })
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null    //取消callbacks属性
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_crime_list, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId){
            R.id.new_crime ->{
                val crime = Crime()
                crimeListViewModel.addCrime(crime)
                callbacks?.onCrimeSelected(crime.id)
                true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    companion object {
        fun newInstance(): CrimeListFragment {
            return CrimeListFragment()      //让activity调用获取fragment实例
        }
    }

    private inner class CrimeHolder(view: View) : RecyclerView.ViewHolder(view),View.OnClickListener {//CrimeHolder的构造函数首先接收并保存view，然后将其作为值参传递给RecyclerView.ViewHolder的构造函数
        private lateinit var crime: Crime

        val titleTextView: TextView = itemView.findViewById(R.id.crime_title)
        private val dateTextView: TextView = itemView.findViewById(R.id.crime_date)
        private val solvedImageView: ImageView = itemView.findViewById(R.id.crime_solved)


        init {
            itemView.setOnClickListener(this)
        }

        fun bind(crime: Crime) {
            this.crime = crime
            titleTextView.text = this.crime.title
            dateTextView.text = this.crime.date.toString()
            solvedImageView.visibility = if (crime.isSolved) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        override fun onClick(v: View) {
//            Toast.makeText(context, "${crime.title} pressed!", Toast.LENGTH_SHORT).show()
            callbacks?.onCrimeSelected(crime.id)        //响应用户点击crime列表项事件
        }



    }

    object FlowerDiffCallback : DiffUtil.ItemCallback<Crime>(){
        override fun areItemsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Crime, newItem: Crime): Boolean {
            return oldItem == newItem
        }
    }
    private inner class CrimeAdapter(var crimes: List<Crime>)
        : androidx.recyclerview.widget.ListAdapter<Crime, CrimeHolder>(FlowerDiffCallback) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CrimeHolder {
            val view = layoutInflater.inflate(R.layout.list_liem_crime, parent, false)
            return CrimeHolder(view)
        }
        override fun getItemCount() = crimes.size
        override fun onBindViewHolder(holder: CrimeHolder, position: Int) {
            val crime = crimes[position]
            holder.bind(crime)
        }

    }
//    private fun updateUI() {
    private fun updateUI(crimes: List<Crime>) {
//        val crimes = crimeListViewModel.crimes
        adapter = CrimeAdapter(crimes)
        crimeRecyclerView.adapter = adapter
    }

}

