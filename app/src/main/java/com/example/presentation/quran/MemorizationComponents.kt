package com.example.presentation.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MemorizationEntity
import com.example.data.local.entity.QuranAyahEntity
import com.example.data.local.entity.QuranSurahEntity
import com.example.ui.theme.AmiriFontFamily

@Composable
fun MemorizationCard(
    record: MemorizationEntity,
    onLogReview: (newLevel: Int, nextDays: Int) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showReviewDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("memorization_card_${record.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Psychology,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "سورة ${record.surahName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontFamily = AmiriFontFamily
                    )
                    Text(
                        text = "الآيات من ${record.startAyah} إلى ${record.endAyah} (${record.endAyah - record.startAyah + 1} آيات)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "حذف سجل الحفظ",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mastery level stars
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "مستوى الإتقان: ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                for (i in 1..5) {
                    Icon(
                        imageVector = if (i <= record.masteryLevel) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = null,
                        tint = if (i <= record.masteryLevel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "المراجعات: ${record.reviewCount}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Dates & Review button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "المراجعة القادمة: ${record.nextReviewDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = { showReviewDialog = true },
                    modifier = Modifier.testTag("log_review_btn_${record.id}")
                ) {
                    Text("تسجيل مراجعة")
                }
            }
        }
    }

    if (showReviewDialog) {
        ReviewLogDialog(
            currentLevel = record.masteryLevel,
            onDismiss = { showReviewDialog = false },
            onConfirm = { newLevel, nextDays ->
                onLogReview(newLevel, nextDays)
                showReviewDialog = false
            }
        )
    }
}

@Composable
fun ReviewLogDialog(
    currentLevel: Int,
    onDismiss: () -> Unit,
    onConfirm: (newLevel: Int, nextDays: Int) -> Unit
) {
    var selectedLevel by remember { mutableIntStateOf(currentLevel) }
    var nextReviewDays by remember { mutableIntStateOf(3) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تسجيل مراجعة الحفظ", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("حدد مستوى الإتقان في هذه المراجعة:")
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (i in 1..5) {
                        IconButton(onClick = { selectedLevel = i }) {
                            Icon(
                                imageVector = if (i <= selectedLevel) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = "$i نجوم",
                                tint = if (i <= selectedLevel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Text("جدولة موعد المراجعة القادمة:")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        1 to "غداً",
                        3 to "3 أيام",
                        7 to "أسبوع",
                        14 to "أسبوعين"
                    ).forEach { (days, label) ->
                        OutlinedButton(
                            onClick = { nextReviewDays = days },
                            modifier = Modifier.weight(1f),
                            colors = if (nextReviewDays == days)
                                androidx.compose.material3.ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            else androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(label, fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedLevel, nextReviewDays) }) {
                Text("حفظ المراجعة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun AddMemorizationDialog(
    surahs: List<QuranSurahEntity>,
    onDismiss: () -> Unit,
    onConfirm: (surahNumber: Int, surahName: String, startAyah: Int, endAyah: Int, masteryLevel: Int, nextReviewDays: Int, notes: String?) -> Unit
) {
    var selectedSurah by remember { mutableStateOf(surahs.firstOrNull()) }
    var startAyahText by remember { mutableStateOf("1") }
    var endAyahText by remember { mutableStateOf(selectedSurah?.totalAyahs?.coerceAtMost(10)?.toString() ?: "7") }
    var masteryLevel by remember { mutableIntStateOf(3) }
    var notes by remember { mutableStateOf("") }
    var showSurahDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة ورد حفظ جديد", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Surah selector
                OutlinedButton(
                    onClick = { showSurahDropdown = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedSurah?.let { "سورة ${it.nameArabic}" } ?: "اختر السورة")
                    DropdownMenu(
                        expanded = showSurahDropdown,
                        onDismissRequest = { showSurahDropdown = false }
                    ) {
                        surahs.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("${s.surahNumber}. سورة ${s.nameArabic} (${s.totalAyahs} آية)") },
                                onClick = {
                                    selectedSurah = s
                                    endAyahText = s.totalAyahs.coerceAtMost(10).toString()
                                    showSurahDropdown = false
                                }
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = startAyahText,
                        onValueChange = { startAyahText = it },
                        label = { Text("من آية") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endAyahText,
                        onValueChange = { endAyahText = it },
                        label = { Text("إلى آية") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("مستوى الإتقان المبدئي:")
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    for (i in 1..5) {
                        IconButton(onClick = { masteryLevel = i }) {
                            Icon(
                                imageVector = if (i <= masteryLevel) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = "$i نجوم",
                                tint = if (i <= masteryLevel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = selectedSurah ?: return@Button
                    val start = startAyahText.toIntOrNull() ?: 1
                    val end = (endAyahText.toIntOrNull() ?: s.totalAyahs).coerceAtMost(s.totalAyahs)
                    onConfirm(s.surahNumber, s.nameArabic, start, end, masteryLevel, 3, notes.ifBlank { null })
                }
            ) {
                Text("إضافة")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

data class QuizQuestion(
    val ayahNumber: Int,
    val fullText: String,
    val maskedText: String,
    val correctWord: String,
    val options: List<String>
)

@Composable
fun MemorizationQuizDialog(
    surah: QuranSurahEntity,
    ayahs: List<QuranAyahEntity>,
    onDismiss: () -> Unit,
    onSaveResult: (total: Int, correct: Int) -> Unit
) {
    // Generate questions from ayahs
    val questions = remember(ayahs) {
        generateQuizQuestions(ayahs)
    }

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }
    var correctCount by remember { mutableIntStateOf(0) }
    var quizCompleted by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Filled.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "اختبار حفظ سورة ${surah.nameArabic}",
                    fontWeight = FontWeight.Bold,
                    fontFamily = AmiriFontFamily
                )
            }
        },
        text = {
            if (questions.isEmpty()) {
                if (ayahs.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "جاري تحميل وتجهيز آيات سورة ${surah.nameArabic} للاختبار الذاتي...",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    Text("لا تتوفر آيات كافية لتوليد أسئلة الاختبار لهذه السورة.")
                }
            } else if (quizCompleted) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "النتيجة النهائية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$correctCount من ${questions.size}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val percent = ((correctCount.toFloat() / questions.size) * 100).toInt()
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (percent >= 80) "ممتاز! حفظك متقن ومبارك 🌟" else "واصل المراجعة والتكرار لتثبيت الحفظ بإذن الله ✨",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                val q = questions[currentQuestionIndex]
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "السؤال ${currentQuestionIndex + 1} من ${questions.size} (الآية ${q.ayahNumber}):",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Masked ayah text card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = q.maskedText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = AmiriFontFamily,
                            lineHeight = 32.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }

                    Text("اختر الكلمة الصحيحة لإكمال الآية:")

                    // 4 choices
                    q.options.forEach { option ->
                        val isSelected = selectedOption == option
                        val isCorrect = option == q.correctWord
                        val backgroundColor = when {
                            !isAnswerSubmitted && isSelected -> MaterialTheme.colorScheme.primaryContainer
                            isAnswerSubmitted && isCorrect -> Color(0xFFD4EDDA)
                            isAnswerSubmitted && isSelected && !isCorrect -> Color(0xFFF8D7DA)
                            else -> MaterialTheme.colorScheme.surface
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(backgroundColor)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .clickable(enabled = !isAnswerSubmitted) {
                                    selectedOption = option
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontFamily = AmiriFontFamily,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isAnswerSubmitted) {
                                    if (isCorrect) {
                                        Icon(Icons.Filled.Check, contentDescription = "صحيح", tint = Color(0xFF28A745))
                                    } else if (isSelected) {
                                        Icon(Icons.Filled.Close, contentDescription = "خطأ", tint = Color(0xFFDC3545))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (questions.isEmpty()) {
                TextButton(onClick = onDismiss) { Text("إغلاق") }
            } else if (quizCompleted) {
                Button(
                    onClick = {
                        onSaveResult(questions.size, correctCount)
                        onDismiss()
                    }
                ) {
                    Text("حفظ النتيجة وإغلاق")
                }
            } else {
                if (!isAnswerSubmitted) {
                    Button(
                        onClick = {
                            if (selectedOption != null) {
                                isAnswerSubmitted = true
                                if (selectedOption == questions[currentQuestionIndex].correctWord) {
                                    correctCount++
                                }
                            }
                        },
                        enabled = selectedOption != null
                    ) {
                        Text("تأكيد الإجابة")
                    }
                } else {
                    Button(
                        onClick = {
                            if (currentQuestionIndex < questions.size - 1) {
                                currentQuestionIndex++
                                selectedOption = null
                                isAnswerSubmitted = false
                            } else {
                                quizCompleted = true
                            }
                        }
                    ) {
                        Text(if (currentQuestionIndex < questions.size - 1) "السؤال التالي" else "عرض النتيجة")
                    }
                }
            }
        },
        dismissButton = {
            if (!quizCompleted) {
                TextButton(onClick = onDismiss) { Text("إنهاء الاختبار") }
            }
        }
    )
}

private fun generateQuizQuestions(ayahs: List<QuranAyahEntity>): List<QuizQuestion> {
    if (ayahs.isEmpty()) return emptyList()

    val questions = mutableListOf<QuizQuestion>()
    val eligibleAyahs = ayahs.filter { it.textUthmani.trim().split("\\s+".toRegex()).size >= 4 }

    // Pick up to 5 questions
    val selectedAyahs = eligibleAyahs.shuffled().take(5)

    val allWords = ayahs.flatMap { it.textUthmani.trim().split("\\s+".toRegex()) }
        .filter { it.length >= 3 }
        .distinct()

    for (ayah in selectedAyahs) {
        val words = ayah.textUthmani.trim().split("\\s+".toRegex())
        val targetWordIndex = (1 until (words.size - 1)).random()
        val correctWord = words[targetWordIndex]

        val maskedWords = words.toMutableList()
        maskedWords[targetWordIndex] = "[ ... ]"
        val maskedText = maskedWords.joinToString(" ")

        val distractors = allWords.filter { it != correctWord }.shuffled().take(3)
        val options = (distractors + correctWord).shuffled()

        questions.add(
            QuizQuestion(
                ayahNumber = ayah.ayahNumberInSurah,
                fullText = ayah.textUthmani,
                maskedText = maskedText,
                correctWord = correctWord,
                options = options
            )
        )
    }
    return questions
}
