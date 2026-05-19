package com.svenruppert.opencore.counter.ui;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.RouteConfiguration;
import com.svenruppert.opencore.counter.app.Application;
import com.svenruppert.opencore.counter.app.ApplicationContext;
import com.svenruppert.opencore.counter.extension.RouteContribution;
import com.svenruppert.opencore.counter.ui.core.CounterView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MainLayout — community menu (no enterprise entries)")
@ViewPackages(packages = {"com.svenruppert.opencore.counter"})
class MainLayoutBrowserlessTest extends BrowserlessTest {

  @BeforeEach
  @Override
  protected void initVaadinEnvironment() {
    Application.replaceContext(new ApplicationContext());
    super.initVaadinEnvironment();
    registerDynamicRoutes();
  }

  private void registerDynamicRoutes() {
    RouteConfiguration routeConfiguration = RouteConfiguration.forApplicationScope();
    routeConfiguration.getHandledRegistry().update(() -> {
      for (RouteContribution route : Application.context().featureRegistry().routes()) {
        if (!routeConfiguration.isPathAvailable(route.path())) {
          routeConfiguration.setRoute(route.path(), route.viewClass(), MainLayout.class);
        }
      }
    });
  }

  @Test
  @DisplayName("community side-nav contains Counter and About but no enterprise entries")
  void sideNavContainsOnlyCommunityEntries() {
    navigate("", CounterView.class);

    MainLayout layout = com.vaadin.flow.component.UI.getCurrent().getInternals()
        .getActiveRouterTargetsChain().stream()
        .filter(c -> c instanceof MainLayout)
        .map(c -> (MainLayout) c)
        .findFirst()
        .orElseThrow(() -> new AssertionError(
            "MainLayout not in active router targets chain: "
                + com.vaadin.flow.component.UI.getCurrent().getInternals()
                    .getActiveRouterTargetsChain()));
    assertNotNull(layout, "MainLayout not rendered");

    SideNav nav = findSideNav(layout);
    assertNotNull(nav, "SideNav not present in MainLayout");

    List<String> labels = nav.getItems().stream().map(SideNavItem::getLabel).toList();

    assertTrue(labels.contains("Counter"), "Missing Counter in " + labels);
    assertTrue(labels.contains("About"), "Missing About in " + labels);
    assertFalse(labels.contains("History"), "Unexpected History in community: " + labels);
    assertFalse(labels.contains("Audit Log"), "Unexpected Audit Log in community: " + labels);
    assertFalse(labels.contains("Export"), "Unexpected Export in community: " + labels);
  }

  private static SideNav findSideNav(Component component) {
    if (component instanceof SideNav nav) {
      return nav;
    }
    return component.getChildren()
        .map(MainLayoutBrowserlessTest::findSideNav)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }
}
