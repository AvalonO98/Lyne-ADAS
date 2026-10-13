package com.lyne.adas.l1.ui

import com.lyne.adas.l1.config.DayNightMode
import com.lyne.adas.l1.config.DeviceTier
import com.lyne.adas.l1.config.SensitivityPreset
import com.lyne.adas.l1.fusion.AlertType

/**
 * 枚举/类型标识的中文显示映射。
 *
 * 注意：这些是「显示名」映射，只影响 UI 文案，不改变任何持久化数据。
 * 持久化数据（Trip.alertCounts 的键、TripEvent.type）仍使用 [AlertType.name] 等英文标识，
 * 以保证历史记录与 ROVIX 旁挂日志的向后兼容，因此读取时用 [alertTypeLabel] 转成中文显示。
 */

fun SensitivityPreset.getDisplayName(): String = when (this) {
    SensitivityPreset.CALM -> "温和"
    SensitivityPreset.NORMAL -> "正常"
    SensitivityPreset.AGGRESSIVE -> "灵敏"
}

fun DayNightMode.getDisplayName(): String = when (this) {
    DayNightMode.AUTO -> "自动"
    DayNightMode.DAY -> "白天"
    DayNightMode.NIGHT -> "夜晚"
}

fun DeviceTier.getDisplayName(): String = when (this) {
    DeviceTier.A -> "旗舰级"
    DeviceTier.B -> "中端级"
    DeviceTier.C -> "入门级"
}

fun AlertType.getDisplayName(): String = when (this) {
    AlertType.FCW -> "前向碰撞"
    AlertType.VRU -> "行人/车辆"
    AlertType.LDW -> "车道偏离"
    AlertType.HEADWAY -> "跟车过近"
    AlertType.OVERSPEED -> "超速"
    AlertType.STOP -> "停车标志"
}

/** 把持久化的告警类型标识（[AlertType.name]）转成中文；未知标识原样返回。 */
fun alertTypeLabel(key: String): String {
    val type = AlertType.entries.firstOrNull { it.name == key } ?: return key
    return type.getDisplayName()
}

/** HUD 目标框类别短标签（受限于绘制空间，用 2 字以内的中文）。 */
fun objectClassLabel(tag: String): String = when (tag) {
    "CAR" -> "车"
    "TRUCK" -> "货车"
    "BUS" -> "客车"
    "AUTO" -> "三轮"
    "MOTO" -> "摩托"
    "CYCLE" -> "单车"
    "PED" -> "行人"
    "STOP" -> "停"
    else -> "目标"
}
