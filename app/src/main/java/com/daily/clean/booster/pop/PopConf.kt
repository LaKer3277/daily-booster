package com.daily.clean.booster.pop

import com.daily.clean.booster.entity.DaiBooPopItemBean

data class PopConf(
    val up_pop: Int = 0,                    //是否开启体外弹窗0表示不弹窗，1表示弹窗；
    val booster_avti: Int = 1,              //是否展示activity弹窗，0表示不展示，1表示展示，默认值1；
    val ref_fer: Int = 1,                   //是否触发展示通知/弹窗，0表示所有用户都展示，1表示仅买量用户展示；2表示仅FB买量用户展示，默认值1；
    val booster_time: DaiBooPopItemBean,    //定时弹窗
    val booster_unl: DaiBooPopItemBean,     //解锁屏幕弹窗
    val booster_uni: DaiBooPopItemBean,     //应用卸载弹窗
    val booster_cha: DaiBooPopItemBean,     //充电弹窗
    val booster_bat: DaiBooPopItemBean,     //电量弹窗
)