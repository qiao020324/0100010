package com.autoledger.app

import android.app.Application
import com.autoledger.app.data.DbHelper

class App : Application() {
    lateinit var db: DbHelper
        private set

    override fun onCreate() {
        super.onCreate()
        db = DbHelper(this)
    }
}
