package quest.nekoniyah.guildly.commands.guilds

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import quest.nekoniyah.guildly.database.managers.guild.GuildManager
import quest.nekoniyah.guildly.utils.Feedback
import quest.nekoniyah.guildly.utils.command.GuildlyNodeCommand

class GuildsLeaveSubcommand : GuildlyNodeCommand() {
    override val subcommands: List<GuildlyNodeCommand>? = null
    override val name: String = "leave"
    override val description: String = "Leaves your current guild, or deletes it if you're the owner and it's empty."
    override val definition: LiteralArgumentBuilder<CommandSourceStack?>? = Commands.literal(name).executes(::execute)

    override fun execute(ctx: CommandContext<CommandSourceStack>): Int {
        val feedback = Feedback(ctx)
        val player = ctx.source.player ?: return 0
        val foundOwnedGuild = GuildManager.loadedGuilds.find { guild -> guild.ownerId == player.stringUUID }
        if (foundOwnedGuild != null) {
            if (foundOwnedGuild.playerIds.isEmpty()) {
                GuildManager.deleteGuild(foundOwnedGuild.name)
                feedback.success("Your guild had no members and has successfully been deleted!")
                return 1
            }
            feedback.fail("You can't leave a guild where you are the owner and there are still members in it!")
            return 0
        }
        val foundMemberGuild = GuildManager.loadedGuilds.find { guild -> guild.playerIds.contains(player.stringUUID) }
        if (foundMemberGuild == null) {
            feedback.fail("You are not a member of a guild!")
            return 0
        }
        val updatedGuild = foundMemberGuild.copy(playerIds = foundMemberGuild.playerIds.toMutableSet())
        updatedGuild.playerIds.remove(player.stringUUID)
        GuildManager.updateGuild(updatedGuild)
        feedback.success("Successfully left ${updatedGuild.name}!")
        return 1
    }
}
