package com.autoledger.app.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.autoledger.app.App
import com.autoledger.app.R
import com.autoledger.app.util.Notify

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val status = findViewById<TextView>(R.id.status)
        val btnEnable = findViewById<Button>(R.id.btn_enable)
        val btnClear = findViewById<Button>(R.id.btn_clear)

        val enabled = Notify.isListenerEnabled(
            this, "com.autoledger.app.service.PaymentNotificationService"
        )
        status.text = if (enabled) getString(R.string.listener_status_on)
        else getString(R.string.listener_status_off)

        btnEnable.setOnClickListener { Notify.openListenerSettings(this) }
        btnClear.setOnClickListener {
            (application as App).db.clear()
            finish()
        }
    }
}
