package pers.vaccae.mvidemo.ui.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import pers.vaccae.mvidemo.R
import pers.vaccae.mvidemo.bean.CDrugs
import pers.vaccae.mvidemo.ui.adapter.DrugsAdapter
import pers.vaccae.mvidemo.ui.intent.ActionIntent
import pers.vaccae.mvidemo.ui.intent.ActionState
import pers.vaccae.mvidemo.ui.viewmodel.MainViewModel

class MainActivity : AppCompatActivity() {

    private val recyclerView: RecyclerView by lazy { findViewById(R.id.recycler_view) }
    private val btncreate: Button by lazy { findViewById(R.id.btncreate) }
    private val btnadd: Button by lazy { findViewById(R.id.btnadd) }
    private val btndel: Button by lazy { findViewById(R.id.btndel) }

    private lateinit var mainViewModel: MainViewModel
    private lateinit var drugsAdapter: DrugsAdapter

    //adapter的位置
    private var adapterpos = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                    }
                }
            }
        }
    }
}