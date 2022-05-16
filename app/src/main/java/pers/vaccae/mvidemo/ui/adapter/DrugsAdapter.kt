package pers.vaccae.mvidemo.ui.adapter

import android.content.ClipData
import android.content.Intent
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import pers.vaccae.mvidemo.R
import pers.vaccae.mvidemo.bean.CDrugs

/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间： 15:08
 * 功能模块说明：
 */
class DrugsAdapter(layoutResId: Int, data: MutableList<CDrugs>?) :
    BaseQuickAdapter<CDrugs, BaseViewHolder>(layoutResId, data) {


    override fun convert(holder: BaseViewHolder, item: CDrugs) {
        holder.setText(R.id.rcl_drugs_ckcode, item.drugs_ckcode.toString())
        holder.setText(R.id.rcl_drugs_code, item.drugs_code)
        holder.setText(R.id.rcl_drugs_name, item.drugs_name)
        holder.setText(R.id.rcl_drugs_specs, item.drugs_specs)
        holder.setText(R.id.rcl_qty, item.qty.toString())
    }
}