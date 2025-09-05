package red.dan5.advancementsRTA

import io.papermc.paper.advancement.AdvancementDisplay
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.Optional
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger


class AdvancementTracker(private val logger: Logger, private val plugin: JavaPlugin) {
    private val firstPlayers = ConcurrentHashMap<NamespacedKey, Optional<UUID>>()
    private val resultFile = File(plugin.dataFolder, "result.yaml")
    
    fun initialize() {
        // Ensure plugin data folder exists
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }
        
        // Check if result.yaml exists
        if (resultFile.exists()) {
            // Restore from existing file
            restore()
        } else {
            // Initialize from Registry
            @Suppress("DEPRECATION") val advancements = Registry.ADVANCEMENT.stream()
                .filter { advancement -> advancement.display?.doesAnnounceToChat() ?: false }
                .toList()
            
            // Initialize all keys with empty Optional values
            advancements.forEach { advancement ->
                firstPlayers[advancement.key] = Optional.empty()
            }
            
            // Save initial state
            flush()
        }
        
        // Generate statistics from firstPlayers keys
        val frameGroups = mutableMapOf<AdvancementDisplay.Frame, Int>()
        
        firstPlayers.keys.forEach { key ->
            @Suppress("DEPRECATION") 
            val advancement = Registry.ADVANCEMENT.get(key)
            
            if (advancement == null) {
                logger.severe("ERROR: Advancement not found in registry: $key")
                throw IllegalStateException("Advancement not found in registry: $key")
            }
            
            val frame = advancement.display?.frame()
            
            if (frame == null) {
                logger.severe("ERROR: Advancement has no display frame: $key")
                throw IllegalStateException("Advancement has no display frame: $key")
            }
            
            frameGroups[frame] = frameGroups.getOrDefault(frame, 0) + 1
        }
        
        logger.info("=== Advancement Statistics ===")
        frameGroups.forEach { (frame, count) ->
            val frameText = when (frame) {
                AdvancementDisplay.Frame.TASK -> "TASK"
                AdvancementDisplay.Frame.GOAL -> "GOAL"
                AdvancementDisplay.Frame.CHALLENGE -> "CHALLENGE"
            }
            logger.info("$frameText: $count advancements")
        }
        
        val total = firstPlayers.size
        logger.info("Total: $total advancements")
        logger.info("==============================")
    }

    // Write firstPlayers to result.yaml
    fun flush() {
        val yaml = YamlConfiguration()
        
        firstPlayers.forEach { (key, uuidOpt) ->
            yaml.set(key.toString(), uuidOpt.map { it.toString() }.orElse(""))
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
            
            // Iterate through all keys in the YAML file
            yaml.getKeys(false).forEach { keyString ->
                val namespacedKey = NamespacedKey.fromString(keyString)
                if (namespacedKey != null) {
                    val uuidString = yaml.getString(keyString)
                    firstPlayers[namespacedKey] = if (!uuidString.isNullOrEmpty()) {
                        Optional.of(UUID.fromString(uuidString))
                    } else {
                        Optional.empty()
                    }
                }
            }
            
            logger.info("Restored advancement data from result.yaml")
        } catch (e: Exception) {
            logger.severe("Failed to load result.yaml: ${e.message}")
        }
    }
}