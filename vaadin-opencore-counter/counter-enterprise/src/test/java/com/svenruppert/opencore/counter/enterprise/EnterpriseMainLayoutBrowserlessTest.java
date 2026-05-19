package com.svenruppert.opencore.counter.enterprise;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.browserless.ViewPackages;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.RouteConfiguration;
import com.svenruppert.opencore.counter.app.Application;
import com.svenruppert.opencore.counter.app.ApplicationContext;
import com.svenruppert.opencore.counter.extension.RouteContribution;
import com.svenruppert.opencore.counter.ui.MainLayout;
import com.svenruppert.opencore.counter.ui.core.CounterView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("MainLayout — enterprise menu (includes History/Audit Log/Export)")
@ViewPackages(packages = {"com.svenruppert.opencore.counter"})
class EnterpriseMainLayoutBrowserlessTest extends BrowserlessTest {

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
  @DisplayName("enterprise side-nav contains all community + enterprise entries")
  void sideNavContainsCommunityAndEnterpriseEntries() {
    navigate("", CounterView.class);

    MainLayout layout = UI.getCurrent().getInternals()
        .getActiveRouterTargetsChain().stream()
        .filter(c -> c instanceof MainLayout)
        .map(c -> (MainLayout) c)
        .findFirst()
        .orElseThrow(() -> new AssertionError(
            "MainLayout not in router chain: "
                + UI.getCurrent().getInternals().getActiveRouterTargetsChain()));

    SideNav nav = findSideNav(layout);
    assertNotNull(nav, "SideNav not present");

    List<String> labels = nav.getItems().stream().map(SideNavItem::getLabel).toList();

    assertTrue(labels.contains("Counter"), "Missing Counter: " + labels);
    assertTrue(labels.contains("History"), "Missing History: " + labels);
    assertTrue(labels.contains("Audit Log"), "Missing Audit Log: " + labels);
    assertTrue(labels.contains("Export"), "Missing Export: " + labels);
    assertTrue(labels.contains("About"), "Missing About: " + labels);
  }

  @Test
  @DisplayName("Enterprise edition badge is rendered in the navbar")
  void editionBadgeInNavbar() {
    navigate("", CounterView.class);

    MainLayout layout = UI.getCurrent().getInternals()
        .getActiveRouterTargetsChain().stream()
        .filter(c -> c instanceof MainLayout)
        .map(c -> (MainLayout) c)
        .findFirst()
        .orElseThrow();

    boolean badgeFound = layout.getChildren()
        .flatMap(this::walk)
        .filter(c -> c instanceof Span)
        .map(c -> (Span) c)
        .anyMatch(span -> "Enterprise Edition".equals(span.getText())
            && span.getStyle().get("background-color") != null);

    assertTrue(badgeFound,
        "Expected a Span with text 'Enterprise Edition' styled as a pill in the navbar");
  }

  private java.util.stream.Stream<com.vaadin.flow.component.Component> walk(
      com.vaadin.flow.component.Component c) {
    return java.util.stream.Stream.concat(
        java.util.stream.Stream.of(c),
        c.getChildren().flatMap(this::walk));
  }

  private static SideNav findSideNav(Component component) {
    if (component instanceof SideNav nav) {
      return nav;
    }
    return component.getChildren()
        .map(EnterpriseMainLayoutBrowserlessTest::findSideNav)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }
}
