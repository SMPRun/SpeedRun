package net.smprun.speedrun.player.listener

import com.tcoded.folialib.FoliaLib
import net.smprun.common.annotations.AutoRegister
import net.smprun.common.utils.Text
import net.smprun.speedrun.Speedrun
import net.smprun.speedrun.game.AutoStartService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

@AutoRegister
class PlayerListener(private val plugin: Speedrun) : Listener {

    private val scoreboard by lazy { plugin.scoreboardService }
    private val foliaLib: FoliaLib by lazy { plugin.foliaLib }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val bukkitPlayer = event.player

        if (bukkitPlayer.hasPermission("speedrun.chat.bypass")) {
            Text.info(bukkitPlayer, "Note: Global chat is muted on Speedrun servers. You have bypass.")
        } else {
            Text.warning(bukkitPlayer, "Global chat is muted on Speedrun servers.")
        }

        // Show scoreboard after join
        foliaLib.scheduler.runLater(Runnable {
            if (bukkitPlayer.isOnline) {
                scoreboard.showFor(bukkitPlayer)
            }
        }, 20L)

        // Check auto-start conditions after join
        AutoStartService.onPlayerJoin()
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        scoreboard.hideFor(event.player)
        // Check auto-start cancellation on leave
        AutoStartService.onPlayerQuit()
    }
}
