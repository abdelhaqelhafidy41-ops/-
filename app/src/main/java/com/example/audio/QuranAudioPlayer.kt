package com.example.audio

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class QuranAudioPlayer(
    private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var mediaPlayer: MediaPlayer? = null
    private var progressTrackingJob: Job? = null

    private val _state = MutableStateFlow(AudioPlayerState())
    val state: StateFlow<AudioPlayerState> = _state.asStateFlow()

    private val httpClient = OkHttpClient()

    private fun getAudioCacheDir(reciter: Reciter): File {
        val dir = File(context.filesDir, "audio_quran/${reciter.subfolder}")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getLocalAyahFile(surahNumber: Int, ayahNumber: Int, reciter: Reciter): File {
        val fileName = String.format("%03d%03d.mp3", surahNumber, ayahNumber)
        return File(getAudioCacheDir(reciter), fileName)
    }

    fun isAyahCached(surahNumber: Int, ayahNumber: Int, reciter: Reciter): Boolean {
        val file = getLocalAyahFile(surahNumber, ayahNumber, reciter)
        return file.exists() && file.length() > 1024
    }

    fun isSurahCached(surahNumber: Int, totalAyahs: Int, reciter: Reciter): Boolean {
        for (i in 1..totalAyahs) {
            if (!isAyahCached(surahNumber, i, reciter)) return false
        }
        return true
    }

    fun setReciter(reciter: Reciter) {
        _state.update { it.copy(reciter = reciter) }
        if (_state.value.isPlaying) {
            playAyah(
                surahNumber = _state.value.currentSurahNumber,
                ayahNumber = _state.value.currentAyahNumber,
                surahName = _state.value.currentSurahName,
                totalAyahs = _state.value.totalAyahsInSurah
            )
        }
    }

    fun setRepeatMode(mode: RepeatMode) {
        _state.update { it.copy(repeatMode = mode) }
    }

    fun playAyah(
        surahNumber: Int,
        ayahNumber: Int,
        surahName: String,
        totalAyahs: Int
    ) {
        val currentReciter = _state.value.reciter
        _state.update {
            it.copy(
                isPlaying = false,
                isLoading = true,
                errorMessage = null,
                currentSurahNumber = surahNumber,
                currentAyahNumber = ayahNumber,
                currentSurahName = surahName,
                totalAyahsInSurah = totalAyahs,
                isVisible = true
            )
        }

        releasePlayer()

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
            }
            mediaPlayer = player

            val localFile = getLocalAyahFile(surahNumber, ayahNumber, currentReciter)
            if (localFile.exists() && localFile.length() > 1024) {
                player.setDataSource(localFile.absolutePath)
            } else {
                val url = currentReciter.getAyahAudioUrl(surahNumber, ayahNumber)
                player.setDataSource(url)
            }

            player.setOnPreparedListener { mp ->
                mp.start()
                _state.update {
                    it.copy(
                        isPlaying = true,
                        isLoading = false,
                        durationMs = mp.duration,
                        currentPositionMs = 0
                    )
                }
                startProgressTracker()
                startForegroundService()
            }

            player.setOnCompletionListener {
                handlePlaybackCompletion()
            }

            player.setOnErrorListener { _, what, extra ->
                _state.update {
                    it.copy(
                        isPlaying = false,
                        isLoading = false,
                        errorMessage = "تعذر تشغيل التلاوة. يرجى التحقق من اتصال الإنترنت أو تجربة قارئ آخر."
                    )
                }
                stopForegroundService()
                true
            }

            player.prepareAsync()
        } catch (e: Exception) {
            e.printStackTrace()
            _state.update {
                it.copy(
                    isPlaying = false,
                    isLoading = false,
                    errorMessage = "خطأ في تشغيل الصوت: ${e.localizedMessage ?: "يرجى المحاولة مجدداً"}"
                )
            }
        }
    }

    private fun handlePlaybackCompletion() {
        val currentState = _state.value
        when (currentState.repeatMode) {
            RepeatMode.REPEAT_AYAH -> {
                playAyah(
                    surahNumber = currentState.currentSurahNumber,
                    ayahNumber = currentState.currentAyahNumber,
                    surahName = currentState.currentSurahName,
                    totalAyahs = currentState.totalAyahsInSurah
                )
            }
            RepeatMode.REPEAT_SURAH -> {
                if (currentState.currentAyahNumber < currentState.totalAyahsInSurah) {
                    playAyah(
                        surahNumber = currentState.currentSurahNumber,
                        ayahNumber = currentState.currentAyahNumber + 1,
                        surahName = currentState.currentSurahName,
                        totalAyahs = currentState.totalAyahsInSurah
                    )
                } else {
                    // Loop to ayah 1
                    playAyah(
                        surahNumber = currentState.currentSurahNumber,
                        ayahNumber = 1,
                        surahName = currentState.currentSurahName,
                        totalAyahs = currentState.totalAyahsInSurah
                    )
                }
            }
            RepeatMode.NONE -> {
                if (currentState.currentAyahNumber < currentState.totalAyahsInSurah) {
                    playAyah(
                        surahNumber = currentState.currentSurahNumber,
                        ayahNumber = currentState.currentAyahNumber + 1,
                        surahName = currentState.currentSurahName,
                        totalAyahs = currentState.totalAyahsInSurah
                    )
                } else {
                    _state.update { it.copy(isPlaying = false, currentPositionMs = 0) }
                    stopForegroundService()
                }
            }
        }
    }

    fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _state.update { s -> s.copy(isPlaying = false) }
            }
        }
    }

    fun resume() {
        mediaPlayer?.let {
            it.start()
            _state.update { s -> s.copy(isPlaying = true) }
            startProgressTracker()
            startForegroundService()
        } ?: run {
            playAyah(
                surahNumber = _state.value.currentSurahNumber,
                ayahNumber = _state.value.currentAyahNumber,
                surahName = _state.value.currentSurahName,
                totalAyahs = _state.value.totalAyahsInSurah
            )
        }
    }

    fun togglePlayPause() {
        if (_state.value.isPlaying) {
            pause()
        } else {
            resume()
        }
    }

    fun nextAyah() {
        val s = _state.value
        if (s.currentAyahNumber < s.totalAyahsInSurah) {
            playAyah(s.currentSurahNumber, s.currentAyahNumber + 1, s.currentSurahName, s.totalAyahsInSurah)
        }
    }

    fun previousAyah() {
        val s = _state.value
        if (s.currentAyahNumber > 1) {
            playAyah(s.currentSurahNumber, s.currentAyahNumber - 1, s.currentSurahName, s.totalAyahsInSurah)
        }
    }

    fun seekTo(positionMs: Int) {
        mediaPlayer?.seekTo(positionMs)
        _state.update { it.copy(currentPositionMs = positionMs) }
    }

    fun stop() {
        releasePlayer()
        _state.update { it.copy(isPlaying = false, isVisible = false, currentPositionMs = 0) }
        stopForegroundService()
    }

    private fun startProgressTracker() {
        progressTrackingJob?.cancel()
        progressTrackingJob = scope.launch {
            while (isActive && mediaPlayer != null && _state.value.isPlaying) {
                mediaPlayer?.let { mp ->
                    if (mp.isPlaying) {
                        _state.update {
                            it.copy(
                                currentPositionMs = mp.currentPosition,
                                durationMs = mp.duration
                            )
                        }
                    }
                }
                delay(500)
            }
        }
    }

    private fun releasePlayer() {
        progressTrackingJob?.cancel()
        mediaPlayer?.let {
            try {
                if (it.isPlaying) it.stop()
                it.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        mediaPlayer = null
    }

    private fun startForegroundService() {
        try {
            val intent = Intent(context, QuranAudioService::class.java).apply {
                action = QuranAudioService.ACTION_START
                putExtra("surah_name", _state.value.currentSurahName)
                putExtra("ayah_number", _state.value.currentAyahNumber)
                putExtra("reciter_name", _state.value.reciter.nameArabic)
                putExtra("is_playing", _state.value.isPlaying)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopForegroundService() {
        try {
            val intent = Intent(context, QuranAudioService::class.java).apply {
                action = QuranAudioService.ACTION_STOP
            }
            context.startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Downloads an entire Surah audio for the selected reciter for offline listening
     */
    suspend fun downloadSurahAudio(
        surahNumber: Int,
        totalAyahs: Int,
        reciter: Reciter,
        onProgress: (downloaded: Int, total: Int) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        val cacheDir = getAudioCacheDir(reciter)
        for (ayah in 1..totalAyahs) {
            val file = getLocalAyahFile(surahNumber, ayah, reciter)
            if (!file.exists() || file.length() < 1024) {
                val url = reciter.getAyahAudioUrl(surahNumber, ayah)
                val request = Request.Builder().url(url).build()
                try {
                    val response = httpClient.newCall(request).execute()
                    if (response.isSuccessful) {
                        response.body?.byteStream()?.use { input ->
                            FileOutputStream(file).use { output ->
                                input.copyTo(output)
                            }
                        }
                    } else {
                        return@withContext false
                    }
                } catch (e: IOException) {
                    return@withContext false
                }
            }
            onProgress(ayah, totalAyahs)
        }
        return@withContext true
    }
}
