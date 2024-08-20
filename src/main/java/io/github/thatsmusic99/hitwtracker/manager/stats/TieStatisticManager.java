package io.github.thatsmusic99.hitwtracker.manager.stats;

import io.github.thatsmusic99.hitwtracker.game.Statistic;
import io.github.thatsmusic99.hitwtracker.manager.IStatisticManager;
import io.github.thatsmusic99.hitwtracker.manager.StatisticManager;
import io.github.thatsmusic99.hitwtracker.util.MiscUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class TieStatisticManager implements IStatisticManager<TieStatisticManager.TieStatistic> {

    private final @NotNull HashMap<String, TieStatistic> tieStats;
    private final @NotNull CompletableFuture<Supplier<List<TieStatistic>>> getStats;

    public TieStatisticManager() {
        this.tieStats = new HashMap<>();
        this.getStats = StatisticManager.get().getAllStats().thenApplyAsync(stats -> {
            loadStatistics(stats);
            return () -> new ArrayList<>(this.tieStats.values());
        });
    }


    @Override
    public void loadStatistics(@NotNull List<Statistic> stats) {

        // Go through each statistic and their ties
        for (Statistic statistic : stats) {
            addStatistic(statistic);
        }
    }

    @Override
    public CompletableFuture<Supplier<List<TieStatistic>>> getStatistics() {
        return this.getStats;
    }

    @Override
    public void addStatistic(@NotNull Statistic statistic) {

        // Go through each tied player
        for (String player : statistic.ties()) {
            final var playerName = MiscUtils.getUsername(player);
            if (this.tieStats.containsKey(playerName)) {

                final var tieStat = this.tieStats.get(playerName);
                tieStat.count++;
                tieStat.add(statistic.map());
            } else {
                final var tieStat = new TieStatistic(playerName, 1);
                tieStat.add(statistic.map());

                this.tieStats.put(playerName, tieStat);
            }
        }
    }

    @Override
    public void deleteStatistic(@NotNull Statistic statistic) {

        // Go through each tied player
        for (String player : statistic.ties()) {
            final var playerName = MiscUtils.getUsername(player);
            if (this.tieStats.containsKey(playerName)) {

                final var tieStat = this.tieStats.get(playerName);
                tieStat.count--;
                tieStat.remove(statistic.map());
            }
        }
    }

    public static class TieStatistic {

        private final @NotNull String player;
        private int count;
        private final HashMap<String, Integer> maps;

        public TieStatistic(@NotNull String player, int count) {
            this.player = MiscUtils.getUsername(player);
            this.count = count;
            this.maps = new HashMap<>();
        }

        public @NotNull String getPlayer() {
            return player;
        }

        public int getCount() {
            return count;
        }

        public int getMapCount(final @NotNull String map) {
            return this.maps.getOrDefault(map.toLowerCase(), 0);
        }

        public Set<String> getTiedMaps() {
            return this.maps.keySet();
        }

        protected void add(final @NotNull String map) {
            int count = getMapCount(map);
            count++;
            this.maps.put(map.toLowerCase(), count);
        }

        protected void remove(final @NotNull String map) {
            int count = getMapCount(map);
            count--;
            this.maps.put(map.toLowerCase(), count);
        }
    }
}
