package quest.nekoniyah.guildly.commands.guilds

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import quest.nekoniyah.guildly.database.managers.guild.GuildManager
import quest.nekoniyah.guildly.database.managers.joinrequest.JoinRequestManager
import quest.nekoniyah.guildly.database.managers.player.PlayerManager
import quest.nekoniyah.guildly.utils.Feedback
import quest.nekoniyah.guildly.utils.command.GuildlyNodeCommand
import java.util.Locale

class GuildsAcceptSubcommand : GuildlyNodeCommand() {
    override val name: String = "accept"
    override val description: String = "Accepts a pending join request for your guild."
    private val suggestions = SuggestionProvider<CommandSourceStack> { _, builder ->
        PlayerManager.onlinePlayers.mapNotNull { it.displayName?.string }.forEach { name -> if (name.lowercase(Locale.ROOT).startsWith(builder.remainingLowerCase)) builder.suggest(name) }
        builder.buildFuture()
    }
    override val definition: LiteralArgumentBuilder<CommandSourceStack?>? = Commands.literal(name).executes(::execute).then(Commands.argument("player", StringArgumentType.string()).suggests(suggestions).executes(::execute))

    override fun execute(ctx: CommandContext<CommandSourceStack>): Int {
        val feedback = Feedback(ctx)
        val player = ctx.source.player ?: return 0
        val playerName = StringArgumentType.getString(ctx, "player")
        val targetData = PlayerManager.getByName(playerName)
        if (targetData == null) {
            feedback.fail("This user never joined the game!")
            return 0
        }
        val foundOwnedGuild = GuildManager.loadedGuilds.find { guild -> guild.ownerId == player.stringUUID }
        if (foundOwnedGuild == null) {
            feedback.fail("You don't own any guild!")
            return 0
        }
        val request = JoinRequestManager.loadedRequests.find { request -> request.guildName == foundOwnedGuild.name && request.userId == targetData.uuid }
        if (request == null) {
            feedback.fail("There is no ongoing request for $playerName")
            return 0
        }
        JoinRequestManager.acceptRequest(targetData.uuid)
        return 1
    }
}
