package quest.nekoniyah.guildly.commands.guilds

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import quest.nekoniyah.guildly.database.managers.guild.GuildManager
import quest.nekoniyah.guildly.utils.command.GuildlyNodeCommand

class GuildsListSubcommand : GuildlyNodeCommand() {
    override val subcommands: List<GuildlyNodeCommand>? = null
    override val name: String = "list"
    override val definition: LiteralArgumentBuilder<CommandSourceStack?>? = Commands.literal(name).executes(::execute)

    override fun execute(ctx: CommandContext<CommandSourceStack>): Int {
        GuildManager.loadedGuilds.forEach { guild -> ctx.source.sendSuccess({ Component.literal(guild.name) }, false) }
        return 1
    }
}
