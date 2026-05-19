package eu.svenruppert.opencore.counter.enterprise;

import eu.svenruppert.opencore.counter.extension.FeatureContribution;
import eu.svenruppert.opencore.counter.extension.FeatureRegistry;
import eu.svenruppert.opencore.counter.extension.MenuContribution;
import eu.svenruppert.opencore.counter.extension.RouteContribution;
import eu.svenruppert.opencore.counter.ui.core.CoreFeatureContribution;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnterpriseFeatureContributionTest {

  @Test
  @DisplayName("ServiceLoader-loaded registry contains both community.core and enterprise.counter")
  void registryContainsBothFeatures() {
    FeatureRegistry registry = new FeatureRegistry();
    List<String> ids = registry.features().stream().map(FeatureContribution::id).toList();

    assertTrue(ids.contains(CoreFeatureContribution.FEATURE_ID),
        "Missing community.core in " + ids);
    assertTrue(ids.contains(EnterpriseFeatureContribution.FEATURE_ID),
        "Missing enterprise.counter in " + ids);
  }

  @Test
  @DisplayName("registry exposes enterprise menu entries History, Audit Log, Export")
  void registryExposesEnterpriseMenus() {
    FeatureRegistry registry = new FeatureRegistry();
    List<String> labels = registry.menuItems().stream().map(MenuContribution::label).toList();

    assertTrue(labels.contains("History"), "Missing History: " + labels);
    assertTrue(labels.contains("Audit Log"), "Missing Audit Log: " + labels);
    assertTrue(labels.contains("Export"), "Missing Export: " + labels);
  }

  @Test
  @DisplayName("registry exposes enterprise routes history, audit-log, export")
  void registryExposesEnterpriseRoutes() {
    FeatureRegistry registry = new FeatureRegistry();
    List<String> paths = registry.routes().stream().map(RouteContribution::path).toList();

    assertTrue(paths.contains("history"), "Missing history: " + paths);
    assertTrue(paths.contains("audit-log"), "Missing audit-log: " + paths);
    assertTrue(paths.contains("export"), "Missing export: " + paths);
  }

  @Test
  @DisplayName("enterprise feature contributes counter event listeners")
  void enterpriseContributesEventListeners() {
    FeatureRegistry registry = new FeatureRegistry();
    int listenerCount = registry.counterEventListeners().size();
    assertEquals(2, listenerCount,
        "Expected exactly two enterprise listeners (history + audit), got " + listenerCount);
  }

  @Test
  @DisplayName("hasFeature(enterprise.counter) returns true with enterprise on the classpath")
  void hasEnterpriseFeature() {
    FeatureRegistry registry = new FeatureRegistry();
    assertTrue(registry.hasFeature(EnterpriseFeatureContribution.FEATURE_ID));
    assertFalse(registry.hasFeature("not.there"));
  }

  @Test
  @DisplayName("enterprise feature self-reports its routes and menu items")
  void enterpriseFeatureSelfReports() {
    EnterpriseFeatureContribution feature = new EnterpriseFeatureContribution();
    assertEquals("enterprise.counter", feature.id());
    assertEquals(3, feature.routes().size());
    assertEquals(3, feature.menuItems().size());
    assertEquals(2, feature.counterEventListeners().size());
  }

  @Test
  @DisplayName("enterprise feature contributes an 'Enterprise Edition' navbar badge")
  void enterpriseContributesNavbarBadge() {
    EnterpriseFeatureContribution feature = new EnterpriseFeatureContribution();
    assertEquals(1, feature.navbarItems().size());
    assertEquals("enterprise.edition.badge", feature.navbarItems().get(0).id());
    assertTrue(feature.navbarItems().get(0).componentFactory().get()
        instanceof eu.svenruppert.opencore.counter.enterprise.ui.components.EnterpriseEditionBadge,
        "Navbar factory must produce an EnterpriseEditionBadge");
  }

  @Test
  @DisplayName("navbar factory produces a fresh component instance every call")
  void navbarFactoryReturnsFreshInstances() {
    EnterpriseFeatureContribution feature = new EnterpriseFeatureContribution();
    var factory = feature.navbarItems().get(0).componentFactory();
    assertTrue(factory.get() != factory.get(),
        "componentFactory must not cache; Vaadin components can only have one parent at a time");
  }
}
