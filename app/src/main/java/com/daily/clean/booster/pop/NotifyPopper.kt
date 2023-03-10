package com.daily.clean.booster.pop

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.daily.clean.booster.R
import com.daily.clean.booster.appIns
import com.daily.clean.booster.base.*
import com.daily.clean.booster.base.FirebaseEvent
import com.daily.clean.booster.datas.RemoteConfig
import com.daily.clean.booster.entity.DaiBooPopItemBean
import com.daily.clean.booster.ui.NotificationActivity
import com.daily.clean.booster.ui.SplashActivity
import com.daily.clean.booster.utils.DaiBooRAMUtils
import com.daily.clean.booster.ext.getString
import com.daily.clean.booster.ext.isSPlus
import java.util.*

open class NotifyPopper {

    private val channelId = NOTIFY_CHANNEL_ID_POP
    private val notificationId = NOTIFY_POP_ID

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
            (appIns.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )
        }
    }

    fun cancelAlertNotification() {
        NotificationManagerCompat.from(appIns).cancel(notificationId)
    }

    @SuppressLint("WrongConstant")
    fun createNotificationAndPop(workId: String, sourceId: String): Notification {
        cancelAlertNotification()
        createNotificationChannel()

        val item: DaiBooPopItemBean? = NotifyManager.getPopItem(sourceId)

        val nBuilder = NotificationCompat.Builder(appIns, channelId)
            .setSmallIcon(R.drawable.ic_daily_booster_logo)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            //????????????PendingIntent????????????????????????????????????????????????????????????
            // ???????????????????????? "???????????? "??????????????????????????? "X "?????????????????????????????????
            // ?????????????????????NotificationManager.cancel(int)????????????????????????????????????
            .setContentIntent(btnClickPending(workId, sourceId))
            .setFullScreenIntent(getFullScreenIntent(workId, sourceId), true)
            .setAutoCancel(true)
            .setGroupSummary(false)
            .setSound(null)
            .setOngoing(true)

        val size = item?.siz ?: 2
        val small = getCustomView(workId, sourceId, 0)
        val conf = getCustomView(workId, sourceId, size)
        if (isSPlus() || checkIsMIUI()) {
            //???
            nBuilder
                .setCustomContentView(small)
                .setCustomHeadsUpContentView(small)
                .setCustomBigContentView(conf)
            //???????????????????????????????????????????????????, Android 31?????????????????????????????????
            if (isSPlus()) nBuilder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
        } else {
            nBuilder.setCustomContentView(conf)
                .setCustomHeadsUpContentView(conf)
                .setCustomBigContentView(conf)
        }

        //build
        val notification = nBuilder.build()
        //notify
        with(NotificationManagerCompat.from(appIns)) {
            notify(notificationId, notification)
        }
        return notification
    }

    private fun getFullScreenIntent(workID: String, tanId: String): PendingIntent {
        val bpiBroad: PendingIntent =
            PendingIntent.getBroadcast(
                appIns, tanId.hashCode() + 20,
                Intent(appIns, NotifyToolReceiver::class.java).apply {
                    putExtra(Noty_KEY_WORK, workID)
                    putExtra(Noty_KEY_SOURCE, tanId)
                    action = DB_ACTION_FROM_POP_NOTY_FULLSCREEN
                }, validateImmutableFlags
            )

        val actIntent: PendingIntent =
            PendingIntent.getActivity(
                appIns, tanId.hashCode() + 20,
                Intent(appIns, NotificationActivity::class.java).apply {
                    putExtra(Noty_KEY_WORK, workID)
                    putExtra(Noty_KEY_SOURCE, tanId)
                    action = DB_ACTION_FROM_POP_NOTY_FULLSCREEN
                }, validateImmutableFlags
            )


        val isAct = 1 == RemoteConfig.daiBooPopBean?.booster_avti
        return if (isAct) actIntent else bpiBroad

    }

    private fun btnClickPendingClose(workID: String, tanId: String): PendingIntent {
        val intent = Intent(appIns, NotifyToolReceiver::class.java).apply {
            putExtra(Noty_KEY_WORK, workID)
            putExtra(Noty_KEY_SOURCE, tanId)
            action = DB_ACTION_FROM_POP_NOTY_EXIT
        }
        return PendingIntent.getBroadcast(appIns, tanId.hashCode() + 3, intent, validateImmutableFlags)

    }

    private fun btnClickPending(workID: String, sourceId: String): PendingIntent {
        return PendingIntent.getActivity(
            appIns,
            sourceId.hashCode(),
            Intent(appIns, SplashActivity::class.java).apply {
                putExtra(Noty_KEY_WORK, workID)
                putExtra(Noty_KEY_SOURCE, sourceId)
                action = DB_ACTION_FROM_POP_NOTY
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            validateImmutableFlags
        )
    }

    private fun checkIsMIUI(context: Context = appIns): Boolean {
        if ("xiaomi".equals(Build.MANUFACTURER, ignoreCase = true)) {
            return true
        }
        val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
        intent.setClassName(
            "com.android.settings",
            "com.miui.securitycenter.permission.AppPermissionsEditor"
        )
        if (queryActivities(context, intent)) {
            return true
        }
        var equalsIgnoreCase = "miui".equals(Build.ID, ignoreCase = true)
        if ("xiaomi".equals(Build.BRAND, ignoreCase = true)) {
            equalsIgnoreCase = true
        }
        val str = Build.MODEL
        if (str != null) {
            val lowerCase = str.lowercase(Locale.getDefault())
            if (lowerCase.contains("xiaomi")) {
                equalsIgnoreCase = true
            }
            if (lowerCase.contains("miui")) {
                return true
            }
        }
        return equalsIgnoreCase
    }

    @SuppressLint("WrongConstant")
    private fun queryActivities(context: Context, intent: Intent): Boolean {
        return context.packageManager.queryIntentActivities(intent, 1).size > 0
    }

    private fun getCustomView(
        workID: String,
        tanID: String,
        size: Int): RemoteViews {

        val layoutResId = when (size) {
            0 -> R.layout.notification_pop_small
            1 -> R.layout.notification_pop_middle
            else -> R.layout.notification_pop_big
        }

        val isBig = size == 2
        val remoteViews = RemoteViews(appIns.packageName, layoutResId).apply {
            setOnClickPendingIntent(R.id.pop_content, btnClickPending(workID, tanID))
            setOnClickPendingIntent(R.id.btnWake_no, btnClickPendingClose(workID, tanID))

            when (workID) {
                NotyWorkBooster -> {
                    setImageViewResource(R.id.ivAlertIcon, if (isBig) R.mipmap.ic_pop_act_boost else R.mipmap.ic_result_boost)
                    setTextViewText(
                        R.id.tvAlertDescription,
                        R.string.des_boot_tan.getString(DaiBooRAMUtils.getUsedMemoryStringPer())
                    )
                    setTextViewText(R.id.btnWake, R.string.boost.getString())
                }
                NotyWorkCpu -> {
                    setImageViewResource(R.id.ivAlertIcon, if (isBig) R.mipmap.ic_pop_act_cpu else R.mipmap.ic_result_cpu)
                    setTextViewText(
                        R.id.tvAlertDescription,
                        R.string.des_cpu_tan.getString("${(24..40).random()}??C")
                    )
                    setTextViewText(R.id.btnWake, R.string.cool_down.getString())
                }
                NotyWorkBattery -> {
                    setImageViewResource(R.id.ivAlertIcon, if (isBig) R.mipmap.ic_pop_act_battery else R.mipmap.ic_result_battery)

                    when (tanID) {
                        NotySourceCharge -> {
                            setTextViewText(
                                R.id.tvAlertDescription,
                                R.string.des_battery_pop_charge.getString()
                            )
                            setTextViewText(R.id.btnWake, R.string.optimize_up.getString())

                        }
                        NotySourceBattery -> {
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
                NotyWorkClean -> {

                    when (tanID) {
                        NotySourceUninstall -> {
                            setImageViewResource(R.id.ivAlertIcon, R.mipmap.ic_pop_act_uninstall)
                            setTextViewText(
                                R.id.tvAlertDescription,
                                R.string.des_uninstall.getString("${(22..79).random()}MB")
                            )
                            setTextViewText(R.id.btnWake, R.string.clean_now.getString())

                        }

                        else -> {
                            setImageViewResource(R.id.ivAlertIcon, if (isBig) R.mipmap.ic_pop_act_clean else R.mipmap.ic_result_junk)
                            setTextViewText(
                                R.id.tvAlertDescription,
                                R.string.des_clean_tan.getString(
                                    DaiBooRAMUtils.getUsedMemoryString(
                                        appIns
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

}