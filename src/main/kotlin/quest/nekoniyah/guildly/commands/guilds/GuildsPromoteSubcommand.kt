package quest.nekoniyah.guildly.commands.guilds

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import quest.nekoniyah.guildly.database.managers.guild.GuildManager
import quest.nekoniyah.guildly.database.managers.guild.GuildMember
import quest.nekoniyah.guildly.database.managers.player.PlayerManager
import quest.nekoniyah.guildly.utils.Feedback
import quest.nekoniyah.guildly.utils.command.GuildlyNodeCommand
import java.util.Locale

class GuildsPromoteSubcommand : GuildlyNodeCommand() {
    override val name: String = "promote"
    override val description: String = "Gives a member staff role or change ownership."

    override fun execute(ctx: CommandContext<CommandSourceStack>): Int {
        Feedback(ctx).fail("Please choose a role between owner or staff.")

        return 1
    }

    class GuildsPromoteStaff : GuildlyNodeCommand() {
        override val name = "staff"

        private val suggestions = SuggestionProvider<CommandSourceStack> { context, builder ->
            val ownerId = context.source.player?.stringUUID
            val ownedGuild = ownerId?.let { id -> GuildManager.loadedGuilds.find { it.ownerId == id } }
            ownedGuild?.playerIds?.mapNotNull { PlayerManager.getByUUID(it.uuid)?.name }?.forEach { memberName ->
                if (memberName.lowercase(Locale.ROOT).startsWith(builder.remainingLowerCase)) {
                    builder.suggest(memberName)
                }
            }
            builder.buildFuture()
        }

        override val definition: LiteralArgumentBuilder<CommandSourceStack?> =
            Commands.literal(name).executes(::execute).then(
                Commands.argument(
                    "player", StringArgumentType.string()
                ).suggests(suggestions).executes(::executeFull)
            )

        override fun execute(ctx: CommandContext<CommandSourceStack>): Int {
            Feedback(ctx).fail("Please provide a name of the member to promote.")
            return 1
        }

        fun executeFull(ctx: CommandContext<CommandSourceStack>): Int {
            val feedback = Feedback(ctx)
            val player = ctx.source.player ?: return 0
            val targetName = StringArgumentType.getString(ctx, "player")
            val ownedGuild = GuildManager.loadedGuilds.find { it.ownerId == player.stringUUID }
            if (ownedGuild == null) {
                feedback.fail("You don't own any guild!")
                return 0
            }
            val targetData = PlayerManager.getByName(targetName)
            if (targetData == null) {
                feedback.fail("This user never joined the game!")
                return 0
            }

            val playerData = ownedGuild.playerIds.find { p -> p.uuid == targetData.uuid }

            if (playerData == null) {
                feedback.fail("$targetName is not a member of your guild!")
                return 0
            }

            val updatedGuild = ownedGuild.copy(playerIds = ownedGuild.playerIds.toMutableSet())
            val p = updatedGuild.playerIds.find { p -> p.uuid == targetData.uuid }!!

            p.role = "staff"

            GuildManager.updateGuild(updatedGuild)

            PlayerManager.findOnlineByUUID(targetData.uuid)
                ?.sendSystemMessage(Feedback.build("You have been promoted to staff, in the ${ownedGuild.name} guild."))
            feedback.success("$targetName has been promoted to staff in ${ownedGuild.name}")
            return 1
        }
    }

    class GuildsTransfer : GuildlyNodeCommand() {
        override val name = "owner"

        private val suggestions = SuggestionProvider<CommandSourceStack> { context, builder ->
            val ownerId = context.source.player?.stringUUID
            val ownedGuild = ownerId?.let { id -> GuildManager.loadedGuilds.find { it.ownerId == id } }
            ownedGuild?.playerIds?.mapNotNull { PlayerManager.getByUUID(it.uuid)?.name }?.forEach { memberName ->
                if (memberName.lowercase(Locale.ROOT).startsWith(builder.remainingLowerCase)) {
                    builder.suggest(memberName)
                }
            }
            builder.buildFuture()
        }

        override val definition: LiteralArgumentBuilder<CommandSourceStack?> =
            Commands.literal(name).executes(::execute).then(
                Commands.argument(
                    "player", StringArgumentType.string()
                ).suggests(suggestions).executes(::executeFull)
            )

        override fun execute(ctx: CommandContext<CommandSourceStack>): Int {
            Feedback(ctx).fail("Please provide a name of the member to promote.")
            return 1
        }

        fun executeFull(ctx: CommandContext<CommandSourceStack>): Int {
            val feedback = Feedback(ctx)
            val player = ctx.source.player ?: return 0
            val targetName = StringArgumentType.getString(ctx, "player")
            val ownedGuild = GuildManager.loadedGuilds.find { it.ownerId == player.stringUUID }
            if (ownedGuild == null) {
                feedback.fail("You don't own any guild!")
                return 0
            }
            val targetData = PlayerManager.getByName(targetName)
            if (targetData == null) {
                feedback.fail("This user never joined the game!")
                return 0
            }

            val playerData = ownedGuild.playerIds.find { p -> p.uuid == targetData.uuid }

            if (playerData == null) {
                feedback.fail("$targetName is not a member of your guild!")
                return 0
            }

            val updatedGuild = ownedGuild.copy(playerIds = ownedGuild.playerIds.toMutableSet())
            val p = updatedGuild.playerIds.find { p -> p.uuid == targetData.uuid }!!

            updatedGuild.playerIds.remove(playerData)
            updatedGuild.ownerId = p.uuid
            updatedGuild.playerIds.add(GuildMember(ownedGuild.ownerId))

            GuildManager.updateGuild(updatedGuild)

            PlayerManager.findOnlineByUUID(targetData.uuid)
                ?.sendSystemMessage(Feedback.build("You are now the owner of the ${ownedGuild.name} guild."))
            feedback.success("$targetName became the new owner of ${ownedGuild.name}")
            return 1
        }
    }

    override val subcommands: List<GuildlyNodeCommand> by lazy {
        listOf(GuildsPromoteStaff(), GuildsTransfer())
    }

    override val definition: LiteralArgumentBuilder<CommandSourceStack?>? =
        Commands.literal(name).executes(::execute).apply {
            subcommands.forEach { nodeCommand -> if (nodeCommand.definition != null) then(nodeCommand.definition) }
        }
}


