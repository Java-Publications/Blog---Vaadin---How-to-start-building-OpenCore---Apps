package eu.svenruppert.opencore.counter.app;

public final class Application {

  private static volatile ApplicationContext context = new ApplicationContext();

  private Application() {
  }

  public static ApplicationContext context() {
    return context;
  }

  public static void replaceContext(ApplicationContext newContext) {
    context = newContext;
  }
}
