package com.example.presentation.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.config.AppConfig
import com.example.data.local.preferences.AVAILABLE_RECITERS
import com.example.data.local.preferences.AudioPreference
import com.example.data.local.preferences.CalculationMethod
import com.example.data.local.preferences.FontFamilyType
import com.example.data.local.preferences.FontSizeScale
import com.example.data.local.preferences.Madhhab
import com.example.data.local.preferences.ThemeMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val userPrefs by viewModel.userPreferences.collectAsStateWithLifecycle()
    val offlineStats by viewModel.offlineStats.collectAsStateWithLifecycle()

    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showDataSourcesDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showResetSettingsDialog by remember { mutableStateOf(false) }
    var showDeleteAllDataDialog by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.setNotificationsEnabled(isGranted)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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

            // 1. Theme Mode Group
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

            // 2. Font Size & Font Family Group
            item {
                SettingsSectionCard(
                    title = "حجم الخط ونوعه",
                    icon = Icons.Filled.FormatSize
                ) {
                    Text(
                        text = "نوع الخط المفضل:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FontFamilyType.values().forEach { family ->
                        ThemeOptionRow(
                            label = family.labelArabic,
                            selected = userPrefs.fontFamilyType == family,
                            onClick = { viewModel.setFontFamilyType(family) },
                            testTag = "font_family_${family.name}"
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "مقياس حجم الخط:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
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

            // 3. Language Group
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

            // 4. Location & Calculation Strategy (Offline-First)
            item {
                SettingsSectionCard(
                    title = "الموقع وطريقة حساب المواقيت",
                    icon = Icons.Filled.Mosque
                ) {
                    // Status row
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CloudDone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "المدينة: ${userPrefs.lastCityName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "حساب فلكي محلي دقيق يعمل بالكامل دون إنترنت",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "طريقة الحساب المعتمدة:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    CalculationMethod.values().forEach { method ->
                        ThemeOptionRow(
                            label = method.labelArabic,
                            selected = userPrefs.calculationMethod == method,
                            onClick = { viewModel.setCalculationMethod(method) },
                            testTag = "calc_method_${method.name}"
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "مذهب صلاة العصر:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
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

            // 5. Notifications & Reminders
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
                                text = "تنبيهات دخول أوقات الصلاة والورد اليومي",
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
                        PrayerNotificationToggleRow("الشروق", userPrefs.notifySunrise) { viewModel.setPrayerNotification("sunrise", it) }
                        PrayerNotificationToggleRow("الظهر", userPrefs.notifyDhuhr) { viewModel.setPrayerNotification("dhuhr", it) }
                        PrayerNotificationToggleRow("العصر", userPrefs.notifyAsr) { viewModel.setPrayerNotification("asr", it) }
                        PrayerNotificationToggleRow("المغرب", userPrefs.notifyMaghrib) { viewModel.setPrayerNotification("maghrib", it) }
                        PrayerNotificationToggleRow("العشاء", userPrefs.notifyIsha) { viewModel.setPrayerNotification("isha", it) }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "تذكير الورد اليومي للأذكار",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = userPrefs.dailyDhikrReminderEnabled,
                                onCheckedChange = { viewModel.setDailyDhikrReminderEnabled(it) }
                            )
                        }
                    }
                }
            }

            // 6. Audio Preference, Reciter & Vibration
            item {
                SettingsSectionCard(
                    title = "الصوتيات والاهتزاز",
                    icon = Icons.AutoMirrored.Filled.VolumeUp
                ) {
                    Text(
                        text = "صوت تنبيه الصلاة:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    AudioPreference.values().forEach { pref ->
                        ThemeOptionRow(
                            label = pref.labelArabic,
                            selected = userPrefs.audioPreference == pref,
                            onClick = { viewModel.setAudioPreference(pref) },
                            testTag = "audio_pref_${pref.name}"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "القارئ المفضل لتلاوة القرآن:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    AVAILABLE_RECITERS.forEach { reciter ->
                        ThemeOptionRow(
                            label = "${reciter.nameArabic} (${reciter.description})",
                            selected = userPrefs.preferredReciter == reciter.id,
                            onClick = { viewModel.setPreferredReciter(reciter.id) },
                            testTag = "reciter_${reciter.id}"
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تشغيل الاهتزاز اللمسي",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "رد فعل لمسي عند نقر السبحة والتنبيهات الصامتة",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = userPrefs.vibrationEnabled,
                            onCheckedChange = { viewModel.setVibrationEnabled(it) },
                            modifier = Modifier.testTag("vibration_switch")
                        )
                    }
                }
            }

            // 7. Offline Storage Status
            item {
                SettingsSectionCard(
                    title = "التخزين والمحتوى دون اتصال (Offline-First)",
                    icon = Icons.Filled.Storage
                ) {
                    Text(
                        text = "جميع المحتويات الأساسية التالية محفوظة بالكامل في ذاكرة جهازك وتعمل بدون حاجة لأي اتصال بالإنترنت:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "سور القرآن", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = "${offlineStats.downloadedSurahsCount} سورة",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "أقسام الأذكار", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = "${offlineStats.adhkarCount} ذكراً",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = "كتب المكتبة", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = "${offlineStats.libraryItemsCount} حديثاً",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // 8. Privacy, Terms & About
            item {
                SettingsSectionCard(
                    title = "الخصوصية والتوثيق وحول التطبيق",
                    icon = Icons.Filled.PrivacyTip
                ) {
                    SettingsActionRow(
                        title = "سياسة الخصوصية",
                        subtitle = "تخزين محلي 100%، خلو تام من التتبع والإعلانات",
                        icon = Icons.Filled.PrivacyTip,
                        onClick = { showPrivacyDialog = true },
                        testTag = "btn_privacy_policy"
                    )
                    SettingsActionRow(
                        title = "شروط وأحكام الاستخدام",
                        subtitle = "رخصة الاستخدام الديني المجاني وأصالة المحتوى",
                        icon = Icons.Filled.Gavel,
                        onClick = { showTermsDialog = true },
                        testTag = "btn_terms_of_use"
                    )
                    SettingsActionRow(
                        title = "مصادر المحتوى والتراخيص",
                        subtitle = "مجمع الملك فهد، تنزيل، وحصن المسلم",
                        icon = Icons.Filled.Verified,
                        onClick = { showDataSourcesDialog = true },
                        testTag = "btn_data_sources_settings"
                    )
                    SettingsActionRow(
                        title = "عن تطبيق ${AppConfig.APP_NAME}",
                        subtitle = "الإصدار ${AppConfig.APP_VERSION} - مبني بأحدث تقنيات أندرويد",
                        icon = Icons.Filled.Info,
                        onClick = { showAboutDialog = true },
                        testTag = "btn_about_app"
                    )
                    SettingsActionRow(
                        title = "التواصل مع المطور",
                        subtitle = "abdeelhap12345@gmail.com",
                        icon = Icons.Filled.Email,
                        onClick = {
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:abdeelhap12345@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "تطبيق نور المسلم - استفسار أو اقتراح")
                            }
                            context.startActivity(Intent.createChooser(emailIntent, "إرسال بريد إلكتروني"))
                        },
                        testTag = "btn_contact_dev"
                    )
                }
            }

            // 9. Danger Zone: Reset & Delete
            item {
                SettingsSectionCard(
                    title = "إدارة البيانات وإعادة الضبط",
                    icon = Icons.Filled.RestartAlt
                ) {
                    OutlinedButton(
                        onClick = { showResetSettingsDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_reset_settings")
                    ) {
                        Icon(imageVector = Icons.Filled.RestartAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إعادة ضبط الإعدادات للوضع الافتراضي")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { showDeleteAllDataDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_delete_all_data_settings")
                    ) {
                        Icon(imageVector = Icons.Filled.DeleteForever, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("حذف جميع البيانات والسجلات المحفوظة", fontWeight = FontWeight.Bold)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    // Dialog 1: Privacy Policy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.PrivacyTip, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("سياسة الخصوصية", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "1. خصوصية تامة دون اتصال:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "جميع بياناتك وسجلات عبادتك من صلوات وأذكار وختمات وحفظ محفوظة محلياً بالكامل على جهازك في قاعدة بيانات Room. لا نجمع ولا نرسل أي بيانات لخوادم سحابية.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "2. إذن الموقع الجغرافي:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "يُستخدم إذن الموقع فقط لحساب مواقيت الصلاة الفلكية لمدينتك واتجاه القبلة محلياً عبر خوارزميات رياضية، ولا يتم نقل إحداثياتك لأي طرف خارجي مطلقاً.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "3. إذن الإشعارات والصوت:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "تُجدول تنبيهات الأذان والتكبير محلياً عبر نظام AlarmManager في أندرويد لخدمتك دون الحاجة لاتصال مستمر.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "4. خلو تام من التتبع والإعلانات:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "التطبيق مجاني وخالٍ تماماً من حزم التتبع أو شبكات الإعلانات التجارية حفاظاً على سكينة وخشوع المستخدم.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("إغلاق", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Dialog 2: Terms of Use Dialog
    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Gavel, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("شروط الاستخدام", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "1. رخصة الاستخدام:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "تطبيق «نور المسلم» وقف إلكتروني مجاني متاح لجميع المسلمين للأغراض الشخصية غير التجارية.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "2. صحة النصوص الدينية:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "النص القرآني معتمد من مجمع الملك فهد لطباعة المصحف الشريف بالرسم العثماني. الأذكار والأحاديث مستقاة من حصن المسلم والكتب المعتمدة (البخاري، مسلم، النووي) مع التخريج والتحقيق.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "3. مواقيت الصلاة الفلكية:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "تُحسب المواقيت بالمعادلات الفلكية المعتمدة رسمياً، ويُستحب دوماً مراعاة فروق الأذان في مساجد بلدتك.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("فهمت وموافق", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Dialog 3: Data Sources Dialog
    if (showDataSourcesDialog) {
        AlertDialog(
            onDismissRequest = { showDataSourcesDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("مصادر البيانات والتراخيص", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "• مجمع الملك فهد لطباعة المصحف الشريف (المدينة المنورة)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Text(text = "• مشروع تنزيل للقرآن الكريم (Tanzil Project - Creative Commons 3.0)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Text(text = "• كتاب «حصن المسلم من أذكار الكتاب والسنة» للشيخ د. سعيد بن وهف القحطاني", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Text(text = "• صحيح البخاري، صحيح مسلم، الأربعون النووية، ورياض الصالحين", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Text(text = "• شبكة EveryAyah المفتوحة لتلاوات مشاهير القراء", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    Text(text = "• تقويم أم القرى ورابطة العالم الإسلامي لحساب المواقيت", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = { showDataSourcesDialog = false }) {
                    Text("إغلاق", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Dialog 4: About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text("عن ${AppConfig.APP_NAME}", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${AppConfig.APP_NAME} - الإصدار ${AppConfig.APP_VERSION}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "تطبيق إسلامي حديث وشامل صُمم بأحدث معايير الأداء والوصولية والخصوصية، ليكون رفيقك اليومي في تلاوة القرآن، الأذكار، الصلاة، وحفظ الآيات دون اتصال بالإنترنت.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "المطور: فريق تطوير نور المسلم\nالبريد: abdeelhap12345@gmail.com",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("حسناً", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Dialog 5: Reset Settings Dialog
    if (showResetSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showResetSettingsDialog = false },
            title = {
                Text("إعادة ضبط الإعدادات؟", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("سيتم استعادة كافة إعدادات المظهر، الخطوط، وتفضيلات الأذان إلى الوضع الافتراضي. لن تُحذف سجلاتك أو ختماتك.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showResetSettingsDialog = false
                        viewModel.resetToDefaults {
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("تمت إعادة ضبط الإعدادات بنجاح")
                            }
                        }
                    }
                ) {
                    Text("تأكيد الاستعادة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetSettingsDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Dialog 6: Delete All Data Dialog
    if (showDeleteAllDataDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDataDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("مسح كافة البيانات؟", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            },
            text = {
                Text("سيتم حذف كافة سجلاتك، تقدم الختمات، الحفظ، والمفضلة نهائياً من هذا الجهاز.\nهل تريد المتابعة؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAllDataDialog = false
                        viewModel.clearAllUserData {
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
                TextButton(onClick = { showDeleteAllDataDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun SettingsActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
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
