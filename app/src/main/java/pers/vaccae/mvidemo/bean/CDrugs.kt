package pers.vaccae.mvidemo.bean

import android.os.Parcel
import android.os.Parcelable

/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间： 14:26
 * 功能模块说明：
 */
class CDrugs() : Parcelable {
    //货格号
    var drugs_ckcode = 0
    //药品编码
    var drugs_code = ""
    //药品名称
    var drugs_name = ""
    //药品规格
    var drugs_specs = ""
    //药品数量
    var qty = 0;

    constructor(parcel: Parcel) : this() {
        drugs_ckcode = parcel.readInt()
        drugs_code = parcel.readString()!!
        drugs_name = parcel.readString()!!
        drugs_specs = parcel.readString()!!
        qty = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(drugs_ckcode)
        parcel.writeString(drugs_code)
        parcel.writeString(drugs_name)
        parcel.writeString(drugs_specs)
        parcel.writeInt(qty)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CDrugs> {
        override fun createFromParcel(parcel: Parcel): CDrugs {
            return CDrugs(parcel)
        }

        override fun newArray(size: Int): Array<CDrugs?> {
            return arrayOfNulls(size)
        }
    }

}