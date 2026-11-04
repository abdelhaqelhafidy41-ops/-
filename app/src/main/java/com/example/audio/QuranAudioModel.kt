package com.example.audio

enum class RepeatMode(val titleArabic: String) {
    NONE("بدون تكرار"),
    REPEAT_AYAH("تكرار الآية"),
    REPEAT_SURAH("تكرار السورة")
}

data class Reciter(
    val id: String,
    val nameArabic: String,
    val subfolder: String,
    val quality: String
) {
    fun getAyahAudioUrl(surahNumber: Int, ayahNumber: Int): String {
        val s = String.format("%03d", surahNumber)
        val a = String.format("%03d", ayahNumber)
        return "https://everyayah.com/data/$subfolder/$s$a.mp3"
    }

    companion object {
        val AVAILABLE_RECITERS = listOf(
            Reciter(
                id = "alafasy",
                nameArabic = "الشيخ مشاري راشد العفاسي",
                subfolder = "Alafasy_128kbps",
                quality = "128 kbps"
            ),
            Reciter(
                id = "husary",
                nameArabic = "الشيخ محمود خليل الحصري",
                subfolder = "Husary_128kbps",
                quality = "128 kbps"
            ),
            Reciter(
                id = "abdulbasit",
                nameArabic = "الشيخ عبد الباسط عبد الصمد (مرتل)",
                subfolder = "Abdul_Basit_Murattal_192kbps",
                quality = "192 kbps"
            ),
            Reciter(
                id = "ghamdi",
                nameArabic = "الشيخ سعد بن سعيد الغامدي",
                subfolder = "Ghamadi_40kbps",
                quality = "40 kbps"
            )
        )

        val DEFAULT = AVAILABLE_RECITERS[0]
    }
}

data class AudioPlayerState(
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val currentSurahNumber: Int = 1,
    val currentAyahNumber: Int = 1,
    val currentSurahName: String = "الفاتحة",
    val totalAyahsInSurah: Int = 7,
    val currentPositionMs: Int = 0,
    val durationMs: Int = 0,
    val reciter: Reciter = Reciter.DEFAULT,
    val repeatMode: RepeatMode = RepeatMode.NONE,
    val errorMessage: String? = null,
    val isVisible: Boolean = false
)
