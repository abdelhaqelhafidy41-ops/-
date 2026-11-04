package com.example.presentation.adhkar

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.DhikrEntity
import com.example.ui.theme.AmiriFontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdhkarScreen(
    viewModel: AdhkarViewModel,
    onNavigateToLibrary: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val selectedMainTab by viewModel.selectedMainTab.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val fontScale by viewModel.fontScale.collectAsStateWithLifecycle()
    val dhikrItems by viewModel.dhikrItems.collectAsStateWithLifecycle()
    val itemCounters by viewModel.itemCounters.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteDhikrIds.collectAsStateWithLifecycle()

    val tasbeehPhrase by viewModel.tasbeehPhrase.collectAsStateWithLifecycle()
    val tasbeehCount by viewModel.tasbeehCount.collectAsStateWithLifecycle()
    val tasbeehTarget by viewModel.tasbeehTarget.collectAsStateWithLifecycle()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsStateWithLifecycle()
    val todayTotal by viewModel.todayTotalCount.collectAsStateWithLifecycle()
    val recentHistory by viewModel.recentHistory.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var showCustomTasbeehDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("adhkar_screen_root")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Tabs: 0: الأذكار والأدعية | 1: السبحة الإلكترونية
            PrimaryTabRow(
                selectedTabIndex = selectedMainTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedMainTab == 0,
                    onClick = { viewModel.selectMainTab(0) },
                    text = { Text("الأذكار والأدعية", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedMainTab == 1,
                    onClick = { viewModel.selectMainTab(1) },
                    text = { Text("السبحة الإلكترونية", fontWeight = FontWeight.Bold) }
                )
            }

            if (selectedMainTab == 0) {
                // ==================== TAB 0: الأذكار والأدعية ====================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        // Unified Search Bar
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = { Text("ابحث في نصوص الأذكار أو المراجع...") },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                        Icon(Icons.Filled.Close, contentDescription = "مسح")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("adhkar_search_input")
                        )
                    }

                    // 12 Horizontal Category Chips (Only shown when not searching)
                    if (searchQuery.isEmpty()) {
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(AdhkarViewModel.CATEGORIES) { cat ->
                                    FilterChip(
                                        selected = selectedCategory == cat,
                                        onClick = { viewModel.selectCategory(cat) },
                                        label = { Text(cat, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Font Size Adjuster Bar & Library Shortcut Banner
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (searchQuery.isNotBlank()) "نتائج البحث (${dhikrItems.size})" else selectedCategory,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.setFontScale(fontScale - 0.1f) },
                                    enabled = fontScale > 0.9f
                                ) {
                                    Text("A-", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                Icon(
                                    imageVector = Icons.Filled.FormatSize,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(18.dp)
                                )
                                IconButton(
                                    onClick = { viewModel.setFontScale(fontScale + 0.1f) },
                                    enabled = fontScale < 1.35f
                                ) {
                                    Text("A+", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }

                    // List of Dhikr Cards
                    if (dhikrItems.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isNotBlank()) "لا توجد أذكار مطابقة لـ \"$searchQuery\"" else "جاري تحميل الأذكار المعتمدة...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(dhikrItems, key = { it.id }) { dhikr ->
                            val currentCount = itemCounters[dhikr.id] ?: 0
                            val isFav = favoriteIds.contains(dhikr.id.toString())

                            DhikrCard(
                                dhikr = dhikr,
                                currentCount = currentCount,
                                fontScale = fontScale,
                                isFavorite = isFav,
                                onIncrement = {
                                    if (vibrationEnabled) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    viewModel.incrementDhikr(dhikr)
                                },
                                onReset = { viewModel.resetDhikr(dhikr) },
                                onToggleFavorite = { viewModel.toggleFavorite(dhikr) },
                                onShare = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "«${dhikr.arabicText}»\n\n[المصدر: ${dhikr.reference ?: "حصن المسلم"}]\n- تطبيق نور المسلم"
                                        )
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "مشاركة الذكر المبارك"))
                                },
                                onCopy = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Dhikr", "${dhikr.arabicText}\n[${dhikr.reference ?: ""}]")
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "تم نسخ الذكر المبارك إلى الحافظة", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(70.dp))
                    }
                }
            } else {
                // ==================== TAB 1: السبحة الإلكترونية ====================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Tasbeeh Card
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("electronic_tasbeeh_card"),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Top controls inside card
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Vibration Toggle
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { viewModel.toggleVibration(!vibrationEnabled) },
                                            modifier = Modifier.testTag("tasbeeh_vibration_toggle")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Vibration,
                                                contentDescription = "الاهتزاز",
                                                tint = if (vibrationEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                                            )
                                        }
                                        Text(
                                            text = if (vibrationEnabled) "اهتزاز مفعل" else "صامت",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Reset Button
                                    IconButton(
                                        onClick = { viewModel.resetTasbeeh() },
                                        modifier = Modifier.testTag("reset_tasbeeh_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Refresh,
                                            contentDescription = "إعادة ضبط العداد",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Current Phrase Display with edit icon
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showCustomTasbeehDialog = true }
                                        .padding(horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = tasbeehPhrase,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontFamily = AmiriFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Edit,
                                        contentDescription = "تعديل الصيغة",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Circular Interactive Tap Area
                                Box(
                                    modifier = Modifier
                                        .size(200.dp)
                                        .shadow(10.dp, CircleShape)
                                        .clip(CircleShape)
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.tertiary
                                                )
                                            )
                                        )
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = ripple(bounded = true, radius = 100.dp)
                                        ) {
                                            val registered = viewModel.incrementTasbeeh()
                                            if (registered && vibrationEnabled) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                        }
                                        .testTag("tasbeeh_tap_circle"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "$tasbeehCount",
                                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 54.sp),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        if (tasbeehTarget > 0) {
                                            Text(
                                                text = "الهدف: $tasbeehTarget",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // Action Buttons: -1 Decrement and +1 Increment
                                Row(
                                    modifier = Modifier.fillMaxWidth(0.9f),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { viewModel.decrementTasbeeh() },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Filled.Remove, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("إنقاص (-1)", fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = {
                                            val registered = viewModel.incrementTasbeeh()
                                            if (registered && vibrationEnabled) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            }
                                        },
                                        modifier = Modifier.weight(1.5f).height(48.dp).testTag("increment_tasbeeh_button"),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("تسبيح (+1)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Authentic Presets Row
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "صيغ التسبيح المأثورة",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(AdhkarViewModel.TASBEEH_PRESETS) { (phrase, target) ->
                                    FilterChip(
                                        selected = tasbeehPhrase == phrase && tasbeehTarget == target,
                                        onClick = { viewModel.setTasbeehConfig(phrase, target) },
                                        label = { Text("$phrase ($target)") },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    // History Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("سجل التسبيح اليومي", fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = "المجموع: ${todayTotal ?: 0}",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                if (recentHistory.isEmpty()) {
                                    Text(
                                        text = "لم تسجل جلسات بعد اليوم. كل تسبيحة تنجزها تسجل تلقائياً في قاعدة البيانات المحلية حتى بعد إغلاق التطبيق.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    recentHistory.take(5).forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(item.dhikrTitle, style = MaterialTheme.typography.bodyMedium)
                                            Text("${item.countDone} مرة", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(70.dp))
                    }
                }
            }
        }
    }

    // Custom Tasbeeh Phrase Dialog
    if (showCustomTasbeehDialog) {
        var customPhrase by remember { mutableStateOf(tasbeehPhrase) }
        var customTargetStr by remember { mutableStateOf(tasbeehTarget.toString()) }

        AlertDialog(
            onDismissRequest = { showCustomTasbeehDialog = false },
            title = { Text("تخصيص صيغة التسبيح", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = customPhrase,
                        onValueChange = { customPhrase = it },
                        label = { Text("صيغة الذكر أو الدعاء") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customTargetStr,
                        onValueChange = { customTargetStr = it },
                        label = { Text("الهدف (مثلاً 33 أو 100)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = customTargetStr.toIntOrNull() ?: 33
                        if (customPhrase.isNotBlank()) {
                            viewModel.setTasbeehConfig(customPhrase.trim(), target)
                        }
                        showCustomTasbeehDialog = false
                    }
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTasbeehDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun DhikrCard(
    dhikr: DhikrEntity,
    currentCount: Int,
    fontScale: Float,
    isFavorite: Boolean,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onToggleFavorite: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (dhikr.targetCount > 0) {
        (currentCount.toFloat() / dhikr.targetCount.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val isCompleted = currentCount >= dhikr.targetCount

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dhikr_card_${dhikr.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isCompleted)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title, Category & Favorite Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dhikr.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = dhikr.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "المفضلة",
                            tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = onCopy) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = "نسخ",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(onClick = onShare) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "مشاركة",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Arabic Text
            Text(
                text = dhikr.arabicText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = (20 * fontScale).sp,
                    lineHeight = (32 * fontScale).sp
                ),
                fontFamily = AmiriFontFamily,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Virtue if available
            dhikr.virtue?.let { virtue ->
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "الفضل: $virtue",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Reference & Authenticity Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dhikr.reference ?: "حصن المسلم",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "درجة: ${dhikr.grade ?: "صحيح"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Counter Bar & Reset Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reset Button
                OutlinedButton(
                    onClick = onReset,
                    enabled = currentCount > 0,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(42.dp)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تصفير")
                }

                // Interactive Counter Action Button
                Button(
                    onClick = onIncrement,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("dhikr_increment_${dhikr.id}")
                ) {
                    if (isCompleted) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تم ($currentCount / ${dhikr.targetCount})", fontWeight = FontWeight.Bold)
                    } else {
                        Text(
                            text = "تكرار ($currentCount / ${dhikr.targetCount})",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
