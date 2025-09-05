package red.dan5.advancementsRTA

import io.papermc.paper.advancement.AdvancementDisplay
import org.bukkit.Bukkit
import org.bukkit.Registry
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.Optional
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger


class AdvancementTracker(private val logger: Logger, private val plugin: JavaPlugin) {
    private val firstPlayers = ConcurrentHashMap<String, Optional<UUID>>()
    private val resultFile = File(plugin.dataFolder, "result.yaml")
    
    fun initialize() {
        // Ensure plugin data folder exists
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }
        
        // Initialize map with all advancement keys
        @Suppress("DEPRECATION") val advancements = Registry.ADVANCEMENT.stream()
            .filter { advancement ->
                val key = advancement.key.toString()
                !key.startsWith("minecraft:recipes/") && !key.startsWith("minecraft:recipes")
            }
            .toList()
        
        // Initialize all keys with empty Optional values
        advancements.forEach { advancement ->
            firstPlayers[advancement.key.toString()] = Optional.empty()
        }
        
        // Check if result.yaml exists and restore or flush accordingly
        if (resultFile.exists()) {
            restore()
        } else {
            flush()
        }
        
        val frameGroups = advancements.groupBy { advancement ->
            advancement.display?.frame()
        }
        
        logger.info("=== Advancement Statistics ===")
        frameGroups.forEach { (frame, list) ->
            val frameText = when (frame) {
                AdvancementDisplay.Frame.TASK -> "TASK"
                AdvancementDisplay.Frame.GOAL -> "GOAL"
                AdvancementDisplay.Frame.CHALLENGE -> "CHALLENGE"
                null -> "NO FRAME"
            }
            logger.info("$frameText: ${list.size} advancements")
        }
        
        val total = advancements.size
        logger.info("Total: $total advancements")
        logger.info("==============================")
    }

    // Write firstPlayers to result.yaml
    fun flush() {
        val yaml = YamlConfiguration()
        
        firstPlayers.forEach { (key, uuidOpt) ->
            yaml.set(key, uuidOpt.map { it.toString() }.orElse(null))
        }
        
        try {
            yaml.save(resultFile)
            logger.info("Saved advancement data to result.yaml")
        } catch (e: Exception) {
            logger.severe("Failed to save result.yaml: ${e.message}")
        }
    }

    // Restore firstPlayers from result.yaml
    fun restore() {
        try {
            val yaml = YamlConfiguration.loadConfiguration(resultFile)
            
            firstPlayers.keys.forEach { key ->
                val uuidString = yaml.getString(key)
                firstPlayers[key] = if (uuidString != null) {
                    Optional.of(UUID.fromString(uuidString))
                } else {
                    Optional.empty()
                }
            }
            
            logger.info("Restored advancement data from result.yaml")
        } catch (e: Exception) {
            logger.severe("Failed to load result.yaml: ${e.message}")
        }
    }
}