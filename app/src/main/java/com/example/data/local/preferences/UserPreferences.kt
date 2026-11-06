package com.example.data.local.preferences

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class FontSizeScale(val scale: Float, val labelArabic: String) {
    NORMAL(1.0f, "افتراضي (100%)"),
    MEDIUM(1.15f, "متوسط (115%)"),
    LARGE(1.30f, "كبير (130%)"),
    XLARGE(1.50f, "كبير جداً (150%)")
}

enum class FontFamilyType(val labelArabic: String) {
    AMIRI("الخط الأميري (تقليدي)"),
    CAIRO("خط كايرو (عصري)"),
    SYSTEM("خط النظام")
}

enum class CalculationMethod(val id: Int, val labelArabic: String) {
    UMM_AL_QURA(1, "أم القرى (مكة المكرمة)"),
    MUSLIM_WORLD_LEAGUE(2, "رابطة العالم الإسلامي"),
    EGYPTIAN(3, "الهيئة المصرية العامة للمساحة"),
    ISNA(4, "الجمعية الإسلامية لأمريكا الشمالية (ISNA)"),
    KARACHI(5, "جامعة العلوم الإسلامية بكراتشي")
}

enum class Madhhab(val labelArabic: String) {
    STANDARD("الجمهور (الشافعي، المالكي، الحنبلي)"),
    HANAFI("الحنفي")
}

enum class AudioPreference(val labelArabic: String) {
    ADHAN("الأذان الكامل"),
    TAKBEER("التكبير فقط"),
    SILENT("تنبيه صامت / اهتزاز")
}

data class ReciterInfo(
    val id: String,
    val nameArabic: String,
    val description: String
)

val AVAILABLE_RECITERS = listOf(
    ReciterInfo("Alafasy_128kbps", "مشاري راشد العفاسي", "تلاوة خاشعة بجودة 128 kbps"),
    ReciterInfo("Husary_128kbps", "محمود خليل الحصري", "المصحف المرتل برواية حفص 128 kbps"),
    ReciterInfo("AbdulSamad_64kbps_QuranicAudio", "عبد الباسط عبد الصمد", "المصحف المرتل 64 kbps"),
    ReciterInfo("Ghamadi_40kbps", "سعد الغامدي", "تلاوة كاملة بجودة 40 kbps"),
    ReciterInfo("Minshawy_Murattal_128kbps", "محمد صديق المنشاوي", "المصحف المرتل 128 kbps")
)

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val fontSizeScale: FontSizeScale = FontSizeScale.NORMAL,
    val fontFamilyType: FontFamilyType = FontFamilyType.CAIRO,
    val language: String = "ar",
    val notificationsEnabled: Boolean = false,
    val calculationMethod: CalculationMethod = CalculationMethod.UMM_AL_QURA,
    val madhhab: Madhhab = Madhhab.STANDARD,
    val audioPreference: AudioPreference = AudioPreference.ADHAN,
    val preferredReciter: String = "Alafasy_128kbps",
    val vibrationEnabled: Boolean = true,
    val dailyDhikrReminderEnabled: Boolean = true,

    // Last known coordinates & city
    val lastLatitude: Double = 21.4225, // Default Mecca
    val lastLongitude: Double = 39.8262,
    val lastCityName: String = "مكة المكرمة",
    val hasLocationPermission: Boolean = false,

    // Per-prayer notification toggles
    val notifyFajr: Boolean = true,
    val notifySunrise: Boolean = false,
    val notifyDhuhr: Boolean = true,
    val notifyAsr: Boolean = true,
    val notifyMaghrib: Boolean = true,
    val notifyIsha: Boolean = true,

    // Reminder before Adhan (0 = off, 5, 10, 15, 20 min)
    val preAdhanMinutes: Int = 10,

    // Manual minute adjustments (+/- minutes)
    val offsetFajr: Int = 0,
    val offsetSunrise: Int = 0,
    val offsetDhuhr: Int = 0,
    val offsetAsr: Int = 0,
    val offsetMaghrib: Int = 0,
    val offsetIsha: Int = 0
)
