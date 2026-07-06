package quest.nekoniyah.guildly

import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.event.server.ServerStoppingEvent
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import quest.nekoniyah.guildly.commands.guilds.GuildsCommand
import quest.nekoniyah.guildly.utils.Cache
import quest.nekoniyah.guildly.utils.database.Database
import quest.nekoniyah.guildly.utils.database.guild.GuildManager
import quest.nekoniyah.guildly.utils.database.joinrequest.JoinRequestManager
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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
    fun onPlayerJoinServer(event: PlayerEvent.PlayerLoggedInEvent){
        Cache.players.add(event.entity)
    }

    @SubscribeEvent
    fun onServerStarting(event: ServerStartingEvent) {
        Database.init()
        GuildManager.loadAll()
        JoinRequestManager.loadAll()

        val scheduler = Executors.newScheduledThreadPool(1)

        scheduler.scheduleAtFixedRate({
          GuildManager.saveAll()
            JoinRequestManager.saveAll()
        }, 0, 1, TimeUnit.HOURS)

        LOGGER.log(Level.INFO, "Hello! This is working!")
    }

    @SubscribeEvent
    fun onServerStop(event: ServerStoppingEvent) {
        JoinRequestManager.saveAll()
        GuildManager.saveAll()
    }
}
