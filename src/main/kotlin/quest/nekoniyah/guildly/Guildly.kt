package quest.nekoniyah.guildly

import net.minecraft.server.level.ServerPlayer
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.EventBusSubscriber
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.event.server.ServerStoppingEvent
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import quest.nekoniyah.guildly.commands.guilds.GuildsCommand
import quest.nekoniyah.guildly.database.core.Database
import quest.nekoniyah.guildly.database.managers.guild.GuildManager
import quest.nekoniyah.guildly.database.managers.joinrequest.JoinRequestManager
import quest.nekoniyah.guildly.database.managers.player.PlayerManager
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Mod(Guildly.ID, dist = [Dist.DEDICATED_SERVER])
@EventBusSubscriber
object Guildly {
    const val ID = "guildly"
    val LOGGER: Logger = LogManager.getLogger(ID)

    @SubscribeEvent
    fun onLoad(event: FMLCommonSetupEvent) {
        LOGGER.log(Level.INFO, "Mod setup!")
    }

    @SubscribeEvent
    fun onServerStarting(event: ServerStartingEvent) {
        Database.init()
        PlayerManager.loadAll()
        GuildManager.loadAll()
        JoinRequestManager.loadAll()
        Executors.newScheduledThreadPool(1).scheduleAtFixedRate({
            PlayerManager.saveAll()
            GuildManager.saveAll()
            JoinRequestManager.saveAll()
        }, 1, 1, TimeUnit.HOURS)
        LOGGER.info("Guildly database initialized.")
    }

    @SubscribeEvent
    fun onServerStop(event: ServerStoppingEvent) {
        PlayerManager.saveAll()
        GuildManager.saveAll()
        JoinRequestManager.saveAll()
    }

    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        LOGGER.log(Level.INFO, "Registering Guildly commands...")
        GuildsCommand().register(event.dispatcher)
    }

    @SubscribeEvent
    fun onPlayerJoinServer(event: PlayerEvent.PlayerLoggedInEvent) {
        val player = event.entity as? ServerPlayer ?: return
        PlayerManager.add(player)
    }

    @SubscribeEvent
    fun onPlayerLeaveServer(event: PlayerEvent.PlayerLoggedOutEvent) {
        val player = event.entity as? ServerPlayer ?: return
        PlayerManager.remove(player)
        PlayerManager.saveAll()
    }
}