package net.smprun.speedrun.game

import net.smprun.common.CommonServices
import net.smprun.common.utils.Colors
import net.smprun.common.utils.Text
import net.smprun.speedrun.Speedrun

class AutoStartService(private val plugin: Speedrun, private val gameService: GameService) {

    private var countdownActive: Boolean = false
    private var secondsRemaining: Int = 0

    private val countdownSeconds = 60
    private val graceSeconds = 15

    private var enabled: Boolean = true
    private var minPlayers: Int = 2

    fun start() {
        reloadConfig()

        CommonServices.foliaLib.scheduler.runTimer(Runnable {
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
        if (gameService.isGameActive) return
        if (gameService.isResetScheduled) return
        if (countdownActive) return

        val online = plugin.server.onlinePlayers.size
        if (online >= minPlayers) {
            startCountdown()
        }
    }

    fun onPlayerQuit() {
        if (!enabled) return
        if (!countdownActive) return
        if (gameService.isGameActive) return

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

        if (gameService.isGameActive || gameService.isResetScheduled) {
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
                gameService.startGame()
            } else {
                cancelCountdown("Not enough players. Countdown cancelled.")
            }
        }
    }
}
