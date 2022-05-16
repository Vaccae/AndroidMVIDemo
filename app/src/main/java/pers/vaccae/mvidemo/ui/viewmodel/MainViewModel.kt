package pers.vaccae.mvidemo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pers.vaccae.mvidemo.bean.CDrugs
import pers.vaccae.mvidemo.repository.DrugsRepository
import pers.vaccae.mvidemo.ui.intent.ActionIntent
import pers.vaccae.mvidemo.ui.intent.ActionState
import java.lang.Exception

/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间： 13:42
 * 功能模块说明：
 */
class MainViewModel : ViewModel() {

    private val _respository = DrugsRepository()

    val actionIntent = Channel<ActionIntent>(Channel.UNLIMITED)
    private val _actionstate = MutableSharedFlow<ActionState>()
    val state: SharedFlow<ActionState>
        get() = _actionstate

    var listDrugs = mutableListOf<CDrugs>()

    init {
        initActionIntent()
        _actionstate.tryEmit(ActionState.Normal)
    }

    private fun initActionIntent() {
        viewModelScope.launch {
            actionIntent.consumeAsFlow().collect {
                when (it) {
                    is ActionIntent.LoadDrugs -> LoadDrugs()
                    is ActionIntent.InsDrugs -> InsDrugs()
                    is ActionIntent.DelDrugs -> {
                        DelDrugs(it.idx)
                    }
                }
            }
            actionIntent.consumeAsFlow()
        }
    }

    private fun DelDrugs(idx: Int) {
        viewModelScope.launch {
            if (idx < 0) {
                _actionstate.emit(ActionState.Error("未选中要删除的药品信息"))
                return@launch
            }
            //修改为加载状态
            _actionstate.emit(ActionState.Loading)
            //开始加载数据
            _actionstate.emit(
                try {
                    listDrugs.removeAt(idx)
                    ActionState.Drugs(listDrugs)
                } catch (e: Exception) {
                    ActionState.Error(e.message.toString())
                }
            )
            //恢复状态
            _actionstate.emit(ActionState.Normal)
        }
    }

    private fun InsDrugs() {
        viewModelScope.launch {
            //修改为加载状态
            _actionstate.emit(ActionState.Loading)
            //开始加载数据
            _actionstate.emit(
                try {
                    listDrugs.add(_respository.getNewDrugs())
                    ActionState.Drugs(listDrugs)
                } catch (e: Exception) {
                    ActionState.Error(e.message.toString())
                }
            )
            //恢复状态
            _actionstate.emit(ActionState.Normal)
        }
    }

    //加载药品信息
    private fun LoadDrugs() {
        viewModelScope.launch {
            //修改为读取状态
            _actionstate.emit(ActionState.Loading)
            //开始加载数据
            _actionstate.emit(
                try {
                    listDrugs = _respository.createDrugs()
                    ActionState.Drugs(listDrugs)
                } catch (e: Exception) {
                    ActionState.Error(e.message.toString())
                }
            )
            //恢复状态
            _actionstate.emit(ActionState.Normal)
        }
    }
}