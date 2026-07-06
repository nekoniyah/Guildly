package quest.nekoniyah.guildly.commands.guilds

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import quest.nekoniyah.guildly.database.managers.guild.GuildData
import quest.nekoniyah.guildly.database.managers.guild.GuildManager
import quest.nekoniyah.guildly.utils.Feedback
import quest.nekoniyah.guildly.utils.command.GuildlyNodeCommand

class GuildsAddSubcommand : GuildlyNodeCommand() {
    override val subcommands: List<GuildlyNodeCommand>? = null
    override val name: String = "add"
    override val description: String = "Creates a new guild owned by you."
    override val definition: LiteralArgumentBuilder<CommandSourceStack?>? = Commands.literal(name).executes(::execute).then(Commands.argument("name", StringArgumentType.greedyString()).executes(::addGuild))

    override fun execute(ctx: CommandContext<CommandSourceStack>): Int {
        Feedback(ctx).fail("Please provide a name for your guild!")
        return 1
    }

    fun addGuild(ctx: CommandContext<CommandSourceStack>): Int {
        val feedback = Feedback(ctx)
        val argName = StringArgumentType.getString(ctx, "name")
        val playerId = ctx.source.player!!.stringUUID
        if (GuildManager.exists(argName)) {
            feedback.fail("A guild with that name already exists.")
            return 0
        }
        val existingGuild = GuildManager.findGuildOf(playerId)
        if (existingGuild != null) {
            if (existingGuild.ownerId == playerId) feedback.fail("you already own a guild.")
            else feedback.fail("You are already a member of the ${existingGuild.name} guild. Leave it first!")
            return 0
        }
        if (!GuildManager.isValidName(argName)) {
            feedback.fail("Please provide a valid name for your guild!")
            return 0
        }
        GuildManager.loadedGuilds.add(
            GuildData(
                name = argName,
                ownerId = playerId,
                playerIds = mutableSetOf(),
                saved = false
            )
        )
        feedback.success("$argName was created successfully!")
        return 1
    }
}
