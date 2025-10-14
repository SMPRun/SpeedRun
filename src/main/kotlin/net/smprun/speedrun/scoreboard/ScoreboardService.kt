package net.smprun.speedrun.scoreboard

import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.smprun.common.CommonServices
import net.smprun.common.scoreboard.SidebarManager
import net.smprun.common.utils.Colors
import net.smprun.speedrun.Speedrun
import net.smprun.speedrun.player.repository.PlayerStatsRepository
import net.smprun.common.utils.TimeUtil
import org.bukkit.entity.Player
import java.util.UUID

class ScoreboardService(private val plugin: Speedrun) : AutoCloseable {

    private val sidebarManager = SidebarManager(plugin)
    private val playerStatsRepository by lazy { PlayerStatsRepository(plugin) }

    fun start() {
        val ok = sidebarManager.start(CommonServices.scoreboardLibrary)
        if (!ok) {
            plugin.logger.warning("ScoreboardLibrary not available. Scoreboards will be disabled.")
            return
        }

        // Periodically update all active sidebars every second
        plugin.foliaLib.scheduler.runTimer(Runnable {
            updateAll()
        }, 20L, 20L)
    }

    fun showFor(player: Player) {
        sidebarManager.show(player, Component.text("SMPRun Network", Colors.BRAND_PRIMARY))?.let { sb ->
            sb.line(0, Component.text("Best Time: ", Colors.BRAND_SECONDARY).append(Component.text("N/A", NamedTextColor.WHITE)))
            sb.line(1, Component.text("Recent Time: ", Colors.BRAND_SECONDARY).append(Component.text("N/A", NamedTextColor.WHITE)))
            sb.line(2, Component.text("Wins: ", Colors.BRAND_SECONDARY).append(Component.text("N/A", NamedTextColor.WHITE)))
            sb.line(3, Component.text("Current Run: ", Colors.BRAND_SECONDARY).append(Component.text("N/A", NamedTextColor.WHITE)))
            sb.line(4, Component.text(""))
            sb.line(5, Component.text("smprun.net", NamedTextColor.GRAY))
            updatePlayer(player.uniqueId)
        }
    }

    fun hideFor(playerId: UUID) {
        sidebarManager.hide(playerId)
    }

    fun hideFor(player: Player) {
        hideFor(player.uniqueId)
    }

    fun updateAll() {
        sidebarManager.updateAll { uuid, _ -> updatePlayer(uuid) }
    }

    fun updatePlayer(playerId: UUID) {
        // Skip if the player has no sidebar
        sidebarManager.update(playerId) { _ -> }

        val bukkitPlayer = plugin.server.getPlayer(playerId) ?: return

        // Fetch player stats async
        plugin.foliaLib.scheduler.runAsync { _ ->
            try {
                val stats = runBlocking { playerStatsRepository.findByUuid(playerId) }

                val bestTimeText = stats?.bestTime?.let { TimeUtil.formatTimeShort(it) } ?: "N/A"
                val recentTimeText = stats?.recentTime?.let { TimeUtil.formatTimeShort(it) } ?: "N/A"
                val winsText = stats?.wins?.toString() ?: "N/A"

                val currentRunText = if (plugin.gameService.isGameActive) {
                    plugin.gameService.getGameDuration()?.let { TimeUtil.formatTimeShort(it) } ?: "N/A"
                } else {
                    "N/A"
                }

                // Apply updates on main thread
                plugin.foliaLib.scheduler.runLater(Runnable {
                    if (!bukkitPlayer.isOnline) return@Runnable
                    sidebarManager.update(playerId) { sb ->
                        sb.title(Component.text("SMPRun Network", Colors.BRAND_PRIMARY))
                        sb.line(0, Component.text("Best Time: ", Colors.BRAND_SECONDARY).append(Component.text(bestTimeText, NamedTextColor.WHITE)))
                        sb.line(1, Component.text("Recent Time: ", Colors.BRAND_SECONDARY).append(Component.text(recentTimeText, NamedTextColor.WHITE)))
                        sb.line(2, Component.text("Wins: ", Colors.BRAND_SECONDARY).append(Component.text(winsText, NamedTextColor.WHITE)))
                        sb.line(3, Component.text("Current Run: ", Colors.BRAND_SECONDARY).append(Component.text(currentRunText, NamedTextColor.WHITE)))
                        sb.line(4, Component.text(""))
                        sb.line(5, Component.text("smprun.net", NamedTextColor.GRAY))
                    }
                }, 0L)
            } catch (e: Exception) {
                plugin.logger.warning("Failed updating scoreboard for ${bukkitPlayer.name}: ${e.message}")
            }
        }
    }

    override fun close() {
        sidebarManager.close()
    }
}