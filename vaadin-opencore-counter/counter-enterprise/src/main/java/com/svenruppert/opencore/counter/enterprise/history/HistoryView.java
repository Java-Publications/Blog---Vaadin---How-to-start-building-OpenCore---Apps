package com.svenruppert.opencore.counter.enterprise.history;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class HistoryView extends VerticalLayout {

  public static final String ID_GRID = "history-grid";

  public HistoryView() {
    add(new H2("History"));

    Grid<HistoryEntry> grid = new Grid<>();
    grid.setId(ID_GRID);
    grid.addColumn(HistoryEntry::timestamp).setHeader("Timestamp");
    grid.addColumn(HistoryEntry::oldValue).setHeader("Old Value");
    grid.addColumn(HistoryEntry::newValue).setHeader("New Value");
    grid.addColumn(entry -> entry.action().name()).setHeader("Action");
    grid.setItems(HistoryStore.getInstance().entries());

    add(grid);
  }
}
