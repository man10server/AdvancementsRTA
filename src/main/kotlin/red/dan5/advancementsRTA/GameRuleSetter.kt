package red.dan5.advancementsRTA

import org.bukkit.GameRule
import org.bukkit.Server
import java.util.logging.Logger

class GameRuleSetter(private val server: Server, private val logger: Logger) {
    private val originalAnnounceAdvancementValues = mutableMapOf<String, Boolean>()

    fun set() {
        // ゲームルールを設定する (進捗達成時のアナウンス無効化)
        server.worlds.forEach { world ->
            world.getGameRuleValue(GameRule.ANNOUNCE_ADVANCEMENTS)?.let { originalValue ->
                originalAnnounceAdvancementValues[world.name] = originalValue
                world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
                logger.info("Disabled advancement announcements for world: ${world.name}")
            }
        }
    }

    fun restore() {
        // ゲームルールを復元する
        originalAnnounceAdvancementValues.forEach { (worldName, originalValue) ->
            server.getWorld(worldName)?.let { world ->
                world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, originalValue)
                logger.info("Restored advancement announcements for world: $worldName to: $originalValue")
            }
        }
        originalAnnounceAdvancementValues.clear()
    }
}