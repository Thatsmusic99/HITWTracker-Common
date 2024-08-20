package io.github.thatsmusic99.hitwtracker.manager.stats;

import io.github.thatsmusic99.hitwtracker.game.Statistic;
import io.github.thatsmusic99.hitwtracker.manager.IStatisticManager;
import io.github.thatsmusic99.hitwtracker.manager.StatisticManager;
import io.github.thatsmusic99.hitwtracker.util.MiscUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class MapStatisticManager implements IStatisticManager<MapStatisticManager.MapStatistic> {

    private final @NotNull HashMap<String, MapStatisticManager.MapStatistic> stats;
    private final @NotNull CompletableFuture<Supplier<List<MapStatisticManager.MapStatistic>>> getStats;

    public MapStatisticManager() {
        this.stats = new HashMap<>();
        this.getStats = StatisticManager.get().getAllStats().thenApplyAsync(results -> {
            loadStatistics(results);
            return () -> new ArrayList<>(this.stats.values());
        });
    }

    @Override
    public void loadStatistics(@NotNull List<Statistic> stats) {
        stats.forEach(this::addStatistic);
    }

    @Override
    public CompletableFuture<Supplier<List<MapStatistic>>> getStatistics() {
        return this.getStats;
    }

    @Override
    public void addStatistic(@NotNull Statistic statistic) {
        addStatistic(statistic.map(), statistic);
        addStatistic("Overall", statistic);
    }

    private void addStatistic(final @NotNull String map, final @NotNull Statistic statistic) {

        // Attempt to see if a map stat exists
        var mapStat = this.stats.get(map);

        // If it doesn't, create a new map stat
        if (mapStat == null) mapStat = new MapStatistic(map);

        mapStat.games++;
        mapStat.placements += statistic.placement();
        mapStat.deaths.add(statistic.deathCause());
        if (statistic.ties().length > 0) {
            mapStat.ties++;
            mapStat.tieSizes.add(statistic.ties().length);
            for (String name : statistic.ties()) {
                mapStat.tiedWith.add(MiscUtils.getUsername(name));
            }
            mapStat.largestTie = Math.max(statistic.ties().length, mapStat.largestTie);
        }

        if (statistic.placement() == 1) {
            mapStat.wins++;
            mapStat.fastestTime = (short) Math.min(mapStat.fastestTime, statistic.seconds());
        }
        if (statistic.placement() <= 3) mapStat.topThrees++;
        mapStat.walls += statistic.walls();
        mapStat.totalTime += statistic.seconds();

        mapStat.topDeathCause = mapStat.getTopResult(mapStat.deaths);
        mapStat.mostTiedWith = mapStat.getTopResult(mapStat.tiedWith);

        this.stats.put(map, mapStat);
    }

    @Override
    public void deleteStatistic(@NotNull Statistic statistic) {

        // Attempt to see if a map stat exists
        final var mapStat = this.stats.get(statistic.map());
        if (mapStat == null) return;

        // If we're wiping the whole stat, let's go
        if (mapStat.games == 0) {
            this.stats.remove(statistic.map());
            return;
        }

        // Reduce the statistic
        mapStat.games--;
        mapStat.placements -= statistic.placement();
        mapStat.deaths.remove(statistic.deathCause());
        if (statistic.ties().length > 0) {
            mapStat.ties--;
            mapStat.tieSizes.remove((Integer) statistic.ties().length);
            for (String name : statistic.ties()) {
                mapStat.tiedWith.remove(MiscUtils.getUsername(name));
            }
        }

        if (statistic.placement() == 1) {
            mapStat.wins--;
            if (mapStat.fastestTime == statistic.seconds()) mapStat.fastestTime = 240;
        }
        if (statistic.placement() <= 3) mapStat.topThrees--;

        mapStat.walls -= statistic.walls();
        mapStat.totalTime -= statistic.seconds();

        mapStat.topDeathCause = mapStat.getTopResult(mapStat.deaths);
        mapStat.mostTiedWith = mapStat.getTopResult(mapStat.tiedWith);
        mapStat.largestTie = mapStat.getTopResultInt(mapStat.tieSizes);

    }

    public static class MapStatistic {

        private final @NotNull String map;
        private int games;
        private int placements;
        private final @NotNull List<String> deaths;
        private @Nullable String topDeathCause;
        private int ties;
        private int largestTie;
        private final @NotNull List<Integer> tieSizes;
        private final @NotNull List<String> tiedWith;
        private @Nullable String mostTiedWith;
        private int wins;
        private int topThrees;
        private int walls;
        private int totalTime;
        private short fastestTime;

        public MapStatistic(final @NotNull String map) {
            this(map, 0, 0, new ArrayList<>(), 0, new ArrayList<>(), new ArrayList<>(), 0,
                    0, 0, (short) 0, (short) 240);
        }

        public MapStatistic(final @NotNull String map,
                            int games,
                            int avgPlacement,
                            @NotNull List<String> deaths,
                            int ties,
                            @NotNull List<Integer> tieSizes,
                            @NotNull List<String> tiedWith,
                            int wins,
                            int topThrees,
                            int walls,
                            short totalTime,
                            short fastestTime) {
            this.map = MiscUtils.capitalise(map);
            this.games = games;
            this.placements = avgPlacement;
            this.deaths = deaths;
            this.topDeathCause = getTopResult(deaths);
            this.ties = ties;
            this.tieSizes = tieSizes;
            this.tiedWith = tiedWith;
            this.mostTiedWith = MiscUtils.getUsername(getTopResult(tiedWith));
            this.wins = wins;
            this.topThrees = topThrees;
            this.walls = walls;
            this.totalTime = totalTime;
            this.fastestTime = fastestTime;
        }

        private @Nullable String getTopResult(@NotNull List<String> list) {

            HashMap<String, Integer> counts = new HashMap<>();
            int maxValue = 0;
            String maxResult = null;

            for (String str : list) {
                if (str.isEmpty()) continue;
                int count = 1;
                if (counts.containsKey(str)) {
                    count = counts.get(str) + 1;
                } else {
                    counts.put(str, count);
                }

                if (maxValue < count) {
                    maxValue = count;
                    maxResult = str;
                }
            }

            return maxResult;
        }

        private int getTopResultInt(@NotNull List<Integer> list) {
            int max = 0;
            for (int x : list) {
                if (max < x) max = x;
            }
            return max;
        }

        public @NotNull String getMap() {
            return map;
        }

        public int getGames() {
            return games;
        }

        public float getAvgPlacement() {
            return placements / (float) this.games;
        }

        public @Nullable String getTopDeathCause() {
            return topDeathCause;
        }

        public int getTies() {
            return ties;
        }

        public int getLargestTie() {
            return largestTie;
        }

        public @Nullable String getMostTiedWith() {
            return mostTiedWith;
        }

        public int getWins() {
            return wins;
        }

        public int getTopThrees() {
            return topThrees;
        }

        public int getWalls() {
            return walls;
        }

        public int getAverageTime() {
            return this.totalTime / this.games;
        }

        public short getFastestTime() {
            return fastestTime;
        }
    }
}
