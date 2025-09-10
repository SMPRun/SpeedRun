package net.smprun.speedrun.game.listener

import net.smprun.speedrun.Speedrun
import net.smprun.speedrun.annotations.AutoRegister
import org.bukkit.entity.EnderDragon
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import net.smprun.speedrun.events.DragonKillEvent

@AutoRegister
class DragonKillListener(private val plugin: Speedrun) : Listener {

    @EventHandler
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity
        if (entity is EnderDragon) {
            handleDragonKill(entity.killer)
        }
    }
    
    @EventHandler
    fun onCustomDragonKill(event: DragonKillEvent) {
        plugin.logger.info("Dragon kill event fired! Killer: ${event.killer?.name ?: "unknown"}")
        handleDragonKill(event.killer)
    }
    
    private fun handleDragonKill(killer: Player?) {
        plugin.gameService.endGame(killer)
    }
}