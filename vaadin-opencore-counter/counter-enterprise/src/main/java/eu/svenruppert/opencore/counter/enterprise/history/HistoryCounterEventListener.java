package eu.svenruppert.opencore.counter.enterprise.history;

import eu.svenruppert.opencore.counter.domain.CounterChangedEvent;
import eu.svenruppert.opencore.counter.extension.CounterEventListener;

public final class HistoryCounterEventListener implements CounterEventListener {

  private final HistoryStore store;

  public HistoryCounterEventListener() {
    this(HistoryStore.getInstance());
  }

  public HistoryCounterEventListener(HistoryStore store) {
    this.store = store;
  }

  @Override
  public void onCounterChanged(CounterChangedEvent event) {
    store.add(new HistoryEntry(
        event.timestamp(),
        event.oldValue(),
        event.newValue(),
        event.action()));
  }
}
