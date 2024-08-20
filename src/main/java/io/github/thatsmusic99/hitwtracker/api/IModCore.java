package io.github.thatsmusic99.hitwtracker.api;

import io.github.thatsmusic99.hitwtracker.gui.GuiManager;
import io.github.thatsmusic99.hitwtracker.manager.DamageManager;
import io.github.thatsmusic99.hitwtracker.manager.IPlayerManager;
import org.jetbrains.annotations.NotNull;

public interface IModCore<D, S, G extends IGameProfile, P extends IPlayer<G>> {

    @NotNull IPlayerManager<G, P> getPlayerManager();

    @NotNull GuiManager getGuiManager();

    @NotNull DamageManager<D, S> getDamageManager();
}
