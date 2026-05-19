package eu.svenruppert.opencore.counter.enterprise.history;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class HistoryStore {

  private static final HistoryStore INSTANCE = new HistoryStore();

  private final List<HistoryEntry> entries = new CopyOnWriteArrayList<>();

  public static HistoryStore getInstance() {
    return INSTANCE;
  }

  public void add(HistoryEntry entry) {
    entries.add(entry);
  }

  public List<HistoryEntry> entries() {
    return List.copyOf(entries);
  }

  public void clear() {
    entries.clear();
  }

  public int size() {
    return entries.size();
  }
}
