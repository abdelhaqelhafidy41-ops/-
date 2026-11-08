package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.core.prayer.PrayerCalculator
import com.example.data.local.preferences.CalculationMethod
import com.example.data.local.preferences.Madhhab
import com.example.widgets.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class Phase8WidgetAuditTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Application>()
    }

    @Test
    fun testExactly15CanonicalWidgetProvidersAreRegistered() {
        val providers = WidgetUpdateCoordinator.allProviderClasses
        assertEquals("There must be exactly 15 canonical widget providers", 15, providers.size)

        // Ensure all are unique
        val uniqueSet = providers.map { it.name }.toSet()
        assertEquals("All 15 providers must be unique classes", 15, uniqueSet.size)
    }

    @Test
    fun testFastInitialRemoteViewsCanBeInflatedForAll15Providers() {
        for (providerClass in WidgetUpdateCoordinator.allProviderClasses) {
            val remoteViews = WidgetUpdateCoordinator.getInitialFastViews(context, providerClass)
            assertNotNull("Initial fast views must not be null for ${providerClass.simpleName}", remoteViews)
            assertTrue("Layout ID must be valid", remoteViews.layoutId > 0)
        }
    }

    @Test
    fun testFullRemoteViewsCanBeBuiltForAll15Providers() = runTest {
        val cal = Calendar.getInstance()
        val today = PrayerCalculator.calculatePrayerTimes(
            latitude = 21.4225,
            longitude = 39.8262,
            date = cal.time,
            method = CalculationMethod.UMM_AL_QURA,
            madhhab = Madhhab.STANDARD,
            cityName = "مكة المكرمة"
        )
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = PrayerCalculator.calculatePrayerTimes(
            latitude = 21.4225,
            longitude = 39.8262,
            date = cal.time,
            method = CalculationMethod.UMM_AL_QURA,
            madhhab = Madhhab.STANDARD,
            cityName = "مكة المكرمة"
        )

        for ((index, providerClass) in WidgetUpdateCoordinator.allProviderClasses.withIndex()) {
            val views = WidgetUpdateCoordinator.buildViewsForProvider(
                context = context,
                appWidgetId = 100 + index,
                providerClass = providerClass,
                todaySchedule = today,
                tomorrowSchedule = tomorrow,
                lat = 21.4225,
                lon = 39.8262,
                cityName = "مكة المكرمة"
            )
            assertNotNull("Built RemoteViews must not be null for ${providerClass.simpleName}", views)
            assertTrue("Layout ID must be valid", views.layoutId > 0)
        }
    }

    @Test
    fun testWidgetNamesAndDescriptionsAreDefinedInResources() {
        val widgetStringIds = listOf(
            R.string.widget_name_next_prayer, R.string.widget_desc_next_prayer,
            R.string.widget_name_prayer_daily, R.string.widget_desc_prayer_daily,
            R.string.widget_name_prayer_mini, R.string.widget_desc_prayer_mini,
            R.string.widget_name_prayer_pair, R.string.widget_desc_prayer_pair,
            R.string.widget_name_ramadan_prayer, R.string.widget_desc_ramadan_prayer,
            R.string.widget_name_ayah_of_day, R.string.widget_desc_ayah_of_day,
            R.string.widget_name_last_reading, R.string.widget_desc_last_reading,
            R.string.widget_name_quran_progress, R.string.widget_desc_quran_progress,
            R.string.widget_name_quran_player, R.string.widget_desc_quran_player,
            R.string.widget_name_daily_dhikr, R.string.widget_desc_daily_dhikr,
            R.string.widget_name_tasbeeh, R.string.widget_desc_tasbeeh,
            R.string.widget_name_qibla, R.string.widget_desc_qibla,
            R.string.widget_name_hijri_date, R.string.widget_desc_hijri_date,
            R.string.widget_name_worship_plan, R.string.widget_desc_worship_plan,
            R.string.widget_name_quick_shortcuts, R.string.widget_desc_quick_shortcuts
        )

        for (resId in widgetStringIds) {
            val str = context.getString(resId)
            assertNotNull(str)
            assertTrue("String resource must not be empty", str.isNotBlank())
        }
    }

    @Test
    fun testAll15WidgetLayoutsInflateCleanlyWithoutUnsupportedViews() {
        val layoutIds = listOf(
            R.layout.widget_prayer_next,
            R.layout.widget_prayer_daily,
            R.layout.widget_prayer_mini,
            R.layout.widget_prayer_pair,
            R.layout.widget_ramadan,
            R.layout.widget_quran_ayah,
            R.layout.widget_quran_last_read,
            R.layout.widget_quran_khatmah,
            R.layout.widget_quran_player,
            R.layout.widget_adhkar_card,
            R.layout.widget_adhkar_tasbeeh,
            R.layout.widget_qibla,
            R.layout.widget_date_hijri,
            R.layout.widget_summary,
            R.layout.widget_shortcuts
        )

        val inflater = android.view.LayoutInflater.from(context)
        for (layoutId in layoutIds) {
            val view = inflater.inflate(layoutId, null)
            assertNotNull("Layout resource $layoutId must inflate successfully without errors", view)
        }
    }
}
