package io.github.thatsmusic99.hitwtracker.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

public interface ICommand {

    @NotNull String execute(final @NotNull CommandContext<?> context);

    void init(final @NotNull CommandDispatcher<Audience> dispatcher);
}
