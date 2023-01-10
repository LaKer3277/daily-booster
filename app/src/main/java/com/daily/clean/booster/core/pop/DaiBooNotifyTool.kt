package com.daily.clean.booster.core.pop

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.daily.clean.booster.DBApp
import com.daily.clean.booster.R
import com.daily.clean.booster.base.DBConfig
import com.daily.clean.booster.ui.SplashActivity
import com.daily.clean.booster.utils.getString
import com.daily.clean.booster.utils.isSPlus


object DaiBooNotifyTool {

    private const val channelId = DBConfig.NOTIFY_CHANNEL_ID_TOOL
    const val notificationId = DBConfig.NOTIFY_TOOL_ID

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
            (DBApp.ins.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )
        }
    }

    private fun cancelNotification() {
        NotificationManagerCompat.from(DBApp.ins).cancel(notificationId)
    }

    private fun clickPending(workID: String): PendingIntent? {
        val newIntent =
            Intent(DBApp.ins, SplashActivity::class.java).apply {
                        putExtra(DBConfig.DAIBOO_KEY_WORK_ID, workID)
                        action = DBConfig.DAIBOO_ACTION_FROM_NOTIFYTOOL
                    }

        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(DBApp.ins).run {
            addNextIntentWithParentStack(newIntent)
            getPendingIntent(workID.hashCode(), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }


        return resultPendingIntent
    }

    private fun customRemoteViews(): RemoteViews {

        val layoutId = if(isSPlus()){
            R.layout.layout_tool_55
        }else{
            R.layout.layout_tool_64
        }
        return RemoteViews(DBApp.ins.packageName, layoutId).apply {
            setOnClickPendingIntent(
                R.id.notify_booster,
                clickPending(DBConfig.DAIBOO_WORK_ID_BOOSTER)
            )
            setOnClickPendingIntent(
                R.id.notify_cpu,
                clickPending(DBConfig.DAIBOO_WORK_ID_CPU)
            )
            setOnClickPendingIntent(
                R.id.notify_battery,
                clickPending(DBConfig.DAIBOO_WORK_ID_BATTERY)
            )
            setOnClickPendingIntent(
                R.id.notify_clean,
                clickPending(DBConfig.DAIBOO_WORK_ID_CLEAN)
            )
        }
    }

    fun createNotification(): Notification {
        createNotificationChannel()
        cancelNotification()
        val notificationBuilder = NotificationCompat.Builder(DBApp.ins, channelId)
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