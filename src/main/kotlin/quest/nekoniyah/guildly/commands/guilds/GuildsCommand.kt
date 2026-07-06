package quest.nekoniyah.guildly.commands.guilds

import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import quest.nekoniyah.guildly.utils.command.GuildlyCoreCommand
import quest.nekoniyah.guildly.utils.command.GuildlyNodeCommand

class GuildsCommand : GuildlyCoreCommand() {
    override val name = "guilds"

    override fun execute(ctx: CommandContext<CommandSourceStack>): Int {
        return subcommands.first { it.name == "help" }.execute(ctx)
    }

    override val subcommands: List<GuildlyNodeCommand> by lazy {
        listOf(
            GuildsHelpSubcommand(this),
            GuildsListSubcommand(),
            GuildsAddSubcommand(),
            GuildsLeaveSubcommand(),
            GuildsJoinSubcommand(),
            GuildsAcceptSubcommand(),
            GuildsKickSubcommand()
        )
    }
}
