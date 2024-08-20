package io.github.thatsmusic99.hitwtracker.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.thatsmusic99.hitwtracker.game.Trap;

import java.util.concurrent.CompletableFuture;

public class TrapSuggestionProvider<S> implements SuggestionProvider<S> {

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {

        return CompletableFuture.supplyAsync(() -> {

            for (Trap trap : Trap.values()) {
                builder.suggest(String.format("\"%s\"", trap.displayName));
            }

            return builder.build();
        });
    }
}
