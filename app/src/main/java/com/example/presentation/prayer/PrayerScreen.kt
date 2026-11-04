package com.example.presentation.prayer

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.prayer.HijriCalendarHelper
import com.example.core.prayer.NextPrayerInfo
import com.example.core.prayer.PrayerSchedule
import com.example.core.prayer.SensorAccuracyLevel
import com.example.ui.theme.AmiriFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun PrayerScreen(
    viewModel: PrayerViewModel,
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val userPrefs by viewModel.userPreferences.collectAsStateWithLifecycle()
    val dayOffset by viewModel.dayOffset.collectAsStateWithLifecycle()
    val isLocating by viewModel.isLocating.collectAsStateWithLifecycle()
    val isUsingSavedLocation by viewModel.isUsingSavedLocation.collectAsStateWithLifecycle()
    val compassReading by viewModel.compassReading.collectAsStateWithLifecycle()
    val currentTimeTicker by viewModel.currentTimeTicker.collectAsStateWithLifecycle()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onLocationPermissionGranted()
        }
    }

    // Auto-request location if not yet permitted
    LaunchedEffect(Unit) {
        if (!userPrefs.hasLocationPermission) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            viewModel.refreshLocation()
        }
    }

    val schedule = remember(dayOffset, userPrefs) {
        viewModel.calculateScheduleForOffset(dayOffset, userPrefs)
    }

    val nextPrayer = remember(currentTimeTicker, userPrefs) {
        viewModel.getNextPrayer(userPrefs)
    }

    var selectedPrayerForOffsetDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showCalibrationDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("prayer_screen_root")
    ) {
        // Tab Row: Times vs Qibla
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { viewModel.selectTab(0) },
                text = { Text(stringResource(R.string.prayer_times_tab), fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("prayer_tab_times")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { viewModel.selectTab(1) },
                text = { Text(stringResource(R.string.prayer_qibla_tab), fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("prayer_tab_qibla")
            )
        }

        if (selectedTab == 0) {
            // Prayer Times Screen
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Location Bar
                item {
                    LocationStatusBar(
                        cityName = userPrefs.lastCityName,
                        isUsingSaved = isUsingSavedLocation,
                        isLocating = isLocating,
                        onRefreshLocation = {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    )
                }

                // Next Prayer Live Countdown Card
                item {
                    NextPrayerHeroCard(
                        nextPrayer = nextPrayer,
                        modifier = Modifier.testTag("next_prayer_hero_card")
                    )
                }

                // Date Navigation Bar (Yesterday, Today, Tomorrow)
                item {
                    DateNavigationBar(
                        dayOffset = dayOffset,
                        currentScheduleDate = schedule.date,
                        onPrevious = { viewModel.previousDay() },
                        onNext = { viewModel.nextDay() },
                        onResetToday = { viewModel.resetToToday() }
                    )
                }

                // Prayer Times Cards List
                item {
                    Text(
                        text = "جدول الصلوات اليومية",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                val timeFormatter = SimpleDateFormat("hh:mm a", Locale("ar"))

                val prayersData = listOf(
                    PrayerItemData("fajr", "الفجر", schedule.fajr, Icons.Filled.WbTwilight, userPrefs.notifyFajr, userPrefs.offsetFajr),
                    PrayerItemData("sunrise", "الشروق", schedule.sunrise, Icons.Filled.WbSunny, userPrefs.notifySunrise, userPrefs.offsetSunrise),
                    PrayerItemData("dhuhr", "الظهر", schedule.dhuhr, Icons.Filled.WbSunny, userPrefs.notifyDhuhr, userPrefs.offsetDhuhr),
                    PrayerItemData("asr", "العصر", schedule.asr, Icons.Filled.WbSunny, userPrefs.notifyAsr, userPrefs.offsetAsr),
                    PrayerItemData("maghrib", "المغرب", schedule.maghrib, Icons.Filled.WbTwilight, userPrefs.notifyMaghrib, userPrefs.offsetMaghrib),
                    PrayerItemData("isha", "العشاء", schedule.isha, Icons.Filled.Mosque, userPrefs.notifyIsha, userPrefs.offsetIsha)
                )

                items(prayersData.size) { index ->
                    val prayer = prayersData[index]
                    val isNext = nextPrayer.prayerNameArabic == prayer.nameArabic && dayOffset == 0

                    PrayerRowCard(
                        prayer = prayer,
                        formattedTime = timeFormatter.format(prayer.timeDate),
                        isNextPrayer = isNext,
                        onToggleNotification = { enabled ->
                            viewModel.togglePrayerNotification(prayer.key, enabled)
                        },
                        onOpenOffsetDialog = {
                            selectedPrayerForOffsetDialog = Pair(prayer.key, prayer.nameArabic)
                        }
                    )
                }

                // Calculation details footer
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "طريقة الحساب: ${userPrefs.calculationMethod.labelArabic} • مذهب العصر: ${userPrefs.madhhab.labelArabic}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "يمكنك تغيير طريقة الحساب والمذهب أو تعديل الدقائق يدوياً من الإعدادات.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        } else {
            // Qibla Compass Screen
            val qiblaBearing = remember(userPrefs) { viewModel.getQiblaBearing(userPrefs) }
            val distanceKm = remember(userPrefs) { viewModel.getDistanceToKaabaKm(userPrefs) }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Location status banner
                item {
                    LocationStatusBar(
                        cityName = userPrefs.lastCityName,
                        isUsingSaved = isUsingSavedLocation,
                        isLocating = isLocating,
                        onRefreshLocation = {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    )
                }

                // Interactive Qibla Compass
                item {
                    QiblaCompassWidget(
                        qiblaBearing = qiblaBearing,
                        compassReading = compassReading,
                        distanceKm = distanceKm,
                        onShowCalibration = { showCalibrationDialog = true }
                    )
                }

                // Calibration Guide Card
                item {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCalibrationDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CompassCalibration,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "معايرة البوصلة ودقة التوجيه",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "الحالة الحالية: ${compassReading.accuracy.labelArabic}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "تعليمات",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Manual Offset Adjustment Dialog
    selectedPrayerForOffsetDialog?.let { (key, name) ->
        val currentOffset = when (key) {
            "fajr" -> userPrefs.offsetFajr
            "sunrise" -> userPrefs.offsetSunrise
            "dhuhr" -> userPrefs.offsetDhuhr
            "asr" -> userPrefs.offsetAsr
            "maghrib" -> userPrefs.offsetMaghrib
            else -> userPrefs.offsetIsha
        }

        AlertDialog(
            onDismissRequest = { selectedPrayerForOffsetDialog = null },
            title = {
                Text("تعديل يدوي لدقائق $name", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "التعديل الحالي: ${if (currentOffset > 0) "+$currentOffset" else "$currentOffset"} دقيقة",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.updateManualOffset(key, -1) }) {
                            Icon(Icons.Filled.Remove, contentDescription = "إنقاص دقيقة")
                        }
                        Text(
                            text = "$currentOffset",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { viewModel.updateManualOffset(key, +1) }) {
                            Icon(Icons.Filled.Add, contentDescription = "زيادة دقيقة")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "يمكنك مطابقة وقت الأذان بدقة مع مسجدك المحلي.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedPrayerForOffsetDialog = null }) {
                    Text("تم", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Calibration Dialog
    if (showCalibrationDialog) {
        AlertDialog(
            onDismissRequest = { showCalibrationDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.CompassCalibration,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("طريقة معايرة بوصلة القبلة", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "للحصول على أدق اتجاه للقبلة الشريفة:",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("١. ابتعد عن الأجهزة الإلكترونية والمعادن والأسطح المغناطيسية.")
                    Text("٢. أمسك الهاتف بشكل أفقي مسطح أمامك.")
                    Text("٣. حرّك الهاتف في الهواء بلطف على شكل الرقم ثمانية (8) الإنجليزي عدة مرات.")
                    Text("٤. عندما يتحول السهم إلى اللون الأخضر الزاهي، ستكون مواجهاً لمكة المكرمة مباشرة.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showCalibrationDialog = false }) {
                    Text("فهمت ذلك", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

data class PrayerItemData(
    val key: String,
    val nameArabic: String,
    val timeDate: Date,
    val icon: ImageVector,
    val isNotifyEnabled: Boolean,
    val offsetMinutes: Int
)

@Composable
fun LocationStatusBar(
    cityName: String,
    isUsingSaved: Boolean,
    isLocating: Boolean,
    onRefreshLocation: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = cityName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isUsingSaved) "الموقع المحفوظ (انقر للتحديث)" else "محدد عبر GPS",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isUsingSaved) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (isLocating) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                IconButton(onClick = onRefreshLocation) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = "تحديث الموقع",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun NextPrayerHeroCard(
    nextPrayer: NextPrayerInfo,
    modifier: Modifier = Modifier
) {
    val totalSeconds = (nextPrayer.remainingMillis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val countdownFormatted = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "الصلاة القادمة",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                    Text(
                        text = nextPrayer.prayerNameArabic,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontFamily = AmiriFontFamily
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = SimpleDateFormat("hh:mm a", Locale("ar")).format(nextPrayer.prayerTime),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "الوقت المتبقي للأذان",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
            )
            Text(
                text = countdownFormatted,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun DateNavigationBar(
    dayOffset: Int,
    currentScheduleDate: Date,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onResetToday: () -> Unit
) {
    val dateLabel = when (dayOffset) {
        0 -> "اليوم (${SimpleDateFormat("EEEE، d MMMM", Locale("ar")).format(currentScheduleDate)})"
        -1 -> "أمس (${SimpleDateFormat("d MMMM", Locale("ar")).format(currentScheduleDate)})"
        1 -> "غداً (${SimpleDateFormat("d MMMM", Locale("ar")).format(currentScheduleDate)})"
        else -> SimpleDateFormat("EEEE، d MMMM", Locale("ar")).format(currentScheduleDate)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPrevious,
            modifier = Modifier.testTag("prayer_prev_day_btn")
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "اليوم السابق")
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clickable(onClick = onResetToday)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            if (dayOffset != 0) {
                Text(
                    text = "انقر للعودة إلى اليوم",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        IconButton(
            onClick = onNext,
            modifier = Modifier.testTag("prayer_next_day_btn")
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "اليوم التالي")
        }
    }
}

@Composable
fun PrayerRowCard(
    prayer: PrayerItemData,
    formattedTime: String,
    isNextPrayer: Boolean,
    onToggleNotification: (Boolean) -> Unit,
    onOpenOffsetDialog: () -> Unit
) {
    val containerColor = if (isNextPrayer) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prayer_row_${prayer.key}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isNextPrayer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = prayer.icon,
                        contentDescription = null,
                        tint = if (isNextPrayer) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = prayer.nameArabic,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isNextPrayer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        if (isNextPrayer) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "القادمة",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    if (prayer.offsetMinutes != 0) {
                        Text(
                            text = "تعديل: ${if (prayer.offsetMinutes > 0) "+${prayer.offsetMinutes}" else "${prayer.offsetMinutes}"} دقيقة",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Manual offset button
                IconButton(
                    onClick = onOpenOffsetDialog,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Tune,
                        contentDescription = "تعديل الدقائق",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Notification toggle icon
                IconButton(
                    onClick = { onToggleNotification(!prayer.isNotifyEnabled) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (prayer.isNotifyEnabled) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                        contentDescription = if (prayer.isNotifyEnabled) "تعطيل التنبيه" else "تفعيل التنبيه",
                        tint = if (prayer.isNotifyEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QiblaCompassWidget(
    qiblaBearing: Float,
    compassReading: com.example.core.prayer.CompassReading,
    distanceKm: Double,
    onShowCalibration: () -> Unit
) {
    val currentCompassAzimuth = compassReading.azimuthDegrees
    // Needle rotation angle pointing to Kaaba:
    val relativeNeedleAngle = (qiblaBearing - currentCompassAzimuth + 360f) % 360f
    val isFacingQibla = relativeNeedleAngle in 355f..360f || relativeNeedleAngle in 0f..5f

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "زاوية القبلة: ${qiblaBearing.roundToInt()}° من الشمال الحقيقي",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "المسافة إلى مكة المكرمة: ${distanceKm.roundToInt()} كم تقريباً",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Large Custom Compass Canvas
        val primaryColor = MaterialTheme.colorScheme.primary
        val tertiaryColor = MaterialTheme.colorScheme.tertiary
        val outlineColor = MaterialTheme.colorScheme.outlineVariant
        val goldColor = Color(0xFFD4AF37)
        val greenFacingColor = Color(0xFF00C853)

        Box(
            modifier = Modifier
                .size(260.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(4.dp, if (isFacingQibla) greenFacingColor else outlineColor, CircleShape)
                .testTag("qibla_compass_canvas"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(240.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2

                // Dial outer circles
                drawCircle(
                    color = outlineColor.copy(alpha = 0.5f),
                    radius = radius - 10.dp.toPx(),
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                // 12 Compass ticks
                for (i in 0 until 12) {
                    val angleDeg = i * 30.0
                    val tickLen = if (i % 3 == 0) 14.dp.toPx() else 8.dp.toPx()
                    val strokeW = if (i % 3 == 0) 3.dp.toPx() else 1.5.dp.toPx()
                    val tickColor = if (i == 0) Color.Red else outlineColor

                    rotate(degrees = (angleDeg - currentCompassAzimuth).toFloat(), pivot = center) {
                        drawLine(
                            color = tickColor,
                            start = Offset(center.x, center.y - radius + 12.dp.toPx()),
                            end = Offset(center.x, center.y - radius + 12.dp.toPx() + tickLen),
                            strokeWidth = strokeW
                        )
                    }
                }

                // Needle pointing towards Mecca
                rotate(degrees = relativeNeedleAngle, pivot = center) {
                    val headPath = Path().apply {
                        moveTo(center.x, center.y - radius + 30.dp.toPx())
                        lineTo(center.x - 16.dp.toPx(), center.y)
                        lineTo(center.x + 16.dp.toPx(), center.y)
                        close()
                    }
                    drawPath(
                        headPath,
                        color = if (isFacingQibla) greenFacingColor else goldColor
                    )

                    val tailPath = Path().apply {
                        moveTo(center.x, center.y + radius - 30.dp.toPx())
                        lineTo(center.x - 12.dp.toPx(), center.y)
                        lineTo(center.x + 12.dp.toPx(), center.y)
                        close()
                    }
                    drawPath(tailPath, color = primaryColor.copy(alpha = 0.7f))

                    drawCircle(
                        color = if (isFacingQibla) greenFacingColor else primaryColor,
                        radius = 12.dp.toPx(),
                        center = center
                    )
                }
            }

            // Direction text
            if (isFacingQibla) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = greenFacingColor.copy(alpha = 0.15f),
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "أنت تواجه القبلة الآن ✓",
                        color = greenFacingColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (!compassReading.isSensorAvailable) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "مستشعر البوصلة غير متوفر على جهازك. تم حساب زاوية القبلة فلكياً بدقة (${qiblaBearing.roundToInt()}° من الشمال) لتوجيهك بدقة بالاستعانة بالشمال.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Sensors,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "دقة المستشعر: ${compassReading.accuracy.labelArabic}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
