package com.svenruppert.opencore.counter.boundary;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CommunityDoesNotReferenceEnterpriseTest {

  static final List<String> FORBIDDEN_TOKENS = List.of(
      ".counter.enterprise",
      "EnterpriseFeatureContribution",
      "HistoryView",
      "AuditLogView",
      "ExportView");

  @Test
  @DisplayName("community sources contain no reference to enterprise package or types")
  void communitySourcesDoNotReferenceEnterprise() throws IOException {
    Path sourceRoot = Path.of("src", "main", "java");
    assertTrue(Files.isDirectory(sourceRoot),
        "Expected community source root at " + sourceRoot.toAbsolutePath());

    List<String> violations = new ArrayList<>();
    try (Stream<Path> files = Files.walk(sourceRoot)) {
      files
          .filter(p -> p.toString().endsWith(".java"))
          .forEach(p -> scanFile(p, violations));
    }

    assertTrue(violations.isEmpty(),
        "Community sources must not reference enterprise:\n  - "
            + String.join("\n  - ", violations));
  }

  private static void scanFile(Path file, List<String> violations) {
    try {
      String content = Files.readString(file);
      for (String forbidden : FORBIDDEN_TOKENS) {
        if (content.contains(forbidden)) {
          violations.add(file + " contains '" + forbidden + "'");
        }
      }
    } catch (IOException e) {
      violations.add(file + " could not be read: " + e.getMessage());
    }
  }
}
