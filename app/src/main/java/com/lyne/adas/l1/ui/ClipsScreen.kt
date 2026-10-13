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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Lists saved dashcam clips with play and share actions. */
@Composable
fun ClipsScreen(clips: List<File>, onPlay: (File) -> Unit, onShare: (File) -> Unit, onClose: () -> Unit) {
    val p = palette()
    Column(Modifier.fillMaxSize().background(p.background).padding(16.dp)) {
        ScreenHeader("行车记录视频", onClose)
        Spacer(Modifier.height(8.dp))
        Text(
            "关键事件时自动保存。需要在设置中启用行车记录功能。",
            color = p.textDim, fontSize = 12.sp,
        )
        Spacer(Modifier.height(12.dp))

        if (clips.isEmpty()) {
            Text("暂无视频。", color = p.textDim, fontSize = 13.sp)
        } else {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                clips.forEach { f ->
                    Column(Modifier.fillMaxWidth().background(p.surfaceAlt, RoundedCornerShape(8.dp)).padding(12.dp)) {
                        Text(stamp(f), color = p.text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("%.1f MB".format(f.length() / 1e6), color = p.textDim, fontSize = 11.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            PillButton("播放", p.accent) { onPlay(f) }
                            PillButton("分享", p.text) { onShare(f) }
                        }
                    }
                }
            }
        }
    }
}

private fun stamp(f: File): String {
    val ms = f.name.removePrefix("clip_").removeSuffix(".mp4").toLongOrNull() ?: f.lastModified()
    return SimpleDateFormat("EEE d MMM, HH:mm:ss", Locale.getDefault()).format(Date(ms))
}
