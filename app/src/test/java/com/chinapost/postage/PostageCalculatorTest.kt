package com.chinapost.postage

import com.chinapost.postage.calculator.PostageCalculator
import com.chinapost.postage.model.ChinaProvinces
import com.chinapost.postage.model.DestinationZone
import com.chinapost.postage.model.OrdinaryParcelTier
import com.chinapost.postage.model.ParcelServiceType
import org.junit.Assert.assertEquals
import org.junit.Test

class PostageCalculatorTest {

    @Test
    fun testOrdinaryParcelHebeiToSichuan15kg() {
        // 官方附表 10-2：河北至四川为第四档（一般跨省）
        val tier = ChinaProvinces.resolveOrdinaryParcelTier("河北", "四川", false)
        assertEquals(OrdinaryParcelTier.TIER_4, tier)

        // 15kg 普通包裹费用计算：首重 8元 + 10kg内 9×3元 + 10-20kg 5×3元 = 8 + 27 + 15 = 50.00元
        val result = PostageCalculator.calculateParcel(
            serviceType = ParcelServiceType.ORDINARY_PARCEL,
            zone = DestinationZone.ZONE_MIDDLE,
            actualWeightKg = 15.0,
            originProvince = "河北",
            destProvince = "四川",
            isLocalCity = false
        )
        assertEquals(50.00, result.totalCost, 0.001)
        assertEquals(8.00, result.baseFreight, 0.001)
        assertEquals(42.00, result.extraFreight, 0.001)
    }

    @Test
    fun testExpressParcelHebeiToSichuan15kg() {
        // 官方附表 10-4：河北至四川快递包裹为第三区（一般跨省）
        // 15kg 快递包裹：首重 10元 + 续重 14×3元 = 52.00元
        val result = PostageCalculator.calculateParcel(
            serviceType = ParcelServiceType.EXPRESS_PARCEL,
            zone = DestinationZone.ZONE_MIDDLE,
            actualWeightKg = 15.0,
            originProvince = "河北",
            destProvince = "四川",
            isLocalCity = false
        )
        assertEquals(52.00, result.totalCost, 0.001)
    }

    @Test
    fun testEmsHebeiToSichuan15kg() {
        // 现行中国邮政特快专递标准（dey.11185.cn官网核实）：
        // 河北寄四川 15kg：首重 21.00元 + 续重 14×12.00元 = 21 + 168 = 189.00元
        val result = PostageCalculator.calculateParcel(
            serviceType = ParcelServiceType.EMS_STANDARD,
            zone = DestinationZone.ZONE_MIDDLE,
            actualWeightKg = 15.0,
            originProvince = "河北",
            destProvince = "四川",
            isLocalCity = false
        )
        assertEquals(189.00, result.totalCost, 0.001)
        assertEquals(21.00, result.baseFreight, 0.001)
        assertEquals(168.00, result.extraFreight, 0.001)
    }

    @Test
    fun testEmsHebeiToBaoding15kg() {
        // 省内特快专递（河北石家庄至保定）：首重 12元 + 续重 14×2元 = 40.00元
        val result = PostageCalculator.calculateParcel(
            serviceType = ParcelServiceType.EMS_STANDARD,
            zone = DestinationZone.SAME_PROVINCE,
            actualWeightKg = 15.0,
            originProvince = "河北",
            destProvince = "河北",
            isLocalCity = false
        )
        assertEquals(40.00, result.totalCost, 0.001)
    }

    @Test
    fun testEmsHebeiToBeijing15kg() {
        // 邻省特快专递（河北至北京）：首重 14元 + 续重 14×2元 = 42.00元
        val result = PostageCalculator.calculateParcel(
            serviceType = ParcelServiceType.EMS_STANDARD,
            zone = DestinationZone.ZONE_NEAR,
            actualWeightKg = 15.0,
            originProvince = "河北",
            destProvince = "北京",
            isLocalCity = false
        )
        assertEquals(42.00, result.totalCost, 0.001)
    }

    @Test
    fun testEmsHebeiToXinjiang15kg() {
        // 偏远特快专递（河北至新疆乌鲁木齐）：首重 24元 + 续重 14×28元 = 416.00元
        val result = PostageCalculator.calculateParcel(
            serviceType = ParcelServiceType.EMS_STANDARD,
            zone = DestinationZone.ZONE_REMOTE,
            actualWeightKg = 15.0,
            originProvince = "河北",
            destProvince = "新疆",
            isLocalCity = false
        )
        assertEquals(416.00, result.totalCost, 0.001)
    }

    @Test
    fun testHkLetter20gAnd100g() {
        // 港澳台航空平信（dey.11185.cn官网核实）：20g为2.50元，100g为9.00元
        val res20g = PostageCalculator.calculateHkMoTwLetter(
            type = com.chinapost.postage.model.HkMoTwMailType.LETTER,
            weightGrams = 20.0,
            isRegistered = false,
            hasReceipt = false,
            isAirMail = true
        )
        assertEquals(2.50, res20g.totalCost, 0.001)

        val res100g = PostageCalculator.calculateHkMoTwLetter(
            type = com.chinapost.postage.model.HkMoTwMailType.LETTER,
            weightGrams = 100.0,
            isRegistered = false,
            hasReceipt = false,
            isAirMail = true
        )
        assertEquals(9.00, res100g.totalCost, 0.001)
    }

    @Test
    fun testInternationalJapanAndUsAirLetter() {
        // 日本（第1组）：20g为5.00元，100g为13.00元
        val resJp20 = PostageCalculator.calculateInternationalLetter(
            type = com.chinapost.postage.model.InternationalMailType.LETTER,
            transportType = com.chinapost.postage.model.InternationalTransportType.AIR,
            zoneGroup = com.chinapost.postage.model.InternationalZoneGroup.GROUP_1,
            weightGrams = 20.0,
            isRegistered = false,
            hasReceipt = false
        )
        assertEquals(5.00, resJp20.totalCost, 0.001)

        val resJp100 = PostageCalculator.calculateInternationalLetter(
            type = com.chinapost.postage.model.InternationalMailType.LETTER,
            transportType = com.chinapost.postage.model.InternationalTransportType.AIR,
            zoneGroup = com.chinapost.postage.model.InternationalZoneGroup.GROUP_1,
            weightGrams = 100.0,
            isRegistered = false,
            hasReceipt = false
        )
        assertEquals(13.00, resJp100.totalCost, 0.001)

        // 美国（第3组）：20g为6.00元，100g为20.40元
        val resUs20 = PostageCalculator.calculateInternationalLetter(
            type = com.chinapost.postage.model.InternationalMailType.LETTER,
            transportType = com.chinapost.postage.model.InternationalTransportType.AIR,
            zoneGroup = com.chinapost.postage.model.InternationalZoneGroup.GROUP_3,
            weightGrams = 20.0,
            isRegistered = false,
            hasReceipt = false
        )
        assertEquals(6.00, resUs20.totalCost, 0.001)

        val resUs100 = PostageCalculator.calculateInternationalLetter(
            type = com.chinapost.postage.model.InternationalMailType.LETTER,
            transportType = com.chinapost.postage.model.InternationalTransportType.AIR,
            zoneGroup = com.chinapost.postage.model.InternationalZoneGroup.GROUP_3,
            weightGrams = 100.0,
            isRegistered = false,
            hasReceipt = false
        )
        assertEquals(20.40, resUs100.totalCost, 0.001)
    }

    @Test
    fun testWeightExceedsLimitWarning() {
        // 80kg超出了快递包裹(20kg)、EMS(40kg)、普通包裹(50kg)的上限
        val resEms80 = PostageCalculator.calculateParcel(
            serviceType = ParcelServiceType.EMS_STANDARD,
            zone = DestinationZone.ZONE_MIDDLE,
            actualWeightKg = 80.0,
            originProvince = "河北",
            destProvince = "四川"
        )
        org.junit.Assert.assertTrue(resEms80.remarks.any { it.contains("超出上限") })
    }
}
