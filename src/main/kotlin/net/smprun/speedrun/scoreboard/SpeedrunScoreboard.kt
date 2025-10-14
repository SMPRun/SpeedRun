package net.smprun.speedrun.scoreboard

import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar
import net.smprun.common.CommonServices
import net.smprun.common.scoreboard.ScoreboardService
import net.smprun.common.utils.Colors
import net.smprun.common.utils.TimeUtil
import net.smprun.speedrun.Speedrun
import net.smprun.speedrun.player.repository.PlayerStatsRepository
import org.bukkit.entity.Player

class SpeedrunScoreboard(private val speedrunPlugin: Speedrun) : ScoreboardService(speedrunPlugin, CommonServices.foliaLib) {

    private val playerStatsRepository by lazy { PlayerStatsRepository(speedrunPlugin) }

    override fun getTitle(player: Player): Component {
        return Component.text("SMPRun Network", Colors.BRAND_PRIMARY)
    }

    override fun initializeSidebar(player: Player, sidebar: Sidebar) {
        sidebar.line(0, Component.text("Best Time: ", Colors.BRAND_SECONDARY).append(Component.text("N/A", NamedTextColor.WHITE)))
        sidebar.line(1, Component.text("Recent Time: ", Colors.BRAND_SECONDARY).append(Component.text("N/A", NamedTextColor.WHITE)))
        sidebar.line(2, Component.text("Wins: ", Colors.BRAND_SECONDARY).append(Component.text("N/A", NamedTextColor.WHITE)))
        sidebar.line(3, Component.text("Current Run: ", Colors.BRAND_SECONDARY).append(Component.text("N/A", NamedTextColor.WHITE)))
        sidebar.line(4, Component.text(""))
        sidebar.line(5, Component.text("smprun.net", NamedTextColor.GRAY))
    }

    override fun fetchScoreboardLines(player: Player): List<Component> {
        val stats = runBlocking { playerStatsRepository.findByUuid(player.uniqueId) }

        val bestTimeText = stats?.bestTime?.let { TimeUtil.formatTimeShort(it) } ?: "N/A"
        val recentTimeText = stats?.recentTime?.let { TimeUtil.formatTimeShort(it) } ?: "N/A"
        val winsText = stats?.wins?.toString() ?: "N/A"

        val currentRunText = if (speedrunPlugin.gameService.isGameActive) {
            speedrunPlugin.gameService.getGameDuration()?.let { TimeUtil.formatTimeShort(it) } ?: "N/A"
        } else {
            "N/A"
        }

        return listOf(
            Component.text("Best Time: ", Colors.BRAND_SECONDARY).append(Component.text(bestTimeText, NamedTextColor.WHITE)),
            Component.text("Recent Time: ", Colors.BRAND_SECONDARY).append(Component.text(recentTimeText, NamedTextColor.WHITE)),
            Component.text("Wins: ", Colors.BRAND_SECONDARY).append(Component.text(winsText, NamedTextColor.WHITE)),
            Component.text("Current Run: ", Colors.BRAND_SECONDARY).append(Component.text(currentRunText, NamedTextColor.WHITE)),
            Component.text(""),
            Component.text("smprun.net", NamedTextColor.GRAY)
        )
    }
}