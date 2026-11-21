package com.chinapost.postage.model

data class Province(
    val name: String,
    val region: String
)

object ChinaProvinces {
    val ALL = listOf(
        Province("北京", "华北"),
        Province("天津", "华北"),
        Province("河北", "华北"),
        Province("山西", "华北"),
        Province("内蒙古", "华北"),
        Province("辽宁", "东北"),
        Province("吉林", "东北"),
        Province("黑龙江", "东北"),
        Province("上海", "华东"),
        Province("江苏", "华东"),
        Province("浙江", "华东"),
        Province("安徽", "华东"),
        Province("福建", "华东"),
        Province("江西", "华东"),
        Province("山东", "华东"),
        Province("河南", "华中"),
        Province("湖北", "华中"),
        Province("湖南", "华中"),
        Province("广东", "华南"),
        Province("广西", "华南"),
        Province("海南", "华南"),
        Province("重庆", "西南"),
        Province("四川", "西南"),
        Province("贵州", "西南"),
        Province("云南", "西南"),
        Province("西藏", "西南"),
        Province("陕西", "西北"),
        Province("甘肃", "西北"),
        Province("青海", "西北"),
        Province("宁夏", "西北"),
        Province("新疆", "西北")
    )

    // 西藏、新疆、青海为特级偏远（快递包裹五区，EMS五区）
    val REMOTE_WEST = setOf("西藏", "新疆", "青海")

    // 较远省份（黑龙江、云南、海南、吉林部分跨区为四区）
    val ZONE_FOUR_PROVINCES = setOf("黑龙江", "云南", "海南")

    // 相邻或周边省份（二区核算）
    private val ADJACENT_MAP = mapOf(
        "北京" to setOf("天津", "河北", "山西", "山东", "内蒙古", "辽宁", "河南"),
        "天津" to setOf("北京", "河北", "山东", "辽宁", "山西", "河南"),
        "河北" to setOf("北京", "天津", "山西", "内蒙古", "辽宁", "山东", "河南"),
        "山西" to setOf("河北", "内蒙古", "陕西", "河南", "北京", "天津", "山东"),
        "内蒙古" to setOf("黑龙江", "吉林", "辽宁", "河北", "山西", "陕西", "宁夏", "甘肃", "北京"),
        "辽宁" to setOf("吉林", "黑龙江", "内蒙古", "河北", "天津", "北京", "山东"),
        "吉林" to setOf("黑龙江", "辽宁", "内蒙古", "北京", "河北"),
        "黑龙江" to setOf("吉林", "辽宁", "内蒙古"),
        "上海" to setOf("江苏", "浙江", "安徽", "山东", "江西", "福建", "河南"),
        "江苏" to setOf("上海", "浙江", "安徽", "山东", "河南", "江西", "湖北", "河北", "北京", "天津", "福建", "湖南"),
        "浙江" to setOf("上海", "江苏", "安徽", "江西", "福建", "广东", "山东", "湖北", "湖南"),
        "安徽" to setOf("江苏", "浙江", "江西", "湖北", "河南", "山东", "上海", "湖南", "河北"),
        "福建" to setOf("浙江", "江西", "广东", "上海", "江苏", "安徽", "湖北", "湖南"),
        "江西" to setOf("安徽", "浙江", "福建", "广东", "湖南", "湖北", "江苏", "上海"),
        "山东" to setOf("河北", "河南", "安徽", "江苏", "北京", "天津", "山西", "辽宁", "上海"),
        "河南" to setOf("河北", "山西", "陕西", "湖北", "安徽", "山东", "北京", "天津", "江苏"),
        "湖北" to setOf("河南", "安徽", "江西", "湖南", "重庆", "陕西", "四川", "贵州", "江苏", "浙江"),
        "湖南" to setOf("湖北", "江西", "广东", "广西", "贵州", "重庆", "安徽", "浙江", "福建"),
        "广东" to setOf("福建", "江西", "湖南", "广西", "海南", "贵州", "浙江"),
        "广西" to setOf("贵州", "湖南", "广东", "云南", "海南", "重庆", "四川"),
        "海南" to setOf("广东", "广西", "福建", "湖南"),
        "重庆" to setOf("四川", "陕西", "湖北", "湖南", "贵州", "云南", "甘肃"),
        "四川" to setOf("重庆", "贵州", "云南", "西藏", "陕西", "甘肃", "青海", "湖北"),
        "贵州" to setOf("四川", "重庆", "湖南", "广西", "云南"),
        "云南" to setOf("四川", "贵州", "广西", "西藏"),
        "西藏" to setOf("新疆", "青海", "四川", "云南"),
        "陕西" to setOf("山西", "内蒙古", "宁夏", "甘肃", "四川", "重庆", "湖北", "河南"),
        "甘肃" to setOf("陕西", "宁夏", "青海", "新疆", "内蒙古", "四川"),
        "青海" to setOf("西藏", "新疆", "甘肃", "四川"),
        "宁夏" to setOf("陕西", "内蒙古", "甘肃"),
        "新疆" to setOf("甘肃", "青海", "西藏")
    )

    // 核心紧密经济圈/近邻组（普通包裹二档核算）
    private val CORE_ECONOMIC_CLUSTERS = listOf(
        setOf("北京", "天津", "河北"),
        setOf("上海", "江苏", "浙江", "安徽", "山东"),
        setOf("广东", "广西", "海南"),
        setOf("四川", "重庆"),
        setOf("陕西", "甘肃", "宁夏", "青海"),
        setOf("湖北", "湖南"),
        setOf("辽宁", "吉林", "黑龙江"),
        setOf("河南", "河北", "山西", "山东")
    )

    private fun isCoreNeighbor(origin: String, dest: String): Boolean {
        return CORE_ECONOMIC_CLUSTERS.any { it.contains(origin) && it.contains(dest) }
    }

    /**
     * 官方附表 10-2：国内普通包裹（普包）6档计费标准推导
     */
    fun resolveOrdinaryParcelTier(origin: String, dest: String, isLocalCity: Boolean): OrdinaryParcelTier {
        if (isLocalCity || origin == dest) {
            return OrdinaryParcelTier.TIER_1 // 一档：省内/同城（首重5元，续重1/2/3元）
        }
        // 六档：西藏、新疆（偏远跨省，首重10元，续重5/7/8元）
        if (dest == "西藏" || dest == "新疆" || origin == "西藏" || origin == "新疆") {
            return OrdinaryParcelTier.TIER_6
        }
        // 五档：黑龙江、云南、海南远途跨省（非邻省，首重9元，续重4/4/5元）
        if (ZONE_FOUR_PROVINCES.contains(dest) || ZONE_FOUR_PROVINCES.contains(origin)) {
            val neighbors = ADJACENT_MAP[origin] ?: emptySet()
            if (!neighbors.contains(dest)) {
                return OrdinaryParcelTier.TIER_5
            }
        }
        // 二档：邻省紧密经济圈（首重6元，续重1.5/2/3元）
        if (isCoreNeighbor(origin, dest)) {
            return OrdinaryParcelTier.TIER_2
        }
        // 三档：邻近周边省份（首重7元，续重2/2/3元）
        val neighbors = ADJACENT_MAP[origin] ?: emptySet()
        if (neighbors.contains(dest)) {
            return OrdinaryParcelTier.TIER_3
        }
        // 四档：一般跨省（如河北到四川、江苏到四川等，首重8元，续重3/3/4元）
        return OrdinaryParcelTier.TIER_4
    }

    /**
     * 准确推导邮政快递包裹计费分区（附表 10-4）
     */
    fun resolveExpressParcelZone(origin: String, dest: String, isLocalCity: Boolean): DestinationZone {
        if (isLocalCity) {
            return DestinationZone.LOCAL_CITY
        }
        if (origin == dest) {
            return DestinationZone.SAME_PROVINCE
        }
        // 五区：寄往或始发西藏、新疆、青海
        if (REMOTE_WEST.contains(dest) || REMOTE_WEST.contains(origin)) {
            return DestinationZone.ZONE_REMOTE
        }
        // 四区：黑龙江、云南等远途跨省（非邻省）
        if (ZONE_FOUR_PROVINCES.contains(dest) || ZONE_FOUR_PROVINCES.contains(origin)) {
            val neighbors = ADJACENT_MAP[origin] ?: emptySet()
            if (!neighbors.contains(dest)) {
                return DestinationZone.ZONE_FAR
            }
        }
        // 二区：邻近周边省份
        val neighbors = ADJACENT_MAP[origin] ?: emptySet()
        if (neighbors.contains(dest)) {
            return DestinationZone.ZONE_NEAR
        }
        // 三区：一般跨省（如河北到四川）
        return DestinationZone.ZONE_MIDDLE
    }

    /**
     * 官方附录十二：国内特快专递(EMS)分省、区、市计费分区推导
     */
    fun resolveEmsZone(origin: String, dest: String, isLocalCity: Boolean): DestinationZone {
        if (isLocalCity) {
            return DestinationZone.LOCAL_CITY
        }
        if (origin == dest) {
            return DestinationZone.SAME_PROVINCE
        }
        // 五区：西藏、新疆（偏远跨省统一五区，续重17元）
        if (dest == "西藏" || dest == "新疆" || origin == "西藏" || origin == "新疆") {
            return DestinationZone.ZONE_REMOTE
        }
        // 河北特快专递分省标准（附录十二）：
        if (origin == "河北") {
            return when (dest) {
                "北京", "天津", "山西", "内蒙古", "山东", "河南" -> DestinationZone.SAME_PROVINCE // 一区 (4元)
                "辽宁", "吉林", "黑龙江", "上海", "江苏", "浙江", "安徽", "江西", "湖北", "湖南", "四川", "重庆", "陕西", "甘肃", "青海", "宁夏" -> DestinationZone.ZONE_NEAR // 二区 (6元)
                "福建", "广东", "贵州", "海南", "云南" -> DestinationZone.ZONE_MIDDLE // 三区 (9元)
                "广西" -> DestinationZone.ZONE_FAR // 四区 (10元)
                else -> DestinationZone.ZONE_MIDDLE
            }
        }
        // 北京特快专递分省标准：
        if (origin == "北京") {
            return when (dest) {
                "天津", "河北", "内蒙古", "山东" -> DestinationZone.SAME_PROVINCE
                "山西", "辽宁", "吉林", "黑龙江", "上海", "江苏", "浙江", "安徽", "江西", "河南", "湖北", "湖南", "陕西", "甘肃", "宁夏" -> DestinationZone.ZONE_NEAR
                "福建", "广东", "四川", "重庆", "青海" -> DestinationZone.ZONE_MIDDLE
                "广西", "海南", "贵州", "云南" -> DestinationZone.ZONE_FAR
                else -> DestinationZone.ZONE_MIDDLE
            }
        }
        // 通用EMS分区判定（依据距离与省际区位）：
        if (ZONE_FOUR_PROVINCES.contains(dest) || ZONE_FOUR_PROVINCES.contains(origin)) {
            val neighbors = ADJACENT_MAP[origin] ?: emptySet()
            if (!neighbors.contains(dest)) {
                return DestinationZone.ZONE_FAR
            }
        }
        val neighbors = ADJACENT_MAP[origin] ?: emptySet()
        if (neighbors.contains(dest)) {
            return DestinationZone.ZONE_NEAR
        }
        return DestinationZone.ZONE_MIDDLE
    }

    /**
     * 兼容通用推导
     */
    fun resolveZone(origin: String, dest: String, isLocalCity: Boolean): DestinationZone {
        return resolveExpressParcelZone(origin, dest, isLocalCity)
    }
}
