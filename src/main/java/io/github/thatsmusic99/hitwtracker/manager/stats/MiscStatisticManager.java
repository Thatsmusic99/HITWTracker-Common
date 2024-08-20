package io.github.thatsmusic99.hitwtracker.manager.stats;

import io.github.thatsmusic99.hitwtracker.game.Statistic;
import io.github.thatsmusic99.hitwtracker.manager.IStatisticManager;
import io.github.thatsmusic99.hitwtracker.manager.StatisticManager;
import io.github.thatsmusic99.hitwtracker.util.MiscUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class MiscStatisticManager implements IStatisticManager<MiscStatisticManager.MiscStatistic> {

    private final @NotNull List<MiscStatisticManager.MiscStatistic> stats;
    private final @NotNull CompletableFuture<Supplier<List<MiscStatisticManager.MiscStatistic>>> getStats;

    public MiscStatisticManager() {
        this.stats = new ArrayList<>();
        this.getStats = StatisticManager.get().getAllStats().thenApplyAsync(results -> {
            loadStatistics(results);
            return () -> new ArrayList<>(this.stats);
        });
    }

    @Override
    public void loadStatistics(@NotNull List<Statistic> stats) {

        this.stats.clear();

        // Check for individual streaks
        // 0 - ties, 1 - wins, 2 - top 3's
        int[] streaks = new int[3];
        int[] maxStreaks = new int[3];

        for (Statistic stat : stats) {
            if (stat.ties().length > 0) {
                streaks[0]++;
            } else {
                if (streaks[0] > maxStreaks[0]) maxStreaks[0] = streaks[0];
                streaks[0] = 0;
            }

            if (stat.placement() == 1) {
                streaks[1]++;
            } else {
                if (streaks[1] > maxStreaks[1]) maxStreaks[1] = streaks[1];
                streaks[1] = 0;
            }

            if (stat.placement() <= 3) {
                streaks[2]++;
            } else {
                if (streaks[2] > maxStreaks[2]) maxStreaks[2] = streaks[2];
                streaks[2] = 0;
            }
        }

        this.stats.add(new MiscStatistic("Highest Tie Streak", String.valueOf(streaks[0])));
        this.stats.add(new MiscStatistic("Highest Win Streak", String.valueOf(streaks[1])));
        this.stats.add(new MiscStatistic("Highest Top Three Streak", String.valueOf(streaks[2])));

        // Use the day stats as an easy way to determine top wins, ties, etc. - bit more memory efficient
        StatisticManager.get().getDayStatisticManager().getStatistics().whenComplete((results, err) -> {

            int totalGames = 0;
            int totalWins = 0;
            int totalTime = 0;

            int topTies = 0;
            int topWins = 0;
            int topThrees = 0;

            for (var dayStats : results.get()) {
                totalGames += dayStats.games();
                totalWins += dayStats.winCount();
                totalTime += dayStats.totalTime();

                topTies = Math.max(dayStats.tieCount(), topTies);
                topWins = Math.max(dayStats.winCount(), topWins);
                topThrees = Math.max(dayStats.topThreeCount(), topThrees);
            }

            this.stats.add(new MiscStatistic("Daily Tie Record", String.valueOf(topTies)));
            this.stats.add(new MiscStatistic("Daily Win Record", String.valueOf(topWins)));
            this.stats.add(new MiscStatistic("Daily Top Three Record", String.valueOf(topThrees)));

            this.stats.add(new MiscStatistic("Average Games per Day", String.valueOf(totalGames / Math.max(1, totalGames))));
            this.stats.add(new MiscStatistic("Average Wins per Day", String.valueOf(totalWins / Math.max(1, totalGames))));
            this.stats.add(new MiscStatistic("Average Time per Day", MiscUtils.toTimeUnits(totalTime / Math.max(1, totalTime))));

            this.stats.add(new MiscStatistic("Total Time In-Game", MiscUtils.toTimeUnits(totalTime)));
        });
    }

    @Override
    public CompletableFuture<Supplier<List<MiscStatistic>>> getStatistics() {
        return this.getStats;
    }

    @Override
    public void addStatistic(@NotNull Statistic statistic) {

    }

    @Override
    public void deleteStatistic(@NotNull Statistic statistic) {

    }

    @Override
    public void updateStatistic(@NotNull Statistic oldStatistic, @NotNull Statistic newStatistic) {
        if (oldStatistic.placement() == newStatistic.placement()
                && oldStatistic.seconds() == newStatistic.seconds()
                && oldStatistic.ties().length == newStatistic.ties().length) return;
        IStatisticManager.super.updateStatistic(oldStatistic, newStatistic);
    }

    public record MiscStatistic(@NotNull String descriptor, @NotNull String value) {}
}
