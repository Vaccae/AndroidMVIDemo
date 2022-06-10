package pers.vaccae.mvidemo.ui.view

import android.content.Context
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import pers.vaccae.mvidemo.R
import pers.vaccae.mvidemo.bean.CDrugs
import pers.vaccae.mvidemo.ui.adapter.DrugsAdapter
import pers.vaccae.mvidemo.ui.intent.ActionIntent
import pers.vaccae.mvidemo.ui.intent.ActionState
import pers.vaccae.mvidemo.ui.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private val TAG = "X Fold"

    private val recyclerView: RecyclerView by lazy { findViewById(R.id.recycler_view) }
    private val btncreate: Button by lazy { findViewById(R.id.btncreate) }
    private val btnadd: Button by lazy { findViewById(R.id.btnadd) }
    private val btndel: Button by lazy { findViewById(R.id.btndel) }
    private val tvmsg:TextView by lazy { findViewById(R.id.tv_Msg) }

    private lateinit var mainViewModel: MainViewModel
    private lateinit var drugsAdapter: DrugsAdapter


    //adapter的位置
    private var adapterpos = -1

    private var mSensorManager: SensorManager? = null
    private var mSensor: Sensor? = null
    private val mSensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(p0: SensorEvent?) {
            //p0.values[0]: 测量的铰链角度,其值范围在0到360度之间
            p0?.let {
                Log.i(TAG, "当前铰链角度为：${it.values[0]}")
            }
        }

        // 当传感器精度发生改变时回调该方法
        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            p0?.let {
                Log.i(TAG, "Sensor:${it.name}, value:$p1")
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy")
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume")
        //开启监听
        mSensorManager?.let {
            it.registerListener(mSensorEventListener, mSensor!!,
                SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause")
        // 取消监听
        mSensorManager?.let {
            it.unregisterListener(mSensorEventListener);
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 获取传感器管理对象
        mSensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // 获取传感器的类型(TYPE_HINGE_ANGLE:铰链角度传感器)
        mSensorManager?.let {
            mSensor = it.getDefaultSensor(Sensor.TYPE_HINGE_ANGLE);
        }

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        drugsAdapter = DrugsAdapter(R.layout.rcl_item, mainViewModel.listDrugs)
        drugsAdapter.setOnItemClickListener { baseQuickAdapter, view, i ->
            adapterpos = i
        }

        val gridLayoutManager = GridLayoutManager(this, 3)
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.adapter = drugsAdapter

        //初始化ViewModel监听
        observeViewModel()

        btncreate.setOnClickListener {
            lifecycleScope.launch {
                mainViewModel.actionIntent.send(ActionIntent.LoadDrugs)
            }
        }

        btnadd.setOnClickListener {
            lifecycleScope.launch {
                mainViewModel.actionIntent.send(ActionIntent.InsDrugs)
            }
        }

        btndel.setOnClickListener {
            lifecycleScope.launch {
                Log.i("status", "$adapterpos")
                val item = try {
                    drugsAdapter.getItem(adapterpos)
                } catch (e: Exception) {
                    CDrugs()
                }
                mainViewModel.actionIntent.send(ActionIntent.DelDrugs(adapterpos, item))
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.state.collect {
                    when (it) {
                        is ActionState.Normal -> {
                            btncreate.isEnabled = true
                            btnadd.isEnabled = true
                            btndel.isEnabled = true
                            tvmsg.text = ""
                        }
                        is ActionState.Loading -> {
                            btncreate.isEnabled = false
                            btncreate.isEnabled = false
                            btncreate.isEnabled = false
                        }
                        is ActionState.Drugs -> {
                            drugsAdapter.setList(it.drugs)
//                            drugsAdapter.setNewInstance(it.drugs)
                        }
                        is ActionState.Error -> {
                            Toast.makeText(this@MainActivity, it.msg, Toast.LENGTH_SHORT).show()
                        }
                        is ActionState.Info -> {
                            tvmsg.append("${it.msg}\r\n")
                        }
                    }
                }
            }
        }
    }


}