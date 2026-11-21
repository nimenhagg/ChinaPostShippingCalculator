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
        // 官方附录十二：河北寄四川特快专递为第二区（续重6元/500g）
        // 起重 20元 + 续重 29个500g × 6元 = 20 + 174 = 194.00元
        val result = PostageCalculator.calculateParcel(
            serviceType = ParcelServiceType.EMS_STANDARD,
            zone = DestinationZone.ZONE_NEAR,
            actualWeightKg = 15.0,
            originProvince = "河北",
            destProvince = "四川",
            isLocalCity = false
        )
        assertEquals(194.00, result.totalCost, 0.001)
    }
}
