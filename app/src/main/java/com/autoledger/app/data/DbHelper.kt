package com.autoledger.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DbHelper(context: Context) : SQLiteOpenHelper(context, "autoledger.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE tx (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                amount REAL NOT NULL,
                type TEXT NOT NULL,
                merchant TEXT,
                category TEXT,
                payMethod TEXT,
                time INTEGER NOT NULL,
                note TEXT,
                source TEXT,
                raw TEXT
            )"""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS tx")
        onCreate(db)
    }

    fun insert(t: Transaction): Long {
        val cv = ContentValues().apply {
            put("amount", t.amount)
            put("type", t.type)
            put("merchant", t.merchant)
            put("category", t.category)
            put("payMethod", t.payMethod)
            put("time", t.time)
            put("note", t.note)
            put("source", t.source)
            put("raw", t.raw)
        }
        return writableDatabase.insert("tx", null, cv)
    }

    /** 防重复：相同原始文本，或相同金额且时间相差 <30s 视为同一笔，不重复写入 */
    fun shouldInsert(raw: String, amount: Double?, time: Long): Boolean {
        val sel = "raw=? OR (amount=? AND abs(time-?)<30000)"
        val args = arrayOf(raw, (amount ?: 0.0).toString(), time.toString())
        val cur = readableDatabase.query("tx", arrayOf("id"), sel, args, null, null, null)
        val exists = cur.moveToFirst()
        cur.close()
        return !exists
    }

    fun getAll(): List<Transaction> {
        val list = mutableListOf<Transaction>()
        val cur = readableDatabase.query("tx", null, null, null, null, null, "time DESC")
        while (cur.moveToNext()) {
            list.add(
                Transaction(
                    id = cur.getLong(cur.getColumnIndexOrThrow("id")),
                    amount = cur.getDouble(cur.getColumnIndexOrThrow("amount")),
                    type = cur.getString(cur.getColumnIndexOrThrow("type")),
                    merchant = cur.getString(cur.getColumnIndexOrThrow("merchant")) ?: "",
                    category = cur.getString(cur.getColumnIndexOrThrow("category")) ?: "other",
                    payMethod = cur.getString(cur.getColumnIndexOrThrow("payMethod")) ?: "",
                    time = cur.getLong(cur.getColumnIndexOrThrow("time")),
                    note = cur.getString(cur.getColumnIndexOrThrow("note")) ?: "",
                    source = cur.getString(cur.getColumnIndexOrThrow("source")) ?: "noti",
                    raw = cur.getString(cur.getColumnIndexOrThrow("raw")) ?: ""
                )
            )
        }
        cur.close()
        return list
    }

    fun getMonthExpense(): Double {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        val cur = readableDatabase.query(
            "tx", arrayOf("SUM(amount)"), "type<>'income' AND time>=?",
            arrayOf(start.toString()), null, null, null
        )
        var sum = 0.0
        if (cur.moveToFirst()) sum = cur.getDouble(0)
        cur.close()
        return sum
    }

    fun clear() {
        writableDatabase.delete("tx", null, null)
    }
}
