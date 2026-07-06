package quest.nekoniyah.guildly.utils.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack

abstract class GuildlyNodeCommand {
    abstract val name: String
    open val definition: LiteralArgumentBuilder<CommandSourceStack?>? = null
    abstract fun execute(ctx: CommandContext<CommandSourceStack>): Int
    open val subcommands: List<GuildlyNodeCommand>? = null
    open val description: String = "No description available."
}
