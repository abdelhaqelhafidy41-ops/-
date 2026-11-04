package com.example.data.local

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.data.local.preferences.AudioPreference
import com.example.data.local.preferences.CalculationMethod
import com.example.data.local.preferences.FontSizeScale
import com.example.data.local.preferences.Madhhab
import com.example.data.local.preferences.ThemeMode
import com.example.data.local.preferences.UserPreferencesDataStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class UserPreferencesTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var dataStore: UserPreferencesDataStore

    @Before
    fun setup() {
        val testPreferencesDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { tmpFolder.newFile("test_user_preferences_${System.nanoTime()}.preferences_pb") }
        )
        dataStore = UserPreferencesDataStore(testPreferencesDataStore)
    }

    @Test
    fun testDefaultUserPreferences() = runTest {
        val initialPrefs = dataStore.userPreferencesFlow.first()
        assertEquals(ThemeMode.SYSTEM, initialPrefs.themeMode)
        assertEquals(FontSizeScale.NORMAL, initialPrefs.fontSizeScale)
        assertEquals("ar", initialPrefs.language)
        assertFalse(initialPrefs.notificationsEnabled)
        assertEquals(CalculationMethod.UMM_AL_QURA, initialPrefs.calculationMethod)
        assertEquals(Madhhab.STANDARD, initialPrefs.madhhab)
        assertEquals(AudioPreference.ADHAN, initialPrefs.audioPreference)
    }

    @Test
    fun testUpdatingPreferences() = runTest {
        dataStore.updateThemeMode(ThemeMode.DARK)
        dataStore.updateFontSizeScale(FontSizeScale.LARGE)
        dataStore.updateLanguage("ar")
        dataStore.updateNotificationsEnabled(true)
        dataStore.updateCalculationMethod(CalculationMethod.MUSLIM_WORLD_LEAGUE)
        dataStore.updateMadhhab(Madhhab.HANAFI)
        dataStore.updateAudioPreference(AudioPreference.TAKBEER)

        val updatedPrefs = dataStore.userPreferencesFlow.first()
        assertEquals(ThemeMode.DARK, updatedPrefs.themeMode)
        assertEquals(FontSizeScale.LARGE, updatedPrefs.fontSizeScale)
        assertEquals(1.30f, updatedPrefs.fontSizeScale.scale, 0.01f)
        assertTrue(updatedPrefs.notificationsEnabled)
        assertEquals(CalculationMethod.MUSLIM_WORLD_LEAGUE, updatedPrefs.calculationMethod)
        assertEquals(Madhhab.HANAFI, updatedPrefs.madhhab)
        assertEquals(AudioPreference.TAKBEER, updatedPrefs.audioPreference)
    }

    @Test
    fun testPrayerNotificationTogglesAndOffsets() = runTest {
        dataStore.updateLastLocation(30.0444, 31.2357, "القاهرة", true)
        dataStore.updatePrayerNotification("fajr", false)
        dataStore.updatePrayerNotification("asr", true)
        dataStore.updatePreAdhanMinutes(15)
        dataStore.updateManualOffsets(2, 0, -1, 3, 0, 4)

        val prefs = dataStore.userPreferencesFlow.first()
        assertEquals(30.0444, prefs.lastLatitude, 0.0001)
        assertEquals(31.2357, prefs.lastLongitude, 0.0001)
        assertEquals("القاهرة", prefs.lastCityName)
        assertTrue(prefs.hasLocationPermission)

        assertFalse(prefs.notifyFajr)
        assertTrue(prefs.notifyAsr)
        assertEquals(15, prefs.preAdhanMinutes)

        assertEquals(2, prefs.offsetFajr)
        assertEquals(-1, prefs.offsetDhuhr)
        assertEquals(3, prefs.offsetAsr)
        assertEquals(4, prefs.offsetIsha)
    }
}
