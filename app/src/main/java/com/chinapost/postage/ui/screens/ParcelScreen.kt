package com.chinapost.postage.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chinapost.postage.calculator.PostageCalculator
import com.chinapost.postage.model.ChinaProvinces
import com.chinapost.postage.model.DestinationZone
import com.chinapost.postage.model.ParcelServiceType
import com.chinapost.postage.ui.components.ProvinceSelectDialog

data class PostBoxPreset(val name: String, val length: Double, val width: Double, val height: Double)

val POST_BOX_PRESETS = listOf(
    PostBoxPreset("1号箱 (53×29×37)", 53.0, 29.0, 37.0),
    PostBoxPreset("2号箱 (53×23×29)", 53.0, 23.0, 29.0),
    PostBoxPreset("3号箱 (43×21×27)", 43.0, 21.0, 27.0),
    PostBoxPreset("4号箱 (35×19×23)", 35.0, 19.0, 23.0),
    PostBoxPreset("5号箱 (29×17×19)", 29.0, 17.0, 19.0),
    PostBoxPreset("6号箱 (26×15×18)", 26.0, 15.0, 18.0)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParcelScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // 寄件与收件省份（无需繁琐区县，省份确定即可算出资费）
    var originProvince by remember { mutableStateOf("河北") }
    var destProvince by remember { mutableStateOf("四川") }
    var isLocalCity by remember { mutableStateOf(false) }

    var showOriginDialog by remember { mutableStateOf(false) }
    var showDestDialog by remember { mutableStateOf(false) }

    // 默认选用普通包裹（普包/平邮），国内大件寄递最常用、最经济
    var selectedService by remember { mutableStateOf(ParcelServiceType.ORDINARY_PARCEL) }
    var actualWeightInput by remember { mutableStateOf("15.0") }

    var enableVolumeCalc by remember { mutableStateOf(false) }
    var lengthInput by remember { mutableStateOf("") }
    var widthInput by remember { mutableStateOf("") }
    var heightInput by remember { mutableStateOf("") }

    var declaredValueInput by remember { mutableStateOf("") }

    val actualWeight = actualWeightInput.toDoubleOrNull() ?: 0.0
    val length = if (enableVolumeCalc) (lengthInput.toDoubleOrNull() ?: 0.0) else 0.0
    val width = if (enableVolumeCalc) (widthInput.toDoubleOrNull() ?: 0.0) else 0.0
    val height = if (enableVolumeCalc) (heightInput.toDoubleOrNull() ?: 0.0) else 0.0
    val declaredValue = declaredValueInput.toDoubleOrNull() ?: 0.0

    // 分别推导普包档次、快包分区与EMS分区
    val ordinaryTier = remember(originProvince, destProvince, isLocalCity) {
        ChinaProvinces.resolveOrdinaryParcelTier(originProvince, destProvince, isLocalCity)
    }
    val expressZone = remember(originProvince, destProvince, isLocalCity) {
        ChinaProvinces.resolveExpressParcelZone(originProvince, destProvince, isLocalCity)
    }
    val emsZone = remember(originProvince, destProvince, isLocalCity) {
        ChinaProvinces.resolveEmsZone(originProvince, destProvince, isLocalCity)
    }

    val currentZoneDesc = remember(selectedService, ordinaryTier, expressZone, emsZone) {
        when (selectedService) {
            ParcelServiceType.ORDINARY_PARCEL -> ordinaryTier.displayName
            ParcelServiceType.EXPRESS_PARCEL -> expressZone.displayName
            ParcelServiceType.EMS_STANDARD -> emsZone.displayName
        }
    }

    // 当前选定业务测算
    val result = remember(selectedService, originProvince, destProvince, isLocalCity, actualWeight, length, width, height, declaredValue) {
        val currentZone = when (selectedService) {
            ParcelServiceType.ORDINARY_PARCEL -> DestinationZone.ZONE_MIDDLE
            ParcelServiceType.EXPRESS_PARCEL -> expressZone
            ParcelServiceType.EMS_STANDARD -> emsZone
        }
        PostageCalculator.calculateParcel(
            serviceType = selectedService,
            zone = currentZone,
            actualWeightKg = actualWeight,
            lengthCm = length,
            widthCm = width,
            heightCm = height,
            declaredValue = declaredValue,
            originProvince = originProvince,
            destProvince = destProvince,
            isLocalCity = isLocalCity
        )
    }

    // 三大业务横向比价测算
    val ordinaryResult = remember(originProvince, destProvince, isLocalCity, actualWeight, length, width, height, declaredValue) {
        PostageCalculator.calculateParcel(
            serviceType = ParcelServiceType.ORDINARY_PARCEL,
            zone = DestinationZone.ZONE_MIDDLE,
            actualWeightKg = actualWeight,
            lengthCm = length,
            widthCm = width,
            heightCm = height,
            declaredValue = declaredValue,
            originProvince = originProvince,
            destProvince = destProvince,
            isLocalCity = isLocalCity
        )
    }
    val expressResult = remember(originProvince, destProvince, isLocalCity, actualWeight, length, width, height, declaredValue) {
        PostageCalculator.calculateParcel(
            serviceType = ParcelServiceType.EXPRESS_PARCEL,
            zone = expressZone,
            actualWeightKg = actualWeight,
            lengthCm = length,
            widthCm = width,
            heightCm = height,
            declaredValue = declaredValue,
            originProvince = originProvince,
            destProvince = destProvince,
            isLocalCity = isLocalCity
        )
    }
    val emsResult = remember(originProvince, destProvince, isLocalCity, actualWeight, length, width, height, declaredValue) {
        PostageCalculator.calculateParcel(
            serviceType = ParcelServiceType.EMS_STANDARD,
            zone = emsZone,
            actualWeightKg = actualWeight,
            lengthCm = length,
            widthCm = width,
            heightCm = height,
            declaredValue = declaredValue,
            originProvince = originProvince,
            destProvince = destProvince,
            isLocalCity = isLocalCity
        )
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. 寄件省份与收件省份选择（选到省市立即出价格，免除繁杂区县）
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "寄件地与收件地",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 寄件地
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showOriginDialog = true },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("寄件省份", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(originProvince, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // 交换
                    IconButton(
                        onClick = {
                            val temp = originProvince
                            originProvince = destProvince
                            destProvince = temp
                        }
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "交换", tint = MaterialTheme.colorScheme.primary)
                    }

                    // 收件地
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showDestDialog = true },
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("收件省份", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(destProvince, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "资费归属: $currentZoneDesc",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("同城/市内互寄", fontSize = 12.sp)
                        Checkbox(checked = isLocalCity, onCheckedChange = { isLocalCity = it })
                    }
                }
            }
        }

        // 2. 服务类型选择
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "业务种类",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                ParcelServiceType.values().forEach { service ->
                    val isSelected = selectedService == service
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedService = service }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = service.displayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = service.speedDesc,
                                            fontSize = 10.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = service.unitDesc,
                                    fontSize = 12.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedService = service }
                            )
                        }
                    }
                }
            }
        }

        // 3. 重量与体积输入
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "重量与规格",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = actualWeightInput,
                    onValueChange = { actualWeightInput = it },
                    label = { Text("实测重量 (公斤 / kg)") },
                    trailingIcon = { Text("kg ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                val presets = listOf("0.5", "1.0", "2.0", "3.0", "5.0", "10.0")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { preset ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (actualWeightInput == preset) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { actualWeightInput = preset }
                        ) {
                            Text(
                                text = "${preset}kg",
                                modifier = Modifier.padding(vertical = 6.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontSize = 12.sp,
                                fontWeight = if (actualWeightInput == preset) FontWeight.Bold else FontWeight.Normal,
                                color = if (actualWeightInput == preset) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("轻泡货体积折算", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text("长×宽×高 / 6000，取两者较大值计费", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = enableVolumeCalc,
                        onCheckedChange = { enableVolumeCalc = it }
                    )
                }

                AnimatedVisibility(visible = enableVolumeCalc) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = lengthInput,
                                onValueChange = { lengthInput = it },
                                label = { Text("长 (cm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = widthInput,
                                onValueChange = { widthInput = it },
                                label = { Text("宽 (cm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = heightInput,
                                onValueChange = { heightInput = it },
                                label = { Text("高 (cm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Text("邮政标准箱规格参考：", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            POST_BOX_PRESETS.chunked(2).forEach { rowPresets ->
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    rowPresets.forEach { preset ->
                                        OutlinedButton(
                                            onClick = {
                                                lengthInput = preset.length.toString()
                                                widthInput = preset.width.toString()
                                                heightInput = preset.height.toString()
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp)
                                        ) {
                                            Text(preset.name, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }

                        if (result.volumetricWeightKg > 0) {
                            Text(
                                text = "折算体积重: ${result.volumetricWeightKg} kg (实重: ${result.actualWeightKg} kg)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (result.isVolumetricCharged) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                OutlinedTextField(
                    value = declaredValueInput,
                    onValueChange = { declaredValueInput = it },
                    label = { Text("保价金额 (元，可选)") },
                    trailingIcon = { Text("元 ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    supportingText = {
                        Text(
                            text = if (selectedService == ParcelServiceType.EMS_STANDARD) "EMS保价费率: 0.5% (最低1元)" else "包裹保价费率: 1% (最低1元)",
                            fontSize = 11.sp
                        )
                    }
                )
            }
        }

        // 4. 三大业务资费横向实时比价卡片
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "三大业务运费横向对比（点击直接切换）",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ComparisonItem(
                        title = "普通包裹",
                        price = ordinaryResult.totalCost,
                        desc = "限重50kg·最实惠",
                        isSelected = selectedService == ParcelServiceType.ORDINARY_PARCEL,
                        onClick = { selectedService = ParcelServiceType.ORDINARY_PARCEL },
                        modifier = Modifier.weight(1f)
                    )
                    ComparisonItem(
                        title = "快递包裹",
                        price = expressResult.totalCost,
                        desc = "限重20kg·送货上门",
                        isSelected = selectedService == ParcelServiceType.EXPRESS_PARCEL,
                        onClick = { selectedService = ParcelServiceType.EXPRESS_PARCEL },
                        modifier = Modifier.weight(1f)
                    )
                    ComparisonItem(
                        title = "EMS特快",
                        price = emsResult.totalCost,
                        desc = "起重500g·次日达",
                        isSelected = selectedService == ParcelServiceType.EMS_STANDARD,
                        onClick = { selectedService = ParcelServiceType.EMS_STANDARD },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 5. 计算结果
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${selectedService.shortName} 运费合计",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "¥ ",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = String.format("%.2f", result.totalCost),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val detail = buildString {
                                appendLine("【中国邮政包裹运费测算】")
                                appendLine("路线: $originProvince -> $destProvince ($currentZoneDesc)")
                                appendLine("服务: ${selectedService.displayName}")
                                appendLine("计费重量: ${result.chargeableWeightKg}kg" + if (result.isVolumetricCharged) " (按体积重)" else " (按实测重)")
                                appendLine("首重费用: ¥${String.format("%.2f", result.baseFreight)}")
                                if (result.extraFreight > 0) appendLine("续重费用: ¥${String.format("%.2f", result.extraFreight)}")
                                if (result.registrationFee > 0) appendLine("包裹挂号单费: ¥${String.format("%.2f", result.registrationFee)}")
                                if (result.insuranceFee > 0) appendLine("保价费: ¥${String.format("%.2f", result.insuranceFee)}")
                                appendLine("总计运费: ¥${String.format("%.2f", result.totalCost)}")
                            }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("运费明细", detail))
                            Toast.makeText(context, "运费明细已复制", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "复制", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("复制明细", fontSize = 13.sp)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "• 路线与资费档: $originProvince -> $destProvince ($currentZoneDesc)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "• 计费重量: ${result.chargeableWeightKg} kg" + (if (result.isVolumetricCharged) " [按体积折算]" else " [实重${result.actualWeightKg}kg]"),
                        fontSize = 12.sp,
                        color = if (result.isVolumetricCharged) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "• 运费说明: ${result.extraDetails}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (result.registrationFee > 0) {
                        Text(
                            text = "• 挂号单费: ¥${String.format("%.2f", result.registrationFee)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    if (result.insuranceFee > 0) {
                        Text(
                            text = "• 保价费: ¥${String.format("%.2f", result.insuranceFee)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                if (result.remarks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    result.remarks.forEach { remark ->
                        Text(
                            text = "说明: $remark",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }

    if (showOriginDialog) {
        ProvinceSelectDialog(
            title = "选择寄件省份",
            currentSelected = originProvince,
            onSelect = { originProvince = it },
            onDismiss = { showOriginDialog = false }
        )
    }

    if (showDestDialog) {
        ProvinceSelectDialog(
            title = "选择收件省份",
            currentSelected = destProvince,
            onSelect = { destProvince = it },
            onDismiss = { showDestDialog = false }
        )
    }
}

@Composable
private fun ComparisonItem(
    title: String,
    price: Double,
    desc: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(10.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "¥${String.format("%.2f", price)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = desc,
                fontSize = 9.sp,
                maxLines = 1,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
