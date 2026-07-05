package quest.nekoniyah.guildly

import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.event.server.ServerStoppingEvent
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import quest.nekoniyah.guildly.commands.guilds.GuildsCommand
import quest.nekoniyah.guildly.utils.GuildManager

/**
 * Main mod class.
 *
 * An example for blocks is in the `blocks` package of this mod.
 */
@Mod(Guildly.ID)
@EventBusSubscriber(value = [Dist.DEDICATED_SERVER])
object Guildly {
    const val ID = "guildly"

    // the logger for our mod
    val LOGGER: Logger = LogManager.getLogger(ID)

    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        LOGGER.log(Level.INFO, "Registering Guildly commands...")
        GuildsCommand().register(event.dispatcher)
    }

    @SubscribeEvent
    fun onServerStarting(event: ServerStartingEvent) {
        GuildManager.loadAll()
        LOGGER.log(Level.INFO, "Hello! This is working!")
    }

    @SubscribeEvent
    fun onServerStop(event: ServerStoppingEvent) {
        GuildManager.saveAll()
    }
}
