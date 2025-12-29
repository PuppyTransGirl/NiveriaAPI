package toutouchien.niveriaapi.utils.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class StorageFriendlyStringArgument implements CustomArgumentType<String, String> {
    private static final SimpleCommandExceptionType ERROR_DOT_IN_STRING = new SimpleCommandExceptionType(
            MessageComponentSerializer.message().serialize(Component.text("The string cannot contain dots."))
    );

    private static final SimpleCommandExceptionType ERROR_PLUS_IN_STRING = new SimpleCommandExceptionType(
            MessageComponentSerializer.message().serialize(Component.text("The string cannot contain plus signs."))
    );

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String parsedString = reader.readUnquotedString();
        if (parsedString.contains("."))
            throw ERROR_DOT_IN_STRING.create();

        if (parsedString.contains("+"))
            throw ERROR_PLUS_IN_STRING.create();

        return parsedString;
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}
