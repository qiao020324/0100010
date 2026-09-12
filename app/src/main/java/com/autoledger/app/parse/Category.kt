package com.autoledger.app.parse

data class Category(
    val id: String,
    val name: String,
    val color: Int,
    val icon: String,
    val keywords: List<String>
)

object Categories {
    val DEFAULT = listOf(
        Category("food", "餐饮", 0xFFFF7A45.toInt(), "餐", listOf("餐", "饭", "吃", "美团", "饿了么", "星巴克", "咖啡", "奶茶", "餐厅", "小吃", "肯德基", "麦当劳", "火锅")),
        Category("transport", "交通", 0xFF36CFC9.toInt(), "行", listOf("滴滴", "打车", "地铁", "公交", "加油", "高铁", "火车", "机票", "停车", "单车", "出租", "uber")),
        Category("shopping", "购物", 0xFF9254DE.toInt(), "购", listOf("淘宝", "天猫", "京东", "拼多多", "超市", "便利店", "商场", "购物", "唯品会", "苏宁", "盒马")),
        Category("home", "居家", 0xFF73D13D.toInt(), "居", listOf("物业", "水电", "房租", "家居", "家政", "维修", "燃气", "宽带")),
        Category("fun", "娱乐", 0xFFFFC53D.toInt(), "乐", listOf("电影", "游戏", "视频", "会员", "ktv", "演出", "门票", "网吧", "直播", "爱奇艺")),
        Category("medical", "医疗", 0xFFFF85C0.toInt(), "医", listOf("医院", "药店", "诊所", "挂号", "体检", "药", "医疗", "保健")),
        Category("edu", "教育", 0xFF40A9FF.toInt(), "学", listOf("教育", "培训", "课程", "书", "网课", "学费", "考试", "知识")),
        Category("comm", "通讯", 0xFFFF9C6E.toInt(), "讯", listOf("话费", "流量", "充值", "通讯", "宽带", "sim")),
        Category("other", "其他", 0xFFBFBFBF.toInt(), "他", emptyList())
    )

    fun get(id: String) = DEFAULT.find { it.id == id } ?: DEFAULT.last()
}
