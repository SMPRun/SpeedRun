package net.smprun.speedrun.winner.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.smprun.speedrun.Speedrun
import net.smprun.speedrun.annotations.AutoRegister
import net.smprun.speedrun.utils.TimeUtil
import net.smprun.speedrun.winner.repository.WinnerRepository
import org.bukkit.command.CommandSender

@AutoRegister
@CommandAlias("winners|srw")
class WinnersCommand(private val plugin: Speedrun) : BaseCommand() {

    private val repo by lazy { WinnerRepository(plugin) }
    private val gameService by lazy { plugin.gameService }

    @Subcommand("list")
    @Description("List all winners")
    fun onList(sender: CommandSender) {
        if (gameService.isGameActive) {
            sender.sendMessage(Component.text("Cannot view winners while a game is active!", NamedTextColor.RED))
            return
        }
        plugin.foliaLib.scheduler.runAsync { _ ->
            try {
                val list = runBlocking { repo.listAll() }
                if (list.winners.isEmpty()) {
                    sender.sendMessage(Component.text("No winners recorded yet.", NamedTextColor.GRAY))
                    return@runAsync
                }
                sender.sendMessage(Component.text("Winners:", NamedTextColor.GOLD))
                list.winners.forEachIndexed { index, w ->
                    val formattedTime = TimeUtil.formatTime(w.winTime)
                    sender.sendMessage(Component.text("${index + 1}. ${w.username} - $formattedTime", NamedTextColor.WHITE))
                }
            } catch (e: Exception) {
                sender.sendMessage(Component.text("Failed to fetch winners: ${e.message}", NamedTextColor.RED))
            }
        }
    }

    @Subcommand("best")
    @Description("Show the best time winner")
    fun onBest(sender: CommandSender) {
        if (gameService.isGameActive) {
            sender.sendMessage(Component.text("Cannot view best time while a game is active!", NamedTextColor.RED))
            return
        }
        plugin.foliaLib.scheduler.runAsync { _ ->
            try {
                val list = runBlocking { repo.listAll() }
                val best = list.getBestTimeWinner()
                if (best == null) {
                    sender.sendMessage(Component.text("No winners recorded yet.", NamedTextColor.GRAY))
                } else {
                    val formattedTime = TimeUtil.formatTime(best.winTime)
                    sender.sendMessage(Component.text("Best time: ${best.username} - $formattedTime", NamedTextColor.GOLD))
                }
            } catch (e: Exception) {
                sender.sendMessage(Component.text("Failed to fetch best winner: ${e.message}", NamedTextColor.RED))
            }
        }
    }
}