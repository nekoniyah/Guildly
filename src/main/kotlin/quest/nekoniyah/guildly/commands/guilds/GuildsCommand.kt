package quest.nekoniyah.guildly.commands.guilds

import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import quest.nekoniyah.guildly.utils.command.GuildlyCoreCommand

class GuildsCommand : GuildlyCoreCommand() {
    override val definition = null
    override val name = "guilds"
    override fun execute(ctx: CommandContext<CommandSourceStack>): Int {
        return 1
    }

    override val subcommands = listOf(
        GuildsListSubcommand(),
        GuildsAddSubcommand(),
        GuildsLeaveSubcommand(),
        GuildsJoinSubcommand(),
        GuildsAcceptSubcommand(),
        GuildsKickSubcommand()
    )
}
