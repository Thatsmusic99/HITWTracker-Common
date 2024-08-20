package io.github.thatsmusic99.hitwtracker.manager.stats;

import io.github.thatsmusic99.hitwtracker.game.Statistic;
import io.github.thatsmusic99.hitwtracker.manager.IStatisticManager;
import io.github.thatsmusic99.hitwtracker.manager.StatisticManager;
import io.github.thatsmusic99.hitwtracker.util.MiscUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class DayStatisticManager implements IStatisticManager<DayStatisticManager.DayStatistic> {

    private final HashMap<Long, DayStatistic> stats;
    private final @NotNull CompletableFuture<Supplier<List<DayStatistic>>> getStats;

    public DayStatisticManager() {
        this.stats = new HashMap<>();
        this.getStats = StatisticManager.get().getAllStats().thenApplyAsync(stats -> {
            loadStatistics(stats);
            return () -> new ArrayList<>(this.stats.values());
        });
    }

    @Override
    public void loadStatistics(@NotNull List<Statistic> stats) {

        stats.forEach(this::addStatistic);
    }

    @Override
    public CompletableFuture<Supplier<List<DayStatistic>>> getStatistics() {
        return this.getStats;
    }

    @Override
    public void addStatistic(@NotNull Statistic statistic) {

        // Get the date to fetch
        final long target = MiscUtils.getDayAtMidnight(statistic.date());

        // Try to find an existing statistic
        final DayStatistic stat = this.stats.get(target);

        // If the statistic for the day doesn't exist, then create one
        final DayStatistic newStat;
        if (stat == null) {

            newStat = new DayStatistic(
                    new Date(target),
                    1,
                    statistic.placement(),
                    (short) (statistic.ties().length > 0 ? 1 : 0),
                    (short) (statistic.placement() == 1 ? 1 : 0),
                    (short) (statistic.placement() <= 3 ? 1 : 0),
                    statistic.walls(),
                    statistic.seconds(),
                    (statistic.placement() == 1 ? statistic.seconds() : 240)
            );

            this.stats.put(target, newStat);

        } else {

            stat.games++;
            stat.placements += statistic.placement();
            if (statistic.ties().length > 0) stat.tieCount++;
            if (statistic.placement() == 1) {
                stat.winCount++;
                stat.fastestWin = (short) Math.min(statistic.seconds(), stat.fastestWin);
            }
            if (statistic.placement() <= 3) stat.topThreeCount++;
            stat.walls += statistic.walls();
            stat.totalTime += statistic.seconds();
        }
    }

    @Override
    public void deleteStatistic(@NotNull Statistic statistic) {

        // Get the date to fetch
        final long target = MiscUtils.getDayAtMidnight(statistic.date());

        // Try to find an existing statistic
        final DayStatistic stat = this.stats.get(target);

        // If no statistic exists, rip bozo!
        if (stat == null) return;

        // If we're deleting the only game for that date, wipe it altogether
        if (stat.games == 1) {
            this.stats.remove(target);
            return;
        }

        stat.games--;
        stat.placements -= statistic.placement();
        if (statistic.ties().length > 0) stat.tieCount -= 1;
        if (statistic.placement() == 1) {
            stat.winCount--;
            // TODO - should find the next shortest time... if possible!?
            if (stat.fastestWin == statistic.seconds()) stat.fastestWin = 240;
        }
        if (statistic.placement() == 3) stat.topThreeCount--;
        stat.walls -= statistic.walls();
        stat.totalTime -= statistic.seconds();
    }


    public static class DayStatistic {

        private final @NotNull Date date;
        private int games;
        private int placements;
        private short tieCount;
        private short winCount;
        private short topThreeCount;
        private int walls;
        private short totalTime;
        private short fastestWin;

        public DayStatistic(final @NotNull Date date,
                            final int games,
                            final int placements,
                            final short tieCount,
                            final short winCount,
                            final short topThreeCount,
                            final int walls,
                            final short totalTime,
                            final short fastestWin) {
            this.date = date;
            this.games = games;
            this.placements = placements;
            this.tieCount = tieCount;
            this.winCount = winCount;
            this.topThreeCount = topThreeCount;
            this.walls = walls;
            this.totalTime = totalTime;
            this.fastestWin = fastestWin;
        }

        public @NotNull Date date() {
            return this.date;
        }

        public int games() {
            return this.games;
        }

        public short tieCount() {
            return this.tieCount;
        }

        public short winCount() {
            return this.winCount;
        }

        public short topThreeCount() {
            return this.topThreeCount;
        }

        public int walls() {
            return this.walls;
        }

        public int fastestWin() {
            return this.fastestWin;
        }

        public float avgPlacement() {
            return this.placements / (float) games;
        }

        public float tieRate() {
            return tieCount / (float) games;
        }

        public float winRate() {
            return winCount / (float) games;
        }

        public float topThreeRate() {
            return topThreeCount / (float) games;
        }

        public float wallsPerWin() {
            return walls / (float) winCount;
        }

        public short averageTime() {
            return (short) (totalTime / games);
        }

        public short totalTime() {
            return this.totalTime;
        }
    }
}
