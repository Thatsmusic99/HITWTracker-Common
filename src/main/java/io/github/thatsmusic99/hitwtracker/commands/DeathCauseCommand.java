package io.github.thatsmusic99.hitwtracker.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.thatsmusic99.hitwtracker.manager.StatisticManager;
import io.github.thatsmusic99.hitwtracker.util.MiscUtils;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

public class DeathCauseCommand implements ICommand {

    public @NotNull String execute(final @NotNull CommandContext<?> context) {

        if (StatisticManager.get().getGameCount() == 0)
            return "You don't have any games to set the death cause of!";

        final String deathCause = StringArgumentType.getString(context, "cause");
        int gameId = MiscUtils.getOrDefault(context, "game", int.class,
                StatisticManager.get().getGameCount());

        StatisticManager.get().editDeathCause(gameId, deathCause);

        // "Failed to change the death cause, whoops!"

        return "Success! Set game ID " + gameId + "'s death cause to " + deathCause + ".";
    }

    @Override
    public void init(@NotNull CommandDispatcher<Audience> dispatcher) {


        dispatcher.register(LiteralArgumentBuilder.<Audience>literal("deathcause")
                .then(RequiredArgumentBuilder.<Audience, String>argument("cause", StringArgumentType.string())
                        .suggests(new TrapSuggestionProvider<>()))
                .executes(context -> {
                    context.getSource().sendMessage(Component.text(execute(context)));
                    return 1;
                })
                .then(RequiredArgumentBuilder.argument("game", IntegerArgumentType.integer()))
                .executes(context -> {
                    context.getSource().sendMessage(Component.text(execute(context)));
                    return 1;
                }));
    }
}
