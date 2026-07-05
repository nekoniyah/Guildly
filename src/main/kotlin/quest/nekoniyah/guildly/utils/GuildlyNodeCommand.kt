package quest.nekoniyah.guildly.utils

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack

abstract class GuildlyNodeCommand {
    abstract val name: String
    abstract val definition: LiteralArgumentBuilder<CommandSourceStack?>?
    abstract fun execute(ctx: CommandContext<CommandSourceStack>): Int
    abstract val subcommands: List<GuildlyNodeCommand>?
}