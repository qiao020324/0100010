package com.autoledger.app.parse

data class ParsedBill(
    val amount: Double?,
    val type: String,        // "expense" | "income"
    val merchant: String,
    val payMethod: String,
    val time: Long,
    val category: String,
    val confidence: Double,
    val raw: String
)

object BillParser {

    fun parse(raw: String): ParsedBill? {
        val text = raw.replace("\\s+".toRegex(), " ").trim()
        if (text.isEmpty()) return null

        val amount = extractAmount(text)
        val merchant = extractMerchant(text)
        val payMethod = extractPayMethod(text)
        val time = extractDate(text)
        val type = extractType(text)
        val category = guessCategory(text, merchant)

        var score = 0
        if (amount != null) score++
        if (merchant.isNotEmpty()) score++
        if (payMethod != "其他") score++
        score++ // 时间始终可获取

        val finalMerchant = if (merchant.isEmpty()) (if (type == "income") "收入" else "未知商户") else merchant
        return ParsedBill(amount, type, finalMerchant, payMethod, time, category, score / 4.0, raw)
    }

    private fun extractAmount(text: String): Double? {
        val candidates = mutableListOf<Pair<Double, Int>>()
        val re = Regex("[¥￥]?\\s?(\\d{1,3}(?:,\\d{3})*(?:\\.\\d{1,2})?|\\d+(?:\\.\\d{1,2})?)")
        for (m in re.findAll(text)) {
            val v = m.groupValues[1].replace(",", "").toDoubleOrNull() ?: continue
            if (v <= 0 || v >= 1e8) continue
            val start = maxOf(0, m.range.first - 24)
            val end = minOf(text.length, m.range.last + 25)
            val ctx = text.substring(start, end)
            var s = 0
            if (Regex("订单号|单号|交易号|流水号|流水").containsMatchIn(ctx)) s -= 5
            if (Regex("实付|应付|支付金额|付款金额|交易金额|微信支付成功|支付宝.*?支付").containsMatchIn(ctx)) s += 6
            if (Regex("金额|付款|支付|消费|合计|应收|收款|共计").containsMatchIn(ctx)) s += 3
            if (m.value.contains("¥") || m.value.contains("￥")) s += 1
            if (Regex("优惠|折扣|红包抵扣|立减").containsMatchIn(ctx)) s -= 4
            candidates.add(v to s)
        }
        if (candidates.isEmpty()) return null
        candidates.sortWith(compareByDescending<Pair<Double, Int>> { it.second }.thenByDescending { it.first })
        return candidates.first().first
    }

    private fun extractMerchant(text: String): String {
        val patterns = listOf(
            Regex("微信支付\\s*[-—]\\s*([^\\s¥￥金额支付成功]{1,30}?)(?:\\s*(?:支付成功|已支付|交易|金额|￥|¥|$))", RegexOption.IGNORE_CASE),
            Regex("支付宝\\s*[-—]\\s*([^\\s¥￥金额支付成功]{1,30}?)(?:\\s*(?:支付成功|已支付|交易|金额|￥|¥|$))", RegexOption.IGNORE_CASE),
            Regex("(?:收款方|商户|商家|付款给|向|付给|给)\\s*[:： ]*([^\\s,，。；;]{1,30}?)(?:\\s*(?:的|付款|支付|成功|金额|￥|¥|，|,|。|$))"),
            Regex("(?:在|于)\\s*([^\\s,，。；;]{1,20}?)\\s*(?:消费|付款|支付|购买)"),
            Regex("(?:购买|支付)\\s*([^\\s,，。；;]{1,20}?)(?:\\s*(?:的|服务|商品|金额|￥|¥|$))"),
            Regex("\\[([^\\]]+)\\]")
        )
        for (p in patterns) {
            val m = p.find(text)
            if (m != null && m.groupValues[1].isNotEmpty()) {
                val name = m.groupValues[1].trim().replace("[，,。.；;的]+$".toRegex(), "").replace("\\s+".toRegex(), " ")
                if (name.length in 1..30) return name
            }
        }
        return ""
    }

    private fun extractPayMethod(text: String): String {
        return when {
            text.contains("微信") -> "微信"
            text.contains("支付宝") || text.contains("花呗") || text.contains("余额宝") -> "支付宝"
            text.contains(Regex("储蓄卡|信用卡|银行卡|借记卡|贷记卡|招商|工商|建设|中国银|交通银行|浦发|中信|民生|光大|平安|兴业|农业|邮储|还款")) -> "银行卡"
            text.contains("现金") -> "现金"
            else -> "其他"
        }
    }

    private fun extractDate(text: String): Long {
        val m = Regex("(\\d{4})[-/.年](\\d{1,2})[-/.月](\\d{1,2})[日]?(?:\\s*(?:(\\d{1,2})[:：](\\d{2}))?)?").find(text)
        if (m != null) {
            val (y, mo, d, h, mi) = m.destructured
            val cal = java.util.Calendar.getInstance()
            cal.set(y.toInt(), mo.toInt() - 1, d.toInt(), (h.ifEmpty { "0" }).toInt(), (mi.ifEmpty { "0" }).toInt(), 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            val t = cal.timeInMillis
            if (t > 0) return t
        }
        if (text.contains(Regex("今天|今日|刚刚|刚才"))) return System.currentTimeMillis()
        if (text.contains(Regex("昨天|昨日"))) return System.currentTimeMillis() - 24 * 3600 * 1000L
        return System.currentTimeMillis()
    }

    private fun extractType(text: String): String {
        return if (text.contains(Regex("收款|收到|入账|退款|到账|红包|返还|退费|返现"))) "income" else "expense"
    }

    fun guessCategory(text: String, merchant: String): String {
        val hay = (text + " " + merchant).lowercase()
        var best = "other"
        var bestScore = 0
        for (c in Categories.DEFAULT) {
            var s = 0
            for (kw in c.keywords) if (kw.isNotEmpty() && hay.contains(kw.lowercase())) s++
            if (s > bestScore) {
                bestScore = s
                best = c.id
            }
        }
        return best
    }
}
