package com.lylx.learnfeature

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_notification.*


class NotificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channelId = "attention"
            var channelName = "关注"
            var importance = NotificationManager.IMPORTANCE_HIGH
            createNotificationChannel(channelId, channelName, importance)

            channelId = "advertise"
            channelName = "广告"
            importance = NotificationManager.IMPORTANCE_HIGH
            createNotificationChannel(channelId, channelName, importance)
        }

        btn_push_attention.setOnClickListener {
            sendAttentionMsg()
        }

        btn_push_advertise.setOnClickListener {
            sendAdvertiseMsg()
        }

    }

    private fun sendAdvertiseMsg() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, "advertise")
                .setContentTitle("收到一条广告")
                .setContentText("房贷利率恢复基准利率，你还在等什么？")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .build()
        manager.notify(1, notification)
    }

    private fun sendAttentionMsg() {
        if (!checkNotificationChannel(this, "attention")) {
            return
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(this, "attention")
                .setContentTitle("收到一条关注的信息")
                .setContentText("Android Q Beta 闪亮登场")
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setAutoCancel(true)
                .build()
        manager.notify(2, notification)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String, importance: Int) {
        val channel = NotificationChannel(channelId, channelName, importance)
        channel.setShowBadge(true)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * 检测通知是否开启
     */
    private fun checkNotificationEnable(context: Context): Boolean {
        val manager = NotificationManagerCompat.from(context)
        return manager.areNotificationsEnabled()
    }

    /**
     * 检测通知渠道是否开启
     *
     * 小米Note3 android8.1.0 关闭通知，不跳转到通知管理
     */
    private fun checkNotificationChannel(context: Context, channelId: String): Boolean {
        if (checkNotificationEnable(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channel = manager.getNotificationChannel(channelId)
                if (channel.importance == NotificationManager.IMPORTANCE_NONE) {
                    // 跳转通知管理的渠道通知
                    val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    intent.putExtra(Settings.EXTRA_CHANNEL_ID, channelId)
                    startActivity(intent)
                    Toast.makeText(this, "请手动将${channel.name}通知打开", Toast.LENGTH_SHORT).show()
                    return false
                }
            }
            return true
        } else {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    // 跳转通知管理
                    val intent = Intent()
                    intent.action = Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS
                    intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    startActivity(intent)
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                    intent.putExtra("app_package", packageName)
                    intent.putExtra("app_uid", context.applicationInfo.uid)
                    startActivity(intent)
                }
                else -> {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                    intent.data = Uri.parse("package:$packageName")
                    startActivity(intent)
                }
            }
        }
        return false
    }
}
