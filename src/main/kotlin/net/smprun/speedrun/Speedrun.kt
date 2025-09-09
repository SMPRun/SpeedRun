package net.smprun.speedrun

import co.aikar.commands.PaperCommandManager
import com.tcoded.folialib.FoliaLib
import net.smprun.speedrun.utils.RegistrationManager
import org.bukkit.plugin.java.JavaPlugin

class Speedrun : JavaPlugin() {

    lateinit var foliaLib: FoliaLib
    lateinit var commandManager: PaperCommandManager
    private lateinit var registrationManager: RegistrationManager

    override fun onEnable() {
        foliaLib = FoliaLib(this)
        commandManager = PaperCommandManager(this)
        registrationManager = RegistrationManager(this)
        
        registrationManager.registerAll()
        
        logger.info("Speedrun plugin enabled!")
    }

    override fun onDisable() {
        logger.info("Speedrun plugin disabled!")
    }
}
