package com.chinapost.postage.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
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
import com.chinapost.postage.model.*
import com.chinapost.postage.ui.components.CountrySearchDialog

enum class LetterFlowType(val title: String) {
    DOMESTIC("国内信函 (全国1.2元/同城0.8元)"),
    INTERNATIONAL("国际及港澳台 (按国家/地区)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LetterScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    var flowType by remember { mutableStateOf(LetterFlowType.DOMESTIC) }

    // 国内极简选项：外埠(全国各地1.20元) / 本埠(同城0.80元)
    var domesticScope by remember { mutableStateOf(MailScope.DOMESTIC) }
    var domesticType by remember { mutableStateOf(MailType.ORDINARY_LETTER) }
    var domesticWeightInput by remember { mutableStateOf("20") }
    var domesticRegistered by remember { mutableStateOf(false) }
    var domesticReceipt by remember { mutableStateOf(false) }
    var domesticDeclaredValueInput by remember { mutableStateOf("") }

    // 国际及港澳台选项
    var selectedCountry by remember {
        mutableStateOf(CountryRepository.ALL.first { it.code == "JP" }) // 默认日本
    }
    var showCountryDialog by remember { mutableStateOf(false) }

    // 港澳台参数
    var hkMoTwType by remember { mutableStateOf(HkMoTwMailType.LETTER) }
    var hkMoTwWeightInput by remember { mutableStateOf("20") }
    var hkMoTwAirMail by remember { mutableStateOf(true) }
    var hkMoTwRegistered by remember { mutableStateOf(false) }
    var hkMoTwReceipt by remember { mutableStateOf(false) }

    // 国际参数
    var intlType by remember { mutableStateOf(InternationalMailType.LETTER) }
    var intlTransport by remember { mutableStateOf(InternationalTransportType.AIR) }
    var intlWeightInput by remember { mutableStateOf("20") }
    var intlRegistered by remember { mutableStateOf(false) }
    var intlReceipt by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // 实时计算结果
    val result: LetterCalculationResult = when (flowType) {
        LetterFlowType.DOMESTIC -> {
            val weight = domesticWeightInput.toDoubleOrNull() ?: 0.0
            val declaredValue = domesticDeclaredValueInput.toDoubleOrNull() ?: 0.0
            PostageCalculator.calculateDomesticLetter(
                type = domesticType,
                scope = domesticScope,
                weightGrams = weight,
                isRegistered = domesticRegistered,
                hasReceipt = domesticReceipt,
                declaredValue = declaredValue
            )
        }
        LetterFlowType.INTERNATIONAL -> {
            if (selectedCountry.category == MailDestinationCategory.HK_MO_TW) {
                val weight = hkMoTwWeightInput.toDoubleOrNull() ?: 0.0
                PostageCalculator.calculateHkMoTwLetter(
                    type = hkMoTwType,
                    weightGrams = weight,
                    isRegistered = hkMoTwRegistered,
                    hasReceipt = hkMoTwReceipt,
                    isAirMail = hkMoTwAirMail
                )
            } else {
                val weight = intlWeightInput.toDoubleOrNull() ?: 0.0
                PostageCalculator.calculateInternationalLetter(
                    type = intlType,
                    transportType = intlTransport,
                    zoneGroup = selectedCountry.zoneGroup ?: InternationalZoneGroup.GROUP_1,
                    weightGrams = weight,
                    isRegistered = intlRegistered,
                    hasReceipt = intlReceipt
                )
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. 流向切换
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LetterFlowType.values().forEach { flow ->
                    val isSelected = flowType == flow
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { flowType = flow }
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = flow.title,
                            modifier = Modifier
                                .padding(vertical = 10.dp)
                                .fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // 2. 根据流向显示具体配置
        if (flowType == LetterFlowType.DOMESTIC) {
            // 国内：无需选省市！全国统一1.20，同城0.80，秒选
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "寄达范围",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 外埠
                        val isDomestic = domesticScope == MailScope.DOMESTIC
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { domesticScope = MailScope.DOMESTIC }
                                .border(
                                    width = if (isDomestic) 2.dp else 1.dp,
                                    color = if (isDomestic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDomestic) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "外埠 (跨市/全国各地)",
                                    fontWeight = if (isDomestic) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isDomestic) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "首重 1.20 元 / 20g",
                                    fontSize = 11.sp,
                                    color = if (isDomestic) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // 本埠
                        val isLocal = domesticScope == MailScope.LOCAL
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { domesticScope = MailScope.LOCAL }
                                .border(
                                    width = if (isLocal) 2.dp else 1.dp,
                                    color = if (isLocal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isLocal) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "本埠 (同城/本县)",
                                    fontWeight = if (isLocal) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isLocal) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "首重 0.80 元 / 20g",
                                    fontSize = 11.sp,
                                    color = if (isLocal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // 业务类型
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("业务种类", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MailType.values().forEach { type ->
                            FilterChip(
                                selected = domesticType == type,
                                onClick = { domesticType = type },
                                label = { Text(type.displayName, fontSize = 12.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            WeightInputCard(
                weightInput = domesticWeightInput,
                onWeightChange = { domesticWeightInput = it },
                maxWeightGrams = domesticType.maxWeightGrams,
                ruleText = "首重100g内每20g进位计费，超过100g每增加100g加收续重"
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("附加业务", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("挂号 (+3.00元)", fontSize = 14.sp)
                        Switch(checked = domesticRegistered, onCheckedChange = { domesticRegistered = it })
                    }

                    if (domesticRegistered) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("回执 (+3.00元)", fontSize = 14.sp)
                            Switch(checked = domesticReceipt, onCheckedChange = { domesticReceipt = it })
                        }

                        OutlinedTextField(
                            value = domesticDeclaredValueInput,
                            onValueChange = { domesticDeclaredValueInput = it },
                            label = { Text("保价金额 (元，可选)") },
                            trailingIcon = { Text("元 ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

        } else {
            // 国际及港澳台：国家/地区搜索与选择
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "寄达国家/地区（点击搜索）",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showCountryDialog = true }
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${selectedCountry.nameCn} (${selectedCountry.nameEn})",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "归属: ${selectedCountry.displayCategoryAndGroup} · 点击搜索切换国家/地区",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "搜索国家",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // 常用国家快速点选小标签
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val quickPicks = listOf("HK", "TW", "JP", "US", "GB", "AU")
                        CountryRepository.ALL.filter { it.code in quickPicks }.forEach { country ->
                            val isCurrent = country.code == selectedCountry.code
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedCountry = country }
                            ) {
                                Text(
                                    text = country.nameCn.replace("中国", ""),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    color = if (isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            if (selectedCountry.category == MailDestinationCategory.HK_MO_TW) {
                HkMoTwMailConfig(
                    selectedType = hkMoTwType,
                    onTypeChange = { hkMoTwType = it },
                    weightInput = hkMoTwWeightInput,
                    onWeightChange = { hkMoTwWeightInput = it },
                    isAirMail = hkMoTwAirMail,
                    onAirMailChange = { hkMoTwAirMail = it },
                    isRegistered = hkMoTwRegistered,
                    onRegisteredChange = { hkMoTwRegistered = it },
                    hasReceipt = hkMoTwReceipt,
                    onReceiptChange = { hkMoTwReceipt = it }
                )
            } else {
                InternationalMailConfig(
                    selectedType = intlType,
                    onTypeChange = { intlType = it },
                    transportType = intlTransport,
                    onTransportChange = { intlTransport = it },
                    zoneGroup = selectedCountry.zoneGroup ?: InternationalZoneGroup.GROUP_1,
                    onZoneGroupChange = { },
                    weightInput = intlWeightInput,
                    onWeightChange = { intlWeightInput = it },
                    isRegistered = intlRegistered,
                    onRegisteredChange = { intlRegistered = it },
                    hasReceipt = intlReceipt,
                    onReceiptChange = { intlReceipt = it }
                )
            }
        }

        // 3. 计算结果卡片
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
                            text = "合计应付邮资",
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
                                appendLine("【中国邮政资费测算】")
                                if (flowType == LetterFlowType.DOMESTIC) {
                                    appendLine("范围: ${domesticScope.displayName}")
                                    appendLine("类型: ${domesticType.displayName}")
                                    appendLine("重量: ${domesticWeightInput}g")
                                } else {
                                    appendLine("目的地: ${selectedCountry.nameCn} (${selectedCountry.displayCategoryAndGroup})")
                                    if (selectedCountry.category == MailDestinationCategory.HK_MO_TW) {
                                        appendLine("类型: 港澳台${hkMoTwType.displayName}")
                                        appendLine("重量: ${hkMoTwWeightInput}g" + if (hkMoTwAirMail) " (含航空费)" else "")
                                    } else {
                                        appendLine("类型: 国际${intlType.displayName} (${intlTransport.displayName})")
                                        appendLine("重量: ${intlWeightInput}g")
                                    }
                                }
                                appendLine("基本资费: ¥${String.format("%.2f", result.basePostage)} (${result.baseFormula})")
                                if (result.airFee > 0) appendLine("航空附加费: ¥${String.format("%.2f", result.airFee)}")
                                if (result.registrationFee > 0) appendLine("挂号费: ¥${String.format("%.2f", result.registrationFee)}")
                                if (result.receiptFee > 0) appendLine("回执费: ¥${String.format("%.2f", result.receiptFee)}")
                                if (result.insuranceFee > 0) appendLine("保价费: ¥${String.format("%.2f", result.insuranceFee)}")
                                appendLine("总计邮资: ¥${String.format("%.2f", result.totalCost)}")
                            }
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("邮资明细", detail))
                            Toast.makeText(context, "计算明细已复制", Toast.LENGTH_SHORT).show()
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
                        text = "• 基本资费: ¥${String.format("%.2f", result.basePostage)} (${result.baseFormula})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (result.airFee > 0) {
                        Text(
                            text = "• 航空费: ¥${String.format("%.2f", result.airFee)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    if (result.registrationFee > 0) {
                        Text(
                            text = "• 挂号费: ¥${String.format("%.2f", result.registrationFee)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    if (result.receiptFee > 0) {
                        Text(
                            text = "• 回执费: ¥${String.format("%.2f", result.receiptFee)}",
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

                if (result.tips.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    result.tips.forEach { tip ->
                        Text(
                            text = "提示: $tip",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }

    if (showCountryDialog) {
        CountrySearchDialog(
            currentCountry = selectedCountry,
            onSelectCountry = { selectedCountry = it },
            onDismiss = { showCountryDialog = false }
        )
    }
}

/**
 * 港澳台函件配置界面
 */
@Composable
fun HkMoTwMailConfig(
    selectedType: HkMoTwMailType,
    onTypeChange: (HkMoTwMailType) -> Unit,
    weightInput: String,
    onWeightChange: (String) -> Unit,
    isAirMail: Boolean,
    onAirMailChange: (Boolean) -> Unit,
    isRegistered: Boolean,
    onRegisteredChange: (Boolean) -> Unit,
    hasReceipt: Boolean,
    onReceiptChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("港澳台种类", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HkMoTwMailType.values().forEach { type ->
                    val isSelected = selectedType == type
                    OutlinedButton(
                        onClick = { onTypeChange(type) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = type.displayName,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }

    if (selectedType == HkMoTwMailType.LETTER) {
        WeightInputCard(
            weightInput = weightInput,
            onWeightChange = onWeightChange,
            maxWeightGrams = 2000,
            ruleText = "阶梯计价：20g内1.5元，50g内2.8元，100g内4元，250g内8.5元，500g内16.7元，1000g内31.7元，2000g内55.8元"
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("运输与附加服务", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            if (selectedType != HkMoTwMailType.AEROGRAMME) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("航空寄递 (航空费)", fontSize = 14.sp)
                        Text(
                            if (selectedType == HkMoTwMailType.LETTER) "信函每10g或零数 0.50元" else "明信片每件 0.50元",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = isAirMail, onCheckedChange = onAirMailChange)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("办理挂号 (+16.00元)", fontSize = 14.sp)
                    Text("港澳台挂号费标准：每件16.00元", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = isRegistered, onCheckedChange = onRegisteredChange)
            }

            if (isRegistered) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("办理回执 (+3.00元)", fontSize = 14.sp)
                        Text("寄往台湾省暂不办理附回执业务", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = hasReceipt, onCheckedChange = onReceiptChange)
                }
            }
        }
    }
}

/**
 * 国际函件配置界面
 */
@Composable
fun InternationalMailConfig(
    selectedType: InternationalMailType,
    onTypeChange: (InternationalMailType) -> Unit,
    transportType: InternationalTransportType,
    onTransportChange: (InternationalTransportType) -> Unit,
    zoneGroup: InternationalZoneGroup,
    onZoneGroupChange: (InternationalZoneGroup) -> Unit,
    weightInput: String,
    onWeightChange: (String) -> Unit,
    isRegistered: Boolean,
    onRegisteredChange: (Boolean) -> Unit,
    hasReceipt: Boolean,
    onReceiptChange: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("国际业务与运输方式", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InternationalMailType.values().forEach { type ->
                    val isSelected = selectedType == type
                    OutlinedButton(
                        onClick = { onTypeChange(type) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = type.displayName,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Text("运输方式", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                InternationalTransportType.values().forEach { trans ->
                    val isSelected = transportType == trans
                    OutlinedButton(
                        onClick = { onTransportChange(trans) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = trans.displayName,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }

    if (selectedType == InternationalMailType.LETTER) {
        WeightInputCard(
            weightInput = weightInput,
            onWeightChange = onWeightChange,
            maxWeightGrams = 2000,
            ruleText = "首重20g以内计费一次，超出部分每10g或零数加收续重"
        )
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("国际附加业务", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("国际挂号 (+16.00元)", fontSize = 14.sp)
                    Text("统一加收国际挂号费 16.00元/件", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = isRegistered, onCheckedChange = onRegisteredChange)
            }

            if (isRegistered) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("国际回执 (+5.00元)", fontSize = 14.sp)
                        Text("国际回执业务 5.00元/件", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = hasReceipt, onCheckedChange = onReceiptChange)
                }
            }
        }
    }
}

@Composable
fun WeightInputCard(
    weightInput: String,
    onWeightChange: (String) -> Unit,
    maxWeightGrams: Int,
    ruleText: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("信件重量", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("限重: ${maxWeightGrams}g", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            val weight = weightInput.toDoubleOrNull() ?: 0.0
            val isOverLimit = weight > maxWeightGrams

            OutlinedTextField(
                value = weightInput,
                onValueChange = onWeightChange,
                label = { Text("输入重量 (克)") },
                trailingIcon = { Text("g ", fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = isOverLimit,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            if (isOverLimit) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚠️ 重量 ${weight}g 已超出最高限重 ${maxWeightGrams}g！",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            val presets = listOf("15", "20", "30", "50", "100", "200", "500")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presets.forEach { preset ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (weightInput == preset) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onWeightChange(preset) }
                    ) {
                        Text(
                            text = "${preset}g",
                            modifier = Modifier.padding(vertical = 6.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 11.sp,
                            fontWeight = if (weightInput == preset) FontWeight.Bold else FontWeight.Normal,
                            color = if (weightInput == preset) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Text(ruleText, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
