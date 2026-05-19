package eu.svenruppert.opencore.counter.extension;

import java.util.List;

public interface FeatureContribution {

  String id();

  List<RouteContribution> routes();

  List<MenuContribution> menuItems();

  /**
   * Optional navbar additions. Default: none.
   *
   * <p>Used by the Enterprise edition to drop an "Enterprise"
   * badge into the navbar without the community module having to know.
   */
  default List<NavbarContribution> navbarItems() {
    return List.of();
  }

  default int order() {
    return 1000;
  }
}
