package com.lyne.adas.l1.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lyne.adas.l1.pipeline.AdasUiState

/**
 * Guided calibration wizard. The effective horizontal FoV dominates monocular distance, so we let
 * the driver nudge it (and an empirical distance scale) against a known gap. Values persist per
 * device via CalibrationStore (wired in the controller).
 */
@Composable
fun CalibrationScreen(
    state: AdasUiState,
    onSetFov: (Float?) -> Unit,
    onSetScale: (Float) -> Unit,
    onClose: () -> Unit,
) {
    val p = palette()
    val intr = state.intrinsics
    var step by remember { mutableIntStateOf(0) }
    val last = 3

    Column(Modifier.fillMaxSize().background(p.background).padding(16.dp)) {
        ScreenHeader("校准  ${step + 1}/${last + 1}", onClose)
        Spacer(Modifier.height(12.dp))

        when (step) {
            0 -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("1 · 安装", color = p.text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("将手机横向安装在挡风玻璃上，后置镜头无遮挡且水平。稳定的安装至关重要——距离准确性依赖于它。", color = p.textDim, fontSize = 14.sp)
            }
            1 -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("2 · 视野", color = p.text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("来源: ${if (intr.fromDevice) "相机特性" else "默认(精度降低)"}", color = p.textDim, fontSize = 12.sp)
                Text("${"%.1f".format(intr.horizontalFovDeg)}°", color = p.text, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Chip("–1°", false) { onSetFov(intr.horizontalFovDeg - 1f) }
                    Chip("+1°", false) { onSetFov(intr.horizontalFovDeg + 1f) }
                    Chip("重置", false) { onSetFov(null) }
                }
            }
            2 -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("3 · 距离比例", color = p.text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("将车停在已知距离的车辆后(例如10米)。在HUD上调整，直到领先距离与现实匹配。", color = p.textDim, fontSize = 14.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Chip("–5%", false) { onSetScale(0.95f) }
                    Chip("+5%", false) { onSetScale(1.05f) }
                    Chip("重置", false) { onSetScale(1.0f) }
                }
            }
            else -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("完成", color = p.text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (state.calibrated) "校准已为此设备保存。" else "使用默认值——距离仍然为估计值。",
                    color = if (state.calibrated) p.ok else p.caution, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                )
                Text("距离和TTC始终是估计值；低置信度值永远不会触发硬警告。", color = p.textDim, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            if (step > 0) PillButton("上一步", p.textDim) { step-- } else Spacer(Modifier.width(1.dp))
            if (step < last) PillButton("下一步", p.accent) { step++ } else PillButton("完成", p.accent, onClose)
        }
    }
}
