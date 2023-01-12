package com.daily.clean.booster.pop

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.daily.clean.booster.App
import com.daily.clean.booster.R
import com.daily.clean.booster.base.*
import com.daily.clean.booster.ui.SplashActivity
import com.daily.clean.booster.ext.getString
import com.daily.clean.booster.ext.isSPlus


object NotifyTools {

    private const val channelId = NOTIFY_CHANNEL_ID_TOOL
    const val notificationId = NOTIFY_TOOL_ID

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Clean Manager",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = R.string.app_name_tools.getString()
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setSound(null, null)
                enableLights(false)
                enableVibration(false)
                setShowBadge(false)
            }
            (App.ins.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )
        }
    }

    private fun cancelPopNotification() {
        NotificationManagerCompat.from(App.ins).cancel(notificationId)
    }

    private fun clickPending(workID: String): PendingIntent? {
        val newIntent =
            Intent(App.ins, SplashActivity::class.java).apply {
                        putExtra(Noty_KEY_WORK, workID)
                        action = DB_ACTION_FROM_NOTY_RESIDENT
                    }

        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(App.ins).run {
            addNextIntentWithParentStack(newIntent)
            getPendingIntent(workID.hashCode(), validateImmutableFlags)
        }


        return resultPendingIntent
    }

    private fun customRemoteViews(): RemoteViews {

        val layoutId = if(isSPlus()){
            R.layout.layout_tool_55
        }else{
            R.layout.layout_tool_64
        }
        return RemoteViews(App.ins.packageName, layoutId).apply {
            setOnClickPendingIntent(
                R.id.notify_booster,
                clickPending(NotyWorkBooster)
            )
            setOnClickPendingIntent(
                R.id.notify_cpu,
                clickPending(NotyWorkCpu)
            )
            setOnClickPendingIntent(
                R.id.notify_battery,
                clickPending(NotyWorkBattery)
            )
            setOnClickPendingIntent(
                R.id.notify_clean,
                clickPending(NotyWorkClean)
            )
        }
    }

    fun createNotification(): Notification {
        createNotificationChannel()
        cancelPopNotification()
        val notificationBuilder = NotificationCompat.Builder(App.ins, channelId)
            .setSmallIcon(R.drawable.ic_daily_booster_logo)
            .setCustomContentView(customRemoteViews())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setSound(null)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setGroupSummary(false)
        if (isSPlus()) {
            notificationBuilder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
        }

        return notificationBuilder.build()
    }


}