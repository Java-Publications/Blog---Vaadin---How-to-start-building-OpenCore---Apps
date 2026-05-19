package com.svenruppert.opencore.counter.enterprise.export;

import com.svenruppert.opencore.counter.enterprise.history.HistoryEntry;
import com.svenruppert.opencore.counter.enterprise.history.HistoryStore;

import java.util.List;

public final class ExportJsonBuilder {

  public static final String FEATURE_ID = "enterprise.counter";

  private ExportJsonBuilder() {
  }

  public static String buildExportJson(int currentValue, List<HistoryEntry> history) {
    StringBuilder builder = new StringBuilder();
    builder.append("{\n");
    builder.append("  \"feature\": \"").append(FEATURE_ID).append("\",\n");
    builder.append("  \"currentValue\": ").append(currentValue).append(",\n");
    builder.append("  \"history\": [");
    for (int i = 0; i < history.size(); i++) {
      HistoryEntry entry = history.get(i);
      if (i > 0) {
        builder.append(",");
      }
      builder.append("\n    {");
      builder.append("\"timestamp\":\"").append(entry.timestamp()).append("\",");
      builder.append("\"oldValue\":").append(entry.oldValue()).append(",");
      builder.append("\"newValue\":").append(entry.newValue()).append(",");
      builder.append("\"action\":\"").append(entry.action().name()).append("\"");
      builder.append("}");
    }
    if (!history.isEmpty()) {
      builder.append("\n  ");
    }
    builder.append("]\n");
    builder.append("}");
    return builder.toString();
  }

  public static String buildFromStore(int currentValue) {
    return buildExportJson(currentValue, HistoryStore.getInstance().entries());
  }
}
