package quest.nekoniyah.guildly.commands.guilds

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import quest.nekoniyah.guildly.database.managers.guild.GuildManager
import quest.nekoniyah.guildly.utils.command.GuildlyNodeCommand

class GuildsListSubcommand : GuildlyNodeCommand() {
    override val name: String = "list"
    override val description: String = "Lists all existing guilds and their member counts."
    override val definition: LiteralArgumentBuilder<CommandSourceStack?>? = Commands.literal(name).executes(::execute)

    override fun execute(ctx: CommandContext<CommandSourceStack>): Int {
        if (GuildManager.loadedGuilds.isEmpty()) {
            ctx.source.sendSuccess({ Component.literal("No guilds have been created yet") }, false)
            return 1
        }
        GuildManager.loadedGuilds.forEach { guild ->
            val memberCount = guild.playerIds.size + 1
            val message = Component.literal("${guild.name}  - $memberCount member${if (memberCount != 1) "s" else ""}")
            ctx.source.sendSuccess({ message }, false)
        }
        return 1
    }
}
