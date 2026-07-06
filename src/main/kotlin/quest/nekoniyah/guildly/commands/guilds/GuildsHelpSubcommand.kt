package quest.nekoniyah.guildly.commands.guilds

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import quest.nekoniyah.guildly.utils.command.GuildlyNodeCommand

class GuildsHelpSubcommand(private val parent: GuildsCommand) : GuildlyNodeCommand() {
	override val subcommands: List<GuildlyNodeCommand>? = null
	override val name: String = "help"
	override val description: String = "Shows this help message."
	override val definition: LiteralArgumentBuilder<CommandSourceStack?>? = Commands.literal(name).executes(::execute)

	override fun execute(ctx: CommandContext<CommandSourceStack>): Int {
		ctx.source.sendSuccess({ Component.literal("--- Guildly Commands ---") }, false)
		parent.subcommands.forEach { subcommand ->
			val message = Component.literal("/guilds ${subcommand.name} - ${subcommand.description}")
			ctx.source.sendSuccess({ message }, false)
		}
		return 1
	}
}
