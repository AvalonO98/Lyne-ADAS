package com.lyne.adas.l1.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lyne.adas.l1.pipeline.AdasUiState

@Composable
fun SessionScreen(state: AdasUiState, onExport: () -> Unit, onOpenTrips: () -> Unit, onOpenClips: () -> Unit, onClose: () -> Unit) {
    val p = palette()
    val s = state.session
    Column(Modifier.fillMaxSize().background(p.background).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ScreenHeader("驾驶会话", onClose)

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("持续时间", s?.let { fmtDuration(it.durationMs) } ?: "00:00", Modifier.weight(1f))
            StatCard("距离", s?.let { "%.1f 公里".format(it.distanceKm) } ?: "0.0 公里", Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard("最高速度", s?.let { "${it.maxSpeedKph.toInt()} 公里/小时" } ?: "-- 公里/小时", Modifier.weight(1f))
            StatCard("警告", s?.totalAlerts?.toString() ?: "0", Modifier.weight(1f))
        }

        s?.alertCounts?.takeIf { it.isNotEmpty() }?.let { counts ->
            SectionLabel("按类型")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                counts.entries.sortedByDescending { it.value }.forEach { (type, n) ->
                    Text("${type.getDisplayName()} $n", color = p.text, fontSize = 12.sp,
                        modifier = Modifier.background(p.surfaceAlt, RoundedCornerShape(6.dp)).padding(8.dp, 5.dp))
                }
            }
        }

        SectionLabel("事件日志")
        Box(Modifier.weight(1f).fillMaxWidth().background(p.surface, RoundedCornerShape(8.dp)).padding(8.dp)) {
            if (state.recentEvents.isEmpty()) {
                Text("暂无事件。警告触发时会显示在这里。", color = p.textDim, fontSize = 12.sp)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(state.recentEvents.asReversed()) { e ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("●", color = p.severityColor(e.severity), fontSize = 12.sp)
                            Text(e.type.getDisplayName(), color = p.text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(e.message, color = p.textDim, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PillButton("行程历史", p.text, onOpenTrips)
            PillButton("行车记录", p.text, onOpenClips)
            PillButton("导出", p.accent, onExport)
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier) {
    val p = palette()
    Column(modifier.background(p.surface, RoundedCornerShape(8.dp)).padding(12.dp)) {
        Text(label, color = p.textDim, fontSize = 10.sp)
        Text(value, color = p.text, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

private fun fmtDuration(ms: Long): String {
    val total = ms / 1000
    val m = total / 60; val s = total % 60
    return "%02d:%02d".format(m, s)
}
