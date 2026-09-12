package com.autoledger.app.service

import android.app.Notification
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.autoledger.app.App
import com.autoledger.app.data.Transaction
import com.autoledger.app.parse.BillParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 系统通知监听服务：付款通知出现时自动解析并入账。
 * 需在系统「通知使用权」中授权后才会被系统绑定。
 */
class PaymentNotificationService : NotificationListenerService() {

    companion object {
        const val SELF_PKG = "com.autoledger.app"
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        val pkg = sbn.packageName ?: return
        if (pkg == SELF_PKG) return

        val text = dump(sbn.notification)
        if (text.isBlank()) return

        val parsed = BillParser.parse(text) ?: return
        val amount = parsed.amount ?: return
        // 可信度不足（如普通消息）不自动记账，避免误记
        if (parsed.confidence < 0.5) return

        val app = applicationContext as App
        val db = app.db
        if (!db.shouldInsert(parsed.raw, amount, parsed.time)) return

        CoroutineScope(Dispatchers.IO).launch {
            db.insert(
                Transaction(
                    amount = amount,
                    type = parsed.type,
                    merchant = parsed.merchant,
                    category = parsed.category,
                    payMethod = parsed.payMethod,
                    time = parsed.time,
                    note = "",
                    source = "noti",
                    raw = parsed.raw
                )
            )
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {}

    /** 将一条通知扁平化为可解析的文本 */
    private fun dump(n: Notification): String {
        val sb = StringBuilder()
        val extras: Bundle = n.extras ?: return ""
        sb.append(extras.getCharSequence(Notification.EXTRA_TITLE) ?: "")
        sb.append(" ")
        sb.append(extras.getCharSequence(Notification.EXTRA_TEXT) ?: "")
        val big = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
        if (big != null) sb.append(" ").append(big)
        val info = extras.getCharSequence(Notification.EXTRA_INFO_TEXT)
        if (info != null) sb.append(" ").append(info)
        val lines = extras.get(Notification.EXTRA_TEXT_LINES)
        if (lines is Collection<*>) lines.forEach { sb.append(" ").append(it ?: "") }
        val ticker = n.tickerText
        if (ticker != null) sb.append(" ").append(ticker)
        return sb.toString()
    }
}
