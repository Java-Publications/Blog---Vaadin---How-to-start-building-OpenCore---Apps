package eu.svenruppert.opencore.counter.enterprise.audit;

import eu.svenruppert.opencore.counter.domain.CounterChangedEvent;
import eu.svenruppert.opencore.counter.extension.CounterEventListener;

public final class AuditLogCounterEventListener implements CounterEventListener {

  public static final String EVENT_TYPE = "COUNTER_CHANGED";

  private final AuditLogStore store;

  public AuditLogCounterEventListener() {
    this(AuditLogStore.getInstance());
  }

  public AuditLogCounterEventListener(AuditLogStore store) {
    this.store = store;
  }

  @Override
  public void onCounterChanged(CounterChangedEvent event) {
    String message = "Counter changed from "
        + event.oldValue() + " to " + event.newValue()
        + " by action " + event.action().name();
    store.add(new AuditEntry(event.timestamp(), EVENT_TYPE, message));
  }
}
