package com.svenruppert.opencore.counter.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import com.svenruppert.opencore.counter.app.Application;
import com.svenruppert.opencore.counter.extension.MenuContribution;
import com.svenruppert.opencore.counter.extension.NavbarContribution;

@Layout("/")
public class MainLayout extends AppLayout {

  public static final String DRAWER_TITLE = "OpenCore Counter";

  public MainLayout() {
    addToNavbar(createHeader());
    addToDrawer(createDrawer());
  }

  private HorizontalLayout createHeader() {
    H1 title = new H1(DRAWER_TITLE);
    title.addClassNames(
        LumoUtility.FontSize.LARGE,
        LumoUtility.Margin.NONE);

    HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), title);
    header.setAlignItems(FlexComponent.Alignment.CENTER);
    header.setWidthFull();

    var navbarItems = Application.context().featureRegistry().navbarItems();
    if (!navbarItems.isEmpty()) {
      HorizontalLayout extras = new HorizontalLayout();
      extras.setAlignItems(FlexComponent.Alignment.CENTER);
      extras.getStyle().set("margin-left", "auto");
      for (NavbarContribution contribution : navbarItems) {
        extras.add(contribution.componentFactory().get());
      }
      header.add(extras);
      header.setFlexGrow(1, extras);
    }
    return header;
  }

  private VerticalLayout createDrawer() {
    SideNav nav = new SideNav();
    for (MenuContribution menuContribution
        : Application.context().featureRegistry().menuItems()) {
      SideNavItem item = new SideNavItem(menuContribution.label(), "/" + menuContribution.path());
      item.setId("nav-" + slug(menuContribution.label()));
      nav.addItem(item);
    }
    VerticalLayout layout = new VerticalLayout(nav);
    layout.setPadding(false);
    layout.setSpacing(false);
    return layout;
  }

  private static String slug(String label) {
    return label.toLowerCase().replace(' ', '-');
  }
}
