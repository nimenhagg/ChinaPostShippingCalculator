package com.chinapost.postage.model

enum class MailDestinationCategory(val displayName: String) {
    DOMESTIC("国内函件"),
    HK_MO_TW("港澳台函件"),
    INTERNATIONAL("国际函件")
}

enum class MailType(val displayName: String, val maxWeightGrams: Int, val description: String) {
    ORDINARY_LETTER("平常信函", 2000, "最高限重2000克"),
    POSTCARD("明信片", 20, "国内统一0.80元/件"),
    PRINTED_MATTER("印刷品", 35000, "书籍报刊宣传页，最高限重35公斤"),
    BLIND_LITERATURE("盲人读物", 7000, "免收基本普通邮资")
}

enum class MailScope(val displayName: String, val description: String) {
    LOCAL("本埠（同城/本县）", "本市县辖区内"),
    DOMESTIC("外埠（国内跨市/全国各地）", "跨市县及全国其他省市")
}

enum class HkMoTwMailType(val displayName: String, val maxWeightGrams: Int) {
    LETTER("信函", 2000),
    POSTCARD("明信片", 20),
    AEROGRAMME("航空邮简", 20)
}

enum class InternationalMailType(val displayName: String, val maxWeightGrams: Int) {
    LETTER("国际信函", 2000),
    POSTCARD("国际明信片", 20)
}

enum class InternationalTransportType(val displayName: String) {
    AIR("航空函件"),
    SAL("空运水陆路 (SAL)"),
    SURFACE("水陆路函件")
}

enum class InternationalZoneGroup(val groupName: String, val countriesExample: String) {
    GROUP_1("第一组", "朝鲜、蒙古、越南、日本、韩国、哈萨克斯坦等亚洲近邻"),
    GROUP_2("第二组", "新加坡、马来西亚、泰国、阿联酋、印度等其他亚洲国家或地区"),
    GROUP_3("第三组", "欧洲各国、美国、加拿大、澳大利亚、新西兰"),
    GROUP_4("第四组", "美洲其他国家、非洲各国、大洋洲岛屿等")
}

data class LetterCalculationResult(
    val basePostage: Double,
    val baseFormula: String,
    val airFee: Double = 0.0,
    val registrationFee: Double = 0.0,
    val receiptFee: Double = 0.0,
    val insuranceFee: Double = 0.0,
    val totalCost: Double,
    val tips: List<String>
)

enum class OrdinaryParcelTier(
    val tierNumber: Int,
    val displayName: String,
    val firstKgCost: Double,
    val stage1Rate: Double, // 10kg及以内每续重1kg
    val stage2Rate: Double, // 10-20kg每续重1kg
    val stage3Rate: Double  // 20kg以上每续重1kg
) {
    TIER_1(1, "一档 (省内/同城)", 5.0, 1.0, 2.0, 3.0),
    TIER_2(2, "二档 (邻省核心圈)", 6.0, 1.5, 2.0, 3.0),
    TIER_3(3, "三档 (邻近跨省)", 7.0, 2.0, 2.0, 3.0),
    TIER_4(4, "四档 (一般跨省)", 8.0, 3.0, 3.0, 4.0),
    TIER_5(5, "五档 (远途跨省)", 9.0, 4.0, 4.0, 5.0),
    TIER_6(6, "六档 (偏远跨省)", 10.0, 5.0, 7.0, 8.0)
}

enum class ParcelServiceType(
    val displayName: String,
    val shortName: String,
    val speedDesc: String,
    val unitDesc: String,
    val maxWeightKg: Int
) {
    ORDINARY_PARCEL(
        "普通包裹（普包/平邮）",
        "普通包裹",
        "约7-15天",
        "首重1kg，6档阶梯续重（限重50kg·最实惠）",
        50
    ),
    EXPRESS_PARCEL(
        "快递包裹（快包）",
        "快递包裹",
        "约3-6天",
        "500g内特惠；500g以上首重1kg/续重1kg（限重20kg·送货上门）",
        20
    ),
    EMS_STANDARD(
        "EMS特快专递",
        "EMS特快",
        "约1-2天",
        "起重500g统一20元，按500g进位续重（时效最快）",
        40
    )
}

enum class DestinationZone(
    val displayName: String,
    val zoneName: String,
    val zoneCode: Int
) {
    LOCAL_CITY("本埠/同城", "同城", 0),
    SAME_PROVINCE("本省内", "一区", 1),
    ZONE_NEAR("跨省周边", "二区", 2),
    ZONE_MIDDLE("一般跨省", "三区", 3),
    ZONE_FAR("跨省远距离", "四区", 4),
    ZONE_REMOTE("偏远跨省 (藏/疆/青)", "五区", 5)
}

data class ParcelCalculationResult(
    val chargeableWeightKg: Double,
    val actualWeightKg: Double,
    val volumetricWeightKg: Double,
    val isVolumetricCharged: Boolean,
    val baseFreight: Double,
    val extraFreight: Double,
    val extraDetails: String,
    val registrationFee: Double = 0.0,
    val insuranceFee: Double = 0.0,
    val totalCost: Double,
    val remarks: List<String>
)
