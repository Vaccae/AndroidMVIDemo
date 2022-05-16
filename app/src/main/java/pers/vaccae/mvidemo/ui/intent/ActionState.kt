package pers.vaccae.mvidemo.ui.intent

import pers.vaccae.mvidemo.bean.CDrugs

/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间： 11:07
 * 功能模块说明：ViewModel状态State
 */
sealed class ActionState {

    object Normal : ActionState()
    object Loading : ActionState()

    data class Drugs(val drugs: MutableList<CDrugs>) : ActionState()

    data class Error(val msg: String?) : ActionState()
}
