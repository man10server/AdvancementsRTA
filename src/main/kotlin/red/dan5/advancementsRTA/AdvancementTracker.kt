package red.dan5.advancementsRTA

import io.papermc.paper.advancement.AdvancementDisplay
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
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
            // Initialize from Bukkit advancementIterator
            val advancements = Bukkit.advancementIterator().asSequence()
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
            val advancement = Bukkit.getAdvancement(key)
            
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
    
    /**
     * Checks if the player is the first to achieve this advancement and records it if so.
     * 
     * @param advancement The advancement being granted
     * @param player The player who achieved the advancement
     * @return Optional<Boolean> with three possible states:
     *         - Optional.of(true): Player is the FIRST to get this advancement (recorded and flushed)
     *         - Optional.of(false): Someone else already has this advancement
     *         - Optional.empty(): This advancement is not being tracked (e.g., recipe advancements)
     */
    fun grantAdvancementAndCheckIsFirst(advancement: org.bukkit.advancement.Advancement, player: org.bukkit.entity.Player): Optional<Boolean> {
        val key = advancement.key
        val playerUUID = player.uniqueId

        val currentValue =
            firstPlayers[key] ?: // This advancement is not tracked (likely a recipe or other excluded advancement)
            return Optional.empty()

        if (currentValue.isPresent) {
            // Someone already has this advancement
            return Optional.of(false)
        }
        
        // This player is the first to achieve this advancement
        firstPlayers[key] = Optional.of(playerUUID)
        flush()
        return Optional.of(true)
    }
}