package quest.nekoniyah.guildly.utils

import com.mojang.brigadier.context.CommandContext
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import quest.nekoniyah.guildly.Guildly

class Feedback {
    private val ctx: CommandContext<CommandSourceStack>

    companion object {
        val PREFIX: MutableComponent =
            Component.literal("[${Guildly.ID}]").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD)

        fun build(message: String): MutableComponent {
            return Component.literal("").append(PREFIX)
                .append(Component.literal(" $message").withStyle(ChatFormatting.RESET))
        }
    }

    constructor(ctx: CommandContext<CommandSourceStack>) {
        this.ctx = ctx
    }


    fun fail(message: String) = ctx.source.sendFailure(build(message))
    fun success(message: String) = ctx.source.sendSuccess({ build(message) }, false)
}