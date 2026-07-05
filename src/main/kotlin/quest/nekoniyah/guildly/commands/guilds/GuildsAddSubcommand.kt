package quest.nekoniyah.guildly.commands.guilds

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.network.chat.Component
import quest.nekoniyah.guildly.utils.GuildManager
import quest.nekoniyah.guildly.Guildly
import quest.nekoniyah.guildly.utils.Feedback
import quest.nekoniyah.guildly.utils.GuildlyNodeCommand

class GuildsAddSubcommand : GuildlyNodeCommand() {
    override val subcommands = null
    override val name: String = "add"
    override val definition =
        Commands.literal(name).executes(::execute).then(
            Commands
                .argument(
                    "name",
                    StringArgumentType.greedyString()
                )
                .executes(::addGuild)
        )

    override fun execute(ctx: CommandContext<CommandSourceStack>): Int {
        Feedback(ctx).fail("Please provide a name for your guild!")
        return 1
    }

    fun addGuild(ctx: CommandContext<CommandSourceStack>): Int {
        val argName = StringArgumentType.getString(ctx, "name")

        if(!GuildManager.isValidName(argName)) {
            Feedback(ctx).fail("Please provide a valid name for your guild!")
            return 0
        }

        return 1
    }
}