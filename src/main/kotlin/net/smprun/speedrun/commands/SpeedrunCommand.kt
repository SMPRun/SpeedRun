package net.smprun.speedrun.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.smprun.speedrun.Speedrun
import net.smprun.speedrun.annotations.AutoRegister
import net.smprun.speedrun.game.WorldResetService
import org.bukkit.command.CommandSender

@AutoRegister
@CommandAlias("speedrun|sr")
@CommandPermission("speedrun.admin")
class SpeedrunCommand(plugin: Speedrun) : BaseCommand() {

    private val resetService = WorldResetService(plugin)

    @Subcommand("complete")
    @Description("Complete the speedrun and reset worlds with new seeds")
    fun onComplete(sender: CommandSender) {
        sender.sendMessage(Component.text("Speedrun completed! Resetting worlds with new seeds and restarting server...", NamedTextColor.GOLD))
        resetService.resetAllWorlds(
            kickReason = Component.text("Speedrun completed! Server is restarting with fresh worlds and new seeds.", NamedTextColor.GREEN)
        )
    }
}


