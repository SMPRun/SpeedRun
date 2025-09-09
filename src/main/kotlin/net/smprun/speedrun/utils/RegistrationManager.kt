package net.smprun.speedrun.utils

import co.aikar.commands.BaseCommand
import net.smprun.speedrun.Speedrun
import net.smprun.speedrun.annotations.AutoRegister
import org.bukkit.event.Listener
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder

class RegistrationManager(private val plugin: Speedrun) {

    fun registerAll() {
        try {
            val reflections = Reflections(
                ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forPackage("net.smprun.speedrun"))
                    .setScanners(Scanners.TypesAnnotated)
            )

            val annotatedClasses = reflections.getTypesAnnotatedWith(AutoRegister::class.java)

            annotatedClasses.forEach { clazz ->
                try {
                    registerClass(clazz)
                } catch (e: Exception) {
                    plugin.logger.warning("Failed to register class ${clazz.simpleName}: ${e.message}")
                }
            }

            plugin.logger.info("Auto-registered ${annotatedClasses.size} classes")
        } catch (e: Exception) {
            plugin.logger.severe("Failed to initialize auto-registration: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun registerClass(clazz: Class<*>) {
        val instance = try {
            // Try constructor with plugin parameter first
            val constructor = clazz.getDeclaredConstructor(Speedrun::class.java)
            constructor.newInstance(plugin)
        } catch (_: NoSuchMethodException) {
            // Fall back to no-args constructor
            try {
                clazz.getDeclaredConstructor().newInstance()
            } catch (_: Exception) {
                plugin.logger.warning("Failed to instantiate ${clazz.simpleName}: No suitable constructor found")
                return
            }
        }

        when (instance) {
            is BaseCommand -> {
                plugin.commandManager.registerCommand(instance)
                plugin.logger.info("Registered command: ${clazz.simpleName}")
            }

            is Listener -> {
                plugin.server.pluginManager.registerEvents(instance, plugin)
                plugin.logger.info("Registered listener: ${clazz.simpleName}")
            }

            else -> {
                plugin.logger.warning("Class ${clazz.simpleName} has @AutoRegister but is not a Command or Listener")
            }
        }
    }
}