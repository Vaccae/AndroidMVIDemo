package pers.vaccae.mvidemo.ui.view

import android.content.res.Configuration
import android.graphics.drawable.ClipDrawable.HORIZONTAL
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowInfoTrackerDecorator
import androidx.window.layout.WindowLayoutInfo

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.launch
import pers.vaccae.mvidemo.R
import pers.vaccae.mvidemo.bean.CDrugs
import pers.vaccae.mvidemo.ui.adapter.DrugsAdapter
import pers.vaccae.mvidemo.ui.intent.ActionIntent
import pers.vaccae.mvidemo.ui.intent.ActionState
import pers.vaccae.mvidemo.ui.viewmodel.MainViewModel

class FoldActivity : AppCompatActivity() {
    private val TAG = "X Fold"

    private val recyclerView: RecyclerView by lazy { findViewById(R.id.recycler_view) }
    private val btncreate: Button by lazy { findViewById(R.id.btncreate) }
    private val btnadd: Button by lazy { findViewById(R.id.btnadd) }
    private val btndel: Button by lazy { findViewById(R.id.btndel) }

    private lateinit var mainViewModel: MainViewModel
    private lateinit var drugsAdapter: DrugsAdapter

    //adapter的位置
    private var adapterpos = -1

    private lateinit var windowInfoTracker :WindowInfoTracker
    private lateinit var windowLayoutInfoFlow : Flow<WindowLayoutInfo>

    private val splitLayout: SplitLayout by lazy { findViewById(R.id.split_layout) }
    private val motionLayout :MotionLayout by lazy { findViewById(R.id.endLayout) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fold)

        windowInfoTracker = WindowInfoTracker.getOrCreate(this@FoldActivity)
        windowLayoutInfoFlow = windowInfoTracker.windowLayoutInfo(this@FoldActivity)
        observeFold()

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
            Log.i(TAG, "create")
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

    private fun observeFold() {
        lifecycleScope.launch(Dispatchers.Main) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                windowLayoutInfoFlow.collect { layoutInfo ->
                        Log.i(TAG, "size:${layoutInfo.displayFeatures.size}")
                        splitLayout.updateWindowLayout(layoutInfo)
                        // New posture information
                        val foldingFeature = layoutInfo.displayFeatures
                            .filterIsInstance<FoldingFeature>()
                            .firstOrNull()
                        foldingFeature?.let {
                            Log.i(TAG, "state:${it.state}")
                        }
                        when {
                            isTableTopPosture(foldingFeature) ->
                                Log.i(TAG, "TableTopPosture")
                            isBookPosture(foldingFeature) ->
                                Log.i(TAG, "BookPosture")
                            isSeparating(foldingFeature) ->
                                // Dual-screen device
                                foldingFeature?.let {
                                    if (it.orientation == FoldingFeature.Orientation.HORIZONTAL) {
                                        Log.i(TAG, "Separating HORIZONTAL")
                                    } else {
                                        Log.i(TAG, "Separating VERTICAL")
                                        motionLayout.transitionToEnd()
                                    }
                                }
                            else -> {
                                Log.i(TAG, "NormalMode")
                                motionLayout.transitionToStart()
                            }
                        }
                    }
            }
        }
    }

    fun isTableTopPosture(foldFeature: FoldingFeature?): Boolean {
        return foldFeature?.state == FoldingFeature.State.HALF_OPENED &&
                foldFeature.orientation == FoldingFeature.Orientation.HORIZONTAL
    }

    fun isBookPosture(foldFeature: FoldingFeature?): Boolean {
        return foldFeature?.state == FoldingFeature.State.HALF_OPENED &&
                foldFeature.orientation == FoldingFeature.Orientation.VERTICAL
    }

    fun isSeparating(foldFeature: FoldingFeature?): Boolean {
        return foldFeature?.state == FoldingFeature.State.FLAT && foldFeature.isSeparating
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.i(TAG, "configurationchanged")
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
                        is ActionState.Error-> {
                            Toast.makeText(this@FoldActivity, it.msg, Toast.LENGTH_SHORT).show()
                        }
                        is ActionState.Info ->{
                            Toast.makeText(this@FoldActivity, it.msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}