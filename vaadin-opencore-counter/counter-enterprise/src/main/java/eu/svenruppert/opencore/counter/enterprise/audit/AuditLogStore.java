package eu.svenruppert.opencore.counter.enterprise.audit;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AuditLogStore {

  private static final AuditLogStore INSTANCE = new AuditLogStore();

  private final List<AuditEntry> entries = new CopyOnWriteArrayList<>();

  public static AuditLogStore getInstance() {
    return INSTANCE;
  }

  public void add(AuditEntry entry) {
    entries.add(entry);
  }

  public List<AuditEntry> entries() {
    return List.copyOf(entries);
  }

  public void clear() {
    entries.clear();
  }

  public int size() {
    return entries.size();
  }
}
