package quest.nekoniyah.guildly.commands.guilds

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import quest.nekoniyah.guildly.database.managers.guild.GuildManager
import quest.nekoniyah.guildly.database.managers.player.PlayerManager
import quest.nekoniyah.guildly.utils.Feedback
import quest.nekoniyah.guildly.utils.command.GuildlyNodeCommand
import java.util.Locale

class GuildsKickSubcommand : GuildlyNodeCommand() {
	override val name: String = "kick"
	override val description: String = "Removes a member from your guild."
	private val suggestions = SuggestionProvider<CommandSourceStack> { context, builder ->
		val ownerId = context.source.player?.stringUUID
		val ownedGuild = ownerId?.let { id -> GuildManager.loadedGuilds.find { it.ownerId == id } }
		ownedGuild?.playerIds?.mapNotNull { PlayerManager.getByUUID(it)?.name }?.forEach { memberName -> if (memberName.lowercase(Locale.ROOT).startsWith(builder.remainingLowerCase)) { builder.suggest(memberName) } }
		builder.buildFuture()
	}
	override val definition: LiteralArgumentBuilder<CommandSourceStack?>? = Commands.literal(name).executes(::execute).then(Commands.argument("player", StringArgumentType.string()).suggests(suggestions).executes(::kickMember))

	override fun execute(ctx: CommandContext<CommandSourceStack>): Int {
		Feedback(ctx).fail("Please provide a name of the member to kick.")
		return 1
	}

	fun kickMember(ctx: CommandContext<CommandSourceStack>): Int {
		val feedback = Feedback(ctx)
		val player = ctx.source.player ?: return 0
		val targetName = StringArgumentType.getString(ctx, "player")
		val ownedGuild = GuildManager.loadedGuilds.find { it.ownerId == player.stringUUID }
		if (ownedGuild == null) {
			feedback.fail("You don't own any guild!")
			return 0
		}
		val targetData = PlayerManager.getByName(targetName)
		if (targetData == null) {
			feedback.fail("This user never joined the game!")
			return 0
		}
		if (!ownedGuild.playerIds.contains(targetData.uuid)) {
			feedback.fail("$targetName is not a member of your guild!")
			return 0
		}
		val updatedGuild = ownedGuild.copy(playerIds = ownedGuild.playerIds.toMutableSet())
		updatedGuild.playerIds.remove(targetData.uuid)
		GuildManager.updateGuild(updatedGuild)
		PlayerManager.findOnlineByUUID(targetData.uuid)?.sendSystemMessage(Feedback.build("You have been kicked from the ${ownedGuild.name} guild."))
		feedback.success("$targetName has been kicked from ${ownedGuild.name}")
		return 1
	}
}
