package pers.vaccae.mvidemo.ui.intent

import pers.vaccae.mvidemo.bean.CDrugs

/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间： 11:00
 * 功能模块说明：用户操作Intent
 */
sealed class ActionIntent {

    //加载药品列表
    object LoadDrugs : ActionIntent()

    //添加药品信息
    object InsDrugs : ActionIntent()

    //删除药品信息
    data class DelDrugs(val idx: Int, val item: CDrugs) : ActionIntent()
}
