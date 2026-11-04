package com.example.presentation.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.preferences.AudioPreference
import com.example.data.local.preferences.CalculationMethod
import com.example.data.local.preferences.FontSizeScale
import com.example.data.local.preferences.Madhhab
import com.example.data.local.preferences.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userPrefs by viewModel.userPreferences.collectAsStateWithLifecycle()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setNotificationsEnabled(isGranted)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
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
        modifier = modifier.testTag("settings_screen_root")
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Theme Mode Group
            item {
                SettingsSectionCard(
                    title = stringResource(R.string.settings_theme_title),
                    icon = Icons.Filled.Palette
                ) {
                    ThemeOptionRow(
                        label = stringResource(R.string.settings_theme_system),
                        selected = userPrefs.themeMode == ThemeMode.SYSTEM,
                        onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                        testTag = "theme_option_system"
                    )
                    ThemeOptionRow(
                        label = stringResource(R.string.settings_theme_light),
                        selected = userPrefs.themeMode == ThemeMode.LIGHT,
                        onClick = { viewModel.setThemeMode(ThemeMode.LIGHT) },
                        testTag = "theme_option_light"
                    )
                    ThemeOptionRow(
                        label = stringResource(R.string.settings_theme_dark),
                        selected = userPrefs.themeMode == ThemeMode.DARK,
                        onClick = { viewModel.setThemeMode(ThemeMode.DARK) },
                        testTag = "theme_option_dark"
                    )
                }
            }

            // Font Size Group
            item {
                SettingsSectionCard(
                    title = stringResource(R.string.settings_font_size),
                    icon = Icons.Filled.FormatSize
                ) {
                    FontSizeScale.values().forEach { scaleOption ->
                        ThemeOptionRow(
                            label = scaleOption.labelArabic,
                            selected = userPrefs.fontSizeScale == scaleOption,
                            onClick = { viewModel.setFontSizeScale(scaleOption) },
                            testTag = "font_scale_${scaleOption.name}"
                        )
                    }
                }
            }

            // Language Group
            item {
                SettingsSectionCard(
                    title = stringResource(R.string.settings_language),
                    icon = Icons.Filled.Language
                ) {
                    ThemeOptionRow(
                        label = stringResource(R.string.settings_lang_ar),
                        selected = userPrefs.language == "ar",
                        onClick = { viewModel.setLanguage("ar") },
                        testTag = "lang_option_ar"
                    )
                    ThemeOptionRow(
                        label = stringResource(R.string.settings_lang_en),
                        selected = userPrefs.language == "en",
                        onClick = { viewModel.setLanguage("en") },
                        testTag = "lang_option_en"
                    )
                }
            }

            // Notifications Group
            item {
                SettingsSectionCard(
                    title = stringResource(R.string.settings_notifications),
                    icon = Icons.Filled.Notifications
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.settings_notifications_enable),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "تنبيهات الأذان ودخول أوقات الصلاة والورد اليومي",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = userPrefs.notificationsEnabled,
                            onCheckedChange = { checked ->
                                if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.setNotificationsEnabled(checked)
                                }
                            },
                            modifier = Modifier.testTag("notifications_switch")
                        )
                    }

                    if (userPrefs.notificationsEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "التنبيه قبل الأذان",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        val preMinutesOptions = listOf(
                            0 to "بدون تنبيه مسبق",
                            5 to "قبل ٥ دقائق",
                            10 to "قبل ١٠ دقائق",
                            15 to "قبل ١٥ دقيقة"
                        )
                        preMinutesOptions.forEach { (mins, label) ->
                            ThemeOptionRow(
                                label = label,
                                selected = userPrefs.preAdhanMinutes == mins,
                                onClick = { viewModel.setPreAdhanMinutes(mins) },
                                testTag = "pre_adhan_opt_$mins"
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "الصلوات المفعلة للتنبيه",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        PrayerNotificationToggleRow("الفجر", userPrefs.notifyFajr) { viewModel.setPrayerNotification("fajr", it) }
                        PrayerNotificationToggleRow("الظهر", userPrefs.notifyDhuhr) { viewModel.setPrayerNotification("dhuhr", it) }
                        PrayerNotificationToggleRow("العصر", userPrefs.notifyAsr) { viewModel.setPrayerNotification("asr", it) }
                        PrayerNotificationToggleRow("المغرب", userPrefs.notifyMaghrib) { viewModel.setPrayerNotification("maghrib", it) }
                        PrayerNotificationToggleRow("العشاء", userPrefs.notifyIsha) { viewModel.setPrayerNotification("isha", it) }
                    }
                }
            }

            // Calculation Method Group
            item {
                SettingsSectionCard(
                    title = stringResource(R.string.settings_calc_method),
                    icon = Icons.Filled.Mosque
                ) {
                    CalculationMethod.values().forEach { method ->
                        ThemeOptionRow(
                            label = method.labelArabic,
                            selected = userPrefs.calculationMethod == method,
                            onClick = { viewModel.setCalculationMethod(method) },
                            testTag = "calc_method_${method.name}"
                        )
                    }
                }
            }

            // Madhhab Group
            item {
                SettingsSectionCard(
                    title = stringResource(R.string.settings_madhhab),
                    icon = Icons.Filled.Mosque
                ) {
                    Madhhab.values().forEach { madhhab ->
                        ThemeOptionRow(
                            label = madhhab.labelArabic,
                            selected = userPrefs.madhhab == madhhab,
                            onClick = { viewModel.setMadhhab(madhhab) },
                            testTag = "madhhab_${madhhab.name}"
                        )
                    }
                }
            }

            // Audio Preference Group
            item {
                SettingsSectionCard(
                    title = stringResource(R.string.settings_audio_pref),
                    icon = Icons.AutoMirrored.Filled.VolumeUp
                ) {
                    AudioPreference.values().forEach { pref ->
                        ThemeOptionRow(
                            label = pref.labelArabic,
                            selected = userPrefs.audioPreference == pref,
                            onClick = { viewModel.setAudioPreference(pref) },
                            testTag = "audio_pref_${pref.name}"
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            content()
        }
    }
}

@Composable
fun ThemeOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun PrayerNotificationToggleRow(
    prayerName: String,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "أذان $prayerName",
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(
            checked = enabled,
            onCheckedChange = onCheckedChange
        )
    }
}

