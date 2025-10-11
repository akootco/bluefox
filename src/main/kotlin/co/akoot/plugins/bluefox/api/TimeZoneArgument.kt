package co.akoot.plugins.bluefox.api

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.MessageComponentSerializer
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import net.kyori.adventure.text.Component
import java.time.ZoneId
import java.util.concurrent.CompletableFuture


class TimeZoneArgument: CustomArgumentType.Converted<ZoneId, String> {

    companion object {
        val ERROR_INVALID_TIMEZONE: DynamicCommandExceptionType = DynamicCommandExceptionType {
            MessageComponentSerializer.message().serialize(Component.text("$it is not a valid time zone!"))
        }
    }

    override fun convert(nativeType: String): ZoneId {
        return try {
            ZoneId.of(nativeType)
        } catch (_: IllegalArgumentException ) {
            throw ERROR_INVALID_TIMEZONE.create(nativeType)
        }
    }

    override fun getNativeType(): ArgumentType<String> {
        return StringArgumentType.word()
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        for(zoneId in ZoneId.getAvailableZoneIds()) {
            if(zoneId.startsWith(builder.remainingLowerCase)) {
                builder.suggest(zoneId)
            }
        }
        return builder.buildFuture()
    }
}