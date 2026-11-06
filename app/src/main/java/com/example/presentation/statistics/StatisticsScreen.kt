package com.example.presentation.statistics

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.config.AppConfig
import com.example.data.local.entity.DailyAchievementEntity
import com.example.ui.theme.AmiriFontFamily
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val todayAchievement by viewModel.todayAchievement.collectAsStateWithLifecycle()
    val streakDays by viewModel.streakDays.collectAsStateWithLifecycle()
    val last30Days by viewModel.last30Days.collectAsStateWithLifecycle()
    val allAchievements by viewModel.allAchievements.collectAsStateWithLifecycle()
    val activeKhatmahs by viewModel.activeKhatmahs.collectAsStateWithLifecycle()
    val memorizationRecords by viewModel.memorizationRecords.collectAsStateWithLifecycle()

    var showDeleteConfirmDialog1 by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog2 by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "الملف الشخصي والإحصائيات",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("statistics_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.testTag("statistics_screen_root")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Hero Streak & Profile Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        )
                        .padding(20.dp)
                        .testTag("statistics_hero_streak")
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.EmojiEvents,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiaryContainer,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "المسار الإيماني",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Text(
                                        text = if (streakDays > 0) "أيام استمرارية متتالية" else "ابدأ مسار استمراريتك اليوم",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                    )
                                }
                            }

                            // Streak Badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$streakDays يوم",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Lifetime Stats Grid (derived from all achievements)
                        val totalPrayers = allAchievements.sumOf { Integer.bitCount(it.prayersCompletedMask) }
                        val totalDhikr = allAchievements.sumOf { it.totalDhikrCount }
                        val totalAyahs = allAchievements.sumOf { it.quranAyahsRead }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatSummaryPill(
                                label = "الصلوات",
                                value = "$totalPrayers",
                                icon = Icons.Filled.Mosque,
                                modifier = Modifier.weight(1f)
                            )
                            StatSummaryPill(
                                label = "التسبيحات",
                                value = "$totalDhikr",
                                icon = Icons.Filled.SelfImprovement,
                                modifier = Modifier.weight(1f)
                            )
                            StatSummaryPill(
                                label = "آيات مقروءة",
                                value = "$totalAyahs",
                                icon = Icons.AutoMirrored.Filled.MenuBook,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 2. Today's Prayers Checklist (Interactive real log)
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("statistics_prayers_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Mosque,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "صلوات اليوم المكتملة",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "انقر على الصلاة لتحديدها كمؤداة وحفظها في سجلك الدائم",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val mask = todayAchievement?.prayersCompletedMask ?: 0
                        val prayers = listOf(
                            Triple("الفجر", 0, (mask and 1) != 0),
                            Triple("الظهر", 1, (mask and 2) != 0),
                            Triple("العصر", 2, (mask and 4) != 0),
                            Triple("المغرب", 3, (mask and 8) != 0),
                            Triple("العشاء", 4, (mask and 16) != 0)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            prayers.forEach { (name, index, done) ->
                                PrayerCheckChip(
                                    prayerName = name,
                                    isDone = done,
                                    onClick = { viewModel.togglePrayer(index) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Today's Quran & Dhikr Details
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "إنجاز اليوم في القرآن والذكر",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val ayahs = todayAchievement?.quranAyahsRead ?: 0
                        val minutes = todayAchievement?.quranMinutesRead ?: 0
                        val dhikr = todayAchievement?.totalDhikrCount ?: 0

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DetailMetricBox(
                                title = "القرآن اليوم",
                                value = if (ayahs > 0) "$ayahs آية ($minutes دقيقة)" else "لم تقرأ بعد",
                                subtitle = "يُسجل تلقائياً أثناء القراءة",
                                icon = Icons.AutoMirrored.Filled.MenuBook,
                                modifier = Modifier.weight(1f)
                            )
                            DetailMetricBox(
                                title = "التسبيح اليوم",
                                value = if (dhikr > 0) "$dhikr ذكر" else "0 تسبيحة",
                                subtitle = "سجل السبحة والأذكار",
                                icon = Icons.Filled.SelfImprovement,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 4. Khatmah & Memorization Progress Section
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "تقدم الختمات ومراجعة الحفظ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (activeKhatmahs.isEmpty() && memorizationRecords.isEmpty()) {
                            Text(
                                text = "لا توجد ختمات نشطة أو مقاطع حفظ محفوظة حالياً. يمكنك إنشاء ختمتك أو تسجيل محفوظاتك من شاشة القرآن الكريم.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            if (activeKhatmahs.isNotEmpty()) {
                                Text(
                                    text = "الختمات الجارية:",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                activeKhatmahs.forEach { khatmah ->
                                    val percent = if (khatmah.totalTarget > 0) (khatmah.currentProgress.toFloat() / khatmah.totalTarget).coerceIn(0f, 1f) else 0f
                                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = khatmah.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                text = "${(percent * 100).toInt()}% (${khatmah.currentProgress}/${khatmah.totalTarget})",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { percent },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp)),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        )
                                    }
                                }
                            }

                            if (memorizationRecords.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                val mastered = memorizationRecords.count { it.masteryLevel >= 4 }
                                Text(
                                    text = "المحفوظات: $mastered مقطع متقن من إجمالي ${memorizationRecords.size} مقطع مسجل",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }

            // 5. Weekly Activity Breakdown (Accessible Bars + Text Description)
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Timeline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "سجل الأسبوع الأخير",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "معدل أداء الصلوات والأوراد لآخر 7 أيام",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        val weeklyDays = remember(last30Days) {
                            val cal = Calendar.getInstance()
                            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                            val dayNameFmt = SimpleDateFormat("E", Locale("ar"))
                            val map = last30Days.associateBy { it.dateString }
                            (0..6).reversed().map { daysAgo ->
                                val dayCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -daysAgo) }
                                val dateKey = fmt.format(dayCal.time)
                                val dayName = dayNameFmt.format(dayCal.time)
                                val entity = map[dateKey]
                                WeeklyDayStat(
                                    dayNameArabic = dayName,
                                    dateString = dateKey,
                                    prayersCount = if (entity != null) Integer.bitCount(entity.prayersCompletedMask) else 0,
                                    dhikrCount = entity?.totalDhikrCount ?: 0,
                                    ayahsRead = entity?.quranAyahsRead ?: 0
                                )
                            }
                        }

                        val hasAnyActivity = weeklyDays.any { it.prayersCount > 0 || it.dhikrCount > 0 || it.ayahsRead > 0 }

                        if (!hasAnyActivity) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "لا توجد سجلات خلال هذا الأسبوع بعد.\nسيبدأ التطبيق في رسم مؤشراتك تلقائياً عند تعليم صلواتك وقراءتك للأوراد.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .padding(horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                weeklyDays.forEach { day ->
                                    val heightFraction = (day.prayersCount / 5f).coerceIn(0.08f, 1f)
                                    val dayDesc = "${day.dayNameArabic} ${day.dateString}: ${day.prayersCount} صلوات، ${day.dhikrCount} تسبيحة"

                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier
                                            .weight(1f)
                                            .semantics { contentDescription = dayDesc }
                                    ) {
                                        Text(
                                            text = "${day.prayersCount}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (day.prayersCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .width(22.dp)
                                                .height((80 * heightFraction).dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (day.prayersCount > 0) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.surfaceVariant
                                                )
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = day.dayNameArabic,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 6. Data Management & Export Actions
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "إدارة البيانات والخصوصية",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "جميع بياناتك وسجلاتك محفوظة محلياً بالكامل على جهازك. يمكنك تصديرها كنسخة احتياطية أو حذفها نهائياً بأمان.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Export JSON button
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val json = viewModel.getExportJson()
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, json)
                                        putExtra(Intent.EXTRA_TITLE, "تصدير بيانات نور المسلم (JSON)")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "تصدير البيانات (JSON)"))
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_export_json")
                        ) {
                            Icon(imageVector = Icons.Filled.FileDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تصدير كافة البيانات (JSON)", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Export CSV button
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val csv = viewModel.getExportCsv()
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, csv)
                                        putExtra(Intent.EXTRA_TITLE, "تصدير سجل الأداء (CSV)")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "تصدير السجل اليومي (CSV)"))
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_export_csv")
                        ) {
                            Icon(imageVector = Icons.Filled.FileDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تصدير السجل اليومي (CSV)", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Delete all data button
                        Button(
                            onClick = { showDeleteConfirmDialog1 = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_clear_all_data")
                        ) {
                            Icon(imageVector = Icons.Filled.DeleteForever, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("مسح كافة البيانات والسجلات", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Confirmation Step 1
    if (showDeleteConfirmDialog1) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog1 = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("مسح كافة البيانات؟", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text("هل ترغب في مسح كافة سجلات الصلاة، التسبيح، الختمات، الحفظ، والمفضلة؟\nلن تتأثر نصوص القرآن والمكتبة.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog1 = false
                        showDeleteConfirmDialog2 = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("متابعة المسح")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog1 = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Confirmation Step 2
    if (showDeleteConfirmDialog2) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog2 = false },
            title = {
                Text("تأكيد نهائي لا يمكن الرجوع عنه", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            },
            text = {
                Text("هذا الإجراء سيحذف نهائياً جميع محفوظاتك وسجلات استمراريتك من قاعدة البيانات المحلية في هذا الهاتف.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog2 = false
                        viewModel.clearAllData {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("تم مسح كافة البيانات والسجلات بنجاح")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("تأكيد المسح النهائي")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog2 = false }) {
                    Text("تراجع")
                }
            }
        )
    }
}

@Composable
fun StatSummaryPill(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
fun PrayerCheckChip(
    prayerName: String,
    isDone: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isDone) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val content = if (isDone) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
    val border = if (isDone) MaterialTheme.colorScheme.primary else Color.Transparent

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bg,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("prayer_chip_$prayerName")
            .semantics {
                contentDescription = "صلاة $prayerName: ${if (isDone) "مؤداة" else "غير مؤداة"}"
            }
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (isDone) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = prayerName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal,
                color = content
            )
        }
    }
}

@Composable
fun DetailMetricBox(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
