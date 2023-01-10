package com.daily.clean.booster.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.daily.clean.booster.R
import com.daily.clean.booster.databinding.LayBtnCleanBinding

class CleanBtnLayout : FrameLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initTypeValue(context, attrs)
    }

    var name: String? = null
    var icon: Int? = null
    private val binding = LayBtnCleanBinding.inflate(LayoutInflater.from(context), this, true)

    private fun initTypeValue(
        context: Context,
        attrs: AttributeSet?
    ) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CleanItemLayout)
        name = typedArray.getString(R.styleable.CleanItemLayout_btn_name)
        icon = typedArray.getResourceId(R.styleable.CleanItemLayout_btn_icon, R.mipmap.ic_home_clean)

        typedArray.recycle()

        name?.let { binding.itemName.text = it }
        icon?.let { binding.itemIcon.setImageResource(it) }
    }






}