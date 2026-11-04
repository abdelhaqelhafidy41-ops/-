package com.example.presentation.quran

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.config.AppConfig
import com.example.data.local.entity.QuranSurahEntity
import com.example.data.local.entity.ReadingProgressEntity
import com.example.ui.theme.AmiriFontFamily

@Composable
fun QuranScreen(
    viewModel: QuranViewModel,
    onNavigateToSurah: (surahNumber: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.selectedTabIndex.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val surahs by viewModel.surahs.collectAsStateWithLifecycle()
    val readingProgress by viewModel.readingProgress.collectAsStateWithLifecycle()
    val favoriteAyahs by viewModel.favoriteAyahs.collectAsStateWithLifecycle()
    val khatmahs by viewModel.khatmahs.collectAsStateWithLifecycle()
    val memorizationRecords by viewModel.memorizationRecords.collectAsStateWithLifecycle()
    val audioState by viewModel.audioPlayer.state.collectAsStateWithLifecycle()

    var showCreateKhatmahDialog by remember { mutableStateOf(false) }
    var showAddMemorizationDialog by remember { mutableStateOf(false) }
    var showQuizDialog by remember { mutableStateOf(false) }
    var showSurahPickerForQuiz by remember { mutableStateOf(false) }
    var quizTargetSurah by remember { mutableStateOf<QuranSurahEntity?>(null) }

    Scaffold(
        bottomBar = {
            QuranAudioPlayerBar(
                audioPlayer = viewModel.audioPlayer,
                state = audioState
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("quran_screen_root")
        ) {
            // 4 Tabs: السور, الختمة, الحفظ والمراجعة, الفواصل والمفضلة
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text("السور (114)", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("quran_tab_surahs")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text("الختمة والورد", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("quran_tab_khatmah")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    text = { Text("الحفظ والمراجعة", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("quran_tab_memorization")
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    text = { Text("الفواصل والمفضلة", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("quran_tab_bookmarks")
                )
            }

            when (selectedTab) {
                // Tab 0: 114 Surahs List with Search
                0 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = { Text("ابحث باسم السورة أو رقمها...") },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("quran_search_field")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Last read banner
                            item {
                                LastReadHeaderCard(
                                    progress = readingProgress,
                                    onContinueReading = {
                                        readingProgress?.let { onNavigateToSurah(it.surahNumber) }
                                    }
                                )
                            }

                            if (surahs.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (searchQuery.isNotBlank()) "لا توجد سورة مطابقة لـ \"$searchQuery\"" else "جاري تجهيز فهرس السور المعتمد...",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                items(surahs, key = { it.surahNumber }) { surah ->
                                    val isLastReadSurah = readingProgress?.surahNumber == surah.surahNumber

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable { onNavigateToSurah(surah.surahNumber) }
                                            .testTag("surah_item_${surah.surahNumber}"),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isLastReadSurah)
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                            else
                                                MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Surah number circular badge
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${surah.surahNumber}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(14.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "سورة ${surah.nameArabic}",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontFamily = AmiriFontFamily,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    if (isLastReadSurah) {
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(MaterialTheme.colorScheme.primary)
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = "آخر قراءة (آية ${readingProgress?.ayahNumber})",
                                                                color = MaterialTheme.colorScheme.onPrimary,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = "${surah.revelationType} • ${surah.totalAyahs} آية • صفحة ${surah.startPage}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            Text(
                                                text = surah.nameEnglish,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(60.dp))
                            }
                        }
                    }
                }

                // Tab 1: الختمة والورد اليومي
                1 -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("برنامج الختمات والورد اليومي", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("نظم قراءتك وأنجز وردك اليومي بسهولة", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(
                                    onClick = { showCreateKhatmahDialog = true },
                                    modifier = Modifier.testTag("create_khatmah_button")
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ختمة جديدة")
                                }
                            }
                        }

                        if (khatmahs.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "لا توجد ختمة نشطة حالياً",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "أنشئ ختمتك الأولى وحدد مدتها (مثلاً 30 يوماً)، وسيقوم التطبيق بحساب وردك اليومي تلقائياً ومتابعة تقدمك.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(onClick = { showCreateKhatmahDialog = true }) {
                                            Text("بدء ختمة الآن")
                                        }
                                    }
                                }
                            }
                        } else {
                            items(khatmahs, key = { it.id }) { khatmah ->
                                KhatmahCard(
                                    khatmah = khatmah,
                                    onAddProgress = { amount -> viewModel.recordKhatmahProgress(khatmah.id, amount) },
                                    onTogglePause = { viewModel.togglePauseKhatmah(khatmah.id, khatmah.isPaused) },
                                    onDelete = { viewModel.deleteKhatmah(khatmah.id) }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(60.dp))
                        }
                    }
                }

                // Tab 2: الحفظ والمراجعة والاختبار الذاتي
                2 -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("سجل الحفظ والمراجعة المجدولة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("تثبيت المحفوظ واختبار الإتقان", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            showSurahPickerForQuiz = true
                                        },
                                        modifier = Modifier.testTag("memorization_quiz_btn")
                                    ) {
                                        Icon(Icons.Filled.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("اختبار ذاتي")
                                    }
                                    Button(
                                        onClick = { showAddMemorizationDialog = true },
                                        modifier = Modifier.testTag("add_memorization_btn")
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("إضافة")
                                    }
                                }
                            }
                        }

                        if (memorizationRecords.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(18.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Psychology,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "ابدأ خطة حفظك ومراجعتك",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "حدد السور والآيات التي تحفظها أو تراجعها، وقيّم مستوى إتقانك، واستخدم ميزة الاختبار الذاتي لإخفاء أجزاء من الآيات والتحقق من حفظك بدقة.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(memorizationRecords, key = { it.id }) { record ->
                                MemorizationCard(
                                    record = record,
                                    onLogReview = { newLevel, nextDays ->
                                        viewModel.logMemorizationReview(record.id, newLevel, nextDays)
                                    },
                                    onDelete = {
                                        viewModel.deleteMemorizationRecord(record.id)
                                    }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(60.dp))
                        }
                    }
                }

                // Tab 3: الفواصل والمفضلة
                3 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Text("فاصلة القراءة المحفوظة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        if (readingProgress != null) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { onNavigateToSurah(readingProgress!!.surahNumber) },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Bookmark,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "سورة ${readingProgress?.surahNameArabic}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontFamily = AmiriFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = "الآية ${readingProgress?.ayahNumber} • الصفحة ${readingProgress?.pageNumber} (انقر للمتابعة)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                        IconButton(onClick = { viewModel.clearBookmark() }) {
                                            Icon(
                                                imageVector = Icons.Filled.Delete,
                                                contentDescription = "حذف الفاصلة",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            item {
                                Text(
                                    text = "لم يتم حفظ فاصلة قراءة بعد. يمكنك حفظ موضعك أثناء قراءة أي سورة.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("الآيات الكريمة المفضلة (${favoriteAyahs.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        if (favoriteAyahs.isEmpty()) {
                            item {
                                Text(
                                    text = "لا توجد آيات محفوظة في المفضلة حالياً. اضغط على أي آية أثناء القراءة لإضافتها للمفضلة.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            items(favoriteAyahs, key = { it.id }) { fav ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = fav.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Icon(
                                                imageVector = Icons.Filled.Favorite,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        fav.arabicText?.let { text ->
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "﴿ $text ﴾",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontFamily = AmiriFontFamily,
                                                lineHeight = 28.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(60.dp))
                        }
                    }
                }
            }
        }
    }

    // Create Khatmah Dialog
    if (showCreateKhatmahDialog) {
        CreateKhatmahDialog(
            onDismiss = { showCreateKhatmahDialog = false },
            onConfirm = { title, days, targetType, totalTarget ->
                viewModel.createKhatmah(title, days, targetType, totalTarget)
                showCreateKhatmahDialog = false
            }
        )
    }

    // Add Memorization Dialog
    if (showAddMemorizationDialog && surahs.isNotEmpty()) {
        AddMemorizationDialog(
            surahs = surahs,
            onDismiss = { showAddMemorizationDialog = false },
            onConfirm = { surahNumber, surahName, startAyah, endAyah, masteryLevel, nextDays, notes ->
                viewModel.addMemorizationRecord(surahNumber, surahName, startAyah, endAyah, masteryLevel, nextDays, notes)
                showAddMemorizationDialog = false
            }
        )
    }

    // Surah Picker for Self-Test Quiz
    if (showSurahPickerForQuiz) {
        QuizSurahPickerDialog(
            surahs = surahs,
            onDismiss = { showSurahPickerForQuiz = false },
            onSurahSelected = { selected ->
                quizTargetSurah = selected
                showSurahPickerForQuiz = false
                showQuizDialog = true
            }
        )
    }

    // Quiz Dialog
    if (showQuizDialog && quizTargetSurah != null) {
        val s = quizTargetSurah!!
        var quizAyahs by remember { mutableStateOf<List<com.example.data.local.entity.QuranAyahEntity>>(emptyList()) }

        androidx.compose.runtime.LaunchedEffect(s.surahNumber) {
            quizAyahs = viewModel.getAyahsForSurah(s.surahNumber)
        }

        MemorizationQuizDialog(
            surah = s,
            ayahs = quizAyahs,
            onDismiss = { showQuizDialog = false },
            onSaveResult = { total, correct ->
                viewModel.saveQuizResult(s.surahNumber, s.nameArabic, total, correct)
            }
        )
    }
}

@Composable
fun LastReadHeaderCard(
    progress: ReadingProgressEntity?,
    onContinueReading: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = progress != null, onClick = onContinueReading)
            .testTag("quran_bookmark_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Bookmark,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "متابعة القراءة",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                if (progress != null) {
                    Text(
                        text = "سورة ${progress.surahNameArabic} - آية ${progress.ayahNumber} (الصفحة ${progress.pageNumber})",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = AmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    Text(
                        text = "لم يتم حفظ موضع قراءة سابق بعد. انقر على أي سورة لبدء القراءة.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun QuizSurahPickerDialog(
    surahs: List<QuranSurahEntity>,
    onDismiss: () -> Unit,
    onSurahSelected: (QuranSurahEntity) -> Unit
) {
    var filterText by remember { mutableStateOf("") }
    val filtered = remember(surahs, filterText) {
        if (filterText.isBlank()) surahs
        else surahs.filter {
            it.nameArabic.contains(filterText.trim()) ||
            it.nameEnglish.contains(filterText.trim(), ignoreCase = true) ||
            it.surahNumber.toString() == filterText.trim()
        }
    }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("اختر السورة للاختبار الذاتي", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(360.dp)) {
                OutlinedTextField(
                    value = filterText,
                    onValueChange = { filterText = it },
                    placeholder = { Text("ابحث باسم السورة أو رقمها...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("لا توجد سورة مطابقة", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filtered, key = { it.surahNumber }) { surah ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onSurahSelected(surah) }
                                    .testTag("quiz_surah_item_${surah.surahNumber}"),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "${surah.surahNumber}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            "سورة ${surah.nameArabic}",
                                            fontFamily = AmiriFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                    Text(
                                        "${surah.totalAyahs} آية",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
