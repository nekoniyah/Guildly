package quest.nekoniyah.guildly.commands.guilds

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import quest.nekoniyah.guildly.utils.Cache
import quest.nekoniyah.guildly.utils.Feedback
import quest.nekoniyah.guildly.utils.database.guild.GuildManager
import quest.nekoniyah.guildly.utils.command.GuildlyNodeCommand
import quest.nekoniyah.guildly.utils.database.joinrequest.JoinRequestManager
import java.util.Locale

class GuildsAcceptSubcommand : GuildlyNodeCommand() {
    override val subcommands = null
    override val name: String = "accept"

    private val PLAYER_SUGGESTIONS = SuggestionProvider<CommandSourceStack> { context, builder ->
        val names = Cache.players.map { it.displayName!!.string }

        names.forEach { name ->
            if (name.lowercase(Locale.ROOT).startsWith(builder.remainingLowerCase)) {
                builder.suggest(name)
            }
        }
        builder.buildFuture()
    }

    override val definition = Commands.literal(name).executes(::execute).then(
        Commands.argument(
            "player", StringArgumentType.string()
        ).suggests(PLAYER_SUGGESTIONS).executes(::execute)
    )

    override fun execute(ctx: CommandContext<CommandSourceStack>): Int {
        val feedback = Feedback(ctx)
        val player = ctx.source.player ?: return 0

        val playerName = StringArgumentType.getString(ctx, "player")

        val targetPlayer = Cache.players.find { it.displayName!!.string == playerName }

        if (targetPlayer == null) {
            feedback.fail("This user never joined the game!")
            return 0
        }

        val foundOwnedGuild = GuildManager.loadedGuilds.find { g ->
            g.ownerId == player.stringUUID
        }

        if (foundOwnedGuild == null) {
            feedback.fail("You don't own any guild!")
            return 0
        }

        val request = JoinRequestManager.loadedRequests.find { r ->
            r.guildName == foundOwnedGuild.name && r.userId == targetPlayer.stringUUID
        }

        if (request == null) {
            feedback.fail("There is no ongoing request for $playerName")
            return 0
        }

        JoinRequestManager.acceptRequest(ctx.source.server, targetPlayer.stringUUID)

        return 1
    }
}