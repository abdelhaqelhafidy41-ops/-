package com.example.presentation.quran

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.QuranAyahEntity
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.CairoFontFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReadingScreen(
    viewModel: QuranReadingViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val audioState by viewModel.audioPlayer.state.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var showAppearanceSheet by remember { mutableStateOf(false) }
    var showJumpDialog by remember { mutableStateOf(false) }
    var showSourceInfoDialog by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var showQuizDialog by remember { mutableStateOf(false) }

    // Resolve color theme based on reading backgroundTheme preference
    val (themeBg, themeTextColor) = when (uiState.backgroundTheme) {
        "SEPIA" -> Color(0xFFFBF0D9) to Color(0xFF382D1D)
        "DARK" -> Color(0xFF14171A) to Color(0xFFEDEDED)
        "GREEN" -> Color(0xFFE8F3EB) to Color(0xFF1B3B24)
        else -> MaterialTheme.colorScheme.background to MaterialTheme.colorScheme.onBackground
    }

    val selectedFontFamily = when (uiState.fontType) {
        "CAIRO" -> CairoFontFamily
        "SYSTEM" -> FontFamily.Default
        else -> AmiriFontFamily
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.surah?.let { "سورة ${it.nameArabic}" } ?: "المصحف الشريف",
                            fontWeight = FontWeight.Bold,
                            fontFamily = AmiriFontFamily,
                            fontSize = 20.sp
                        )
                        uiState.surah?.let {
                            Text(
                                text = "${it.revelationType} • ${it.totalAyahs} آية • سورة رقم ${it.surahNumber}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                },
                actions = {
                    // Self-test Quiz for this Surah
                    IconButton(
                        onClick = { showQuizDialog = true },
                        modifier = Modifier.testTag("surah_reader_quiz_btn")
                    ) {
                        Icon(Icons.Filled.Quiz, contentDescription = "اختبار حفظ هذه السورة")
                    }
                    // Search in surah
                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(Icons.Filled.Search, contentDescription = "بحث في السورة")
                    }
                    // Jump to Ayah
                    IconButton(onClick = { showJumpDialog = true }) {
                        Icon(Icons.Filled.TouchApp, contentDescription = "الانتقال إلى آية")
                    }
                    // Font & Theme settings
                    IconButton(onClick = { showAppearanceSheet = true }) {
                        Icon(Icons.Filled.FormatSize, contentDescription = "حجم الخط والمظهر")
                    }
                    // Source Info
                    IconButton(onClick = { showSourceInfoDialog = true }) {
                        Icon(Icons.Filled.Info, contentDescription = "توثيق المصدر")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            QuranAudioPlayerBar(
                audioPlayer = viewModel.audioPlayer,
                state = audioState,
                onDownloadSurah = { viewModel.downloadSurahAudio() },
                isDownloading = uiState.isAudioDownloading,
                downloadProgress = uiState.audioDownloadProgress
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(themeBg)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // In-surah search bar
                AnimatedVisibility(visible = showSearchBar) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.onSearchQueryChanged(it) },
                                placeholder = { Text("ابحث عن كلمة أو رقم آية...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = {
                                viewModel.onSearchQueryChanged("")
                                showSearchBar = false
                            }) {
                                Icon(Icons.Filled.Close, contentDescription = "إغلاق البحث")
                            }
                        }
                    }
                }

                // Main Reading content
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("جاري تحميل آيات السورة الكريمة من المصدر المعتمد...", color = themeTextColor)
                            }
                        }
                    }

                    uiState.errorMessage != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "تنبيه في تحميل البيانات",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = uiState.errorMessage ?: "",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { viewModel.retryDownload() },
                                        modifier = Modifier.testTag("retry_download_button")
                                    ) {
                                        Icon(Icons.Filled.Refresh, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("إعادة المحاولة")
                                    }
                                }
                            }
                        }
                    }

                    uiState.filteredAyahs.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("لا توجد آيات مطابقة للبحث", color = themeTextColor)
                        }
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            // Basmalah Header (Surah 1 and 9 don't show separate header)
                            if (uiState.surah != null && uiState.surah!!.surahNumber != 1 && uiState.surah!!.surahNumber != 9) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                            fontFamily = AmiriFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = (uiState.fontSizeSp * 1.05f).sp,
                                            color = themeTextColor
                                        )
                                    }
                                }
                            }

                            // Ayahs List
                            items(uiState.filteredAyahs, key = { it.ayahNumberInSurah }) { ayah ->
                                val isCurrentlyPlaying = audioState.isPlaying &&
                                        audioState.currentSurahNumber == ayah.surahNumber &&
                                        audioState.currentAyahNumber == ayah.ayahNumberInSurah

                                AyahRow(
                                    ayah = ayah,
                                    fontSize = uiState.fontSizeSp,
                                    fontFamily = selectedFontFamily,
                                    textColor = themeTextColor,
                                    isPlaying = isCurrentlyPlaying,
                                    onClick = {
                                        viewModel.selectAyahForActions(ayah)
                                    }
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Appearance Bottom Sheet
    if (showAppearanceSheet) {
        ModalBottomSheet(onDismissRequest = { showAppearanceSheet = false }) {
            AppearanceSettingsContent(
                fontSize = uiState.fontSizeSp,
                onFontSizeChange = { viewModel.setFontSize(it) },
                fontType = uiState.fontType,
                onFontTypeChange = { viewModel.setFontType(it) },
                backgroundTheme = uiState.backgroundTheme,
                onBackgroundThemeChange = { viewModel.setBackgroundTheme(it) }
            )
        }
    }

    // Jump to Ayah Dialog
    if (showJumpDialog) {
        var ayahTargetText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showJumpDialog = false },
            title = { Text("الانتقال إلى آية", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("أدخل رقم الآية (1 إلى ${uiState.surah?.totalAyahs ?: 1}):")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ayahTargetText,
                        onValueChange = { ayahTargetText = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = ayahTargetText.toIntOrNull()
                        if (num != null && num in 1..(uiState.surah?.totalAyahs ?: 1)) {
                            showJumpDialog = false
                            val targetIndex = uiState.filteredAyahs.indexOfFirst { it.ayahNumberInSurah == num }
                            if (targetIndex >= 0) {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(targetIndex)
                                }
                            }
                        }
                    }
                ) {
                    Text("انتقال")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJumpDialog = false }) { Text("إلغاء") }
            }
        )
    }

    // Ayah Actions Bottom Sheet
    uiState.selectedAyahForActions?.let { ayah ->
        ModalBottomSheet(onDismissRequest = { viewModel.selectAyahForActions(null) }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "سورة ${uiState.surah?.nameArabic} - الآية ${ayah.ayahNumberInSurah}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "﴿ ${ayah.textUthmani} ﴾",
                    fontFamily = AmiriFontFamily,
                    fontSize = 19.sp,
                    lineHeight = 30.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Play
                    ActionButtonItem(
                        icon = Icons.Filled.PlayArrow,
                        label = "استماع",
                        onClick = {
                            viewModel.playAyah(ayah.ayahNumberInSurah)
                            viewModel.selectAyahForActions(null)
                        }
                    )
                    // Bookmark
                    ActionButtonItem(
                        icon = Icons.Filled.Bookmark,
                        label = "فاصلة قراءة",
                        onClick = {
                            viewModel.saveBookmarkForAyah(ayah)
                            viewModel.selectAyahForActions(null)
                        }
                    )
                    // Favorite
                    ActionButtonItem(
                        icon = if (uiState.isFavoriteSelected) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        label = if (uiState.isFavoriteSelected) "في المفضلة" else "إضافة مفضلة",
                        tint = if (uiState.isFavoriteSelected) Color(0xFFE91E63) else MaterialTheme.colorScheme.primary,
                        onClick = {
                            viewModel.toggleFavoriteSelectedAyah()
                        }
                    )
                    // Copy
                    ActionButtonItem(
                        icon = Icons.Filled.ContentCopy,
                        label = "نسخ",
                        onClick = {
                            viewModel.copyAyahText(ayah)
                            viewModel.selectAyahForActions(null)
                        }
                    )
                    // Share
                    ActionButtonItem(
                        icon = Icons.Filled.Share,
                        label = "مشاركة",
                        onClick = {
                            viewModel.shareAyahText(ayah)
                            viewModel.selectAyahForActions(null)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Source Info Dialog
    if (showSourceInfoDialog) {
        AlertDialog(
            onDismissRequest = { showSourceInfoDialog = false },
            title = { Text("توثيق المصحف الشريف والتلاوات", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "1. النص القرآني والرسم العثماني:",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "• مجمع الملك فهد لطباعة المصحف الشريف بالمدينة المنورة.\n• مشروع تنزيل المعتمد (Tanzil Project - Creative Commons Attribution 3.0).\n• برواية حفص عن عاصم، مضبوط بالكامل وخالٍ تماماً من أي نصوص وهمية."
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "2. التلاوات الصوتية:",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "• شبكة EveryAyah الرسمية الموثقة.\n• القراء المعتمدون: الشيخ مشاري العفاسي، الشيخ محمود الحصري، الشيخ عبد الباسط عبد الصمد، الشيخ سعد الغامدي."
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showSourceInfoDialog = false }) {
                    Text("حسناً")
                }
            }
        )
    }

    // Quiz Dialog for Current Surah
    if (showQuizDialog && uiState.surah != null) {
        MemorizationQuizDialog(
            surah = uiState.surah!!,
            ayahs = uiState.ayahs,
            onDismiss = { showQuizDialog = false },
            onSaveResult = { _, _ -> }
        )
    }
}

@Composable
fun AyahRow(
    ayah: QuranAyahEntity,
    fontSize: Float,
    fontFamily: FontFamily,
    textColor: Color,
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else Color.Transparent
            )
            .border(
                width = if (isPlaying) 1.5.dp else 0.dp,
                color = if (isPlaying) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Column {
            Text(
                text = "${ayah.textUthmani} ﴿${ayah.ayahNumberInSurah}﴾",
                fontFamily = fontFamily,
                fontSize = fontSize.sp,
                lineHeight = (fontSize * 1.7f).sp,
                color = textColor,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun AppearanceSettingsContent(
    fontSize: Float,
    onFontSizeChange: (Float) -> Unit,
    fontType: String,
    onFontTypeChange: (String) -> Unit,
    backgroundTheme: String,
    onBackgroundThemeChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("تخصيص القراءة والمظهر", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        // Font size slider
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("حجم الخط:")
                Text("${fontSize.toInt()} sp", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Slider(
                value = fontSize,
                onValueChange = onFontSizeChange,
                valueRange = 18f..42f
            )
        }

        // Font Family
        Column {
            Text("نوع الخط العربي:")
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "AMIRI" to "الأميري (عثماني)",
                    "CAIRO" to "كايرو (حديث)",
                    "SYSTEM" to "النظام"
                ).forEach { (type, label) ->
                    OutlinedButton(
                        onClick = { onFontTypeChange(type) },
                        modifier = Modifier.weight(1f),
                        colors = if (fontType == type)
                            androidx.compose.material3.ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        else androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text(label, fontSize = 12.sp)
                    }
                }
            }
        }

        // Background Theme
        Column {
            Text("لون الخلفية للقراءة:")
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "WHITE" to ("فاتح" to Color.White),
                    "SEPIA" to ("ورقي" to Color(0xFFFBF0D9)),
                    "GREEN" to ("أخضر" to Color(0xFFE8F3EB)),
                    "DARK" to ("ليلي" to Color(0xFF1E2124))
                ).forEach { (theme, pair) ->
                    val (label, bg) = pair
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(bg)
                            .border(
                                width = if (backgroundTheme == theme) 2.dp else 1.dp,
                                color = if (backgroundTheme == theme) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onBackgroundThemeChange(theme) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (theme == "DARK") Color.White else Color.Black,
                            fontWeight = if (backgroundTheme == theme) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
fun ActionButtonItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}
