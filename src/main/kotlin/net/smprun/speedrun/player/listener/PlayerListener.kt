package net.smprun.speedrun.player.listener

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.smprun.speedrun.Speedrun
import net.smprun.speedrun.annotations.AutoRegister
import net.smprun.speedrun.player.Player
import net.smprun.speedrun.player.repository.PlayerRepository
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

@AutoRegister
class PlayerListener(private val plugin: Speedrun) : Listener {

    private val repository by lazy { PlayerRepository(plugin) }
    private val ioScope = CoroutineScope(Dispatchers.IO)

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val bukkitPlayer = event.player
        val uuid = bukkitPlayer.uniqueId
        val name = bukkitPlayer.name

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
    }
}