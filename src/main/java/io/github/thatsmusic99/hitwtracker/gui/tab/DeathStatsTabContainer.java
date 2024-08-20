package io.github.thatsmusic99.hitwtracker.gui.tab;

import io.github.thatsmusic99.hitwtracker.CoreContainer;
import io.github.thatsmusic99.hitwtracker.gui.IColumn;
import io.github.thatsmusic99.hitwtracker.gui.IEntryListTab;
import io.github.thatsmusic99.hitwtracker.manager.StatisticManager;
import io.github.thatsmusic99.hitwtracker.manager.stats.DeathStatisticManager;

import java.util.Comparator;

public class DeathStatsTabContainer {

    private final IEntryListTab<DeathStatisticManager.DeathStatistic, IColumn<DeathStatisticManager.DeathStatistic>> tab;
    private final IColumn<DeathStatisticManager.DeathStatistic> RATE_COLUMN;

    public DeathStatsTabContainer() {

        final var guiManager = CoreContainer.get().getGuiManager();

        this.tab = guiManager.createTab("Deaths", 20);

        this.tab.addColumn(this.tab.createColumn("Death",
                stat -> stat.getItem().getReason(),
                Comparator.comparing(stat -> stat.getItem().getReason())));

        this.tab.addColumn(this.tab.createColumn("Count",
                stat -> String.valueOf(stat.getItem().getCount()),
                Comparator.comparing(stat -> stat.getItem().getCount())));

        this.tab.addColumn(RATE_COLUMN = this.tab.createColumn("Rate",
                stat -> String.format("%.2f", stat.getItem().getCount() / (float) stat.getItem().getGames() * 100),
                Comparator.comparing(stat -> stat.getItem().getCount() / (float) stat.getItem().getGames())));

        this.tab.setupGrid();

        StatisticManager.get().getDeathStatisticManager().getStatistics().whenComplete((stats, err) -> {

            stats.get().forEach(this.tab::addEntryAtEnd);
            RATE_COLUMN.setDirection(0);
            this.tab.setScrollAmount(0);
        });
    }

    public IEntryListTab<DeathStatisticManager.DeathStatistic, IColumn<DeathStatisticManager.DeathStatistic>> getTab() {
        return this.tab;
    }
}
