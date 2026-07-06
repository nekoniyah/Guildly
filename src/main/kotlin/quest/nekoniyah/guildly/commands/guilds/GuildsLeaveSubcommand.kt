package quest.nekoniyah.guildly.commands.guilds

import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import quest.nekoniyah.guildly.utils.Feedback
import quest.nekoniyah.guildly.utils.database.guild.GuildManager
import quest.nekoniyah.guildly.utils.command.GuildlyNodeCommand

class GuildsLeaveSubcommand : GuildlyNodeCommand() {
    override val subcommands = null
    override val name: String = "leave"
    override val definition = Commands.literal(name).executes(::execute)

    override fun execute(ctx: CommandContext<CommandSourceStack>): Int {
        val feedback = Feedback(ctx)
        val player = ctx.source.player ?: return 0


        val foundOwnedGuild = GuildManager.loadedGuilds.find { g ->
            g.ownerId == player.stringUUID
        }

        if (foundOwnedGuild != null) {
            if (foundOwnedGuild.playerIds.isEmpty()) {
                GuildManager.deleteFromDatabase(foundOwnedGuild.name)
                GuildManager.loadedGuilds.remove(foundOwnedGuild)
                feedback.success("Your guild had no members and has successfully been deleted!")
                return 1
            } else {
                feedback.fail("You can't leave a guild where you are the owner!")
                return 0
            }
        }

        val foundMemberGuild = GuildManager.loadedGuilds.find { g ->
            g.playerIds.contains(player.stringUUID)
        }

        if (foundMemberGuild == null) {
            feedback.fail("You are not member of a guild!")
            return 0
        }

        val dupGuild = foundMemberGuild.copy()
        GuildManager.loadedGuilds.remove(foundMemberGuild)
        dupGuild.playerIds.remove(player.stringUUID)
        GuildManager.loadedGuilds.add(dupGuild)

        feedback.success("Successfully left ${dupGuild.name}!")

        return 1
    }
}