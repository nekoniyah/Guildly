package quest.nekoniyah.guildly.commands.guilds

import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import quest.nekoniyah.guildly.utils.database.guild.GuildManager
import quest.nekoniyah.guildly.utils.command.GuildlyNodeCommand

class GuildsListSubcommand : GuildlyNodeCommand() {
    override val subcommands = null
    override val name: String = "list"
    override val definition = Commands.literal(name).executes(::execute)

    override fun execute(ctx: CommandContext<CommandSourceStack>): Int {
        GuildManager.loadedGuilds.forEach { guild ->
            val message = Component.literal(guild.name)
            ctx.source.sendSuccess({ message }, false)
        }

        return 1
    }
}