package pers.vaccae.mvidemo.repository

import pers.vaccae.mvidemo.bean.CDrugs
import pers.vaccae.mvidemo.ui.intent.ActionState

/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间： 13:50
 * 功能模块说明：
 */
class DrugsRepository {

    //生成数据
    fun createDrugs(): MutableList<CDrugs> {
        val drugslist = mutableListOf<CDrugs>()
        var ckcode = 1000
        for (i in 1..9) {
            val item = CDrugs()
            if (i % 3 == 0) {
                item.drugs_ckcode = ckcode + 3
                ckcode += 1000
            } else {
                item.drugs_ckcode = ckcode + i % 3
            }
            item.drugs_code = "00000$i"
            item.drugs_name = "测试药品$i"
            item.drugs_specs = "50ml"
            item.qty = i

            drugslist.add(item)
        }

        return drugslist
    }

    //获取新的药品数据
    fun getNewDrugs(): CDrugs {
        val randoms = (100..1000).random()
        var newDrugs = CDrugs()
        newDrugs.drugs_code = randoms.toString().padStart(6, '0')
        newDrugs.drugs_name = "测试药品$randoms"
        newDrugs.drugs_specs = "50ml"
        newDrugs.qty = randoms % 10

        return newDrugs
    }
}