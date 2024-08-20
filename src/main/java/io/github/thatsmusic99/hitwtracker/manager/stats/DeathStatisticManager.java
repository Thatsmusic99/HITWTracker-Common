package io.github.thatsmusic99.hitwtracker.manager.stats;

import io.github.thatsmusic99.hitwtracker.game.Statistic;
import io.github.thatsmusic99.hitwtracker.manager.IStatisticManager;
import io.github.thatsmusic99.hitwtracker.manager.StatisticManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class DeathStatisticManager implements IStatisticManager<DeathStatisticManager.DeathStatistic> {

    private final @NotNull HashMap<String, DeathStatistic> stats;
    private final @NotNull CompletableFuture<Supplier<List<DeathStatistic>>> getStats;

    public DeathStatisticManager() {
        this.stats = new HashMap<>();
        this.getStats = StatisticManager.get().getAllStats().thenApplyAsync(stats -> {
            loadStatistics(stats);
            return () -> new ArrayList<>(this.stats.values());
        });
    }

    @Override
    public void loadStatistics(@NotNull List<Statistic> stats) {

        final HashMap<String, DeathStatistic> deathMap = new HashMap<>();
        int games = 0;

        for (Statistic stat : stats) {
            if (stat.deathCause().isEmpty()) continue;
            games++;
            if (deathMap.containsKey(stat.deathCause())) {
                deathMap.computeIfPresent(stat.deathCause(), (key, deathStat) -> {
                    deathStat.count++;
                    return deathStat;
                });
            } else {
                deathMap.put(stat.deathCause(), new DeathStatistic(stat.deathCause(), 1, 0));
            }
        }

        final int finalGames = games;

        deathMap.values().forEach(deathStat -> {
            deathStat.games = finalGames;
            this.stats.put(deathStat.reason, deathStat);
        });
    }

    @Override
    public CompletableFuture<Supplier<List<DeathStatistic>>> getStatistics() {
        return this.getStats;
    }

    @Override
    public void addStatistic(@NotNull Statistic statistic) {

    }

    @Override
    public void updateStatistic(@NotNull Statistic oldStatistic, @NotNull Statistic newStatistic) {
        if (oldStatistic.deathCause().equals(newStatistic.deathCause())) return;
        deleteStatistic(oldStatistic);
        addStatistic(newStatistic);
    }

    @Override
    public void deleteStatistic(@NotNull Statistic statistic) {

    }

    public static class DeathStatistic {

        private final @NotNull String reason;
        private int count;
        private int games;

        public DeathStatistic(@NotNull String reason, int count, int games) {
            this.reason = reason;
            this.count = count;
            this.games = games;
        }

        public int getGames() {
            return games;
        }

        public int getCount() {
            return count;
        }

        public @NotNull String getReason() {
            return reason;
        }
    }
}
