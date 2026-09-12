package com.autoledger.app.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.autoledger.app.App
import com.autoledger.app.databinding.ActivityMainBinding
import com.autoledger.app.service.KeepAliveService
import com.autoledger.app.util.Notify

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: TxAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
        adapter = TxAdapter(emptyList())
        binding.recycler.adapter = adapter

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.banner.setOnClickListener {
            Notify.openListenerSettings(this)
        }

        startKeepAlive()
    }

    private fun startKeepAlive() {
        val intent = Intent(this, KeepAliveService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val enabled = Notify.isListenerEnabled(
            this, "com.autoledger.app.service.PaymentNotificationService"
        )
        binding.banner.visibility = if (enabled) View.GONE else View.VISIBLE

        val db = (application as App).db
        val list = db.getAll()
        val month = db.getMonthExpense()
        binding.monthExpense.text = "¥" + String.format("%.2f", month)

        adapter.update(list)
        val empty = list.isEmpty()
        binding.recycler.visibility = if (empty) View.GONE else View.VISIBLE
        binding.empty.visibility = if (empty) View.VISIBLE else View.GONE
    }
}
