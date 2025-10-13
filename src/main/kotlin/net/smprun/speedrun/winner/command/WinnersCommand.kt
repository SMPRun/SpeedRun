package net.smprun.speedrun.winner.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.format.NamedTextColor
import net.smprun.common.utils.Colors
import net.smprun.common.utils.Text
import net.smprun.speedrun.Speedrun
import net.smprun.common.annotations.AutoRegister
import net.smprun.common.utils.TimeUtil
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
            Text.error(sender, "Cannot view winners while a game is active!")
            return
        }
        plugin.foliaLib.scheduler.runAsync { _ ->
            try {
                val list = runBlocking { repo.listAll() }
                if (list.winners.isEmpty()) {
                    Text.info(sender, "No winners recorded yet.")
                    return@runAsync
                }
                Text.component(sender, Text.header("Winners"))
                list.winners.forEachIndexed { index, w ->
                    val formattedTime = TimeUtil.formatTime(w.winTime)
                    Text.raw(sender, "${index + 1}. ${w.username} - $formattedTime", NamedTextColor.WHITE)
                }
                Text.component(sender, Text.footer())
            } catch (e: Exception) {
                Text.error(sender, "Failed to fetch winners: ${e.message}")
            }
        }
    }

    @Subcommand("best")
    @Description("Show the best time winner")
    fun onBest(sender: CommandSender) {
        if (gameService.isGameActive) {
            Text.error(sender, "Cannot view best time while a game is active!")
            return
        }
        plugin.foliaLib.scheduler.runAsync { _ ->
            try {
                val list = runBlocking { repo.listAll() }
                val best = list.getBestTimeWinner()
                if (best == null) {
                    Text.info(sender, "No winners recorded yet.")
                } else {
                    val formattedTime = TimeUtil.formatTime(best.winTime)
                    Text.colored(sender, "Best time: ${best.username} - $formattedTime", Colors.BRAND_PRIMARY)
                }
            } catch (e: Exception) {
                Text.error(sender, "Failed to fetch best winner: ${e.message}")
            }
        }
    }
}