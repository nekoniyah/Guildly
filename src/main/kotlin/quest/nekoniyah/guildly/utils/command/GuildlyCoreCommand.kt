package quest.nekoniyah.guildly.utils.command

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

abstract class GuildlyCoreCommand : GuildlyNodeCommand() {
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register(Commands.literal(name).executes(::execute).apply {
            subcommands?.forEach { sc ->
                if (sc.definition != null) then(sc.definition)
            }
        })
    }
}