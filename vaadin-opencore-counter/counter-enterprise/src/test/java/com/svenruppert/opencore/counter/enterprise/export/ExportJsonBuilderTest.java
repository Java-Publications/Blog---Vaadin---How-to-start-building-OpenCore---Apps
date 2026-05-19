package com.svenruppert.opencore.counter.enterprise.export;

import com.svenruppert.opencore.counter.domain.CounterAction;
import com.svenruppert.opencore.counter.enterprise.history.HistoryEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportJsonBuilderTest {

  @Test
  @DisplayName("export JSON contains feature id, current value and an empty history array")
  void emptyHistoryProducesEmptyArray() {
    String json = ExportJsonBuilder.buildExportJson(3, List.of());

    assertTrue(json.contains("\"feature\": \"enterprise.counter\""), json);
    assertTrue(json.contains("\"currentValue\": 3"), json);
    assertTrue(json.contains("\"history\": []"), json);
  }

  @Test
  @DisplayName("history entries are serialised with timestamp, old/new values and action")
  void historyEntriesAreSerialised() {
    Instant t = Instant.parse("2026-05-01T12:00:00Z");
    HistoryEntry entry = new HistoryEntry(t, 4, 5, CounterAction.INCREMENT);

    String json = ExportJsonBuilder.buildExportJson(5, List.of(entry));

    assertTrue(json.contains("\"timestamp\":\"" + t + "\""), json);
    assertTrue(json.contains("\"oldValue\":4"), json);
    assertTrue(json.contains("\"newValue\":5"), json);
    assertTrue(json.contains("\"action\":\"INCREMENT\""), json);
  }

  @Test
  @DisplayName("multiple history entries are comma-separated WITHOUT a leading comma")
  void multipleHistoryEntriesSeparated() {
    Instant t1 = Instant.parse("2026-05-01T12:00:00Z");
    Instant t2 = Instant.parse("2026-05-01T12:00:01Z");
    List<HistoryEntry> entries = List.of(
        new HistoryEntry(t1, 0, 1, CounterAction.INCREMENT),
        new HistoryEntry(t2, 1, 2, CounterAction.INCREMENT));

    String json = ExportJsonBuilder.buildExportJson(2, entries);

    int historyStart = json.indexOf("\"history\": [");
    String afterBracket = json.substring(historyStart + "\"history\": [".length()).trim();
    assertFalse(afterBracket.startsWith(","),
        "history array must not start with a comma: " + json);

    assertEquals(2, countOccurrences(json, "\"action\":\"INCREMENT\""),
        "Expected exactly two INCREMENT entries: " + json);
  }

  @Test
  @DisplayName("buildFromStore reads history from the singleton HistoryStore")
  void buildFromStoreReturnsValidJson() {
    com.svenruppert.opencore.counter.enterprise.history.HistoryStore.getInstance().clear();
    String json = ExportJsonBuilder.buildFromStore(7);

    assertTrue(json.contains("\"currentValue\": 7"), json);
    assertTrue(json.contains("\"history\": []"), json);
    assertTrue(json.contains("\"feature\": \"enterprise.counter\""), json);
  }

  private static int countOccurrences(String haystack, String needle) {
    int count = 0;
    int idx = 0;
    while ((idx = haystack.indexOf(needle, idx)) != -1) {
      count++;
      idx += needle.length();
    }
    return count;
  }
}
