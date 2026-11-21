package com.chinapost.postage.calculator

import com.chinapost.postage.model.*
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

object PostageCalculator {

    /**
     * 计算国内函件资费（依据《邮政营业员（教材）》附录十附表10-1标准）
     */
    fun calculateDomesticLetter(
        type: MailType,
        scope: MailScope,
        weightGrams: Double,
        isRegistered: Boolean,
        hasReceipt: Boolean,
        declaredValue: Double = 0.0
    ): LetterCalculationResult {
        val tips = mutableListOf<String>()

        if (weightGrams <= 0) {
            return LetterCalculationResult(
                basePostage = 0.0,
                baseFormula = "请输入有效重量",
                registrationFee = 0.0,
                receiptFee = 0.0,
                insuranceFee = 0.0,
                totalCost = 0.0,
                tips = listOf("重量需大于0克")
            )
        }

        if (weightGrams > type.maxWeightGrams) {
            tips.add("当前重量 ${weightGrams}g 超过${type.displayName}最大上限 ${type.maxWeightGrams}g")
        }

        var baseCost = 0.0
        var baseFormula = ""

        when (type) {
            MailType.ORDINARY_LETTER -> {
                if (weightGrams <= 100) {
                    val units = ceil(weightGrams / 20.0).toInt().coerceAtLeast(1)
                    val unitRate = if (scope == MailScope.LOCAL) 0.80 else 1.20
                    baseCost = units * unitRate
                    baseFormula = "首重100g内：${units}个20g × ${unitRate}元 = ${String.format("%.2f", baseCost)}元"
                } else {
                    val first100Cost = if (scope == MailScope.LOCAL) 4.00 else 6.00
                    val extraGrams = weightGrams - 100
                    val extraUnits = ceil(extraGrams / 100.0).toInt().coerceAtLeast(1)
                    val extraRate = if (scope == MailScope.LOCAL) 1.20 else 2.00
                    val extraCost = extraUnits * extraRate
                    baseCost = first100Cost + extraCost
                    baseFormula = "首重100g(${String.format("%.2f", first100Cost)}元) + 续重(${extraUnits}个100g × ${extraRate}元 = ${String.format("%.2f", extraCost)}元)"
                }
            }
            MailType.POSTCARD -> {
                baseCost = 0.80
                baseFormula = "国内明信片统一资费：0.80元/件"
            }
            MailType.PRINTED_MATTER -> {
                val firstCost = if (scope == MailScope.LOCAL) 0.80 else 1.20
                if (weightGrams <= 100) {
                    baseCost = firstCost
                    baseFormula = "印刷品首重100g：${if (scope == MailScope.LOCAL) "本埠0.80元" else "外埠1.20元"}"
                } else {
                    val extraUnits = ceil((weightGrams - 100) / 100.0).toInt().coerceAtLeast(1)
                    val extraRate = if (scope == MailScope.LOCAL) 0.20 else 0.40
                    val extraCost = extraUnits * extraRate
                    baseCost = firstCost + extraCost
                    baseFormula = "首重100g(${String.format("%.2f", firstCost)}元) + 续重(${extraUnits}个100g × ${extraRate}元 = ${String.format("%.2f", extraCost)}元)"
                }
            }
            MailType.BLIND_LITERATURE -> {
                baseCost = 0.0
                baseFormula = "盲人读物免收基本普通邮资"
            }
        }

        val registrationCost = if (isRegistered) 3.00 else 0.00
        val receiptCost = if (hasReceipt) 3.00 else 0.00

        // 保价费：每保100元或零数收1.00元（1%，最低1元）
        val insuranceCost = if (declaredValue > 0) {
            val blocks = ceil(declaredValue / 100.0).toInt().coerceAtLeast(1)
            blocks * 1.00
        } else {
            0.00
        }

        val totalCost = roundToTwoDecimals(baseCost + registrationCost + receiptCost + insuranceCost)

        return LetterCalculationResult(
            basePostage = roundToTwoDecimals(baseCost),
            baseFormula = baseFormula,
            registrationFee = registrationCost,
            receiptFee = receiptCost,
            insuranceFee = insuranceCost,
            totalCost = totalCost,
            tips = tips
        )
    }

    /**
     * 计算港澳台函件资费（依据《国际及台港澳邮件处理规则（2022版）》附录三标准）
     */
    fun calculateHkMoTwLetter(
        type: HkMoTwMailType,
        weightGrams: Double,
        isRegistered: Boolean,
        hasReceipt: Boolean,
        isAirMail: Boolean
    ): LetterCalculationResult {
        val tips = mutableListOf<String>()

        if (weightGrams <= 0) {
            return LetterCalculationResult(
                basePostage = 0.0,
                baseFormula = "请输入有效重量",
                registrationFee = 0.0,
                receiptFee = 0.0,
                totalCost = 0.0,
                tips = listOf("重量需大于0克")
            )
        }

        if (weightGrams > type.maxWeightGrams) {
            tips.add("重量 ${weightGrams}g 超过限重 ${type.maxWeightGrams}g")
        }

        var baseCost = 0.0
        var baseFormula = ""
        var airCost = 0.0

        when (type) {
            HkMoTwMailType.LETTER -> {
                when {
                    weightGrams <= 20 -> {
                        baseCost = 1.50
                        baseFormula = "信函20克及以内：1.50元"
                    }
                    weightGrams <= 50 -> {
                        baseCost = 2.80
                        baseFormula = "信函20-50克：2.80元"
                    }
                    weightGrams <= 100 -> {
                        baseCost = 4.00
                        baseFormula = "信函50-100克：4.00元"
                    }
                    weightGrams <= 250 -> {
                        baseCost = 8.50
                        baseFormula = "信函100-250克：8.50元"
                    }
                    weightGrams <= 500 -> {
                        baseCost = 16.70
                        baseFormula = "信函250-500克：16.70元"
                    }
                    weightGrams <= 1000 -> {
                        baseCost = 31.70
                        baseFormula = "信函500-1000克：31.70元"
                    }
                    else -> {
                        baseCost = 55.80
                        baseFormula = "信函1000-2000克：55.80元"
                    }
                }

                if (isAirMail) {
                    val airUnits = ceil(weightGrams / 10.0).toInt().coerceAtLeast(1)
                    airCost = airUnits * 0.50
                }
            }
            HkMoTwMailType.POSTCARD -> {
                baseCost = 3.50
                baseFormula = "港澳台明信片：3.50元/件"
                if (isAirMail) {
                    airCost = 0.50
                }
            }
            HkMoTwMailType.AEROGRAMME -> {
                baseCost = 1.80
                baseFormula = "港澳台航空邮简：1.80元/件（已含航空费）"
            }
        }

        val registrationCost = if (isRegistered) 16.00 else 0.00
        val receiptCost = if (hasReceipt) 3.00 else 0.00

        if (hasReceipt) {
            tips.add("注：寄往台湾省的函件目前暂不办收附回执业务")
        }

        val totalCost = roundToTwoDecimals(baseCost + airCost + registrationCost + receiptCost)

        return LetterCalculationResult(
            basePostage = roundToTwoDecimals(baseCost),
            baseFormula = baseFormula,
            airFee = roundToTwoDecimals(airCost),
            registrationFee = registrationCost,
            receiptFee = receiptCost,
            totalCost = totalCost,
            tips = tips
        )
    }

    /**
     * 计算国际函件资费（依据《国际及台港澳邮件处理规则（2022版）》附录二标准）
     */
    fun calculateInternationalLetter(
        type: InternationalMailType,
        transportType: InternationalTransportType,
        zoneGroup: InternationalZoneGroup,
        weightGrams: Double,
        isRegistered: Boolean,
        hasReceipt: Boolean
    ): LetterCalculationResult {
        val tips = mutableListOf<String>()

        if (weightGrams <= 0) {
            return LetterCalculationResult(
                basePostage = 0.0,
                baseFormula = "请输入有效重量",
                registrationFee = 0.0,
                receiptFee = 0.0,
                totalCost = 0.0,
                tips = listOf("重量需大于0克")
            )
        }

        if (weightGrams > type.maxWeightGrams) {
            tips.add("重量 ${weightGrams}g 超过限重 ${type.maxWeightGrams}g")
        }

        var baseCost = 0.0
        var baseFormula = ""

        when (type) {
            InternationalMailType.POSTCARD -> {
                when (transportType) {
                    InternationalTransportType.AIR -> {
                        // 2022版官方附录二：明信片第一组4.50元，第二组5.00元，第三组5.50元
                        baseCost = when (zoneGroup) {
                            InternationalZoneGroup.GROUP_1 -> 4.50
                            InternationalZoneGroup.GROUP_2 -> 5.00
                            InternationalZoneGroup.GROUP_3 -> 5.50
                            InternationalZoneGroup.GROUP_4 -> 5.50
                        }
                        baseFormula = "国际航空明信片(${zoneGroup.groupName})：${String.format("%.2f", baseCost)}元/件"
                    }
                    InternationalTransportType.SAL -> {
                        baseCost = 4.50
                        baseFormula = "国际SAL明信片：4.50元/件"
                    }
                    InternationalTransportType.SURFACE -> {
                        baseCost = 3.50
                        baseFormula = "国际水陆路明信片：3.50元/件"
                    }
                }
            }

            InternationalMailType.LETTER -> {
                when (transportType) {
                    InternationalTransportType.AIR -> {
                        val (first20g, extra10g) = when (zoneGroup) {
                            InternationalZoneGroup.GROUP_1 -> Pair(5.00, 1.00)
                            InternationalZoneGroup.GROUP_2 -> Pair(5.50, 1.50)
                            InternationalZoneGroup.GROUP_3 -> Pair(6.00, 1.80)
                            InternationalZoneGroup.GROUP_4 -> Pair(7.00, 2.30)
                        }

                        if (weightGrams <= 20) {
                            baseCost = first20g
                            baseFormula = "航空信函20g以内(${zoneGroup.groupName})：${String.format("%.2f", first20g)}元"
                        } else {
                            val extraUnits = ceil((weightGrams - 20) / 10.0).toInt().coerceAtLeast(1)
                            val extraCost = extraUnits * extra10g
                            baseCost = first20g + extraCost
                            baseFormula = "首重20g(${String.format("%.2f", first20g)}元) + 续重(${extraUnits}个10g × ${extra10g}元 = ${String.format("%.2f", extraCost)}元)"
                        }
                    }

                    InternationalTransportType.SAL -> {
                        val (first20g, extra10g) = when (zoneGroup) {
                            InternationalZoneGroup.GROUP_1 -> Pair(4.50, 0.50)
                            InternationalZoneGroup.GROUP_2 -> Pair(5.00, 0.60)
                            InternationalZoneGroup.GROUP_3 -> Pair(5.50, 0.70)
                            InternationalZoneGroup.GROUP_4 -> Pair(6.50, 0.80)
                        }

                        if (weightGrams <= 20) {
                            baseCost = first20g
                            baseFormula = "SAL信函20g以内(${zoneGroup.groupName})：${String.format("%.2f", first20g)}元"
                        } else {
                            val extraUnits = ceil((weightGrams - 20) / 10.0).toInt().coerceAtLeast(1)
                            val extraCost = extraUnits * extra10g
                            baseCost = first20g + extraCost
                            baseFormula = "首重20g(${String.format("%.2f", first20g)}元) + 续重(${extraUnits}个10g × ${extra10g}元 = ${String.format("%.2f", extraCost)}元)"
                        }
                    }

                    InternationalTransportType.SURFACE -> {
                        val first20g = 4.00
                        val extra10g = 0.50
                        if (weightGrams <= 20) {
                            baseCost = first20g
                            baseFormula = "水陆路信函20g以内：4.00元"
                        } else {
                            val extraUnits = ceil((weightGrams - 20) / 10.0).toInt().coerceAtLeast(1)
                            val extraCost = extraUnits * extra10g
                            baseCost = first20g + extraCost
                            baseFormula = "首重20g(4.00元) + 续重(${extraUnits}个10g × 0.50元 = ${String.format("%.2f", extraCost)}元)"
                        }
                    }
                }
            }
        }

        val registrationCost = if (isRegistered) 16.00 else 0.00
        val receiptCost = if (hasReceipt) 5.00 else 0.00

        val totalCost = roundToTwoDecimals(baseCost + registrationCost + receiptCost)

        return LetterCalculationResult(
            basePostage = roundToTwoDecimals(baseCost),
            baseFormula = baseFormula,
            registrationFee = registrationCost,
            receiptFee = receiptCost,
            totalCost = totalCost,
            tips = tips
        )
    }

    /**
     * 计算包裹快递运费（依据教材附录十、十一、十二官方资费标准）
     */
    fun calculateParcel(
        serviceType: ParcelServiceType,
        zone: DestinationZone,
        actualWeightKg: Double,
        lengthCm: Double = 0.0,
        widthCm: Double = 0.0,
        heightCm: Double = 0.0,
        declaredValue: Double = 0.0,
        originProvince: String = "河北",
        destProvince: String = "四川",
        isLocalCity: Boolean = false
    ): ParcelCalculationResult {
        val remarks = mutableListOf<String>()

        var volumetricWeightKg = 0.0
        var isVolumetric = false

        // 严格根据官方规范执行各业务计泡规则：
        when (serviceType) {
            ParcelServiceType.EXPRESS_PARCEL -> {
                // 官方标准：长宽高三边之和大于（含等于）100cm 进行计泡
                if (lengthCm + widthCm + heightCm >= 100.0) {
                    val divisor = if (zone == DestinationZone.ZONE_FAR || zone == DestinationZone.ZONE_REMOTE) 5000.0 else 8000.0
                    volumetricWeightKg = roundToTwoDecimals((lengthCm * widthCm * heightCm) / divisor)
                    isVolumetric = volumetricWeightKg > actualWeightKg
                }
            }
            ParcelServiceType.EMS_STANDARD -> {
                // 官方标准：任一单边达到60cm（含）以上进行计泡，除以6000
                if (lengthCm >= 60.0 || widthCm >= 60.0 || heightCm >= 60.0) {
                    volumetricWeightKg = roundToTwoDecimals((lengthCm * widthCm * heightCm) / 6000.0)
                    isVolumetric = volumetricWeightKg > actualWeightKg
                }
            }
            ParcelServiceType.ORDINARY_PARCEL -> {
                // 官方标准：单边最大长度超过50cm进行计泡，除以6000
                if (max(max(lengthCm, widthCm), heightCm) > 50.0) {
                    volumetricWeightKg = roundToTwoDecimals((lengthCm * widthCm * heightCm) / 6000.0)
                    isVolumetric = volumetricWeightKg > actualWeightKg
                }
            }
        }

        val rawChargeable = max(actualWeightKg, volumetricWeightKg).coerceAtLeast(0.01)

        var baseFreight = 0.0
        var extraFreight = 0.0
        var extraDetails = ""
        var chargeableWeightKg = rawChargeable

        when (serviceType) {
            ParcelServiceType.ORDINARY_PARCEL -> {
                // 官方附表 10-2：国内普通包裹资费表（首重1kg，区分6档阶梯续重，免收挂号单费）
                val units1kg = ceil(rawChargeable).toInt().coerceAtLeast(1)
                chargeableWeightKg = units1kg.toDouble()

                val tier = ChinaProvinces.resolveOrdinaryParcelTier(originProvince, destProvince, isLocalCity)
                baseFreight = tier.firstKgCost

                if (units1kg > 1) {
                    val extraKg = units1kg - 1
                    val stage1Kg = minOf(extraKg, 9) // 2kg至10kg部分，最多9kg
                    val stage2Kg = if (units1kg > 10) minOf(units1kg - 10, 10) else 0 // 11kg至20kg部分，最多10kg
                    val stage3Kg = if (units1kg > 20) units1kg - 20 else 0 // 20kg以上部分

                    val cost1 = stage1Kg * tier.stage1Rate
                    val cost2 = stage2Kg * tier.stage2Rate
                    val cost3 = stage3Kg * tier.stage3Rate

                    extraFreight = cost1 + cost2 + cost3

                    val parts = mutableListOf<String>()
                    if (stage1Kg > 0) parts.add("10kg内${stage1Kg}kg×${tier.stage1Rate}元")
                    if (stage2Kg > 0) parts.add("10-20kg段${stage2Kg}kg×${tier.stage2Rate}元")
                    if (stage3Kg > 0) parts.add("20kg以上${stage3Kg}kg×${tier.stage3Rate}元")

                    extraDetails = "首重1kg(${tier.firstKgCost}元) + 续重(${parts.joinToString(" + ")}) = ${String.format("%.2f", extraFreight)}元【${tier.displayName}】"
                } else {
                    extraDetails = "首重1kg以内：${tier.firstKgCost}元【${tier.displayName}】"
                }

                if (rawChargeable > 50.0) {
                    remarks.add("普通包裹最高限重50kg，当前重量 ${rawChargeable}kg 已超出上限！")
                }
            }

            ParcelServiceType.EXPRESS_PARCEL -> {
                val effectiveZone = ChinaProvinces.resolveExpressParcelZone(originProvince, destProvince, isLocalCity)
                // 官方附表10-3：单件重量500g（含）以内特惠资费标准
                if (rawChargeable <= 0.5) {
                    chargeableWeightKg = 0.5
                    val smallPrice = when (effectiveZone) {
                        DestinationZone.LOCAL_CITY,
                        DestinationZone.SAME_PROVINCE -> {
                            if (destProvince == "内蒙古" || destProvince == "甘肃") 8.0
                            else if (ChinaProvinces.REMOTE_WEST.contains(destProvince)) 10.0
                            else 5.0
                        }
                        DestinationZone.ZONE_REMOTE -> 12.0 // 寄往西藏、新疆、青海省际 12元
                        else -> 8.0 // 省际统一8元
                    }
                    baseFreight = smallPrice
                    extraFreight = 0.0
                    extraDetails = "500g以内小件特惠价：${String.format("%.2f", smallPrice)}元"
                } else {
                    // 官方附表10-4：500g以上快递包裹资费表（首重1kg，续重按公斤计）
                    val units1kg = ceil(rawChargeable).toInt().coerceAtLeast(1)
                    chargeableWeightKg = units1kg.toDouble()

                    val (firstCost, extraRatePerKg) = when (effectiveZone) {
                        DestinationZone.LOCAL_CITY,
                        DestinationZone.SAME_PROVINCE -> Pair(6.00, 2.00) // 一区（省内/同城）首重6 续重2
                        DestinationZone.ZONE_NEAR -> Pair(8.00, 2.00)     // 二区（周边跨省）首重8 续重2
                        DestinationZone.ZONE_MIDDLE -> Pair(10.00, 3.00)  // 三区（一般跨省）首重10 续重3
                        DestinationZone.ZONE_FAR -> Pair(14.00, 4.00)     // 四区（远途跨省）首重14 续重4
                        DestinationZone.ZONE_REMOTE -> Pair(16.00, 12.00) // 五区（西藏/青海/新疆）首重16 续重12
                    }

                    baseFreight = firstCost
                    val extraUnits = units1kg - 1
                    extraFreight = extraUnits * extraRatePerKg
                    extraDetails = if (extraUnits > 0) {
                        "首重1kg(${firstCost}元) + 续重${extraUnits}kg × ${extraRatePerKg}元 = ${String.format("%.2f", extraFreight)}元"
                    } else {
                        "首重1kg以内：${firstCost}元"
                    }
                }

                if (rawChargeable > 20.0) {
                    remarks.add("快递包裹最高限重20kg，当前重量 ${rawChargeable}kg 已超出上限！超重请改选普通包裹（限重50kg）")
                }
            }

            ParcelServiceType.EMS_STANDARD -> {
                val effectiveZone = ChinaProvinces.resolveEmsZone(originProvince, destProvince, isLocalCity)
                // 现行中国邮政官方国内特快专递标准（1kg首重 + 1kg进位续重）
                val units1kg = ceil(rawChargeable).toInt().coerceAtLeast(1)
                chargeableWeightKg = units1kg.toDouble()

                val (firstCost, extraRatePerKg) = when (effectiveZone) {
                    DestinationZone.LOCAL_CITY,
                    DestinationZone.SAME_PROVINCE -> Pair(12.00, 2.00) // 一区（省内/同城）：首重12元，续重2元/kg
                    DestinationZone.ZONE_NEAR -> Pair(14.00, 2.00)     // 二区（邻省/周边经济圈）：首重14元，续重2元/kg
                    DestinationZone.ZONE_MIDDLE,
                    DestinationZone.ZONE_FAR -> Pair(21.00, 12.00)     // 三区/四区（跨省标快）：首重21元，续重12元/kg
                    DestinationZone.ZONE_REMOTE -> Pair(24.00, 28.00)  // 五区（西藏、新疆偏远）：首重24元，续重28元/kg
                }

                baseFreight = firstCost
                if (units1kg > 1) {
                    val extraUnits = units1kg - 1
                    extraFreight = extraUnits * extraRatePerKg
                    extraDetails = "首重1kg(${firstCost}元) + 续重${extraUnits}kg × ${extraRatePerKg}元 = ${String.format("%.2f", extraFreight)}元【${effectiveZone.displayName}】"
                } else {
                    extraDetails = "首重1kg以内：${firstCost}元【${effectiveZone.displayName}】"
                }

                if (rawChargeable > 40.0) {
                    remarks.add("国内特快专递最高限重40kg，当前重量 ${rawChargeable}kg 已超出上限！")
                }
            }
        }

        // 保价费官方标准：EMS为5‰ (千分之五，最低1元)；包裹为1% (百分之一，最低1元)
        val insuranceCost = if (declaredValue > 0) {
            val rate = if (serviceType == ParcelServiceType.EMS_STANDARD) 0.005 else 0.01
            max(1.00, roundToTwoDecimals(declaredValue * rate))
        } else {
            0.00
        }

        if (isVolumetric) {
            remarks.add("三边超限计泡：按体积折算重 ${volumetricWeightKg}kg 计收（实重 ${actualWeightKg}kg）")
        }

        val totalCost = roundToTwoDecimals(baseFreight + extraFreight + insuranceCost)

        return ParcelCalculationResult(
            chargeableWeightKg = chargeableWeightKg,
            actualWeightKg = actualWeightKg,
            volumetricWeightKg = volumetricWeightKg,
            isVolumetricCharged = isVolumetric,
            baseFreight = baseFreight,
            extraFreight = extraFreight,
            extraDetails = extraDetails,
            registrationFee = 0.0,
            insuranceFee = insuranceCost,
            totalCost = totalCost,
            remarks = remarks
        )
    }

    private fun roundToTwoDecimals(value: Double): Double {
        return (value * 100.0).roundToInt() / 100.0
    }
}
