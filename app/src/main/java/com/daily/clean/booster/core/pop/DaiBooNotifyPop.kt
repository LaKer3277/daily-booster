package com.daily.clean.booster.core.pop

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.daily.clean.booster.App
import com.daily.clean.booster.R
import com.daily.clean.booster.base.DBConfig
import com.daily.clean.booster.base.FiBLogEvent
import com.daily.clean.booster.base.FiBRemoteUtil
import com.daily.clean.booster.entity.DaiBooPopItemBean
import com.daily.clean.booster.ui.NotificationActivity
import com.daily.clean.booster.ui.SplashActivity
import com.daily.clean.booster.utils.DaiBooRAMUtils
import com.daily.clean.booster.utils.LogDB
import com.daily.clean.booster.utils.getString
import com.daily.clean.booster.utils.isSPlus

object DaiBooNotifyPop {

    private const val channelId = DBConfig.NOTIFY_CHANNEL_ID_POP
    private const val notificationId = DBConfig.NOTIFY_POP_ID

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                R.string.app_name.getString(),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Doc Manager Clean Pop-ups Notification"
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setSound(null, null)
                enableLights(false)
                enableVibration(false)
            }
            (App.ins.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )
        }

    }


    fun cancelAlertNotification() {
        NotificationManagerCompat.from(App.ins).cancel(notificationId)
    }


    @SuppressLint("WrongConstant")
    fun createNotificationAndPop(context: Context, workId: String, tanId: String): Notification {

        cancelAlertNotification()

        createNotificationChannel()

        val item: DaiBooPopItemBean? = PopCheckHelper.getPopItem(tanId)

        val nBuilder = NotificationCompat.Builder(App.ins, channelId)
            .setSmallIcon(R.drawable.ic_daily_booster_logo)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)//设置该通知优先级
            .setCategory(NotificationCompat.CATEGORY_CALL)////设置通知类别
            //提供一个PendingIntent，当用户直接从通知面板上清除通知时发送。
            // 例如，当用户点击 "清除所有 "按钮或通知上的单个 "X "按钮时，将发送此意图。
            // 当应用程序调用NotificationManager.cancel(int)时，这个意图不会被发送。
            .setContentIntent(btnClickPending(workId, tanId))
//            .setDeleteIntent(btnClickPending(workId, tanId))
            .setFullScreenIntent(btnClickPending_fsi(workId, tanId), true)
            .setAutoCancel(true)
            .setGroupSummary(false)
            .setSound(null)


        val size = item?.siz ?: 2
        if (isSPlus() || checkIsMIUI()) {
            //小
            nBuilder.setCustomContentView(getCustomView(workId, tanId, 0, true))
            nBuilder.setCustomHeadsUpContentView(getCustomView(workId, tanId, 0, true))
            //大
            nBuilder.setCustomBigContentView(getCustomView(workId, tanId, size, true))
            //自带标题，并且可以再通知栏里面折叠, Android 31，需要设置了才能显示全
            nBuilder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
        } else {
            nBuilder.setCustomContentView(getCustomView(workId, tanId, size))
            nBuilder.setCustomHeadsUpContentView(getCustomView(workId, tanId, size))
            //大
//            nBuilder.setCustomBigContentView(getCustomView(workId, tanId, 2))
        }

        //build
        val notification = nBuilder.build()
        //notify
        with(NotificationManagerCompat.from(App.ins)) {
            LogDB.dpop("-notify---$notificationId")
            //保存
            PopCheckHelper.saveLastPopTime(tanId)
            notify(notificationId, notification)
            FiBLogEvent.pop_log(tanId, 1)
        }
        return notification

    }


    private fun getCustomView(
        workID: String,
        tanID: String,
        size: Int,
        isHideTitle: Boolean = false
    ): RemoteViews {

        LogDB.dpop("getCustomView size = $size")

        val layoutResId = when (size) {
            0 -> R.layout.layout_notification_pop_small_40
            1 -> R.layout.layout_notification_big1
            2 -> {
                if (tanID == DBConfig.DAIBOO_NOTY_UNINSTALL)
                    R.layout.layout_notification_big2
                else
                    R.layout.layout_notification_big3
            }
            else -> R.layout.layout_notification_big1
        }

        val remoteViews = RemoteViews(App.ins.packageName, layoutResId).apply {

            if (isHideTitle) {
                setViewVisibility(R.id.ll_title, View.GONE)
            }

            setOnClickPendingIntent(R.id.btnWake, btnClickPending(workID, tanID))
            if (size == 2) {
                setOnClickPendingIntent(R.id.btnWake_no, btnClickPending_exit(workID, tanID))
            }


            when (workID) {
                DBConfig.DAIBOO_WORK_ID_BOOSTER -> {
                    setImageViewResource(R.id.ivAlertIcon, if (size == 2) R.mipmap.ic_pop_act_boost else R.mipmap.ic_result_boost)
                    setTextViewText(
                        R.id.tvAlertDescription,
                        R.string.des_boot_tan.getString(DaiBooRAMUtils.getUsedMemoryStringPer())
                    )
                    setTextViewText(R.id.btnWake, R.string.boost.getString())
                }
                DBConfig.DAIBOO_WORK_ID_CPU -> {
                    setImageViewResource(R.id.ivAlertIcon, if (size == 2) R.mipmap.ic_pop_act_cpu else R.mipmap.ic_result_cpu)
                    setTextViewText(
                        R.id.tvAlertDescription,
                        R.string.des_cpu_tan.getString("${(24..40).random()}°C")
                    )
                    setTextViewText(R.id.btnWake, R.string.cool_down.getString())
                }
                DBConfig.DAIBOO_WORK_ID_BATTERY -> {
                    setImageViewResource(R.id.ivAlertIcon, if (size == 2) R.mipmap.ic_pop_act_battery else R.mipmap.ic_result_battery)

                    when (tanID) {
                        DBConfig.DAIBOO_NOTY_CHARGE -> {
                            setTextViewText(
                                R.id.tvAlertDescription,
                                R.string.des_battery_pop_charge.getString()
                            )
                            setTextViewText(R.id.btnWake, R.string.optimize_up.getString())

                        }
                        DBConfig.DAIBOO_NOTY_BATTERY -> {
                            setTextViewText(
                                R.id.tvAlertDescription,
                                R.string.des_battery_pop_size.getString()
                            )
                            setTextViewText(R.id.btnWake, R.string.extend_now.getString())
                        }
                        else -> {
                            setTextViewText(
                                R.id.tvAlertDescription,
                                R.string.des_battery_pop_time.getString()
                            )
                            setTextViewText(R.id.btnWake, R.string.manage.getString())
                        }
                    }
                }
                DBConfig.DAIBOO_WORK_ID_CLEAN -> {

                    when (tanID) {
                        DBConfig.DAIBOO_NOTY_UNINSTALL -> {
                            setImageViewResource(R.id.ivAlertIcon, R.mipmap.ic_pop_act_uninstall)
                            setTextViewText(
                                R.id.tvAlertDescription,
                                R.string.des_uninstall.getString("${(22..79).random()}MB")
                            )
                            setTextViewText(R.id.btnWake, R.string.clean_now.getString())

                        }

                        else -> {
                            setImageViewResource(R.id.ivAlertIcon, if (size == 2) R.mipmap.ic_pop_act_clean else R.mipmap.ic_result_junk)
                            setTextViewText(
                                R.id.tvAlertDescription,
                                R.string.des_clean_tan.getString(
                                    DaiBooRAMUtils.getUsedMemoryString(
                                        App.ins
                                    )
                                )
                            )
                            setTextViewText(R.id.btnWake, R.string.clean.getString())
                        }
                    }
                }
            }
        }
        return remoteViews
    }


    private fun btnClickPending_fsi(workID: String, tanId: String): PendingIntent {
//        val intent = Intent()
        val bpiBroad: PendingIntent =
            PendingIntent.getBroadcast(
                App.ins, tanId.hashCode() + 20,
                Intent(App.ins, NotifyToolReceiver::class.java).apply {
                    putExtra(DBConfig.DAIBOO_KEY_WORK_ID, workID)
                    putExtra(DBConfig.DAIBOO_KEY_NOTY_ID, tanId)
                    action = DBConfig.DAIBOO_ACTION_FROM_POP_NOTY_POP_FULLSCREEN
                }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val actIntent: PendingIntent =
            PendingIntent.getActivity(
                App.ins, tanId.hashCode() + 20,
                Intent(App.ins, NotificationActivity::class.java).apply {
                    putExtra(DBConfig.DAIBOO_KEY_WORK_ID, workID)
                    putExtra(DBConfig.DAIBOO_KEY_NOTY_ID, tanId)
                    action = DBConfig.DAIBOO_ACTION_FROM_POP_NOTY_POP_FULLSCREEN
                }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )


        val isAct = 1 == FiBRemoteUtil.daiBooPopBean?.booster_avti
        return if (isAct) actIntent else bpiBroad

    }

    private fun btnClickPending_exit(workID: String, tanId: String): PendingIntent {
        val intent = Intent(App.ins, NotifyToolReceiver::class.java).apply {
            putExtra(DBConfig.DAIBOO_KEY_WORK_ID, workID)
            putExtra(DBConfig.DAIBOO_KEY_NOTY_ID, tanId)
            action = DBConfig.DAIBOO_ACTION_FROM_POP_NOTY_POP_EXIT
        }
        return PendingIntent.getBroadcast(App.ins, tanId.hashCode() + 3, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    }

    private fun btnClickPending(workID: String, tanId: String): PendingIntent {

//        if (1 == FireBRemoteUtil.open_control?.notify) {
//            // Create the TaskStackBuilder
//            val clz = if (workID == DBConfig.DAIBOO_WORK_ID_CLEAN) {
//                JunkScanActivity::class.java
//            } else {
//                BoostActivity::class.java
//            }
//            return TaskStackBuilder.create(DBApp.ins).run {
//                //不展示开屏 ,直接进入流程
//                addNextIntentWithParentStack(Intent(DBApp.ins, clz).apply {
//                    putExtra(DBConfig.DAIBOO_KEY_WORK_ID, workID)
//                    putExtra(DBConfig.DAIBOO_KEY_NOTY_ID, tanId)
//                    action = DBConfig.DAIBOO_ACTION_FROM_POP_NOTY_POP
//                })
//                getPendingIntent(tanId.hashCode() + 2, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//            }
//        } else {
        return PendingIntent.getActivity(
            App.ins,
            tanId.hashCode(),
            Intent(App.ins, SplashActivity::class.java).apply {
                putExtra(DBConfig.DAIBOO_KEY_WORK_ID, workID)
                putExtra(DBConfig.DAIBOO_KEY_NOTY_ID, tanId)
                action = (DBConfig.DAIBOO_ACTION_FROM_POP_NOTY_POP)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
//        }


    }

    private fun checkIsMIUI(): Boolean {
        return Build.BRAND.equals("xiaomi", true)
                || Build.BRAND.equals("Redmi", true)
                || Build.BRAND.equals("miui", true)
    }

}