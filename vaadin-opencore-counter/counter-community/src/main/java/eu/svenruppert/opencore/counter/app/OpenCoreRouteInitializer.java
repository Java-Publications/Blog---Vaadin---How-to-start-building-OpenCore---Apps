package eu.svenruppert.opencore.counter.app;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.RouteRegistry;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import eu.svenruppert.opencore.counter.extension.RouteContribution;
import eu.svenruppert.opencore.counter.ui.MainLayout;

public class OpenCoreRouteInitializer implements VaadinServiceInitListener {

  @Override
  public void serviceInit(ServiceInitEvent event) {
    RouteConfiguration routeConfiguration = RouteConfiguration.forApplicationScope();
    RouteRegistry registry = routeConfiguration.getHandledRegistry();
    registry.update(() -> {
      for (RouteContribution route : Application.context().featureRegistry().routes()) {
        Class<? extends Component> viewClass = route.viewClass();
        if (!routeConfiguration.isPathAvailable(route.path())) {
          routeConfiguration.setRoute(route.path(), viewClass, MainLayout.class);
        }
      }
    });
  }
}
