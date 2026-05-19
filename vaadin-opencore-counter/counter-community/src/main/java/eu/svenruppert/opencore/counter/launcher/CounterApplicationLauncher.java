package eu.svenruppert.opencore.counter.launcher;

import eu.svenruppert.opencore.counter.app.Application;
import eu.svenruppert.opencore.counter.extension.FeatureContribution;
import org.eclipse.jetty.ee11.annotations.AnnotationConfiguration;
import org.eclipse.jetty.ee11.webapp.MetaInfConfiguration;
import org.eclipse.jetty.ee11.webapp.WebAppContext;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;

public final class CounterApplicationLauncher {

  public static final int DEFAULT_PORT = 8080;

  private CounterApplicationLauncher() {
  }

  public static void main(String[] args) throws Exception {
    int port = parsePortOrDefault(args, DEFAULT_PORT);

    printStartupHeader(port);

    Server server = startServer(port);
    server.join();
  }

  static Server startServer(int port) throws Exception {
    ensureProductionTokenFile();

    Server server = new Server(port);
    WebAppContext webapp = new WebAppContext();
    webapp.setContextPath("/");

    ResourceFactory rf = ResourceFactory.of(webapp);
    Resource base = rf.newClassLoaderResource("META-INF/resources", true);
    if (base == null) {
      base = rf.newMemoryResource(
          CounterApplicationLauncher.class.getResource("/"));
    }
    webapp.setBaseResource(base);

    webapp.setConfigurationDiscovered(true);
    webapp.addConfiguration(new AnnotationConfiguration());
    webapp.setAttribute(MetaInfConfiguration.CONTAINER_JAR_PATTERN, ".*\\.jar$");
    webapp.setAttribute(MetaInfConfiguration.WEBINF_JAR_PATTERN, ".*\\.jar$");
    webapp.setParentLoaderPriority(true);

    org.eclipse.jetty.ee11.servlet.ServletHolder holder =
        new org.eclipse.jetty.ee11.servlet.ServletHolder(new CounterServlet());
    holder.setInitOrder(1);
    holder.setAsyncSupported(true);
    holder.setInitParameter("productionMode", "true");
    webapp.addServlet(holder, "/*");

    server.setHandler(webapp);
    server.start();
    return server;
  }

  static void ensureProductionTokenFile() throws java.io.IOException {
    if (CounterApplicationLauncher.class.getClassLoader()
        .getResource("META-INF/VAADIN/config/flow-build-info.json") != null) {
      return;
    }
    String url = CounterApplicationLauncher.class.getProtectionDomain()
        .getCodeSource().getLocation().toString();
    if (!url.startsWith("file:") || !url.endsWith("/")) {
      return;
    }
    java.nio.file.Path classesDir = java.nio.file.Path.of(java.net.URI.create(url));
    java.nio.file.Path target = classesDir.resolve("META-INF/VAADIN/config/flow-build-info.json");
    java.nio.file.Files.createDirectories(target.getParent());
    java.nio.file.Files.writeString(target,
        "{\"productionMode\":true,\"eagerServerLoad\":false,\"react.enable\":true}\n");
  }

  static int parsePortOrDefault(String[] args, int fallback) {
    if (args == null || args.length == 0) {
      return fallback;
    }
    try {
      return Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  private static void printStartupHeader(int port) {
    System.out.println("OpenCore Counter started.");
    System.out.println("Mode is determined by classpath.");
    System.out.println("Open http://localhost:" + port);
    System.out.println("Loaded features:");
    for (FeatureContribution feature : Application.context().featureRegistry().features()) {
      System.out.println("- " + feature.id());
    }
  }
}
