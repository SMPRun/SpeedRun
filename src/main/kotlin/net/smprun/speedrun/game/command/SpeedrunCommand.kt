package net.smprun.speedrun.game.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.smprun.speedrun.Speedrun
import net.smprun.speedrun.annotations.AutoRegister
import net.smprun.speedrun.events.DragonKillEvent
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
            sender.sendMessage(Component.text("A speedrun is already in progress!", NamedTextColor.RED))
            return
        }

        plugin.gameService.startGame()
        sender.sendMessage(Component.text("Speedrun timer started!", NamedTextColor.GOLD))
    }

    @Subcommand("forcestop")
    @Description("Force stop the speedrun without recording winner and reset worlds")
    fun onForceStop(sender: CommandSender) {
        if (!plugin.gameService.isGameActive) {
            sender.sendMessage(Component.text("No speedrun is currently active!", NamedTextColor.RED))
            return
        }

        plugin.gameService.forceStopGame()
        sender.sendMessage(Component.text("Speedrun force stopped! Resetting worlds...", NamedTextColor.YELLOW))
    }

    @Subcommand("complete")
    @Description("Complete the speedrun by simulating an Ender Dragon kill")
    fun onComplete(sender: CommandSender) {
        if (!plugin.gameService.isGameActive) {
            sender.sendMessage(Component.text("No speedrun is currently active! Start one first with /speedrun start", NamedTextColor.RED))
            return
        }

        val killer: Player? = sender as? Player ?: // fallback to first online player
        Bukkit.getOnlinePlayers().firstOrNull()

        // Fire custom dragon kill event to trigger real speedrun completion logic
        val event = DragonKillEvent(killer)
        Bukkit.getPluginManager().callEvent(event)

        sender.sendMessage(Component.text("Dragon kill event fired! Real speedrun completion logic will handle the rest.", NamedTextColor.GREEN))
    }
}