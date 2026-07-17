package quest.nekoniyah.guildly.utils

import com.mojang.brigadier.context.CommandContext
import net.minecraft.ChatFormatting
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import quest.nekoniyah.guildly.Guildly

class Feedback {
    private val context: CommandContext<CommandSourceStack>

    companion object {
        val PREFIX: MutableComponent = Component.literal("[${Guildly.ID}]").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD)
        fun build(message: String): MutableComponent = Component.literal("").append(PREFIX).append(Component.literal(" $message").withStyle(ChatFormatting.RESET))
        fun build(component: MutableComponent): MutableComponent = Component.literal("").append(PREFIX).append(component)
    }

    constructor(ctx: CommandContext<CommandSourceStack>) {
        context = ctx
    }

    fun fail(message: String) = context.source.sendFailure(build(message))
    fun success(message: String) = context.source.sendSuccess({ build(message) }, false)
}
