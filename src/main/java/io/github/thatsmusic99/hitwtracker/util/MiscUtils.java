package io.github.thatsmusic99.hitwtracker.util;

import com.mojang.brigadier.context.CommandContext;
import io.github.thatsmusic99.hitwtracker.CoreContainer;
import io.github.thatsmusic99.hitwtracker.api.IGameProfile;
import io.github.thatsmusic99.hitwtracker.manager.IPlayerManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

public class MiscUtils {

    @Contract("null -> null")
    public static String capitalise(@Nullable String str) {
        if (str == null) return null;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    @Contract(pure = true)
    public static @NotNull String toTime(int seconds) {
        return (seconds / 60) + ":" + (seconds % 60 < 10 ? "0" + (seconds % 60) : (seconds % 60));
    }

    @Contract(pure = true)
    public static @NotNull String toTimeUnits(int seconds) {
        if (seconds < 60) {
            return seconds + "s";
        }
        if (seconds < 3600) {
            int remainingSecs = (seconds % 60);
            return (seconds / 60) + "m" + (remainingSecs == 0 ? "" : " " + remainingSecs + "s");
        }
        int remainingSecs = (seconds % 60);
        int remainingMinutes = (seconds % 3600) / 60;
        int remainingHours = seconds / 3600;
        return remainingHours + "hr"
                + (remainingMinutes == 0 ? "" : " " + remainingMinutes + "m")
                + (remainingSecs == 0 ? "" : " " + remainingSecs + "s");
    }

    @Contract(pure = true)
    public static <G extends IGameProfile> @NotNull String getUsername(final @Nullable String player) {
        if (player == null) return "N/A";
        final UUID uuid;
        try {
            uuid = UUID.fromString(player);
        } catch (IllegalArgumentException ex) {
            return player;
        }

        final IPlayerManager<G, ?> manager = (IPlayerManager<G, ?>) CoreContainer.get().getPlayerManager();

        G profile = manager.getByUUID(uuid);
        if (profile == null) return player;

        return profile.getName();
    }

    @Contract(pure = true)
    public static <T> T getOrDefault(final @NotNull CommandContext<?> context, final @NotNull String name,
                                     final @NotNull Class<T> clazz, final @NotNull T defaultOpt) {
        try {
            return context.getArgument(name, clazz);
        } catch (IllegalArgumentException ex) {
            return defaultOpt;
        }
    }


    @Contract(pure = true)
    public static long getDayAtMidnight(final @NotNull Date date) {
        final Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }
}
