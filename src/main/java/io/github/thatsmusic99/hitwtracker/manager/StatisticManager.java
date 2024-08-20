package io.github.thatsmusic99.hitwtracker.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.thatsmusic99.hitwtracker.game.Statistic;
import io.github.thatsmusic99.hitwtracker.manager.stats.*;
import io.github.thatsmusic99.hitwtracker.serializer.StatisticSerializer;
import io.github.thatsmusic99.hitwtracker.util.GameSaver;
import io.github.thatsmusic99.hitwtracker.util.HITWExecutor;
import io.github.thatsmusic99.hitwtracker.util.MiscUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class StatisticManager {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(StatisticManager.class);
    private static StatisticManager instance;
    private final @NotNull List<Statistic> fullStats;
    private final @NotNull DayStatisticManager dayStatisticManager;
    private final @NotNull DeathStatisticManager deathStatisticManager;
    private final @NotNull MapStatisticManager mapStatisticManager;
    private final @NotNull MiscStatisticManager miscStatisticManager;
    private final @NotNull TieStatisticManager tieStatisticManager;
    private transient @Nullable CompletableFuture<Void> loading;
    private boolean loaded;

    public StatisticManager() {
        instance = this;
        this.fullStats = new ArrayList<>();
        this.dayStatisticManager = new DayStatisticManager();
        this.deathStatisticManager = new DeathStatisticManager();
        this.mapStatisticManager = new MapStatisticManager();
        this.miscStatisticManager = new MiscStatisticManager();
        this.tieStatisticManager = new TieStatisticManager();

        this.loaded = false;
    }

    public static StatisticManager get() {
        return instance;
    }

    public @NotNull DayStatisticManager getDayStatisticManager() {
        return dayStatisticManager;
    }

    public @NotNull DeathStatisticManager getDeathStatisticManager() {
        return deathStatisticManager;
    }

    public @NotNull MapStatisticManager getMapStatisticManager() {
        return mapStatisticManager;
    }

    public @NotNull MiscStatisticManager getMiscStatisticManager() {
        return miscStatisticManager;
    }

    public @NotNull TieStatisticManager getTieStatisticManager() {
        return tieStatisticManager;
    }

    public int getGameCount() {
        return this.fullStats.size();
    }

    public void editDeathCause(final int gameId, final @NotNull String deathCause) throws IndexOutOfBoundsException {

        getAllStats().whenComplete((stats, err) -> {

            final Statistic oldStat = this.fullStats.get(gameId);
            if (oldStat == null) throw new IndexOutOfBoundsException();

            final Statistic newStat = new Statistic(oldStat.placement(), oldStat.ties(), oldStat.map(), deathCause,
                    oldStat.walls(), oldStat.seconds(), oldStat.plobby(), oldStat.date());
            this.fullStats.set(gameId, newStat);

            deathStatisticManager.updateStatistic(oldStat, newStat);
        });
    }

    private @NotNull CompletableFuture<Void> load() {
        if (loading != null) return loading;
        LOGGER.info("Loading statistic data...");

        return loading = CompletableFuture.runAsync(() -> {

            // Go through each file in the games folder
            final File gamesFolder = new File("games");
            if (!gamesFolder.exists()) {
                LOGGER.info("Games folder does not exist.");
                loading = null;
                return;
            }

            final File hitwFolder = new File(gamesFolder, "hitw");
            if (!hitwFolder.exists()) {
                LOGGER.info("HITW folder does not exist.");
                loading = null;
                return;
            }

            // Go through each file
            final File[] files = hitwFolder.listFiles();
            if (files == null || files.length == 0) {
                LOGGER.info("Folder is empty.");
                loading = null;
                return;
            }
            for (int i = files.length - 1; i >= 0; i--) {

                // Get the file itself
                final File file = files[i];

                // Read the file
                final String content;
                try {
                    content = GameSaver.getFileContents(file);
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }

                final Gson gson = new GsonBuilder().registerTypeAdapter(Statistic.class, new StatisticSerializer()).create();
                final Statistic[] stats = gson.fromJson(content, Statistic[].class);

                // Go through each statistic and get its date
                this.fullStats.addAll(Arrays.asList(stats));
            }

            // Reverse times
            this.fullStats.sort(Comparator.comparing(Statistic::date));

            loading = null;
            loaded = true;
            LOGGER.info("Loaded " + this.fullStats.size() + " stats.");
        }, HITWExecutor.executorService);
    }

    public @NotNull CompletableFuture<@NotNull List<Statistic>> getAllStats() {

        return getStats(this.fullStats, () -> this.fullStats);
    }

    private <T extends Collection<?>> @NotNull CompletableFuture<T> getStats(T check, Supplier<T> supplier) {
        return getStats(check, x -> x.isEmpty(), supplier);
    }

    private <T> @NotNull CompletableFuture<T> getStats(@Nullable T check, Predicate<T> isEmpty, Supplier<T> supplier) {

        if (!isEmpty.test(check)) {
            return CompletableFuture.completedFuture(check);
        }

        if (!loaded) {
            return load().thenApplyAsync(x -> supplier.get(), HITWExecutor.executorService);
        }

        return CompletableFuture.supplyAsync(supplier, HITWExecutor.executorService);
    }


    public void addStatToCache(final @NotNull Statistic statistic) {

        // If stats haven't already been loaded, don't for now
        if (!loaded) {
            LOGGER.info("Statistics are not loaded, not adding the stat to cache.");
        } else {

            this.fullStats.add(statistic);

            // Update in managers
            this.dayStatisticManager.addStatistic(statistic);
            this.deathStatisticManager.addStatistic(statistic);
            this.mapStatisticManager.addStatistic(statistic);
            this.miscStatisticManager.addStatistic(statistic);
            this.tieStatisticManager.addStatistic(statistic);
        }

        // Save to file
        final var midnight = MiscUtils.getDayAtMidnight(statistic.date());
        try {
            GameSaver.saveGame(this.fullStats.stream()
                    .filter(stat -> MiscUtils.getDayAtMidnight(stat.date()) == midnight)
                    .toList(),
                    new Date(midnight));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
