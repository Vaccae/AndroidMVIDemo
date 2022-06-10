package pers.vaccae.mvidemo.ui.view

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import pers.vaccae.mvidemo.R
import pers.vaccae.mvidemo.ui.intent.ActionIntent
import pers.vaccae.mvidemo.ui.intent.ActionState
import pers.vaccae.mvidemo.ui.viewmodel.MainViewModel

class WindowInfoTrackerActivity : AppCompatActivity() {
    private val TAG = "X Fold"
    private val imgv :ImageView by lazy { findViewById(R.id.imv_view) }
    private val tvmsg : TextView by lazy { findViewById(R.id.tv_imgstatus) }

    private lateinit var mainViewModel: MainViewModel

    private lateinit var windowInfoTracker : WindowInfoTracker
    private lateinit var windowLayoutInfoFlow : Flow<WindowLayoutInfo>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_windowinfotracker)

        mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        observeViewModel()

        windowInfoTracker = WindowInfoTracker.getOrCreate(this@WindowInfoTrackerActivity)
        windowLayoutInfoFlow = windowInfoTracker.windowLayoutInfo(this@WindowInfoTrackerActivity)
        observeFold()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.state.collect {
                    when (it) {
                        is ActionState.Info -> {
                            setControlShow(it.msg)
                        }
                        else -> {
                            setControlShow("主屏")
                        }
                    }
                }
            }
        }
    }

    private fun setControlShow(msg: String?) {
        msg?.let {
            tvmsg.text = it
            when(it){
                "横向半开" -> imgv.setImageDrawable(resources.getDrawable(R.drawable.tabletop))
                "竖向半开" -> imgv.setImageDrawable(resources.getDrawable(R.drawable.book))
                "横向全展开" -> imgv.setImageDrawable(resources.getDrawable(R.drawable.flat))
                "竖向全展开" -> imgv.setImageDrawable(resources.getDrawable(R.drawable.flat))
                else -> imgv.setImageDrawable(resources.getDrawable(R.drawable.phone))
            }
        }

    }


    private fun observeFold() {
        lifecycleScope.launch(Dispatchers.Main) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                windowLayoutInfoFlow.collect { layoutInfo ->
                    Log.i(TAG, "size:${layoutInfo.displayFeatures.size}")
                    val foldingFeature = layoutInfo.displayFeatures
                        .filterIsInstance<FoldingFeature>()
                        .firstOrNull()
                    foldingFeature?.let {
                        Log.i(TAG, "state:${it.state}")
                    }
                    when {
                        isTableTopPosture(foldingFeature) ->
                            sendInfoMsg("横向半开")
                        isBookPosture(foldingFeature) ->
                            sendInfoMsg("竖向半开")
                        isSeparating(foldingFeature) ->
                            // Dual-screen device
                            foldingFeature?.let {
                                if (it.orientation == FoldingFeature.Orientation.HORIZONTAL) {
                                    sendInfoMsg("横向全展开")
                                } else {
                                    sendInfoMsg("竖向全展开")
                                }
                            }
                        else -> {
                            sendInfoMsg("主屏")
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

    fun sendInfoMsg(msg :String){
        lifecycleScope.launch {
            Log.i(TAG, msg)
            mainViewModel.actionIntent.send(ActionIntent.Info(msg))
        }
    }
}