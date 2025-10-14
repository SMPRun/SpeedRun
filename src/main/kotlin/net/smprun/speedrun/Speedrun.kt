package net.smprun.speedrun

import co.aikar.commands.PaperCommandManager
import net.smprun.common.CommonServices
import net.smprun.speedrun.game.GameService
import net.smprun.common.database.MongoService
import net.smprun.common.utils.RegistrationManager
import net.smprun.speedrun.scoreboard.SpeedrunScoreboard
import org.bukkit.plugin.java.JavaPlugin

class Speedrun : JavaPlugin() {

    lateinit var commandManager: PaperCommandManager
    private lateinit var registrationManager: RegistrationManager
    lateinit var mongoService: MongoService
    lateinit var gameService: GameService
    lateinit var scoreboardService: SpeedrunScoreboard

    override fun onEnable() {
        commandManager = PaperCommandManager(this)
        registrationManager = RegistrationManager(this, commandManager, basePackage = "net.smprun.speedrun")
        saveDefaultConfig()

        mongoService = CommonServices.mongo

        gameService = GameService(this)
        registrationManager.registerAll()

        scoreboardService = SpeedrunScoreboard(this)
        CommonServices.registerScoreboard(scoreboardService)
        
        logger.info("Speedrun plugin enabled!")
    }

    override fun onDisable() {
        // Scoreboard closed by Common automatically
        logger.info("Speedrun plugin disabled!")
    }
}