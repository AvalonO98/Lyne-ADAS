package com.lyne.adas.l1.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lyne.adas.l1.config.DayNightMode
import com.lyne.adas.l1.perf.PerfSnapshot
import com.lyne.adas.l1.pipeline.AdasUiState

@Composable
fun StatusStrip(state: AdasUiState) {
    val p = palette()
    Column(Modifier.background(p.chip, RoundedCornerShape(6.dp)).padding(8.dp, 6.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("级别 ${state.tier.getDisplayName()}", color = p.text, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(state.perf?.backend?.name ?: "--", color = p.accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(if (state.usingStub) "模拟" else "实时", color = if (state.usingStub) p.caution else p.ok, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(state.sensitivity.getDisplayName(), color = p.textDim, fontSize = 11.sp)
        }
        Text(
            "内参 ${if (state.intrinsics.fromDevice) "设备" else "默认"} ${state.intrinsics.horizontalFovDeg.toInt()}°  " +
                (if (state.calibrated) "已校准" else "未校准") + "  " + (if (state.isNight) "夜间" else "白天"),
            color = p.textDim, fontSize = 10.sp,
        )
        if (state.usingStub) Text("物体: 无权重文件 — 需要 .tflite", color = p.textDim, fontSize = 9.sp)
        else if (state.signStub) Text("交通标志: 无标志模型 (通过检测器识别STOP)", color = p.textDim, fontSize = 9.sp)
    }
}

@Composable
fun PerfPanel(perf: PerfSnapshot) {
    val p = palette()
    Column(
        Modifier.background(p.chip, RoundedCornerShape(6.dp)).padding(8.dp, 6.dp),
        horizontalAlignment = Alignment.End,
    ) {
        Text("${"%.1f".format(perf.fps)} 帧/秒", color = p.ok, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text("总计 ${"%.0f".format(perf.totalMs)}毫秒", color = p.text, fontSize = 10.sp)
        Text("物体${stage(perf, "object")} 车道${stage(perf, "lane")} 标志${stage(perf, "sign")}", color = p.textDim, fontSize = 10.sp)
        Text("丢弃 ${perf.droppedFrames}  堆内存 ${perf.heapUsedMb}/${perf.heapMaxMb}MB", color = p.textDim, fontSize = 10.sp)
        Text("分辨率 ${perf.inputResolution}  节奏 x${perf.cadenceMultiplier}  温 ${perf.thermal}", color = p.textDim, fontSize = 10.sp)
    }
}

private fun stage(p: PerfSnapshot, name: String): String = p.stageMs[name]?.let { "%.0f".format(it) } ?: "-"

@Composable
fun ControlBar(
    state: AdasUiState,
    modifier: Modifier = Modifier,
    onToggleMute: () -> Unit,
    onToggleInject: () -> Unit,
    onOpenCalibration: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenSession: () -> Unit,
    onCycleDayNight: () -> Unit,
) {
    val p = palette()
    val dn = when (state.dayNightMode) { DayNightMode.AUTO -> "自动"; DayNightMode.DAY -> "白天"; DayNightMode.NIGHT -> "夜晚" }
    Row(modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PillButton(if (state.muted) "已静音" else "声音", if (state.muted) p.caution else p.ok, onToggleMute)
        PillButton(if (state.debugInject) "注入•" else "注入", if (state.debugInject) p.caution else p.textDim, onToggleInject)
        PillButton(dn, p.accent, onCycleDayNight)
        PillButton("行程", p.text, onOpenSession)
        PillButton("校准", p.text, onOpenCalibration)
        PillButton("设置", p.text, onOpenSettings)
    }
}
