package net.smprun.speedrun

import co.aikar.commands.PaperCommandManager
import net.smprun.common.CommonServices
import net.smprun.speedrun.game.GameService
import net.smprun.common.database.MongoService
import net.smprun.common.utils.RegistrationManager
import net.smprun.speedrun.game.AutoStartService
import net.smprun.speedrun.scoreboard.SpeedrunScoreboard
import org.bukkit.plugin.java.JavaPlugin

class Speedrun : JavaPlugin() {

    lateinit var commandManager: PaperCommandManager
    private lateinit var registrationManager: RegistrationManager
    lateinit var mongoService: MongoService
    lateinit var gameService: GameService
    lateinit var scoreboardService: SpeedrunScoreboard
    lateinit var autoStartService: AutoStartService

    override fun onEnable() {
        saveDefaultConfig()
        commandManager = PaperCommandManager(this)
        registrationManager = RegistrationManager(this, commandManager, basePackage = "net.smprun.speedrun")

        mongoService = CommonServices.mongo

        gameService = GameService(this)
        autoStartService = AutoStartService(this, gameService)
        autoStartService.start()

        registrationManager.registerAll()

        scoreboardService = SpeedrunScoreboard(this)
        CommonServices.registerScoreboard(scoreboardService)
        
        logger.info("Speedrun plugin enabled!")
    }

    override fun onDisable() {
        logger.info("Speedrun plugin disabled!")
    }
}
