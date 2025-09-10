package net.smprun.speedrun.winner.listener

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import net.smprun.speedrun.Speedrun
import net.smprun.speedrun.annotations.AutoRegister
import net.smprun.speedrun.player.repository.PlayerRepository
import net.smprun.speedrun.winner.WinnerRecord
import net.smprun.speedrun.winner.repository.WinnerRepository
import org.bukkit.Bukkit
import org.bukkit.entity.EnderDragon
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import net.smprun.speedrun.game.WorldResetService

@AutoRegister
class DragonKillListener(private val plugin: Speedrun) : Listener {

    private val repository by lazy { WinnerRepository(plugin) }
    private val playerRepository by lazy { PlayerRepository(plugin) }
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val resetService by lazy { WorldResetService(plugin) }
    @Volatile private var resetScheduled: Boolean = false

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity
        if (entity is EnderDragon) {
            val killer: Player? = entity.killer
            val name = killer?.name
            val uuid = killer?.uniqueId

            ioScope.launch {
                try {
                    if (uuid != null && name != null) {
                        // For now, winTime is set to the server uptime in seconds
                        val winTime = (System.currentTimeMillis() - plugin.server.worlds[0].fullTime) // placeholder if needed
                        val record = WinnerRecord(uuid = uuid, username = name, winTime = winTime)
                        repository.insert(record)

                        // Update player wins/bestTime using Player.addWin
                        val player = playerRepository.findByUuid(uuid)
                        if (player != null) {
                            playerRepository.upsert(player.addWin(winTime))
                        }

                        Bukkit.getServer().broadcast(Component.text("$name has defeated the Ender Dragon!"))
                    } else {
                        Bukkit.getServer().broadcast(Component.text("The Ender Dragon has been defeated!"))
                    }
                } catch (e: Exception) {
                    plugin.logger.severe("Failed to record winner: ${e.message}")
                }
            }

            // Schedule world reset after 60 seconds to let players see the result
            if (!resetScheduled) {
                resetScheduled = true
                Bukkit.getServer().broadcast(Component.text("Server will reset in 60 seconds..."))
                plugin.foliaLib.scheduler.runLater(Runnable {
                    val winnerText = name ?: "Unknown Winner"
                    resetService.resetAllWorlds(
                        kickReason = Component.text("Speedrun complete! Winner: ${winnerText}. Restarting for a new run...")
                    )
                }, 20L * 60)
            }
        }
    }
}