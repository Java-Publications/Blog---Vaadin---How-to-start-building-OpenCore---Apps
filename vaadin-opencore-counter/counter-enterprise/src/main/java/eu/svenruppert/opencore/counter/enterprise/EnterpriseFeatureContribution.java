package eu.svenruppert.opencore.counter.enterprise;

import com.vaadin.flow.component.Component;
import eu.svenruppert.opencore.counter.enterprise.audit.AuditLogCounterEventListener;
import eu.svenruppert.opencore.counter.enterprise.audit.AuditLogView;
import eu.svenruppert.opencore.counter.enterprise.export.ExportView;
import eu.svenruppert.opencore.counter.enterprise.history.HistoryCounterEventListener;
import eu.svenruppert.opencore.counter.enterprise.history.HistoryView;
import eu.svenruppert.opencore.counter.enterprise.ui.components.EnterpriseEditionBadge;
import eu.svenruppert.opencore.counter.extension.CounterEventFeature;
import eu.svenruppert.opencore.counter.extension.CounterEventListener;
import eu.svenruppert.opencore.counter.extension.MenuContribution;
import eu.svenruppert.opencore.counter.extension.NavbarContribution;
import eu.svenruppert.opencore.counter.extension.RouteContribution;

import java.util.List;
import java.util.function.Supplier;

public final class EnterpriseFeatureContribution implements CounterEventFeature {

  public static final String FEATURE_ID = "enterprise.counter";
  public static final String NAVBAR_BADGE_ID = "enterprise.edition.badge";

  @Override
  public String id() {
    return FEATURE_ID;
  }

  @Override
  public List<RouteContribution> routes() {
    return List.of(
        new RouteContribution("history", HistoryView.class),
        new RouteContribution("audit-log", AuditLogView.class),
        new RouteContribution("export", ExportView.class));
  }

  @Override
  public List<MenuContribution> menuItems() {
    return List.of(
        new MenuContribution("History", "history", 300, "vaadin:clock"),
        new MenuContribution("Audit Log", "audit-log", 400, "vaadin:list"),
        new MenuContribution("Export", "export", 500, "vaadin:download"));
  }

  @Override
  public List<CounterEventListener> counterEventListeners() {
    return List.of(
        new HistoryCounterEventListener(),
        new AuditLogCounterEventListener());
  }

  @Override
  public List<NavbarContribution> navbarItems() {
    return List.of(new NavbarContribution() {
      @Override
      public String id() {
        return NAVBAR_BADGE_ID;
      }

      @Override
      public Supplier<Component> componentFactory() {
        return EnterpriseEditionBadge::new;
      }

      @Override
      public int order() {
        return 100;
      }
    });
  }

  @Override
  public int order() {
    return 500;
  }
}
