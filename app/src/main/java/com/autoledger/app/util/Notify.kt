package com.autoledger.app.util

import android.content.Context
import android.content.Intent
import android.provider.Settings

object Notify {

    /** 是否已授予「通知使用权」（通知监听所需系统权限） */
    fun isListenerEnabled(context: Context, clsName: String): Boolean {
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: return false
        return flat.split(":").any { it.equals("${context.packageName}/$clsName", ignoreCase = true) }
    }

    fun openListenerSettings(context: Context) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
