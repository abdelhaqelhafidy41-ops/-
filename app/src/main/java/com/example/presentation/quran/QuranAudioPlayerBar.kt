package com.example.presentation.quran

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioPlayerState
import com.example.audio.QuranAudioPlayer
import com.example.audio.Reciter
import com.example.audio.RepeatMode

@Composable
fun QuranAudioPlayerBar(
    audioPlayer: QuranAudioPlayer,
    state: AudioPlayerState,
    onDownloadSurah: (() -> Unit)? = null,
    isDownloading: Boolean = false,
    downloadProgress: Pair<Int, Int>? = null,
    modifier: Modifier = Modifier
) {
    var showReciterMenu by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = state.isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            tonalElevation = 8.dp,
            shadowElevation = 12.dp,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("quran_audio_player_bar")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Audio error banner if any
                state.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    )
                }

                // Download progress if active
                if (isDownloading && downloadProgress != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "جاري تنزيل السورة للاستماع دون اتصال: ${downloadProgress.first} من ${downloadProgress.second} آية",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        LinearProgressIndicator(
                            progress = { downloadProgress.first.toFloat() / downloadProgress.second.toFloat() },
                            modifier = Modifier.width(100.dp)
                        )
                    }
                }

                // Top Info Row: Surah Name, Ayah, Reciter selector, Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "سورة ${state.currentSurahName} • الآية ${state.currentAyahNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { showReciterMenu = true }
                                .padding(vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = state.reciter.nameArabic,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )

                            DropdownMenu(
                                expanded = showReciterMenu,
                                onDismissRequest = { showReciterMenu = false }
                            ) {
                                Reciter.AVAILABLE_RECITERS.forEach { reciter ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(reciter.nameArabic, fontWeight = if (reciter.id == state.reciter.id) FontWeight.Bold else FontWeight.Normal)
                                                Text(reciter.quality, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                            }
                                        },
                                        onClick = {
                                            audioPlayer.setReciter(reciter)
                                            showReciterMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Download offline button
                    if (onDownloadSurah != null) {
                        IconButton(
                            onClick = onDownloadSurah,
                            enabled = !isDownloading
                        ) {
                            if (isDownloading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Download,
                                    contentDescription = "تنزيل السورة دون اتصال",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Stop/Close button
                    IconButton(onClick = { audioPlayer.stop() }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "إغلاق المشغل",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Slider & Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(state.currentPositionMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                    Slider(
                        value = if (state.durationMs > 0) state.currentPositionMs.toFloat() else 0f,
                        onValueChange = { audioPlayer.seekTo(it.toInt()) },
                        valueRange = 0f..(if (state.durationMs > 0) state.durationMs.toFloat() else 1f),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )
                    Text(
                        text = formatTime(state.durationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                // Controls Row: Repeat, Previous, Play/Pause, Next
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Repeat mode toggle
                    IconButton(
                        onClick = {
                            val nextMode = when (state.repeatMode) {
                                RepeatMode.NONE -> RepeatMode.REPEAT_AYAH
                                RepeatMode.REPEAT_AYAH -> RepeatMode.REPEAT_SURAH
                                RepeatMode.REPEAT_SURAH -> RepeatMode.NONE
                            }
                            audioPlayer.setRepeatMode(nextMode)
                        }
                    ) {
                        Icon(
                            imageVector = when (state.repeatMode) {
                                RepeatMode.REPEAT_AYAH -> Icons.Filled.RepeatOne
                                RepeatMode.REPEAT_SURAH -> Icons.Filled.Repeat
                                RepeatMode.NONE -> Icons.Filled.Repeat
                            },
                            contentDescription = state.repeatMode.titleArabic,
                            tint = if (state.repeatMode != RepeatMode.NONE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Previous Ayah
                    IconButton(
                        onClick = { audioPlayer.previousAyah() },
                        enabled = state.currentAyahNumber > 1
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipPrevious,
                            contentDescription = "الآية السابقة",
                            tint = if (state.currentAyahNumber > 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Play / Pause FAB-style button
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .clickable { audioPlayer.togglePlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (state.isPlaying) "إيقاف مؤقت" else "تشغيل",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Next Ayah
                    IconButton(
                        onClick = { audioPlayer.nextAyah() },
                        enabled = state.currentAyahNumber < state.totalAyahsInSurah
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SkipNext,
                            contentDescription = "الآية التالية",
                            tint = if (state.currentAyahNumber < state.totalAyahsInSurah) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(millis: Int): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

