package net.smprun.speedrun.game

import com.tcoded.folialib.FoliaLib
import gg.scala.flavor.inject.Inject
import gg.scala.flavor.service.Configure
import gg.scala.flavor.service.Service
import net.smprun.common.utils.Colors
import net.smprun.common.utils.Text
import org.bukkit.plugin.java.JavaPlugin

@Service(name = "AutoStartService", priority = 6)
object AutoStartService {

    @Inject
    lateinit var plugin: JavaPlugin
    
    @Inject
    lateinit var foliaLib: FoliaLib

    private var countdownActive: Boolean = false
    private var secondsRemaining: Int = 0

    private val countdownSeconds = 60
    private val graceSeconds = 15

    private var enabled: Boolean = true
    private var minPlayers: Int = 2

    @Configure
    fun start() {
        reloadConfig()

        foliaLib.scheduler.runTimer(Runnable {
            try {
                tick()
            } catch (e: Exception) {
                plugin.logger.warning("AutoStart tick failed: ${e.message}")
            }
        }, 20L, 20L)
    }

    fun reloadConfig() {
        val cfg = plugin.config
        enabled = cfg.getBoolean("autostart.enabled", true)
        minPlayers = cfg.getInt("autostart.minPlayers", 10)
    }

    fun onPlayerJoin() {
        if (!enabled) return
        if (GameService.isGameActive) return
        if (GameService.isResetScheduled) return
        if (countdownActive) return

        val online = plugin.server.onlinePlayers.size
        if (online >= minPlayers) {
            startCountdown()
        }
    }

    fun onPlayerQuit() {
        if (!enabled) return
        if (!countdownActive) return
        if (GameService.isGameActive) return

        val onlineAfterQuit = plugin.server.onlinePlayers.size
        if (onlineAfterQuit < minPlayers && secondsRemaining > graceSeconds) {
            cancelCountdown("Not enough players. Countdown cancelled.")
        }
    }

    private fun startCountdown() {
        countdownActive = true
        secondsRemaining = countdownSeconds
        Text.broadcast("Enough players joined! Game starts in ${secondsRemaining}s.", Colors.SUCCESS)
    }

    private fun cancelCountdown(reason: String? = null) {
        if (!countdownActive) return
        countdownActive = false
        secondsRemaining = 0
        if (!reason.isNullOrBlank()) {
            Text.broadcast(reason, Colors.WARNING)
        }
    }

    private fun tick() {

        if (!enabled) {
            if (countdownActive) cancelCountdown("Auto-start disabled. Countdown cancelled.")
            return
        }

        if (GameService.isGameActive || GameService.isResetScheduled) {
            if (countdownActive) cancelCountdown()
            return
        }

        if (!countdownActive) return

        // Announce at key times
        when (secondsRemaining) {
            60, 45, 30, 15, 10, 5, 4, 3, 2, 1 ->
                Text.broadcast("Game starting in ${secondsRemaining}s...", Colors.BRAND_PRIMARY)
        }

        // If players dropped below threshold, and we're not in the last grace window, cancel
        if (plugin.server.onlinePlayers.size < minPlayers && secondsRemaining > graceSeconds) {
            cancelCountdown("Not enough players. Countdown cancelled.")
            return
        }

        secondsRemaining -= 1

        if (secondsRemaining <= 0) {
            // Final check before starting
            if (plugin.server.onlinePlayers.size >= minPlayers) {
                cancelCountdown()
                GameService.startGame()
            } else {
                cancelCountdown("Not enough players. Countdown cancelled.")
            }
        }
    }
}
