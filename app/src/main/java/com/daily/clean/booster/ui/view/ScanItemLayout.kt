package com.daily.clean.booster.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import com.daily.clean.booster.R
import com.daily.clean.booster.databinding.ItemScanBinding
import com.lzp.dslanimator.PlayMode
import com.lzp.dslanimator.animSet

class ScanItemLayout : FrameLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initTypeValue(context, attrs)
    }


    var name: String? = null
    var size: String? = null
    var icon: Int? = null
    var icon_load: Int? = null
    private val binding = ItemScanBinding.inflate(LayoutInflater.from(context), this, true)

    private fun initTypeValue(
        context: Context,
        attrs: AttributeSet?
    ) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScanItemLayout)
        name = typedArray.getString(R.styleable.ScanItemLayout_item_name)
        size = typedArray.getString(R.styleable.ScanItemLayout_item_size)
        icon = typedArray.getResourceId(R.styleable.ScanItemLayout_item_icon, R.mipmap.ic_clean_log_files)
        icon_load = typedArray.getResourceId(
            R.styleable.ScanItemLayout_item_icon_end,
            R.mipmap.ic_loading
        )
        typedArray.recycle()

        name?.let { binding.itemName.text = it }
        icon?.let { binding.itemIcon.setImageResource(it) }
        icon_load?.let { binding.itemIconEnd.setImageResource(it) }
        size?.let { binding.itemDes.text = it }


    }


    fun setLoadImg(res:Int){
        binding.itemIconEnd.setImageResource(res)
    }
    fun startLoad() {
        animLoading.start()
    }

    fun loadComplete() {
        animLoading.cancel()
        binding.itemIconEnd.rotation = 0f
        binding.itemIconEnd.setImageResource(R.mipmap.ic_choice_yes)
    }


    fun setSizeDes(s: String?) {
        s?.let { binding.itemDes.text = it }
    }


    private val animLoading by lazy {
        animSet {
            rotationAnim {
                target = binding.itemIconEnd
                values = floatArrayOf(0f, 360f)
                repeatCount = -1
            }
            duration = 1000
            interpolator = LinearInterpolator()
            playMode = PlayMode.TOGETHER
            onEnd {
            }
        }

    }


}