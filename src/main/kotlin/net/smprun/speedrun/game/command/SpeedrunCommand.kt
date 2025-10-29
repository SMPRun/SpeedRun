package net.smprun.speedrun.game.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import net.kyori.adventure.text.Component
import net.smprun.speedrun.Speedrun
import net.smprun.common.annotations.AutoRegister
import net.smprun.common.utils.Text
import net.smprun.speedrun.events.DragonKillEvent
import net.smprun.speedrun.game.world.WorldService
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@AutoRegister
@CommandAlias("speedrun|sr")
@CommandPermission("speedrun.admin")
class SpeedrunCommand(private val plugin: Speedrun) : BaseCommand() {

    @Subcommand("start")
    @Description("Start the speedrun timer")
    fun onStart(sender: CommandSender) {
        if (plugin.gameService.isGameActive) {
            Text.error(sender, "A speedrun is already in progress!")
            return
        }

        plugin.gameService.startGame()
        Text.success(sender, "Speedrun timer started!")
    }

    @Subcommand("forcestop")
    @Description("Force stop the speedrun without recording winner and reset worlds")
    fun onForceStop(sender: CommandSender) {
        if (!plugin.gameService.isGameActive) {
            Text.error(sender, "No speedrun is currently active!")
            return
        }

        plugin.gameService.forceStopGame()
        Text.warning(sender, "Speedrun force stopped! Resetting worlds...")
    }

    @Subcommand("complete")
    @Description("Complete the speedrun by simulating an Ender Dragon kill")
    fun onComplete(sender: CommandSender) {
        if (!plugin.gameService.isGameActive) {
            Text.error(sender, "No speedrun is currently active! Start one first with /speedrun start")
            return
        }

        val killer: Player? = sender as? Player ?: // fallback to first online player
        Bukkit.getOnlinePlayers().firstOrNull()

        // Fire custom dragon kill event to trigger real speedrun completion logic
        val event = DragonKillEvent(killer)
        Bukkit.getPluginManager().callEvent(event)

        Text.success(sender, "Dragon kill event fired! Real speedrun completion logic will handle the rest.")
    }

    @Subcommand("reset")
    @Description("Reset the world with a new random seed (only when no game is active)")
    fun onReset(sender: CommandSender) {
        if (plugin.gameService.isGameActive) {
            Text.error(sender, "Cannot reset world while a speedrun is active!")
            return
        }

        if (plugin.gameService.isResetScheduled) {
            Text.error(sender, "World reset is already scheduled! Please wait for it to complete.")
            return
        }

        Text.warning(sender, "Resetting world with new random seed...")

        val resetService = WorldService(plugin)
        resetService.resetAllWorlds(
            kickReason = Component.text("World is being reset with a new random seed. Reconnecting...")
        )
    }

    @Subcommand("reload")
    @Description("Reload Speedrun configuration without restarting the plugin")
    fun onReload(sender: CommandSender) {
        try {
            plugin.reloadConfig()
            plugin.autoStartService.reloadConfig()
            val enabled = plugin.config.getBoolean("autostart.enabled", true)
            val minPlayers = plugin.config.getInt("autostart.minPlayers", 2)
            Text.success(sender, "Config reloaded. AutoStart: ${if (enabled) "enabled" else "disabled"}, minPlayers=$minPlayers")
        } catch (e: Exception) {
            Text.error(sender, "Failed to reload config: ${e.message}")
        }
    }
}
