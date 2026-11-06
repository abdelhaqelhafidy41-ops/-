package com.example.presentation.more

import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.config.AppConfig

@Composable
fun MoreScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToLibrary: () -> Unit = {},
    onNavigateToStatistics: () -> Unit = {},
    onNavigateToWallpapers: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDataSourcesDialog by remember { mutableStateOf(false) }
    var showWidgetsInfoDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("more_screen_root")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.more_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Islamic Wallpapers Navigation Card
        item {
            MoreMenuCard(
                title = "الخلفيات الإسلامية",
                subtitle = "مكتبة متجددة تضم أكثر من 160 خلفية قرآنية وطبيعية وزخارف بدقة عالية",
                icon = Icons.Filled.Image,
                onClick = onNavigateToWallpapers,
                testTag = "more_menu_wallpapers"
            )
        }

        // Islamic Widgets Guide Card
        item {
            MoreMenuCard(
                title = "الويدجت الإسلامية للواجهة",
                subtitle = "أكثر من 35 ويدجت لمواقيت الصلاة، آية اليوم، والمسبحة والقبلة لشاشتك الرئيسية",
                icon = Icons.Filled.Apps,
                onClick = { showWidgetsInfoDialog = true },
                testTag = "more_menu_widgets"
            )
        }

        // Profile & Statistics Navigation Card
        item {
            MoreMenuCard(
                title = "الملف الشخصي والإحصائيات",
                subtitle = "إنجاز اليوم، الاستمرارية، سجل الصلوات المؤداة، والأوراد",
                icon = Icons.Filled.EmojiEvents,
                onClick = onNavigateToStatistics,
                testTag = "more_menu_statistics"
            )
        }

        // Islamic Library Navigation Card
        item {
            MoreMenuCard(
                title = "المكتبة الإسلامية والحديث النبوي",
                subtitle = "صحيح البخاري، صحيح مسلم، الأربعون النووية، رياض الصالحين، وحصن المسلم",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                onClick = onNavigateToLibrary,
                testTag = "more_menu_library"
            )
        }

        // Settings Navigation Card
        item {
            MoreMenuCard(
                title = stringResource(R.string.nav_settings),
                subtitle = "المظهر، حجم الخط، حساب الصلاة، والتنبيهات",
                icon = Icons.Filled.Settings,
                onClick = onNavigateToSettings,
                testTag = "more_menu_settings"
            )
        }

        // Data Sources & Verification Documentation Card
        item {
            MoreMenuCard(
                title = stringResource(R.string.more_data_sources),
                subtitle = "توثيق مصادر المصحف الشريف والأذكار ومواقيت الصلاة",
                icon = Icons.Filled.Verified,
                onClick = { showDataSourcesDialog = true },
                testTag = "more_menu_data_sources"
            )
        }

        // About Application Card
        item {
            MoreMenuCard(
                title = stringResource(R.string.more_about),
                subtitle = "${AppConfig.APP_NAME} - الإصدار ${AppConfig.APP_VERSION}",
                icon = Icons.Filled.Info,
                onClick = { showAboutDialog = true },
                testTag = "more_menu_about"
            )
        }

        // Share App Card
        item {
            MoreMenuCard(
                title = stringResource(R.string.more_share),
                subtitle = "شارك تطبيق ${AppConfig.APP_NAME} مع الأهل والأصدقاء",
                icon = Icons.Filled.Share,
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "تطبيق ${AppConfig.APP_NAME} - زادك اليومي من القرآن والأذكار والصلاة."
                        )
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "مشاركة تطبيق نور المسلم")
                    context.startActivity(shareIntent)
                },
                testTag = "more_menu_share"
            )
        }
    }

    if (showDataSourcesDialog) {
        AlertDialog(
            onDismissRequest = { showDataSourcesDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Verified,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "مصادر البيانات المعتمدة",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "1. القرآن الكريم:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "طبعة مجمع الملك فهد لطباعة المصحف الشريف بالمدينة المنورة برواية حفص عن عاصم بالرسم العثماني الموثق.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "2. الأذكار والأدعية:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "كتاب «حصن المسلم من أذكار الكتاب والسنة» للشيخ سعيد بن علي بن وهف القحطاني رحمه الله مع تخريج الأحاديث.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "3. حساب مواقيت الصلاة:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "معادلات فلكية دقيقة معتمدة رسمياً من تقويم أم القرى ورابطة العالم الإسلامي والهيئة المصرية العامة للمساحة.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showDataSourcesDialog = false }) {
                    Text("إغلاق", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showWidgetsInfoDialog) {
        AlertDialog(
            onDismissRequest = { showWidgetsInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Apps,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "دليل الويدجت الإسلامية",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "يوفر تطبيق نور المسلم أكثر من 35 ويدجت ذكية لشاشتك الرئيسية:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• مواقيت الصلاة: الصلاة القادمة، العد التنازلي، والصلوات الخمس.\n• القرآن الكريم: آية اليوم، متابعة موضع القراءة، والختمة اليومية.\n• الأذكار والمسبحة: المسبحة التفاعلية (تسبيح فوري)، أذكار الصباح والمساء.\n• القبلة والتقويم: اتجاه القبلة المباشر والتاريخ الهجري والميلادي.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "كيف تضيف الويدجت إلى شاشتك؟",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "1. اضغط مطولاً على أي مساحة فارغة في الشاشة الرئيسية لجهازك.\n2. اختر «الويدجت» أو Widgets.\n3. ابحث عن «نور المسلم» واختر الويدجت بالحجم والمظهر المناسب لك ثم اسحبه إلى الشاشة.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showWidgetsInfoDialog = false }) {
                    Text("حسناً فهمت", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text(
                    text = "عن ${AppConfig.APP_NAME}",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = AppConfig.APP_NAME,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "تطبيق إسلامي حديث مبني بأحدث تقنيات أندرويد وJetpack Compose، يهدف إلى تقديم تجربة إيمانية هادئة وسريعة وخالية من الإعلانات المشوشة والمحتوى غير الموثق.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "الإصدار: ${AppConfig.APP_VERSION}",
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
}

@Composable
fun MoreMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
