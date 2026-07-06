package quest.nekoniyah.guildly.commands.guilds

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import quest.nekoniyah.guildly.database.managers.guild.GuildManager
import quest.nekoniyah.guildly.database.managers.joinrequest.JoinRequestData
import quest.nekoniyah.guildly.database.managers.joinrequest.JoinRequestManager
import quest.nekoniyah.guildly.utils.Feedback
import quest.nekoniyah.guildly.utils.command.GuildlyNodeCommand
import kotlin.random.Random

class GuildsJoinSubcommand : GuildlyNodeCommand() {
    override val subcommands: List<GuildlyNodeCommand>? = null
    override val name: String = "join"
    override val definition: LiteralArgumentBuilder<CommandSourceStack?>? = Commands.literal(name).executes(::execute).then(Commands.argument("name", StringArgumentType.greedyString()).executes(::joinGuild))

    override fun execute(ctx: CommandContext<CommandSourceStack>): Int {
        Feedback(ctx).fail("Please provide a name for the guild to join.")
        return 1
    }

    fun joinGuild(ctx: CommandContext<CommandSourceStack>): Int {
        val feedback = Feedback(ctx)
        val argName = StringArgumentType.getString(ctx, "name")
        val playerId = ctx.source.player!!.stringUUID
        val foundExistingGuild = GuildManager.getGuild(argName)
        if (foundExistingGuild == null) {
            feedback.fail("A guild with that name doesn't exist!")
            return 0
        }
        if (foundExistingGuild.ownerId == playerId) {
            feedback.fail("You are the owner of the guild!")
            return 0
        }
        if (foundExistingGuild.playerIds.contains(playerId)) {
            feedback.fail("You are already a member of that guild!")
            return 0
        }
        JoinRequestManager.addRequest(
            JoinRequestData(
                id = Random.nextInt().toString(16),
                userId = playerId,
                guildName = argName
            )
        )
        feedback.success("Successfully requested to join the $argName guild!")
        return 1
    }
}
