package com.svenruppert.opencore.counter.enterprise;

import com.svenruppert.opencore.counter.domain.CounterAction;
import com.svenruppert.opencore.counter.domain.CounterChangedEvent;
import com.svenruppert.opencore.counter.enterprise.audit.AuditEntry;
import com.svenruppert.opencore.counter.enterprise.audit.AuditLogCounterEventListener;
import com.svenruppert.opencore.counter.enterprise.audit.AuditLogStore;
import com.svenruppert.opencore.counter.enterprise.history.HistoryCounterEventListener;
import com.svenruppert.opencore.counter.enterprise.history.HistoryEntry;
import com.svenruppert.opencore.counter.enterprise.history.HistoryStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnterpriseEventListenerTest {

  @BeforeEach
  void resetStores() {
    HistoryStore.getInstance().clear();
    AuditLogStore.getInstance().clear();
  }

  @Test
  @DisplayName("HistoryCounterEventListener stores a HistoryEntry that mirrors the event")
  void historyListenerStoresEntry() {
    HistoryStore store = new HistoryStore();
    HistoryCounterEventListener listener = new HistoryCounterEventListener(store);

    Instant now = Instant.parse("2026-01-02T03:04:05Z");
    listener.onCounterChanged(new CounterChangedEvent(2, 3, CounterAction.INCREMENT, now));

    assertEquals(1, store.size());
    HistoryEntry entry = store.entries().get(0);
    assertEquals(now, entry.timestamp());
    assertEquals(2, entry.oldValue());
    assertEquals(3, entry.newValue());
    assertEquals(CounterAction.INCREMENT, entry.action());
  }

  @Test
  @DisplayName("AuditLogCounterEventListener stores an AuditEntry with action and values in message")
  void auditListenerStoresEntry() {
    AuditLogStore store = new AuditLogStore();
    AuditLogCounterEventListener listener = new AuditLogCounterEventListener(store);

    Instant now = Instant.parse("2026-01-02T03:04:05Z");
    listener.onCounterChanged(new CounterChangedEvent(1, 0, CounterAction.RESET, now));

    assertEquals(1, store.size());
    AuditEntry entry = store.entries().get(0);
    assertEquals(now, entry.timestamp());
    assertEquals(AuditLogCounterEventListener.EVENT_TYPE, entry.eventType());
    assertTrue(entry.message().contains("RESET"), "Missing action: " + entry.message());
    assertTrue(entry.message().contains("from 1 to 0"), "Missing values: " + entry.message());
  }

  @Test
  @DisplayName("HistoryStore is independent from AuditLogStore — feeds receive their own events")
  void independentStores() {
    HistoryStore historyStore = new HistoryStore();
    AuditLogStore auditStore = new AuditLogStore();
    HistoryCounterEventListener history = new HistoryCounterEventListener(historyStore);
    AuditLogCounterEventListener audit = new AuditLogCounterEventListener(auditStore);

    CounterChangedEvent event = new CounterChangedEvent(
        0, 1, CounterAction.INCREMENT, Instant.now());
    history.onCounterChanged(event);
    audit.onCounterChanged(event);

    assertEquals(1, historyStore.size());
    assertEquals(1, auditStore.size());
  }

  @Test
  @DisplayName("multiple events accumulate in order")
  void multipleEventsAccumulate() {
    HistoryStore store = new HistoryStore();
    HistoryCounterEventListener listener = new HistoryCounterEventListener(store);

    listener.onCounterChanged(new CounterChangedEvent(0, 1, CounterAction.INCREMENT, Instant.now()));
    listener.onCounterChanged(new CounterChangedEvent(1, 2, CounterAction.INCREMENT, Instant.now()));
    listener.onCounterChanged(new CounterChangedEvent(2, 1, CounterAction.DECREMENT, Instant.now()));

    assertEquals(3, store.size());
    assertEquals(CounterAction.INCREMENT, store.entries().get(0).action());
    assertEquals(CounterAction.INCREMENT, store.entries().get(1).action());
    assertEquals(CounterAction.DECREMENT, store.entries().get(2).action());
  }

  @Test
  @DisplayName("HistoryStore.clear() empties the store")
  void historyStoreClearEmpties() {
    HistoryStore store = new HistoryStore();
    store.add(new com.svenruppert.opencore.counter.enterprise.history.HistoryEntry(
        Instant.now(), 0, 1, CounterAction.INCREMENT));
    assertEquals(1, store.size());

    store.clear();

    assertEquals(0, store.size());
    assertTrue(store.entries().isEmpty(), "entries() must be empty after clear()");
  }

  @Test
  @DisplayName("AuditLogStore.clear() empties the store")
  void auditStoreClearEmpties() {
    AuditLogStore store = new AuditLogStore();
    store.add(new AuditEntry(Instant.now(), "TEST", "msg"));
    assertEquals(1, store.size());

    store.clear();

    assertEquals(0, store.size());
    assertTrue(store.entries().isEmpty(), "entries() must be empty after clear()");
  }
}
