package io.github.thatsmusic99.hitwtracker.manager;

import io.github.thatsmusic99.hitwtracker.game.Statistic;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * A class used to load a certain type of statistic.
 *
 * @param <S> the statistic type in question being handled.
 */
public interface IStatisticManager<S> {

    void loadStatistics(final @NotNull List<Statistic> stats);

    CompletableFuture<Supplier<List<S>>> getStatistics();

    void addStatistic(final @NotNull Statistic statistic);

    default void updateStatistic(final @NotNull Statistic oldStatistic, final @NotNull Statistic newStatistic) {
        deleteStatistic(oldStatistic);
        addStatistic(newStatistic);
    }

    void deleteStatistic(final @NotNull Statistic statistic);
}
