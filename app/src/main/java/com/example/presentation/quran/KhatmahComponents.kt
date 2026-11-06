package com.example.presentation.quran

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.KhatmahEntity

@Composable
fun KhatmahCard(
    khatmah: KhatmahEntity,
    onAddProgress: (amount: Int) -> Unit,
    onTogglePause: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("khatmah_card_${khatmah.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (khatmah.isCompleted)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Title, Status badge, Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (khatmah.isCompleted)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (khatmah.isCompleted) Icons.Filled.CheckCircle else Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = if (khatmah.isCompleted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = khatmah.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (khatmah.isCompleted) "ختمة مباركة مكتملة 🎉"
                        else if (khatmah.isPaused) "متوقفة مؤقتاً"
                        else "الهدف: ${khatmah.targetDays} يوماً • ورد اليوم: ${khatmah.dailyPortion} ${if (khatmah.targetType == "PAGES") "صفحة" else "آية"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Pause toggle
                if (!khatmah.isCompleted) {
                    IconButton(onClick = onTogglePause) {
                        Icon(
                            imageVector = if (khatmah.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                            contentDescription = if (khatmah.isPaused) "استئناف" else "إيقاف مؤقت",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Delete
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "حذف الختمة",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular percentage
                Box(
                    modifier = Modifier.size(68.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { (khatmah.progressPercentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier.size(68.dp),
                        strokeWidth = 6.dp,
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = "${khatmah.progressPercentage.toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "المقروء: ${khatmah.currentProgress} من ${khatmah.totalTarget} ${if (khatmah.targetType == "PAGES") "صفحة" else "آية"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "تاريخ البداية: ${khatmah.startDate}  |  المتبقي: ${khatmah.totalTarget - khatmah.currentProgress}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick log buttons if not completed
            if (!khatmah.isCompleted && !khatmah.isPaused) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onAddProgress(1) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+1 ${if (khatmah.targetType == "PAGES") "صفحة" else "آية"}")
                    }
                    OutlinedButton(
                        onClick = { onAddProgress(5) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("+5")
                    }
                    Button(
                        onClick = { onAddProgress(khatmah.dailyPortion) },
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("تم ورد اليوم (+${khatmah.dailyPortion})")
                    }
                }
            }
        }
    }
}

@Composable
fun CreateKhatmahDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, targetDays: Int, targetType: String, totalTarget: Int) -> Unit
) {
    var title by remember { mutableStateOf("ختمة القرآن الكريم") }
    var targetDaysText by remember { mutableStateOf("30") }
    var targetType by remember { mutableStateOf("PAGES") } // "PAGES" or "AYAHS"

    val days = targetDaysText.toIntOrNull() ?: 30
    val totalTarget = if (targetType == "PAGES") 604 else 6236
    val dailyPortion = if (days > 0) Math.ceil(totalTarget.toDouble() / days).toInt() else 20

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("إنشاء ختمة جديدة", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("اسم الختمة") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = targetDaysText,
                    onValueChange = { targetDaysText = it },
                    label = { Text("المدة المستهدفة (بالأيام)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Quick presets
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(15, 30, 60).forEach { pDays ->
                        OutlinedButton(
                            onClick = { targetDaysText = pDays.toString() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("$pDays يوم")
                        }
                    }
                }

                // Type selector
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { targetType = "PAGES" },
                        modifier = Modifier.weight(1f),
                        colors = if (targetType == "PAGES")
                            androidx.compose.material3.ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        else androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("بالصفحات (604)")
                    }
                    OutlinedButton(
                        onClick = { targetType = "AYAHS" },
                        modifier = Modifier.weight(1f),
                        colors = if (targetType == "AYAHS")
                            androidx.compose.material3.ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        else androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("بالآيات (6236)")
                    }
                }

                Text(
                    text = "الورد اليومي المقدر: $dailyPortion ${if (targetType == "PAGES") "صفحة يومياً" else "آية يومياً"}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && days > 0) {
                        onConfirm(title.trim(), days, targetType, totalTarget)
                    }
                }
            ) {
                Text("بدء الختمة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
