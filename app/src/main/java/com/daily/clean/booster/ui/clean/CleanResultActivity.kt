package com.daily.clean.booster.ui.clean

import android.annotation.SuppressLint
import android.graphics.Color
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
import com.daily.clean.booster.base.*
import com.daily.clean.booster.base.FirebaseEvent
import com.daily.clean.booster.core.CleanData
import com.daily.clean.booster.databinding.ActivityResultBinding
import com.daily.clean.booster.entity.DaiBooUIItem
import com.daily.clean.booster.ext.*
import com.daily.clean.booster.pop.*
import com.daily.clean.booster.utils.*

class CleanResultActivity : BaseActivity<ActivityResultBinding>() {

    override fun dailyBinding(): ActivityResultBinding {
        return ActivityResultBinding.inflate(layoutInflater)
    }

    override fun statusColor(): Int {
        return Color.parseColor("#3126F5")
    }

    override fun statusTxtColorDark(): Boolean {
        return false
    }

    private var extraStr = "0B"
    private var workId = NotyWorkBooster
    private var isFirst = false

    override fun dailyData() {
        extraStr = intent.getStringExtra(DB_KEY_CLEAN_SIZE) ?: "0B"
        workId = intent.getStringExtra(Noty_KEY_WORK) ?: NotyWorkBooster
        isFirst = intent.getBooleanExtra(DB_KEY_IS_FIRST, false)

        binding.titleText.text = workId.getTitleText()
        binding.titleBack.setOnClickListener {
            onBackPressed()
        }
        initList()

        binding.tvResultInfo.text = when (workId) {
            NotyWorkClean -> {
                if ("0B" != extraStr) {
                    getString(R.string.des_complete_clean, extraStr)
                } else {
                    getString(R.string.des_complete_clean2)
                }
            }
            NotyWorkCpu -> getString(R.string.des_complete_cpu)
            NotyWorkBattery -> getString(R.string.des_complete_battery, "${(3..15).random()}%")
            else -> getString(R.string.des_complete_boot)
        }
    }

    override fun dailyLoad() {
        log()
        loadNavAd()
    }

    private fun loadNavAd() {
        binding.resultNatAdLayout.visibility = View.VISIBLE
        FirebaseEvent.adChance(AdPos.NavResult)
        AdsLoader.loadAd(this, AdPos.NavResult, object :AdsListener() {
            override fun onLoaded(ad: BaseAd) {
                if (isActivityPaused) {
                    AdsLoader.add2Cache(AdPos.NavResult, ad)
                    return
                }
                if (ad !is BaseNav) return
                ad.show(this@CleanResultActivity, binding.resultNatAdLayout)
            }

            override fun onShown() {
                FirebaseEvent.adImpression(AdPos.NavResult)
            }
        })
    }

    private fun log() {
        if (DB_ACTION_FROM_POP_NOTY == intent?.action) {
            FirebaseEvent.logEvent("up_all_relust")
        }
        if (isFirst) {
            FirebaseEvent.logEvent("start_first_result")
        }else{
            FirebaseEvent.pageResultShow(workId)
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
                        NotyWorkClean -> {
                            des.text = getString(R.string.des_recom_clean, DaiBooRAMUtils.getUsedMemoryString())
                            btn.text = getString(R.string.clean_now_up)
                        }
                        NotyWorkBooster -> {
                            des.text = getString(
                                R.string.des_recom_boot,
                                DaiBooRAMUtils.getUsedMemoryStringPer()
                            )
                            btn.text = getString(R.string.boost_up)
                        }
                        NotyWorkCpu -> {
                            btn.text = getString(R.string.cool_down_up)
                            des.text = getString(R.string.des_recom_cpu)
                        }
                        NotyWorkBattery -> {
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
                NotyWorkClean -> {
                    clickJunkBtn()
                }
                NotyWorkBooster -> {
                    goBoosting(NotyWorkBooster)
                    finish()
                }
                NotyWorkCpu -> {
                    goBoosting(NotyWorkCpu)
                    finish()
                }
                NotyWorkBattery -> {
                    goBoosting(NotyWorkBattery)
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
            FirebaseEvent.logEvent("start_first_return")
        }else{
            FirebaseEvent.pageReturnClick(workId)
        }
        goMain()
        finish()
    }


}