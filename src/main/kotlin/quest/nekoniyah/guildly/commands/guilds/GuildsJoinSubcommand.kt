package quest.nekoniyah.guildly.commands.guilds

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import quest.nekoniyah.guildly.utils.database.guild.GuildManager
import quest.nekoniyah.guildly.utils.Feedback
import quest.nekoniyah.guildly.utils.command.GuildlyNodeCommand
import quest.nekoniyah.guildly.utils.database.joinrequest.JoinRequestData
import quest.nekoniyah.guildly.utils.database.joinrequest.JoinRequestManager
import kotlin.random.Random

class GuildsJoinSubcommand : GuildlyNodeCommand() {
    override val subcommands = null
    override val name: String = "join"
    override val definition =
        Commands.literal(name).executes(::execute).then(
            Commands
                .argument(
                    "name",
                    StringArgumentType.greedyString()
                )
                .executes(::joinGuild)
        )

    override fun execute(ctx: CommandContext<CommandSourceStack>): Int {
        Feedback(ctx).fail("Please provide a name for the guild to join.")
        return 1
    }

    fun joinGuild(ctx: CommandContext<CommandSourceStack>): Int {
        val feedback = Feedback(ctx)

        val argName = StringArgumentType.getString(ctx, "name")

        val foundExistingGuild = GuildManager.loadedGuilds.find { g ->
            g.name == argName
        }

        if (foundExistingGuild == null) {
            feedback.fail("A guild with that name doesn't exist!")
            return 0
        }

        if (foundExistingGuild.ownerId == ctx.source.player!!.stringUUID) {
            feedback.fail("You are the owner of that guild!")
            return 0
        }

        if (foundExistingGuild.playerIds.contains(ctx.source.player!!.stringUUID)) {
            feedback.fail("You are already a member of that guild!")
            return 0
        }

        JoinRequestManager.addRequest(JoinRequestData(Random.nextInt().toHexString(), ctx.source.player!!.stringUUID, argName))

        feedback.success("Successfully requested to join the $argName guild!")

        return 1
    }
}