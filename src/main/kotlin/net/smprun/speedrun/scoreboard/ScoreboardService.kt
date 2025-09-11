package net.smprun.speedrun.scoreboard

import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar
import net.smprun.speedrun.Speedrun
import net.smprun.speedrun.player.repository.PlayerRepository
import net.smprun.speedrun.utils.TimeUtil
import org.bukkit.entity.Player
import java.util.UUID

class ScoreboardService(private val plugin: Speedrun) : AutoCloseable {

    private var library: ScoreboardLibrary? = null
    private val sidebarsByPlayer: MutableMap<UUID, Sidebar> = HashMap()
    private val playerRepository by lazy { PlayerRepository(plugin) }

    fun start() {
        library = try {
            ScoreboardLibrary.loadScoreboardLibrary(plugin)
        } catch (ex: Exception) {
            plugin.logger.warning("Failed to load ScoreboardLibrary: ${ex.message}")
            ex.printStackTrace()
            null
        }

        if (library == null) {
            plugin.logger.warning("ScoreboardLibrary not available. Scoreboards will be disabled.")
            return
        }

        // Periodically update all active sidebars every second
        plugin.foliaLib.scheduler.runTimer(Runnable {
            updateAll()
        }, 20L, 20L)
    }

    fun showFor(player: Player) {
        val lib = library ?: return

        // Close existing
        hideFor(player.uniqueId)

        val sidebar = lib.createSidebar()
        sidebar.title(Component.text("SMPRun Network", NamedTextColor.GOLD))

        // Initialize lines
        sidebar.line(0, Component.text("Best Time: N/A", NamedTextColor.WHITE))
        sidebar.line(1, Component.text("Wins: N/A", NamedTextColor.WHITE))
        sidebar.line(2, Component.text("Current Run: N/A", NamedTextColor.WHITE))
        sidebar.line(3, Component.text("", NamedTextColor.DARK_GRAY))
        sidebar.line(4, Component.text("smprun.net", NamedTextColor.GRAY))

        sidebar.addPlayer(player)
        sidebarsByPlayer[player.uniqueId] = sidebar

        // Immediate update
        updatePlayer(player.uniqueId)
    }

    fun hideFor(playerId: UUID) {
        sidebarsByPlayer.remove(playerId)?.close()
    }

    fun hideFor(player: Player) {
        hideFor(player.uniqueId)
    }

    fun updateAll() {
        if (sidebarsByPlayer.isEmpty()) return
        sidebarsByPlayer.keys.forEach { updatePlayer(it) }
    }

    fun updatePlayer(playerId: UUID) {
        sidebarsByPlayer[playerId] ?: return

        val bukkitPlayer = plugin.server.getPlayer(playerId) ?: return

        // Fetch player stats async
        plugin.foliaLib.scheduler.runAsync { _ ->
            try {
                val player = runBlocking { playerRepository.findByUuid(playerId) }

                val bestTimeText = player?.bestTime?.let { TimeUtil.formatTimeShort(it) } ?: "N/A"
                val winsText = player?.wins?.toString() ?: "N/A"

                val currentRunText = if (plugin.gameService.isGameActive) {
                    plugin.gameService.getGameDuration()?.let { TimeUtil.formatTimeShort(it) } ?: "N/A"
                } else {
                    "N/A"
                }

                // Apply updates on main thread
                plugin.foliaLib.scheduler.runLater(Runnable {
                    if (!bukkitPlayer.isOnline) return@Runnable
                    val sb = sidebarsByPlayer[playerId] ?: return@Runnable
                    sb.title(Component.text("SMPRun Network", NamedTextColor.GOLD))
                    sb.line(0, Component.text("Best Time: $bestTimeText", NamedTextColor.WHITE))
                    sb.line(1, Component.text("Wins: $winsText", NamedTextColor.WHITE))
                    sb.line(2, Component.text("Current Run: $currentRunText", NamedTextColor.WHITE))
                    sb.line(3, Component.text("", NamedTextColor.DARK_GRAY))
                    sb.line(4, Component.text("smprun.net", NamedTextColor.GRAY))
                }, 0L)
            } catch (e: Exception) {
                plugin.logger.warning("Failed updating scoreboard for ${bukkitPlayer.name}: ${e.message}")
            }
        }
    }

    override fun close() {
        try {
            sidebarsByPlayer.values.forEach { it.close() }
            sidebarsByPlayer.clear()
        } finally {
            library?.close()
            library = null
        }
    }
}