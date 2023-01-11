package com.daily.clean.booster.entity

import android.app.PendingIntent
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.text.format.Formatter
import com.blankj.utilcode.util.TimeUtils
import com.daily.clean.booster.R
import com.daily.clean.booster.base.DBConfig
import com.daily.clean.booster.utils.getString
import kotlinx.android.parcel.Parcelize
import java.util.*


data class DaiBooIpBean(
    val ip: String = "",
    val country: String = "",
    val cc: String = ""
)


data class DaiBooAdAllBean(
    val db_Open: MutableList<AdConf>,
    val result_NV: MutableList<AdConf>,
    val clean_IV: MutableList<AdConf>,
)

data class AdConf(
    val Network: String,//平台 Admob 和 Max
    val Hierarchy: Int,//优先级
    val Form: String,//类型
    val Id: String
) {
    fun getFormat() = when (Form) {
        "interstitial" -> "interstitial"
        "inter" -> "interstitial"
        "open" -> "open"
        "native" -> "native"
        else -> Form
    }

    fun isOpenOrInter() = when (getFormat()) {
        "interstitial" -> true
        "open" -> true
        else -> false
    }

    fun isNative() = Form == "native"


    fun isMax() = "Max".equals(Network, true)
}

data class DaiBooAdEvent(
    val daiboo_key: String,
    val daiboo_ad_network: String = "admob",
    var daiboo_impression: Int = 0,
    var daiboo_load_on: Int = 0,
    val daiboo_ad_item: AdConf,
    var daiboo_value_micros: Long = 0L,
    var daiboo_precision_type: String = ""
)

data class DaiBooLogEvent(
    val daiboo_log_name: String,
    val daiboo_log_param: Bundle?
)


interface CleanTypeGroup {
    val groupType: Int
}

class DaiBooCleanDatas(
    var appCache: HashMap<String, DaiBooCleanChildBean> = hashMapOf(),
    var apkFiles: MutableList<DaiBooCleanChildBean> = mutableListOf(),
    var appResidual: MutableList<DaiBooCleanChildBean> = mutableListOf(),
    var logFiles: MutableList<DaiBooCleanChildBean> = mutableListOf(),
    var tempFiles: MutableList<DaiBooCleanChildBean> = mutableListOf(),
    var otherFiles: DaiBooCleanChildBean = DaiBooCleanChildBean(name = "Other Junks"),
)

class DaiBooCleanChildBean(
    var name: String,
    var pathList: MutableList<String> = mutableListOf(),
    var icon: Drawable? = null,
    var size: Long = 0L,
    var isSelected: Boolean = true,
    var visible: Boolean = false,
    var parentIndex: Int = 0,
    var isEnd: Boolean = false,
    override val groupType: Int = 1
) : CleanTypeGroup

class DaiBooCleanParentBean(
    var name: String,
    var cleanType: CleanType,
    var childDaiBooCleans: MutableList<DaiBooCleanChildBean> = mutableListOf(),
    var isExpanded: Boolean = false,
    var isSelected: Boolean = true,
    var fileSize: Long = 0L,
    override val groupType: Int = 0,
) : CleanTypeGroup {
    fun getSelectedSize(): Long {
//        if (CleanType.RAM_USED == cleanType) return fileSize
        var totalSize = 0L
        childDaiBooCleans.forEach { cacheChild ->
            if (cacheChild.isSelected) totalSize += cacheChild.size
        }
        totalSize += fileSize
        return totalSize
    }


    fun getSelectedSizeFormat(context: Context): String {
        return try {
            Formatter.formatFileSize(context, getSelectedSize()).uppercase()
        } catch (e: Exception) {
            "0 B"
        }
    }

    fun isChecked(): Boolean {
        if (childDaiBooCleans.isEmpty()) {
            return isSelected
        } else
            childDaiBooCleans.forEach { cacheChild ->
                if (cacheChild.isSelected.not()) return false
            }
        return true
    }

    fun getIcon(): Int {
        return when (cleanType) {
            CleanType.RAM_USED -> R.mipmap.ic_clean_ram_used
            CleanType.APP_CACHE -> R.mipmap.ic_clean_app_cache
            CleanType.APK_FILES -> R.mipmap.ic_clean_apk_files
            CleanType.APP_RESIDUAL -> R.mipmap.ic_clean_app_residual
            CleanType.LOG_FILES -> R.mipmap.ic_clean_log_files
            CleanType.TEMP_FILES -> R.mipmap.ic_clean_temp_files
            CleanType.AD_JUNK -> R.mipmap.ic_clean_ad_junk
            else -> R.mipmap.ic_clean_log_files
        }
    }

}

enum class CleanType(val code: String) {
    RAM_USED("ram_used"),
    APP_CACHE("app_cache"),
    APK_FILES("apk_files"),
    APP_RESIDUAL("app_residual"),
    LOG_FILES("log_files"),
    TEMP_FILES("temp_files"),
    AD_JUNK("ad_junk"),
}


class DaiBooCleanEvent(
    var isCleanRAM: Boolean = true,
)


/**
 * 弹窗
 */
data class DaiBooPopBean(
    val up_pop: Int = 0,//是否开启体外弹窗0表示不弹窗，1表示弹窗；
    val booster_avti: Int = 1,//是否展示activity弹窗，0表示不展示，1表示展示，默认值1；
    val ref_fer: Int = 1,//是否触发展示通知/弹窗，0表示所有用户都展示，1表示仅买量用户展示；2表示仅FB买量用户展示，默认值1；
    val booster_time: DaiBooPopItemBean,//定时弹窗
    val booster_unl: DaiBooPopItemBean,//解锁屏幕弹窗
    val booster_uni: DaiBooPopItemBean,//应用卸载弹窗
    val booster_cha: DaiBooPopItemBean,//充电弹窗
    val booster_bat: DaiBooPopItemBean,//电量弹窗
)


data class DaiBooPopItemBean(
    var first: Int = 0,////首次安装间隔多少小时后再展示弹窗，单位小时，0表示默认开始
    var up: Int = 0,//当天展示次数上限，超过不展示，0无上限
    var int: Int = 0, //间隔时间 单位分钟
    var siz: Int = 2, //通知高度尺寸，0=64dp，1=160dp，2=256dp;默认值2
    var acti_pos: Int = 1, //activity弹窗位置，0在顶部，1在中间，2在底部，默认值1
)

data class DaiBooOpenControlBean(
    //0 第一次启动保持原有逻辑不改动，进入展示启动页和开屏广告；
    //1，第一次启动进入启动页快速启动，不展示开屏广告，且第一次boost流程不展示插屏广告；
    var first: Int = 0,
    //0，保持原有逻辑不改动，进入展示启动页和开屏广告；
    //1，热启动的情况下，从通知进入应用，不展示启动页和开屏广告，直接展示对应功能界面；
    var notify: Int = 0,
)

@Parcelize
data class DaiBooPopShowItem(
    var time: Long = Date().time,
    var counts: Int = 1
) : Parcelable


data class DaiBooNMBean(
    val pkg: String,
    val title: String,
    val content: String,
    val time: Long,
    val pin: PendingIntent?
) {
    fun getCreateTime(): String {
        return TimeUtils.millis2String(time, "HH:mm")
    }
}

data class AppInfo(
    val appName: String,
    val pkgName: String,
    val icon: Drawable,
    val isSystem: Boolean = false,
    var isNotDnd: Boolean = false,
)

data class EventNM(
    val nmb: DaiBooNMBean
)


sealed class DaiBooUIItem(
    val id: String,
    val name: String,
    val icon_home: Int,
    val icon_recom: Int,
) {

    object Clean :
        DaiBooUIItem(
            DBConfig.DAIBOO_WORK_ID_CLEAN,
            R.string.clean.getString(),
            R.mipmap.ic_home_clean,
            R.mipmap.ic_result_junk,

            )

    object Boost : DaiBooUIItem(
        DBConfig.DAIBOO_WORK_ID_BOOSTER,
        R.string.booster.getString(),
        R.mipmap.ic_home_boost,
        R.mipmap.ic_result_boost,
    )

    object CPU : DaiBooUIItem(
        DBConfig.DAIBOO_WORK_ID_CPU,
        R.string.cpu.getString(),
        R.mipmap.ic_home_cpu,
        R.mipmap.ic_result_cpu,
    )

    object Battery : DaiBooUIItem(
        DBConfig.DAIBOO_WORK_ID_BATTERY,
        R.string.battery.getString(),
        R.mipmap.ic_home_battery,
        R.mipmap.ic_result_battery,
    )


    object Items {
        val list = mutableListOf(Boost, Clean, CPU, Battery)

        val listPop = mutableListOf(Boost, CPU, Battery, Clean)

        fun resetListPop() {
            listPop.clear()
            listPop.add(Boost)
            listPop.add(CPU)
            listPop.add(Battery)
            listPop.add(Clean)
        }

        fun getPopList(): MutableList<DaiBooUIItem> {
            if (listPop.isEmpty()) {
                resetListPop()
            }
            return listPop
        }
    }

}

