package com.svenruppert.opencore.counter.extension;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.svenruppert.opencore.counter.ui.core.AboutView;
import com.svenruppert.opencore.counter.ui.core.CoreFeatureContribution;
import com.svenruppert.opencore.counter.ui.core.CounterView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeatureRegistryTest {

  @Test
  @DisplayName("ServiceLoader-loaded registry contains the community.core feature")
  void serviceLoaderRegistryContainsCommunityCore() {
    FeatureRegistry registry = new FeatureRegistry();
    List<String> ids = registry.features().stream().map(FeatureContribution::id).toList();

    assertTrue(ids.contains(CoreFeatureContribution.FEATURE_ID),
        "Expected community.core in " + ids);
    assertFalse(ids.contains("enterprise.counter"),
        "Enterprise feature must not be loaded in the community module");
  }

  @Test
  @DisplayName("community registry exposes Counter and About menu entries")
  void communityMenuEntries() {
    FeatureRegistry registry = new FeatureRegistry();
    List<String> labels = registry.menuItems().stream().map(MenuContribution::label).toList();

    assertTrue(labels.contains("Counter"), "Missing Counter menu: " + labels);
    assertTrue(labels.contains("About"), "Missing About menu: " + labels);
  }

  @Test
  @DisplayName("community registry exposes empty path and 'about' route")
  void communityRoutes() {
    FeatureRegistry registry = new FeatureRegistry();
    List<String> paths = registry.routes().stream().map(RouteContribution::path).toList();

    assertTrue(paths.contains(""), "Missing empty root route: " + paths);
    assertTrue(paths.contains("about"), "Missing about route: " + paths);
  }

  @Test
  @DisplayName("hasFeature reports correct presence")
  void hasFeatureReportsPresence() {
    FeatureRegistry registry = new FeatureRegistry();
    assertTrue(registry.hasFeature(CoreFeatureContribution.FEATURE_ID));
    assertFalse(registry.hasFeature("not.there"));
  }

  @Test
  @DisplayName("features are sorted by order")
  void featuresSortedByOrder() {
    FeatureContribution low = stubFeature("low", 10);
    FeatureContribution high = stubFeature("high", 800);
    FeatureContribution mid = stubFeature("mid", 400);

    FeatureRegistry registry = new FeatureRegistry(List.of(high, low, mid));
    List<String> ids = registry.features().stream().map(FeatureContribution::id).toList();

    assertEquals(List.of("low", "mid", "high"), ids);
  }

  @Test
  @DisplayName("menu items are sorted by their order property")
  void menuItemsSortedByOrder() {
    FeatureContribution a = new StubFeature("a", 0,
        List.of(new RouteContribution("a", Div.class)),
        List.of(new MenuContribution("AAA", "a", 50, null)),
        List.of());
    FeatureContribution b = new StubFeature("b", 0,
        List.of(new RouteContribution("b", Div.class)),
        List.of(new MenuContribution("BBB", "b", 5, null)),
        List.of());

    FeatureRegistry registry = new FeatureRegistry(List.of(a, b));
    List<String> labels = registry.menuItems().stream().map(MenuContribution::label).toList();

    assertEquals(List.of("BBB", "AAA"), labels);
  }

  @Test
  @DisplayName("duplicate route paths fail fast")
  void duplicateRoutePathsFailFast() {
    FeatureContribution a = new StubFeature("a", 100,
        List.of(new RouteContribution("dup", Div.class)),
        List.of(),
        List.of());
    FeatureContribution b = new StubFeature("b", 200,
        List.of(new RouteContribution("dup", Div.class)),
        List.of(),
        List.of());

    IllegalStateException ex = assertThrows(IllegalStateException.class,
        () -> new FeatureRegistry(List.of(a, b)));
    assertTrue(ex.getMessage().contains("dup"));
  }

  @Test
  @DisplayName("event listeners come from CounterEventFeature implementations only")
  void eventListenersFromEventFeatures() {
    CounterEventListener listener = event -> {};
    FeatureContribution plain = new StubFeature("plain", 100,
        List.of(new RouteContribution("p", Div.class)),
        List.of(),
        List.of());
    FeatureContribution eventFeature = new StubEventFeature("ev", 200,
        List.of(new RouteContribution("e", Div.class)),
        List.of(),
        List.of(listener));

    FeatureRegistry registry = new FeatureRegistry(List.of(plain, eventFeature));

    assertEquals(1, registry.counterEventListeners().size());
    assertEquals(listener, registry.counterEventListeners().get(0));
  }

  @Test
  @DisplayName("RouteContribution rejects paths starting with '/'")
  void routePathLeadingSlashRejected() {
    assertThrows(IllegalArgumentException.class,
        () -> new RouteContribution("/forbidden", CounterView.class));
  }

  @Test
  @DisplayName("Default FeatureContribution.navbarItems() is empty")
  void defaultNavbarItemsEmpty() {
    FeatureContribution defaultFeature = new FeatureContribution() {
      @Override
      public String id() {
        return "default-nav";
      }

      @Override
      public List<RouteContribution> routes() {
        return List.of();
      }

      @Override
      public List<MenuContribution> menuItems() {
        return List.of();
      }
    };
    assertEquals(0, defaultFeature.navbarItems().size());
  }

  @Test
  @DisplayName("Registry collects and sorts NavbarContributions across all features")
  void navbarContributionsAreCollectedAndSorted() {
    NavbarContribution far = new NavbarContribution() {
      @Override
      public String id() { return "far"; }
      @Override
      public java.util.function.Supplier<Component> componentFactory() { return Div::new; }
      @Override
      public int order() { return 9000; }
    };
    NavbarContribution near = new NavbarContribution() {
      @Override
      public String id() { return "near"; }
      @Override
      public java.util.function.Supplier<Component> componentFactory() { return Div::new; }
      @Override
      public int order() { return 100; }
    };
    FeatureContribution feature = new StubFeatureWithNavbar(
        "navbar-feature", 500,
        List.of(new RouteContribution("nav-feature", Div.class)),
        List.of(),
        List.of(),
        List.of(far, near));

    FeatureRegistry registry = new FeatureRegistry(List.of(feature));
    List<String> ids = registry.navbarItems().stream().map(NavbarContribution::id).toList();
    assertEquals(List.of("near", "far"), ids);
  }

  @Test
  @DisplayName("Community registry by default has no navbar contributions")
  void communityRegistryNoNavbar() {
    FeatureRegistry registry = new FeatureRegistry();
    assertEquals(0, registry.navbarItems().size());
  }

  @Test
  @DisplayName("FeatureContribution.order() defaults to 1000")
  void defaultOrderIs1000() {
    FeatureContribution defaultOrderFeature = new FeatureContribution() {
      @Override
      public String id() {
        return "default-order";
      }

      @Override
      public List<RouteContribution> routes() {
        return List.of();
      }

      @Override
      public List<MenuContribution> menuItems() {
        return List.of();
      }
    };
    assertEquals(1000, defaultOrderFeature.order());
  }

  @Test
  @DisplayName("AboutView and CounterView classes are registered as routes")
  void routeClassesAreCommunityViews() {
    FeatureRegistry registry = new FeatureRegistry();
    List<Class<? extends Component>> viewClasses =
        registry.routes().stream().map(RouteContribution::viewClass).toList();

    assertTrue(viewClasses.contains(CounterView.class));
    assertTrue(viewClasses.contains(AboutView.class));
  }

  private static FeatureContribution stubFeature(String id, int order) {
    return new StubFeature(id, order, List.of(), List.of(), List.of());
  }

  private record StubFeature(
      String id,
      int order,
      List<RouteContribution> routes,
      List<MenuContribution> menuItems,
      List<CounterEventListener> listeners
  ) implements FeatureContribution {

    @Override
    public List<RouteContribution> routes() {
      return routes;
    }

    @Override
    public List<MenuContribution> menuItems() {
      return menuItems;
    }
  }

  private record StubEventFeature(
      String id,
      int order,
      List<RouteContribution> routes,
      List<MenuContribution> menuItems,
      List<CounterEventListener> listeners
  ) implements CounterEventFeature {

    @Override
    public List<RouteContribution> routes() {
      return routes;
    }

    @Override
    public List<MenuContribution> menuItems() {
      return menuItems;
    }

    @Override
    public List<CounterEventListener> counterEventListeners() {
      return listeners;
    }
  }

  private record StubFeatureWithNavbar(
      String id,
      int order,
      List<RouteContribution> routes,
      List<MenuContribution> menuItems,
      List<CounterEventListener> listeners,
      List<NavbarContribution> navbar
  ) implements FeatureContribution {

    @Override
    public List<RouteContribution> routes() {
      return routes;
    }

    @Override
    public List<MenuContribution> menuItems() {
      return menuItems;
    }

    @Override
    public List<NavbarContribution> navbarItems() {
      return navbar;
    }
  }
}
