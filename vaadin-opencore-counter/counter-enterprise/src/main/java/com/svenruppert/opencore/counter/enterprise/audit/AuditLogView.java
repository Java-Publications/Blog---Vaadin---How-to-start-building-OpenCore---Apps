package com.svenruppert.opencore.counter.enterprise.audit;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class AuditLogView extends VerticalLayout {

  public static final String ID_GRID = "audit-log-grid";

  public AuditLogView() {
    add(new H2("Audit Log"));

    Grid<AuditEntry> grid = new Grid<>();
    grid.setId(ID_GRID);
    grid.addColumn(AuditEntry::timestamp).setHeader("Timestamp");
    grid.addColumn(AuditEntry::eventType).setHeader("Event Type");
    grid.addColumn(AuditEntry::message).setHeader("Message");
    grid.setItems(AuditLogStore.getInstance().entries());

    add(grid);
  }
}
