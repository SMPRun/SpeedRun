package net.smprun.speedrun.player.listener

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.smprun.speedrun.Speedrun
import net.smprun.common.annotations.AutoRegister
import net.smprun.speedrun.player.Player
import net.smprun.speedrun.player.repository.PlayerRepository
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

@AutoRegister
class PlayerListener(private val plugin: Speedrun) : Listener {

    private val repository by lazy { PlayerRepository(plugin) }
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val scoreboard by lazy { plugin.scoreboardService }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val bukkitPlayer = event.player
        val uuid = bukkitPlayer.uniqueId
        val name = bukkitPlayer.name

        if (bukkitPlayer.hasPermission("speedrun.chat.bypass")) {
            bukkitPlayer.sendMessage(
                Component.text("Note: Global chat is muted on Speedrun servers. You have bypass.", NamedTextColor.GRAY)
            )
        } else {
            bukkitPlayer.sendMessage(
                Component.text("Global chat is muted on Speedrun servers.", NamedTextColor.YELLOW)
            )
        }

        ioScope.launch {
            try {
                val existing = repository.findByUuid(uuid)
                if (existing == null) {
                    val created = Player(uuid = uuid, username = name)
                    repository.upsert(created)
                    plugin.logger.info("Created PlayerInfo for $name")
                } else if (existing.username != name) {
                    // Update username if it changed
                    repository.upsert(existing.copy(username = name))
                }
            } catch (e: Exception) {
                plugin.logger.severe("Failed to ensure PlayerInfo for ${name}: ${e.message}")
            }
        }

        // Show scoreboard after join
        plugin.foliaLib.scheduler.runLater(Runnable {
            if (bukkitPlayer.isOnline) {
                scoreboard.showFor(bukkitPlayer)
            }
        }, 20L)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        scoreboard.hideFor(event.player)
    }
}