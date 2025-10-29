package net.smprun.speedrun.player.listener

import net.smprun.common.CommonServices
import net.smprun.common.utils.Text
import net.smprun.speedrun.Speedrun
import net.smprun.common.annotations.AutoRegister
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

@AutoRegister
class PlayerListener(private val plugin: Speedrun) : Listener {

    private val scoreboard by lazy { plugin.scoreboardService }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val bukkitPlayer = event.player

        if (bukkitPlayer.hasPermission("speedrun.chat.bypass")) {
            Text.info(bukkitPlayer, "Note: Global chat is muted on Speedrun servers. You have bypass.")
        } else {
            Text.warning(bukkitPlayer, "Global chat is muted on Speedrun servers.")
        }

        // Show scoreboard after join
        CommonServices.foliaLib.scheduler.runLater(Runnable {
            if (bukkitPlayer.isOnline) {
                scoreboard.showFor(bukkitPlayer)
            }
        }, 20L)

        // Check auto-start conditions after join
        plugin.autoStartService.onPlayerJoin()
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        scoreboard.hideFor(event.player)
        // Check auto-start cancellation on leave
        plugin.autoStartService.onPlayerQuit()
    }
}
