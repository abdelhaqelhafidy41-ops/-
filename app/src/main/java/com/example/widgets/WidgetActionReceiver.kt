package com.example.widgets

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.MainActivity
import com.example.NoorMuslimApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WidgetActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val app = context.applicationContext as? NoorMuslimApp
        val container = app?.container

        when (action) {
            ACTION_TASBEEH_INCREMENT -> {
                container?.let { c ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val state = c.database.dhikrDao().getTasbeehState()
                        val current = state ?: com.example.data.local.entity.TasbeehStateEntity()
                        val newCount = current.count + 1
                        c.database.dhikrDao().saveTasbeehState(
                            current.copy(
                                count = newCount,
                                lastUpdated = System.currentTimeMillis()
                            )
                        )
                        c.achievementRepository.logDhikrCount(1)
                        WidgetUpdateCoordinator.updateAllWidgets(context)
                    }
                }
            }

            ACTION_TASBEEH_RESET -> {
                container?.let { c ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val state = c.database.dhikrDao().getTasbeehState()
                        if (state != null) {
                            c.database.dhikrDao().saveTasbeehState(
                                state.copy(count = 0, lastUpdated = System.currentTimeMillis())
                            )
                        }
                        WidgetUpdateCoordinator.updateAllWidgets(context)
                    }
                }
            }

            ACTION_PLAYER_TOGGLE -> {
                container?.let { c ->
                    CoroutineScope(Dispatchers.Main).launch {
                        val player = c.audioPlayer
                        val isPlaying = player.state.value.isPlaying
                        if (isPlaying) {
                            player.pause()
                        } else {
                            player.resume()
                        }
                        WidgetUpdateCoordinator.updateAllWidgets(context)
                    }
                }
            }

            ACTION_REFRESH -> {
                WidgetUpdateCoordinator.updateAllWidgets(context)
            }

            ACTION_OPEN_DESTINATION -> {
                val route = intent.getStringExtra(EXTRA_ROUTE) ?: "home"
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("initial_route", route)
                }
                context.startActivity(launchIntent)
            }
        }
    }

    companion object {
        const val ACTION_TASBEEH_INCREMENT = "com.example.widgets.ACTION_TASBEEH_INCREMENT"
        const val ACTION_TASBEEH_RESET = "com.example.widgets.ACTION_TASBEEH_RESET"
        const val ACTION_PLAYER_TOGGLE = "com.example.widgets.ACTION_PLAYER_TOGGLE"
        const val ACTION_REFRESH = "com.example.widgets.ACTION_REFRESH"
        const val ACTION_OPEN_DESTINATION = "com.example.widgets.ACTION_OPEN_DESTINATION"
        const val EXTRA_ROUTE = "extra_route"
    }
}
