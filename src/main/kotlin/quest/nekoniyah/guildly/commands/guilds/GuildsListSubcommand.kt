package quest.nekoniyah.guildly.commands.guilds

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import quest.nekoniyah.guildly.database.managers.guild.GuildManager
import quest.nekoniyah.guildly.utils.Feedback
import quest.nekoniyah.guildly.utils.command.GuildlyNodeCommand

class GuildsListSubcommand : GuildlyNodeCommand() {
    override val name: String = "list"
    override val description: String = "Lists all existing guilds and their member counts."
    override val definition: LiteralArgumentBuilder<CommandSourceStack?>? = Commands.literal(name).executes(::execute)

    override fun execute(ctx: CommandContext<CommandSourceStack>): Int {
        if (GuildManager.loadedGuilds.isEmpty()) {
            val feedback = Feedback.build("No guilds have been created yet")
            ctx.source.sendSuccess({ feedback }, false)
            return 1
        }

        GuildManager.loadedGuilds.forEach { guild ->
            val memberCount = guild.playerIds.size + 1

            val feedback = Feedback.build(guild.name + "\n")
                .append(
                    Component.literal("Members: ")
                        .withStyle(ChatFormatting.BOLD)
                )
                .append(
                    Component.literal(memberCount.toString())
                        .withStyle(ChatFormatting.GRAY)
                ).append("\n\n").append("Type ").append(
                    Component.literal("/guilds members [guildname]").withStyle(ChatFormatting.UNDERLINE)
                        .withStyle(ChatFormatting.GRAY)
                ).append(" to get the username list of the target guild name.")

            ctx.source.sendSuccess({ feedback }, false)
        }

        return 1
    }
}
