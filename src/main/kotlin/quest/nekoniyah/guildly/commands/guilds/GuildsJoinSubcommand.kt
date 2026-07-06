package quest.nekoniyah.guildly.commands.guilds

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import quest.nekoniyah.guildly.database.managers.guild.GuildManager
import quest.nekoniyah.guildly.database.managers.joinrequest.JoinRequestData
import quest.nekoniyah.guildly.database.managers.joinrequest.JoinRequestManager
import quest.nekoniyah.guildly.database.managers.player.PlayerManager
import quest.nekoniyah.guildly.utils.Feedback
import quest.nekoniyah.guildly.utils.command.GuildlyNodeCommand
import kotlin.random.Random

class GuildsJoinSubcommand : GuildlyNodeCommand() {
    override val name: String = "join"
    override val description: String = "Request to join a guild by name."
    override val definition: LiteralArgumentBuilder<CommandSourceStack?>? = Commands.literal(name).executes(::execute).then(Commands.argument("name", StringArgumentType.greedyString()).executes(::joinGuild))

    override fun execute(ctx: CommandContext<CommandSourceStack>): Int {
        Feedback(ctx).fail("Please provide a name for the guild to join.")
        return 1
    }

    fun joinGuild(ctx: CommandContext<CommandSourceStack>): Int {
        val feedback = Feedback(ctx)
        val argName = StringArgumentType.getString(ctx, "name")
        val playerId = ctx.source.player!!.stringUUID
        val targetGuild = GuildManager.getGuild(argName)
        if (targetGuild == null) {
            feedback.fail("A guild with that name doesn't exist!")
            return 0
        }
        val existingGuild = GuildManager.findGuildOf(playerId)
        if (existingGuild != null) {
            if (existingGuild.name == targetGuild.name) {
                feedback.fail(
                    if (existingGuild.ownerId == playerId) "You are the owner of that guild!"
                    else "You are already a member of that guild!"
                )
            } else feedback.fail("You already belong to the ${existingGuild.name}. Leave it before joining another!")
            return 0
        }
        val alreadyRequested = JoinRequestManager.loadedRequests.any { request -> request.userId == playerId && request.guildName == targetGuild.name }
        if (alreadyRequested) {
            feedback.fail("You already have a pending request to join ${targetGuild.name}.")
            return 0
        }
        JoinRequestManager.addRequest(
            JoinRequestData(
                id = Random.nextInt().toString(16),
                userId = playerId,
                guildName = targetGuild.name
            )
        )
        val requesterName = PlayerManager.getByUUID(playerId)?.name ?: "A player"
        PlayerManager.findOnlineByUUID(targetGuild.ownerId)?.sendSystemMessage(Feedback.build("$requesterName has requested to join your guild, ${targetGuild.name}. Use /guilds accept $requesterName to approve."))
        feedback.success("Successfully requested to join the ${targetGuild.name} guild!")
        return 1
    }
}
