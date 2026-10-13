package com.lyne.adas.l1.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class Step(val title: String, val body: String)

private val STEPS = listOf(
    Step("LYNE ADAS", "手机端驾驶辅助系统。前碰撞、车道偏离、行人、车间距和交通标志警告——全部本地运行，无需云服务。"),
    Step("仅为警告", "Lyne 不会控制您的车辆。它是一个辅助工具，不能替代注意力。距离和碰撞时间是估算值。请始终关注道路。"),
    Step("安装手机", "将手机横向放置在挡风玻璃/仪表板上，后置摄像头面向道路，镜头无遮挡且大致与眼睛平齐。稳定的安装对准确性很重要。"),
    Step("权限", "摄像头是观看道路所必需的。位置是可选的，用于支持基于速度的警告（车间距、超速）。"),
    Step("校准", "单目距离取决于您的摄像头视野。您可以在 CAL 屏幕上随时根据已知间距进行微调。"),
)

@Composable
fun OnboardingScreen(cameraGranted: Boolean, onRequestPermissions: () -> Unit, onFinish: () -> Unit) {
    val p = palette()
    var step by remember { mutableIntStateOf(0) }
    val s = STEPS[step]
    val isPermStep = step == 3

    Column(Modifier.fillMaxSize().background(p.background).padding(24.dp)) {
        // progress dots
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            STEPS.indices.forEach { i ->
                Box(Modifier.size(if (i == step) 10.dp else 7.dp).background(if (i <= step) p.accent else p.surfaceAlt, CircleShape))
            }
        }
        Spacer(Modifier.weight(0.3f))
        Text(s.title, color = p.text, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.size(12.dp))
        Text(s.body, color = p.textDim, fontSize = 15.sp)
        if (isPermStep) {
            Spacer(Modifier.size(16.dp))
            if (cameraGranted) Text("摄像头已授权 ✓", color = p.ok, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            else PillButton("授予权限", p.accent, onRequestPermissions)
        }
        Spacer(Modifier.weight(1f))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            if (step > 0) PillButton("上一步", p.textDim) { step-- } else Spacer(Modifier.size(1.dp))
            val canAdvance = !isPermStep || cameraGranted
            val label = if (step == STEPS.lastIndex) "开始" else "下一步"
            PillButton(label, if (canAdvance) p.accent else p.textDim) {
                if (!canAdvance) return@PillButton
                if (step == STEPS.lastIndex) onFinish() else step++
            }
        }
    }
}
