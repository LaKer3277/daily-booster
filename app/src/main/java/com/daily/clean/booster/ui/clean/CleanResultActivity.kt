package com.daily.clean.booster.ui.clean

import android.annotation.SuppressLint
import android.graphics.Rect
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.daily.clean.booster.R
import com.daily.clean.booster.ads.AdsListener
import com.daily.clean.booster.ads.AdsLoader
import com.daily.clean.booster.ads.conf.AdPos
import com.daily.clean.booster.ads.model.BaseAd
import com.daily.clean.booster.ads.model.BaseNav
import com.daily.clean.booster.base.BaseActivity
import com.daily.clean.booster.base.DBConfig
import com.daily.clean.booster.base.FiBLogEvent
import com.daily.clean.booster.core.clean.CleanData
import com.daily.clean.booster.databinding.ActivityResultBinding
import com.daily.clean.booster.entity.DaiBooUIItem
import com.daily.clean.booster.utils.*

class CleanResultActivity : BaseActivity<ActivityResultBinding>() {

    override fun dailyBinding(): ActivityResultBinding {
        return ActivityResultBinding.inflate(layoutInflater)
    }

    private var extraStr = "0B"
    private var workId = DBConfig.DAIBOO_WORK_ID_BOOSTER
    private var isFirst = false
    override fun dailyData() {
        extraStr = intent.getStringExtra(DBConfig.DAIBOO_KEY_CLEAN_SIZE) ?: "0B"
        workId = intent.getStringExtra(DBConfig.DAIBOO_KEY_WORK_ID) ?: DBConfig.DAIBOO_WORK_ID_BOOSTER
        isFirst = intent.getBooleanExtra(DBConfig.DAIBOO_KEY_IS_FIRST, false)
        binding.titleText.text = workId.getTitleText()
        binding.titleBack.setOnClickListener {
            onBackPressed()
        }
        initList()

        binding.tvResultInfo.text = when (workId) {
            DBConfig.DAIBOO_WORK_ID_CLEAN -> {
                if ("0B" != extraStr) {
                    getString(R.string.des_complete_clean, extraStr)
                } else {
                    getString(R.string.des_complete_clean2)
                }
            }
            DBConfig.DAIBOO_WORK_ID_ClEAN_NOTIFICATION -> getString(R.string.des_complete_notification, extraStr)
            DBConfig.DAIBOO_WORK_ID_CPU -> getString(R.string.des_complete_cpu)
            DBConfig.DAIBOO_WORK_ID_BATTERY -> getString(R.string.des_complete_battery, "${(3..15).random()}%")
            else -> getString(R.string.des_complete_boot)
        }
    }

    override fun dailyLoad() {
        log()
        loadNavAd()
    }

    private fun loadNavAd() {
        binding.resultNatAdLayout.visibility = View.VISIBLE
        AdsLoader.loadAd(this, AdPos.NavResult, object :AdsListener() {
            override fun onLoaded(ad: BaseAd) {
                if (isActivityPaused) {
                    AdsLoader.add2Cache(AdPos.NavResult, ad)
                    return
                }
                if (ad !is BaseNav) return
                ad.show(this@CleanResultActivity, binding.resultNatAdLayout)
            }
        })
    }

    private fun log() {
        if (DBConfig.DAIBOO_ACTION_FROM_POP_NOTY_POP == intent?.action) {
            FiBLogEvent.up_all_relust()
        }
        if (isFirst) {
            FiBLogEvent.start_first_result()
        }else{
            FiBLogEvent.page_result_show(workId)
        }
    }


    private fun initList() {
        val list = mutableListOf<DaiBooUIItem>()
        list.addAll(DaiBooUIItem.Items.list)
        val item = list.singleOrNull { it.id == workId }
        list.remove(item)

        val mAdapter =
            object :
                BaseQuickAdapter<DaiBooUIItem, BaseViewHolder>(R.layout.lay_result_rcom_item, list) {

                @SuppressLint("SetTextI18n", "StringFormatInvalid")
                override fun convert(holder: BaseViewHolder, item: DaiBooUIItem) {
                    //val root = holder.getView<LinearLayout>(R.id.item_root)
                    val name = holder.getView<TextView>(R.id.tv_item_name)
                    val des = holder.getView<TextView>(R.id.tv_item_info)
                    val icon = holder.getView<ImageView>(R.id.iv_item_icon)
                    val btn = holder.getView<TextView>(R.id.tv_item_tv)
                    name.text = item.name
                    icon.setImageResource(item.icon_recom)

                    when (item.id) {
                        DBConfig.DAIBOO_WORK_ID_CLEAN -> {
                            des.text = getString(R.string.des_recom_clean, DaiBooRAMUtils.getUsedMemoryString())
                            btn.text = getString(R.string.clean_now_up)
                        }
                        DBConfig.DAIBOO_WORK_ID_BOOSTER -> {
                            des.text = getString(
                                R.string.des_recom_boot,
                                "${DaiBooRAMUtils.getUsedMemoryStringPer()}"
                            )
                            btn.text = getString(R.string.boost_up)
                        }
                        DBConfig.DAIBOO_WORK_ID_CPU -> {
                            btn.text = getString(R.string.cool_down_up)
                            des.text = getString(R.string.des_recom_cpu)
                        }
                        DBConfig.DAIBOO_WORK_ID_BATTERY -> {
                            btn.text = getString(R.string.optimize_up)
                            des.text =
                                (R.string.des_recom_battery.getString("${(3..CleanData.getAppSize()).random()}"))
                        }
                    }
                }
            }

        binding.recycler.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView) {
                outRect.bottom = 0.dip
            }
        })
        binding.recycler.adapter = mAdapter
        mAdapter.setOnItemClickListener { adapter, view, position ->
            when (list[position].id) {
                DBConfig.DAIBOO_WORK_ID_CLEAN -> {
                    clickJunkBtn()
                }
                DBConfig.DAIBOO_WORK_ID_BOOSTER -> {
                    goBoosting(DBConfig.DAIBOO_WORK_ID_BOOSTER)
                    finish()
                }
                DBConfig.DAIBOO_WORK_ID_CPU -> {
                    goBoosting(DBConfig.DAIBOO_WORK_ID_CPU)
                    finish()
                }
                DBConfig.DAIBOO_WORK_ID_BATTERY -> {
                    goBoosting(DBConfig.DAIBOO_WORK_ID_BATTERY)
                    finish()
                }
            }
        }
    }

    private fun clickJunkBtn() {
        checkStoragePermission({
            goJunkCleanScanning()
            finish()
        }, {
            if (it) {
                toast(R.string.access_storage_prompt.getString())
            }
        })
    }

    override fun onBackPressed() {
        if (isFirst) {
            FiBLogEvent.start_first_return()
        }else{
            FiBLogEvent.page_return_click(workId)
        }
        goMain()
        finish()
    }


}