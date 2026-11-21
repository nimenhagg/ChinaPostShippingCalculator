package com.chinapost.postage.model

data class CountryRegion(
    val code: String,
    val nameCn: String,
    val nameEn: String,
    val pinyin: String,
    val pinyinShort: String,
    val category: MailDestinationCategory,
    val zoneGroup: InternationalZoneGroup? = null,
    val isHot: Boolean = false
) {
    fun matches(query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim().lowercase()
        return nameCn.contains(q) ||
                nameEn.lowercase().contains(q) ||
                pinyin.lowercase().contains(q) ||
                pinyinShort.lowercase().contains(q) ||
                code.lowercase().contains(q)
    }

    val displayCategoryAndGroup: String
        get() = when (category) {
            MailDestinationCategory.HK_MO_TW -> "港澳台地区"
            MailDestinationCategory.INTERNATIONAL -> "国际 ${zoneGroup?.groupName ?: ""}"
            else -> "国内"
        }
}

object CountryRepository {

    val ALL = listOf(
        // 港澳台地区
        CountryRegion("HK", "中国香港", "Hong Kong", "zhongguoxianggang", "zgxg", MailDestinationCategory.HK_MO_TW, isHot = true),
        CountryRegion("MO", "中国澳门", "Macau", "zhongguoaomen", "zgam", MailDestinationCategory.HK_MO_TW, isHot = true),
        CountryRegion("TW", "中国台湾", "Taiwan", "zhongguotaiwan", "zgtw", MailDestinationCategory.HK_MO_TW, isHot = true),

        // 国际第一组 (亚洲近邻国家)
        CountryRegion("JP", "日本", "Japan", "riben", "rb", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_1, isHot = true),
        CountryRegion("KR", "韩国", "South Korea", "hanguo", "hg", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_1, isHot = true),
        CountryRegion("KP", "朝鲜", "North Korea", "chaoxian", "cx", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_1),
        CountryRegion("MN", "蒙古", "Mongolia", "menggu", "mg", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_1),
        CountryRegion("VN", "越南", "Vietnam", "yuenan", "yn", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_1),
        CountryRegion("LA", "老挝", "Laos", "laowo", "lw", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_1),
        CountryRegion("KH", "柬埔寨", "Cambodia", "jianpuzhai", "jpz", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_1),
        CountryRegion("MM", "缅甸", "Myanmar", "miandian", "md", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_1),
        CountryRegion("KZ", "哈萨克斯坦", "Kazakhstan", "hasakesitan", "hskst", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_1),
        CountryRegion("UZ", "乌兹别克斯坦", "Uzbekistan", "wuzibiekesitan", "wzbkst", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_1),
        CountryRegion("KG", "吉尔吉斯斯坦", "Kyrgyzstan", "jierjisistan", "jejsst", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_1),
        CountryRegion("TJ", "塔吉克斯坦", "Tajikistan", "tajikesitan", "tjkst", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_1),
        CountryRegion("TM", "土库曼斯坦", "Turkmenistan", "tukumansitan", "tkmst", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_1),

        // 国际第二组 (其他亚洲国家或地区)
        CountryRegion("SG", "新加坡", "Singapore", "xinjiapo", "xjp", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_2, isHot = true),
        CountryRegion("MY", "马来西亚", "Malaysia", "malaixiya", "mlxy", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_2, isHot = true),
        CountryRegion("TH", "泰国", "Thailand", "taiguo", "tg", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_2, isHot = true),
        CountryRegion("PH", "菲律宾", "Philippines", "feilvbin", "flb", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_2),
        CountryRegion("ID", "印度尼西亚", "Indonesia", "yindunixiya", "ydnxy", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_2),
        CountryRegion("IN", "印度", "India", "yindu", "yd", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_2),
        CountryRegion("PK", "巴基斯坦", "Pakistan", "bajisitan", "bjst", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_2),
        CountryRegion("BD", "孟加拉国", "Bangladesh", "mengjialaguo", "mjlg", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_2),
        CountryRegion("LK", "斯里兰卡", "Sri Lanka", "sililanka", "sllk", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_2),
        CountryRegion("NP", "尼泊尔", "Nepal", "niboer", "nbe", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_2),
        CountryRegion("AE", "阿联酋 (阿拉伯联合酋长国)", "United Arab Emirates", "alianqiu", "alq", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_2, isHot = true),
        CountryRegion("SA", "沙特阿拉伯", "Saudi Arabia", "shatealabo", "stalb", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_2),
        CountryRegion("TR", "土耳其", "Turkey", "tuerqi", "teq", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_2),
        CountryRegion("IL", "以色列", "Israel", "yiselie", "ysl", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_2),
        CountryRegion("QA", "卡塔尔", "Qatar", "kataer", "kte", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_2),
        CountryRegion("KW", "科威特", "Kuwait", "keweite", "kwt", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_2),
        CountryRegion("OM", "阿曼", "Oman", "aman", "am", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_2),
        CountryRegion("JO", "约旦", "Jordan", "yuedan", "yd", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_2),
        CountryRegion("IR", "伊朗", "Iran", "yilang", "yl", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_2),
        CountryRegion("BN", "文莱", "Brunei", "wenlai", "wl", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_2),
        CountryRegion("MV", "马尔代夫", "Maldives", "maerdaifu", "medf", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_2),
        CountryRegion("GE", "格鲁吉亚", "Georgia", "gelujiya", "gljy", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_2),
        CountryRegion("AZ", "阿塞拜疆", "Azerbaijan", "asaibaijiang", "asbj", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_2),
        CountryRegion("AM", "亚美尼亚", "Armenia", "yameiniya", "ymny", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_2),

        // 国际第三组 (欧洲各国、美国、加拿大、澳大利亚、新西兰)
        CountryRegion("US", "美国", "United States", "meiguo", "mg", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3, isHot = true),
        CountryRegion("GB", "英国", "United Kingdom", "yingguo", "yg", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3, isHot = true),
        CountryRegion("DE", "德国", "Germany", "deguo", "dg", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3, isHot = true),
        CountryRegion("FR", "法国", "France", "faguo", "fg", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3, isHot = true),
        CountryRegion("AU", "澳大利亚", "Australia", "aodaliya", "adly", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3, isHot = true),
        CountryRegion("CA", "加拿大", "Canada", "jianada", "jnd", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3, isHot = true),
        CountryRegion("NZ", "新西兰", "New Zealand", "xinxilan", "xxl", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3, isHot = true),
        CountryRegion("RU", "俄罗斯", "Russia", "eluosi", "els", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3, isHot = true),
        CountryRegion("IT", "意大利", "Italy", "yidali", "ydl", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3, isHot = true),
        CountryRegion("ES", "西班牙", "Spain", "xibanya", "xby", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),
        CountryRegion("PT", "葡萄牙", "Portugal", "putaoya", "pty", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),
        CountryRegion("NL", "荷兰", "Netherlands", "helan", "hl", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),
        CountryRegion("BE", "比利时", "Belgium", "bilishi", "bls", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),
        CountryRegion("CH", "瑞士", "Switzerland", "ruishi", "rs", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),
        CountryRegion("SE", "瑞典", "Sweden", "ruidian", "rd", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),
        CountryRegion("NO", "挪威", "Norway", "nuowei", "nw", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),
        CountryRegion("DK", "丹麦", "Denmark", "danmai", "dm", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),
        CountryRegion("FI", "芬兰", "Finland", "fenlan", "fl", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),
        CountryRegion("IE", "爱尔兰", "Ireland", "aierlan", "ael", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),
        CountryRegion("PL", "波兰", "Poland", "bolan", "bl", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),
        CountryRegion("AT", "奥地利", "Austria", "aodili", "adl", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),
        CountryRegion("GR", "希腊", "Greece", "xila", "xl", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),
        CountryRegion("CZ", "捷克", "Czech Republic", "jieke", "jk", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),
        CountryRegion("HU", "匈牙利", "Hungary", "xiongyali", "xyl", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),
        CountryRegion("UA", "乌克兰", "Ukraine", "wukelan", "wkl", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),
        CountryRegion("RO", "罗马尼亚", "Romania", "luomaniya", "lmny", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),
        CountryRegion("SK", "斯洛伐克", "Slovakia", "siluofake", "slfk", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),
        CountryRegion("HR", "克罗地亚", "Croatia", "keluodiya", "kldy", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),
        CountryRegion("RS", "塞尔维亚", "Serbia", "saierweiya", "sewy", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),
        CountryRegion("BG", "保加利亚", "Bulgaria", "baojialiya", "bjly", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),
        CountryRegion("BY", "白俄罗斯", "Belarus", "baieluosi", "bels", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),
        CountryRegion("EE", "爱沙尼亚", "Estonia", "aishaniya", "asny", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),
        CountryRegion("LV", "拉脱维亚", "Latvia", "latuoweiya", "ltwy", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),
        CountryRegion("LT", "立陶宛", "Lithuania", "litaowan", "ltw", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),
        CountryRegion("IS", "冰岛", "Iceland", "bingdao", "bd", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),
        CountryRegion("LU", "卢森堡", "Luxembourg", "lusenbao", "lsb", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_3),

        // 国际第四组 (美洲其他国家、非洲各国、大洋洲岛屿)
        CountryRegion("BR", "巴西", "Brazil", "baxi", "bx", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_4, isHot = true),
        CountryRegion("AR", "阿根廷", "Argentina", "agenting", "agt", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_4),
        CountryRegion("MX", "墨西哥", "Mexico", "moxige", "mxg", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_4),
        CountryRegion("CL", "智利", "Chile", "zhili", "zl", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_4),
        CountryRegion("PE", "秘鲁", "Peru", "milu", "ml", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_4),
        CountryRegion("CO", "哥伦比亚", "Colombia", "gelunbiya", "glby", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_4),
        CountryRegion("VE", "委内瑞拉", "Venezuela", "weineiruila", "wnrl", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_4),
        CountryRegion("EG", "埃及", "Egypt", "aiji", "aj", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_4, isHot = true),
        CountryRegion("ZA", "南非", "South Africa", "nanfei", "nf", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_4, isHot = true),
        CountryRegion("KE", "肯尼亚", "Kenya", "kenniya", "kny", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_4),
        CountryRegion("NG", "尼日利亚", "Nigeria", "niriliya", "nrly", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_4),
        CountryRegion("MA", "摩洛哥", "Morocco", "moluoge", "mlg", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_4),
        CountryRegion("ET", "埃塞俄比亚", "Ethiopia", "aisaiebiya", "aseby", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_4),
        CountryRegion("GH", "加纳", "Ghana", "jiana", "jn", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_4),
        CountryRegion("TZ", "坦桑尼亚", "Tanzania", "tansangniya", "tsny", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_4),
        CountryRegion("DZ", "阿尔及利亚", "Algeria", "aerjiliya", "aejly", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_4),
        CountryRegion("TN", "突尼斯", "Tunisia", "tunisi", "tns", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_4),
        CountryRegion("FJ", "斐济", "Fiji", "feiji", "fj", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_4),
        CountryRegion("PG", "巴布亚新几内亚", "Papua New Guinea", "babuyaxinjineiya", "bbyxjny", MailDestinationCategory.INTERNATIONAL, InternationalZoneGroup.GROUP_4)
    )

    val HOT_REGIONS = ALL.filter { it.isHot }
}
